package com.example.jwt_testproject.config.auth;

import com.example.jwt_testproject.config.jwt.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.DelegatingLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {



    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        System.out.println("logoutSuccessHandler");

        Cookie cookie = WebUtils.getCookie(request, JwtProperties.HEADER_STRING);

        System.out.println("cookies value : " + cookie.getValue());

        cookie.setValue(null);

        System.out.println("set null cookie value : " + cookie.getValue());

        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.sendRedirect("/home");


    }
}
