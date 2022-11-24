package com.example.jwt_testproject.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.jwt_testproject.config.auth.PrincipalDetails;
import com.example.jwt_testproject.model.User;
import com.example.jwt_testproject.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("Authorities request");

//        String jwtHeader = request.getHeader("Authorization");
//        System.out.println("jwtHeader : " + jwtHeader);

//        String jwtHeader = WebUtils.getCookie(request, JwtProperties.HEADER_STRING).toString();
        Cookie jwtCookie = WebUtils.getCookie(request, JwtProperties.HEADER_STRING);
        System.out.println("jwtHeader : " + jwtCookie);


        if(jwtCookie == null || !jwtCookie.getValue().startsWith(JwtProperties.TOKEN_PREFIX)){
            System.out.println("header is null");
            chain.doFilter(request, response);
            return;
        }



        System.out.println("jwtHeader is not null");

        /*String jwtToken = request
                .getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX, "");*/

        String jwtToken = jwtCookie.getValue().replace(JwtProperties.TOKEN_PREFIX, "");




        System.out.println("AuthorizationFilter jwtToken : " + jwtToken);

        String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET))
                .build()
                .verify(jwtToken)
                .getClaim("username")
                .asString();

        System.out.println("Authorization Filter username : " + username);

        if(username != null){
            System.out.println("username is nomal state");
            User userEntity = userRepository.findByUsername(username);
            System.out.println("JwtAuthorizationFilter's userEntity : " + userEntity.getUsername());
            PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());


            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        }

    }
}
