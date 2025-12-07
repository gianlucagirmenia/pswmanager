package com.durdencorp.pswmanager.interceptor;

import com.durdencorp.pswmanager.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static class RequestTracker {
        int count;
        LocalDateTime windowStart;
        
        RequestTracker() {
            this.count = 1;
            this.windowStart = LocalDateTime.now();
        }
        
        boolean canMakeRequest(int maxRequests, int windowMinutes) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowEnd = windowStart.plusMinutes(windowMinutes);
            
            if (now.isAfter(windowEnd)) {
                count = 1;
                windowStart = now;
                LogUtils.logApplication(LogUtils.Level.DEBUG, "Rate limit window reset for tracker");
                return true;
            }
            
            if (count < maxRequests) {
                count++;
                return true;
            }
            
            return false;
        }
        
        long getSecondsUntilReset(int windowMinutes) {
            LocalDateTime windowEnd = windowStart.plusMinutes(windowMinutes);
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(windowEnd)) {
                return 0;
            }
            
            return java.time.Duration.between(now, windowEnd).getSeconds();
        }
        
        int getRemainingAttempts(int maxRequests) {
            return Math.max(0, maxRequests - count);
        }
    }
    
    private final Map<String, RequestTracker> loginTrackers = new ConcurrentHashMap<>();
    
    @Value("${rate.limit.master-password.max-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${rate.limit.master-password.window-minutes:15}")
    private int loginWindowMinutes;

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        LogUtils.setupRequestContext(request);
        
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        LogUtils.logApplication(LogUtils.Level.DEBUG, "Interceptor called: {} {} from IP: {}", requestMethod, requestPath, clientIp);
        
        if ("POST".equalsIgnoreCase(requestMethod) && requestPath.equals("/login")) {
            return handleLoginRequest(clientIp, request, response);
        }
        
        if ("GET".equalsIgnoreCase(requestMethod) && requestPath.equals("/login")) {
            handleGetLoginRequest(clientIp, request);
        }
        
        return true;
    }
    
    private boolean handleLoginRequest(String clientIp, HttpServletRequest request, 
                                     HttpServletResponse response) throws Exception {
        String trackerKey = "login:" + clientIp;
        
        RequestTracker tracker = loginTrackers.computeIfAbsent(
            trackerKey, k -> new RequestTracker());
        
        LogUtils.logApplication(LogUtils.Level.INFO, 
            "Login attempt #{} for IP: {}", tracker.count, clientIp);
        
        if (tracker.canMakeRequest(maxLoginAttempts, loginWindowMinutes)) {
            int remaining = tracker.getRemainingAttempts(maxLoginAttempts);
            
            LogUtils.logSecurity(LogUtils.Level.INFO, 
                "Login allowed for IP: {}, remaining attempts: {}", 
                clientIp, remaining);
            
            request.setAttribute("remainingAttempts", remaining);
            return true;
        } else {
            long retryAfter = tracker.getSecondsUntilReset(loginWindowMinutes);
            
            LogUtils.logSecurity(LogUtils.Level.WARN, 
                "Rate limit exceeded for IP: {}, attempts: {}, blocked for {} seconds", 
                clientIp, tracker.count, retryAfter);
            
            request.getSession().setAttribute("rateLimitError", true);
            request.getSession().setAttribute("retryAfterSeconds", retryAfter);
            request.getSession().setAttribute("retryAfterFormatted", formatTime(retryAfter));
            
            response.sendRedirect(request.getContextPath() + "/login?error=rate_limit");
            return false;
        }
    }
    
    private void handleGetLoginRequest(String clientIp, HttpServletRequest request) {
        String trackerKey = "login:" + clientIp;
        RequestTracker tracker = loginTrackers.get(trackerKey);
        
        if (tracker != null) {
            int remaining = tracker.getRemainingAttempts(maxLoginAttempts);
            request.setAttribute("remainingAttempts", remaining);
            
            LogUtils.logApplication(LogUtils.Level.DEBUG, 
                "GET /login - Remaining attempts for IP {}: {}", 
                clientIp, remaining);
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        LogUtils.clearContext();
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " secondi";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minuto" + (minutes > 1 ? "i" : "");
        } else {
            long hours = seconds / 3600;
            return hours + " ora" + (hours > 1 ? "e" : "");
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}