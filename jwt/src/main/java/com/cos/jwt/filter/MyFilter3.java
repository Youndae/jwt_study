package com.cos.jwt.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter3 implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        /*
            이 테스트로 알 수 잇는점은
            아이디와 비밀번호를 정상적으로 받았다면 로그인 인증을 진행하고 토큰을 만들어 준 뒤 응답을 해준다.
            즉, 토큰을 넘겨준다.
            그럼 이제 클라이언트에서는 요청할때마다 헤더에다 Authorization에 토큰값을 갖고 오면
            이 토큰이 내가(서버) 만든 토큰이 맞는지만 검증하면 된다.
            내가 만든 토큰이 아니라면 security filter로 넘어갈 필요가 없고
            내가 만든 토큰이라면 security filter로 넘겨주면 된다.

         */

        //토큰 : 코스
        //코스라는 토큰이 넘어오면 필터를 계속 타서 인증이 되도록 하고
        //그게 아니라면 필터를 타지 않아서 컨트롤러에 진입하지 못하도록.
        if(req.getMethod().equals("POST")){
            System.out.println("Post 요청됨.");
            String headerAuth = req.getHeader("Authorization");
            System.out.println(headerAuth);
            System.out.println("필터 3");

            if(headerAuth.equals("cos")){
                filterChain.doFilter(req,res);
            }else {
                System.out.println("필터 3");
                PrintWriter outPrintWriter = res.getWriter();
                outPrintWriter.print("인증안됨.");
            }
        }


    }
}
