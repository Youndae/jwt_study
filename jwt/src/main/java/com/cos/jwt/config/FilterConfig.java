package com.cos.jwt.config;

import com.cos.jwt.filter.MyFilter1;
import com.cos.jwt.filter.MyFilter2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    /*
        필터를 추가하는 방법은 securityConfig에 추가하는 방법도 있다.
        http.addFilter(new MyFilter1);
        이렇게 추가해줄 수 있는데 이러면 오류가 발생하긴 한다.
        시큐리티 필터가 아니라 이렇게 추가할 수는 없다.
        addFilterBefore나 addFilterAfter로 추가해야 한다.
        라는 내용의 오류가 발생한다.

        그래서 http.addFilterBefore(new MyFilter1(), BasicAuthenticationFilter.class)
        이렇게 추가할 수 잇다.
        이 경우 작성할 때 추가할 필터가 앞에 오고 뒤에는 기준이 되는 필터를 넣어주면 된다.
        그럼 이 코드로 봤을 때 BasicAuthenticationFilter가 실행되기 전에 MyFilter가 실행되도록 한다는 의미이다.

        이 필터들은 security Filter보다 늦게 실행된다.
        테스트를 위해 MyFilter3을 만들고 이걸 securityConfig에 addFilterBefore로 BasicAuthenticationFilter.class 를 기준으로
        테스트 하게 되면
        3 1 2 가 출력된다.
        그리고 addFilterAfter로 테스트를 진행해봐도 동일한 결과를 출력한다.
        그럼 결국 security FilterChain이 만들어둔 기본 필터보다 먼저 실행된다는 의미이다.
        그렇기 때문에 만든 필터가 security FilterChain보다 먼저 실행되도록 하고 싶다면
        securityConfig에 addFilterBefore로 추가를 해줘야 한다.

        검색 좀 해보다 보면 securityFilterChain구조에 대한 그림을 볼 수 있는데
        UsernamePasswordAuthenticationFilter나 SecurityContextPersistenceFilter 앞에 실행하도록 before를 작성해주면 된다.

        필터를 정리한 이유는 이 필터를 통해 jwt처리를 하기 위함이다.
     */
    @Bean
    public FilterRegistrationBean<MyFilter1> filter1(){

        FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>(new MyFilter1());
        bean.addUrlPatterns("/*");//모든 요청에서 전부 실행할것.
        bean.setOrder(0);// 낮은 번호가 필터중에서 가장 먼저 실행.

        return bean;
    }

    @Bean
    public FilterRegistrationBean<MyFilter2> filter2(){

        FilterRegistrationBean<MyFilter2> bean = new FilterRegistrationBean<>(new MyFilter2());
        bean.addUrlPatterns("/*");//모든 요청에서 전부 실행할것.
        bean.setOrder(1);// 낮은 번호가 필터중에서 가장 먼저 실행.

        return bean;
    }
}
