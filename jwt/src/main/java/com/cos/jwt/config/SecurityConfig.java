package com.cos.jwt.config;


import com.cos.jwt.config.jwt.JwtAuthenticationFilter;
import com.cos.jwt.config.jwt.JwtAuthorizationFilter;
import com.cos.jwt.filter.MyFilter1;
import com.cos.jwt.filter.MyFilter3;
import com.cos.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CorsFilter corsFilter;

    private final UserRepository userRepository;

    @Bean
    public BCryptPasswordEncoder passwordEncodero(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.addFilterBefore(new MyFilter3(), BasicAuthenticationFilter.class);
        http.csrf().disable();

        //jwt를 사용하면 이 세팅이 기본이라고 함.
        //근데 formlogin을 안쓴다는건 새로운 로그인 페이지를 안만든다는 의미가 아닌지??
        //그거에 대해서는 강의 끝나고 테스트 해볼것.

        /*
            corsfilter에 대해 조금 정리.
            crossOrigin에 대해 모든 응답을 허용하겠다고 corsConfig에서 작성해두었는데 그 필터를 모든 요청이 타도록 하기 위해
            여기다가 addFilter()로 추가해준것인데
            이걸 다른 방식으로 설정할 수도 있다.
            컨트롤러 상단에 @CrossOrigin 어노테이션을 부여해서 처리할 수 있지만
            이렇게 하게 되면 인증이 필요한 시큐리티 요청에 대해서는 거부가 된다.
            인증이 필요하지 않은 요청만 허용이 되는것이다.
            로그인이라거나 이런건 저 어노테이션을 하나 걸어준다고 해결이 되지 않는다.

            이렇게 config에서 걸어주는것과 어노테이션의 차이를 정리.
            어노테이션은 인증이 없을 때 사용하고
            인증이 있을때는 시큐리티 필터에 등록을 해줘야 한다.

            이렇게 처리하고 나서 home에 다시 접근해보면 접근 거부가 안된다.
            하지만 이렇게 되면 security를 사용하지만 세션을 사용하지 않기 때문에 모든 페이지로 접근이 가능해진다는 것이다.
            모든 페이지라고 해서 정말 모~든 페이지를 의미하는것은 아니고
            인증이 걸려있지 않은 페이지만을 의미한다.
            아래 설정에서 지정한 user, manager, admin 페이지에 대해서는 접근할 수 없다.
            403오류가 발생하고
            그 외에 다른 페이지를 접근해보면 존재하지 않는다는 404만 발생한다.

            지금까지 만든 시큐리티와 다른점은 STATELESS로 사용한다는점.
            그리고 crossOrigin 정책에서 벗어나 모든 요청을 허용할 것이고
            formlogin을 사용하지 않는 다는 점이다.
         */
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//세션을 사용하지 않겠다는 것.
                .and()
                .addFilter(corsFilter)// 이렇게 해야 CorsConfig에 만들어놓은 corsFilter를 모든 요청이 타게 된다.
                .formLogin().disable()// formlogin을 안함.
                .httpBasic().disable()// http 로그인 방식을 안쓴다.
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

    }
}
