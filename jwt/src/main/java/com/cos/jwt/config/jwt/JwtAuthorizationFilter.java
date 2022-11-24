package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    인증 요청이 있을 때 동작하는 것이 아니라
    security를 타도록 할 것이다.
    security가 filter를 갖고 있는데 그 filter중에 BasicAuthenticationFilter라는 것이 있다.
    그럼 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있다.
    만약 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않는다.

    securityConfig에 이 필터를 추가하고 권한이 필요한 페이지에 접근하게 되면 이 필터를 타는것을 확인할 수 있다.

 */

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
//        System.out.println("인증이나 권한이 필요한 주소 요청");
    }

    /*
        인증이나 권한이 필요한 주소 요청이 있을 때 해당 필터를 타게 될 것이다.
        그럼 여기서 이제 헤더값을 확인할 수 있다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
//        super.doFilterInternal(request, response, chain);
        //위 super를 지우지 않으면 여기서 한번 응답하고 아래에 가서 응답을 또 하기 때문에 오류가 발생한다.
        /*
            super를 지우지 않고 접근을 시도하는 경우 두번 응답을 해서 인지 403오류가 발생한다.
            아래 코드들도 실행이 되기는 하지만 이미 super로 응답을 받아서 그에 대한 오류가 발생하는것 같다.

            그리고 아래 코드에서 authentication 객체를 만들어 세션에 강제로 접근하여 authentication 객체를 넣어주도록 되어있는데
            이때 세션 자체가 생성이 안되는 것으로 보인다.

            컨트롤러에서 Authentication 을 받아
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            이렇게 authentication 객체를 호출 했을 때
            NullPointerException이 발생한다.
            그럼 세션이 잘 생성되지 않았다고 봐야할 듯.

            여기서 super.doFilterInternal만 지워주게 되면
            정상적으로 동작한다.
         */


        System.out.println("인증이나 권한이 필요한 주소 요청");

        String jwtHeader = request.getHeader("Authorization");
        System.out.println("jwtHeader : " + jwtHeader);


        // 헤더에 토큰이 실려 왔는지, 그리고 토큰 값이 Bearer로 시작하는지 검증.
        // 아니라면 토큰 검증을 하지 않고 다시 필터를 타도록 한다.
        if(jwtHeader == null || !jwtHeader.startsWith("Bearer")){
            chain.doFilter(request, response);
            return;
        }

        // 토큰을 검증해서 정상적인 사용자인지를 확인해야 한다.
        // jwtToken을 이렇게 만들어주면 Bearer 뒤로 있는 값만 들어가게 된다. 그래서 Bearer 하고 한칸 띄워줘야 한다.
        String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");

        // .verify(jwtToken)으로 토큰을 서명하게 된다. 이게 정상적으로 되면 .getClaim을 통해 username을 가져오고 asString()으로
        // string으로 캐스팅해준다. 이 username이 정상적으로 들어왔다는 것은 서명이 정상적으로 되었다는 것을 의미한다.
        String username = JWT.require(Algorithm.HMAC512("cos")).build().verify(jwtToken).getClaim("username").asString();

        //서명이 되었다면
        if(username != null){
            System.out.println("username 정상");
            User userEntity = userRepository.findByUsername(username);
            System.out.println("userEntity : " + userEntity.getUsername());
            PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
            // authentication을 통해 로그인을 진행할것이 아니라 임의로 가짜 authentication을 만드는 것이기 때문에
            // credential 부분은 null로 보내도 된다.
            // 그리고 이 객체를 만들 때 권한을 알려줘야 한다.
            // 이 객체가 실제로 로그인을 해서 만들어지는 것이 아니라 서명을 통해 검증을 완료해서
            // username이 있으면 authentication을 만들어주는 것이기 때문에
            // 로그인을 위해 만드는 것이 아닌 토큰 서명을 통해 만들어주는 개념이다.
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());


            // security를 저장할 수 있는 세션 공간
            // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);



            chain.doFilter(request, response);
        }


    }
}
