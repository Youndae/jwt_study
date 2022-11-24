package com.example.jwt_testproject.config;

import com.example.jwt_testproject.config.auth.CustomLogoutSuccessHandler;
import com.example.jwt_testproject.config.jwt.JwtAuthenticationFilter;
import com.example.jwt_testproject.config.jwt.JwtAuthorizationFilter;
import com.example.jwt_testproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final CorsFilter corsFilter;

    private final UserRepository userRepository;

    private AuthenticationManager authenticationManager;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomLogoutSuccessHandler logoutSuccessHandler(){
        return new CustomLogoutSuccessHandler();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(corsFilter)
                .formLogin().disable()
                .httpBasic().disable()
                .logout().logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository))
                .authorizeRequests()
                .antMatchers("/api/v1/user/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/manager/**")
                .access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/admin/**")
                .access("hasRole('ROLE_ADMIN')")
                .anyRequest()
                .permitAll();

        return http.build();
    }

    private final AuthenticationManagerBuilder localConfigureAuthenticationBldr;
    private boolean authenticationManagerInitialized;
    private boolean disableLocalConfigureAuthenticationBldr;
    private final AuthenticationConfiguration authenticationConfiguration;

    protected AuthenticationManager authenticationManager() throws Exception{
        if(!this.authenticationManagerInitialized){
            this.configure(this.localConfigureAuthenticationBldr);
            if(this.disableLocalConfigureAuthenticationBldr){
                this.authenticationManager = this.authenticationConfiguration.getAuthenticationManager();
            }else{
                this.authenticationManager = (AuthenticationManager) this.localConfigureAuthenticationBldr.build();
            }

            this.authenticationManagerInitialized = true;
        }

        return this.authenticationManager;
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        this.disableLocalConfigureAuthenticationBldr = true;
    }
}
