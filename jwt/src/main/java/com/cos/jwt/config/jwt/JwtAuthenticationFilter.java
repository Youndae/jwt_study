package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

/*
    스프링 시큐리티에 UsernamePasswordAuthenticationFilter가 있다.
    이 필터가 동작하는 시점은 /login 요청이 와서 username과 password를 post로 전송했을때 동작한다.

    현재 이 필터가 동작하지 않는 이유가 formlogin().disabled() 설정때문인 것이다.
    이걸 다시 작동시키는 방법은 이 필터를 다시 securityconfig에 등록을 해주면 된다.

    .addFilter(new JwtAuthenticationFilter())로 달아주기만 하면 된다.
    이때 꼭 전달해야 하는 파라미터가 존재하는데 AuthenticationManager를 전달해야 한다.

    AuthenticationManager는 WebSecurityConfigurerAdapter가 들고 있기 때문에 넘겨주는게 간단하다.
    하지만 문제점. 강의 시점에서는 괜찮지만 현재 시점에서는 Adapter가 Deprecated 상태이다.
    강의 끝나고 해결 방법을 한번 찾아볼것.

    config에서 .addFilter(new JwtAuthenticationFilter(authenticationManager())
    로 작성해 authenticationManager를 넘겨주더라도 여기서 받아주는게 없기 때문에 오류가 발생한다.

    그래서 생성자를 만들어 처리해주거나 아래처럼 처리하면 된다.

    그럼 이제 authenticationManager를 통해 로그인 시도를 하면 되는데
    이때 실행되는 함수 이름이 attemptAuthentication이다.
    함수가 동작하는 시점은 /login 요청을 했을 때 로그인 시도를 위해서 실행된다.

    테스트를 해보면 NullPointerException이 발생하지만 로그는 찍히는 것을 확인할 수 있다
    그럼 로그인 시도가 발생하면 무조건 호출이 된다고 볼 수 잇다.
    로그에서도 return 부분에서 발생한 Exception으로 나오니 호출이 정상적으로 되었다고 볼 수 있다.

    그럼 이제 이 함수에서 해야할 일.
    1. username과 password를 받아서
    2. 정상인지 로그인 시도를 한다. authenticationManager로 로그인 시도를 하면 PrincipalDetailsService가 호출된다.
        즉, loadUserByUsername이 실행된다.
    3. 정상적으로 리턴이 되면 PrincipalDetails를 세션에 담고
    4. jwt토큰을 만들어서 응답해주면 된다.

    여기서 굳이 세션에 담는 이유는 권한관리 때문이다.
    세션에 값이 있어야 시큐리티가 권한관리를 해줄 수 있기 때문이다.
    만약 권한관리가 필요없는 프로젝트라면 굳이 세션에 담을 필요가 없다.



 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        System.out.println("attemptAuthentication");

        /*
            로그인 시도중인 이 함수에서 request에 담긴 아이디와 패스워드를 받는다.

         */

        //1. username과 password를 받는다.
        try {
            /*
                request.getInputStream().toString() 이 출력된다는 것은 이 안에 아이디, 비밀번호 정보가 있다는 의미다.
                그럼 가장 원시적인 방법으로는 BufferedReader로 읽어서 처리하는 방법이 있다.
                출력해보면 username=coco&password=1234가 출력되는 것을 볼 수 있다.

                데이터를 보내는 방식에 따라 어떻게 받아서 처리할지에 대해 달라지겠지만
                일반적으로 웹에서 보내는 방식은 x-www-form-urlencoded 방식이다.

                요즘에는 json으로 많이 처리하기도 하고 안드로이드에서는 json으로 처리하니 강의에서는 json을 통한 파싱을 처리한다.

             */

            /*BufferedReader br = request.getReader();
            String input = null;
            while((input = br.readLine()) != null){
                System.out.println(input);
            }*/
//            System.out.println(request.getInputStream().toString());

            /*
                json으로 파싱하는 경우는 위 방법보다 더 편한 방법이 있다.

                ObjectMapper는 json데이터를 파싱해준다.
                아래처럼 readValue로 처리해주면 User 오브젝트에 담기게 된다.
                그럼 postman에서 raw -> json으로 설정해 로그인 시도를 하면
                User(id=0, username=coco, password=1234, roles=null)
                이렇게 출력되는것을 볼 수 있다.
                넘겨주는 데이터는 username과 password뿐이니 나머지는 DB 데이터와 맞지 않은 데이터라고 보면 된다.

                그럼 이 담겨있는 데이터를 갖고 로그인 시도를 하면 된다.
                로그인 시도를 하기 위해서는 직접 토큰을 만들어야 한다.

                원래는 formlogin을 사용하면 자동으로 해주는건데 막아놨기 때문에 직접 해야 한다.
                UsernaePasswordAuthenticationToken으로 토큰을 만드는데
                들어가야될 값들은 principal, credentials로 나온다.
                또는 principal, credentials, Collection타입의 roles를 넣어주면 된다.
                roles는 의미 그대로 이해할 수 있으니 넘어가고
                principal에는 username, credentials에는 password를 넣어주면 된다.

                그럼 이 토큰으로 로그인 시도를 해보면 된다.
                이때 필요한게 authenticationManager다.
                Authentication(security.core) 타입으로 만들고
                manager.authenticate(token);
                이렇게 넣어주면 되는데

                이게 실행될때 PrincipalDetailsService의 loadUserByUsername이 실행되게 된다.

                이 authentication에는 로그인한 정보가 담기게 된다.

                principalDetails.getUser().getUsername()이 정상적으로 나온다는 것은
                인증이 정상적으로 처리되었다는 것이다.
                authentication은 authenticate를 통해 토큰으로 로그인 시도를 해보고
                로그인이 정상적으로 되면 만들어 지게 된다.
                그럼 DB에 있는 정보와 일치한다는 인증이 되었다는 의미이다.
                authentication에 있는 principal 객체를 가져와
                출력이 된다는 것은 로그인이 되었다고 볼 수 있고 이 authentication을 리턴해 세션에 저장해줘야 한다.
                굳이 리턴해주는 이유는 권한 관리를 시큐리티가 대신 해주기 때문에 하는 것이다.
                jwt를 이용하면서는 세션을 만들 이유가 없는데 권한처리때문에 만들어서 넣어주는 것이다.
                여기서 추가 테스트 해볼것.
                강의대로 따라했을때는 UserDetails에 @Data를 추가해 principalDetails.getUser()가 가능해진다.
                @Data 어노테이션이 없다면 getUser()를 사용할 수 없는데 이미 principalDetails.getUsername()은 호출이 가능하다.
                이 두가지의 차이점이 무엇인지 테스트해볼것.
                테스트 결과.
                동일하게 처리가 된다.
                @Data 어노테이션을 유지한 상태로 principalDeatils.getUsername() 을 테스트해보고
                어노테이션을 삭제한 상태로 principalDetails.getUsername()을 테스트 해봤으나
                둘다 동일하게 동작.
                그 후에 나오는 오류도 따로 존재하지 않았기 때문에 동일하게 동작된다고 봐야할것 같다.
                그럼 롬복에 대해 공부할때 항상 따라붙던말이 @Data를 남발하지 말아라.
                모든 엔티티에 Getter와 Setter를 붙이는 것은 큰 문제를 발생시킬 수 있다
                라는 말이 항상 따라 붙었기 때문에 굳이 사용하지 않는 쪽으로 하는것이 더 낫지 않을까 라는 생각을 한다.
             */

            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);

            System.out.println(user);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            Authentication authentication =
                    authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            System.out.println("로그인 완료 : " + principalDetails.getUsername());

            /*
                여기까지 처리되었으면 마지막으로 해줘야 하는것이 리턴을 해주는 것인데
                Authentication을 리턴하기 때문에 manager로 해서 담고있는 authentication 객체를 그대로 리턴해주면 된다.
                try-catch로 묶여있기 때문에 try 내에서 리턴을 하도록 해주고 오류가 발생했을 때는 바깥의 리턴을 통해 null을 리턴하도록 한다.

                authentication이 잘 리턴 되었다면 authentication이 세션에 저장이 된다.

             */

            /*
                리턴 해주기전에 해야 할 일이 하나 남았는데 jwt 토큰을 만들어주는 것이다.
                리턴 직전에 토큰을 생성해준다.

                굳이 여기서 안만들어도 되는데 이유는 attemptAuthentication함수가 종료되면 이 뒤에 실행되는 함수가
                successfulAuthentication이다.

                그럼 거기서 jwt토큰을 만들어서 request를 한 사용자에게 jwt토큰을 response 해주면 된다.
                그래서 토큰 구현은 아래 successfulAuthentication 에서 확인.
             */

            return authentication;

        } catch (IOException e) {
            e.printStackTrace();
        }
//        return super.attemptAuthentication(request, response);
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        /*
            비밀번호를 다르게 입력하면 이 함수가 실행되지 않는다.
         */
        //인증이 완료되었는지 확인하기 위한 로그
        System.out.println("successAuthentication 실행. 인증 완료 되었다는 의미.");

        //이 정보를 통해 토큰 생성.
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        /*
            라이브러리로 생성.

            RSA 방식이 아닌 Hash 암호화 방식이다.
            HMAC512 특징은 서버가 secretkey를 갖고 있어야 한다.
            이방식을 많이 사용한다.
         */

        String jwtToken = JWT.create()
                .withSubject("cocoToken")// 어떻게 하든 크게 의미 없다.
                        .withExpiresAt(new Date(System.currentTimeMillis() + (60000*10)))//만료시간. 이 토큰 유효기간을 의미. 이 먄료시간을 짧게 줘야 탈취가 되었을때의 문제가 줄어든다.
                                .withClaim("id", principalDetails.getUser().getId())
                                        .withClaim("username", principalDetails.getUser().getUsername())
                                                .sign(Algorithm.HMAC512("cos"));

        /*
            addHeader를 사용해 header에 담아서 응답한다.
            Bearer 다음 한칸을 꼭 띄워줘야 한다.
            addHeader의 속성이 name, value 인것으로 보아
            헤더에 "Authorization" : "Bearer "+jwtToken 이런 형태로 들어가는 것이다.

            withClaim은 비공개 클레임인데 넣고싶은 키밸류값을 막 넣으면 된다.
            강의에서는 아이디랑 username정도만 담는다.

            이렇게해서 로그인을 찍어보면
            응답에는 아무것도 나오지않고 Response header에는 Authorization이 생기고
            값으로 Bearer ~~~~~~~~
            이런 토큰값이 생긴것을 확인할 수 있다.

            그럼 이제 이 토큰을 받아서 처리하는 필터가 필요하다.

            일반적으로 id, pw로 로그인을 해서 정상이면
            서버에서는 세션 id를 만들어주고 이걸 클라이언트에게 쿠키로 세션아이디를 응답해준다.
            그 다음부터 요청할때마다 쿠키값 세션 id를 항상 들고 서버쪽으로 요청하기 때문에
            서버는 이 세션 id가 유효한지 판단해서 유효하면 인증 페이지로 연결할 수 있도록 해준다.

            세션 id를 들고 오니까 spring security는 세션이 정상적이면 인증이 필요한 페이지의 권한을 체크해서 알아서 보내준다.
            근데 지금은 이 방식이 아니다.

            id, pw가 정상이면 jwt 토큰을 생성하고 클라이언트로 jwt토큰을 응답한다.
            요청할때마다 jwt 토큰을 갖고 요청해야 한다.
            그럼 서버는 세션으로 판단하지 못하기 때문에 jwt토큰이 유효한지를 판단해야 하는데 이걸 처리하는 필터를 생성해야 한다.

         */
        response.addHeader("Authorization", "Bearer "+jwtToken);

//        super.successfulAuthentication(request, response, chain, authResult);
    }
}
