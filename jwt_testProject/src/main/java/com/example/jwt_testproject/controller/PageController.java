package com.example.jwt_testproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    @GetMapping("/loginForm")
    public String loginForm(){

        System.out.println("loginForm");

        return "th/loginForm";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String userData(){
        System.out.println("user Data");

        return "th/user";
    }

}
