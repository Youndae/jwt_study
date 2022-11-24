package com.cos.jwt.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter1 implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        System.out.println("필터1");


        filterChain.doFilter(servletRequest, servletResponse);//필터 체인에 다시 필터체인을 타라고 등록을 해줘야 한다. 이걸 안하면 그냥 여기서 끝나버린다.



    }
}
