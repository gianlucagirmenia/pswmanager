package com.durdencorp.pswmanager.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdamController {

    @GetMapping("/adam")
    public String hello() {
        return "Questa è ora ossa delle mie ossa e carne della mia carne!";
    }
}
