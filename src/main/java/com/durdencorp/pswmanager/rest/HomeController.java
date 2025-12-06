package com.durdencorp.pswmanager.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/swagger")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui.html";
    }
    
    @GetMapping("/api-docs")
    public String apiDocsRedirect() {
        return "redirect:/v3/api-docs";
    }
}