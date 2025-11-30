package com.durdencorp.pswmanager.rest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("errorMessage", "Errore: " + e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public String handleSecurityException(IllegalStateException e) {
        return "redirect:/login";
    }
}