package com.cos.security1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity//SpringSecurity filter(SecurityConfig)가 SpringFilterChain에 등록된다.
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)// secured 어노테이션이 활성화.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //해당 메소드의 리턴되는 오브젝트를 IoC로 등록
    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/user/**").authenticated()
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN' )")
                .anyRequest().permitAll()
                .and()
                .formLogin() // formlogin().loginPage()를 추가함으로써 권한이 없는 페이지에 접근 시 로그인 페이지가 출력된다.
                .loginPage("/loginForm")// 이게 없이 권한이 없는 페이지에 접근하게 되면 403 오류페이지만 출력된다.
                .loginProcessingUrl("/login") // login주소가 호출이 되면 security가 낚아채서 대신 로그인을 진행해준다.
                .defaultSuccessUrl("/");// 로그인이 성공했을때의 default url 특정 페이지를 요청했을 때 권한에 의해 로그인 페이지로 이동하게 되면
                                        // 로그인 성공 시 defaultUrl이 아닌 접근 요청 페이지로 이동해준다.
    }
}
