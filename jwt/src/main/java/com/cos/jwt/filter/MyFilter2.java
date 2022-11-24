package com.cos.jwt.filter;

import javax.servlet.*;
import java.io.IOException;

public class MyFilter2 implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("필터 2");
        filterChain.doFilter(servletRequest, servletResponse);//필터 체인에 다시 필터체인을 타라고 등록을 해줘야 한다. 이걸 안하면 그냥 여기서 끝나버린다.


    }
}
