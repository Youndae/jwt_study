package com.cos.security1.config.auth;


import com.cos.security1.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/*
    security가 /login 요청을 낚아채서 로그인을 처리해준다.
    이때 로그인 진행이 완료가 되면 session을 만들어준다.
    이 session은 security만의 session이다. 같은 HttpSession이지만 security의 정보를 담고 있고 security만의 세션공간이라고 보면 된다.
    그럼 키값으로 구분을 한다는 것인데 security contextHolder라는 키값을 담고 있다.
    이 세션에 들어갈 수 있는 정보는 시큐리티가 갖고 있는 세션에 들어갈 수 있는 오브젝트가 정해져있다.
    Authentication 타입의 객체여야 한다.
    이 Authentication안에는 User정보가 있어야 한다.
    이 User 오브젝트의 타입은 UserDetails 타입의 객체여야 한다.

    시큐리티가 갖고 있는 세션에 들어갈 수 있는 객체는 Authentication이라는 타입의 객체여야 하고
    이 Authentication 객체 안에 유저 정보를 저장할 때 유저 정보는 UserDetails 타입이어야 한다.
    그래서 session정보를 get해서 꺼내게 되면 Authentication 객체가 나오게 되고
    그 안에서 userDeatils객체를 꺼내면 User오브젝트에 접근할 수 있다.

 */
public class PrincipalDetails implements UserDetails {

    private User user; //composition

    public PrincipalDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*
            해당 유저의 권한을 리턴하는 곳.
            user.getRole(); 가 권한에 대한 것인데 이걸 지금 String 타입으로 설정해놨기 때문에
            타입이 맞지 않아 넘겨줄 수 없다.
            그래서 맞는 타입으로 변환해야 한다.

            아래와 같이 Collection 타입으로 변환해서 넘겨주면 된다.
         */

        Collection<GrantedAuthority> collect = new ArrayList<>();

        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });

        return collect;
    }

    @Override
    public String getPassword() {
        // password return 이기 때문에 user.getpassword()를 리턴해주면 된다.
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        //password 와 마찬가지로 같은 String 타입이므로 그냥 리턴.
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정이 만료되었는지에 대한 처리 메소드
        // 강의에서는 true를 return에 만료되지 않았다고 처리.
        // 그럼 메소드명 그대로 isAccountNonExpired 즉, 계정이 만료되지 않았는가? 에 대한 리턴을 한다고 생각하면 될것 같다.
        // 그래서 true를 리턴하면 계정이 만료되지 않았고 false를 리턴하면 계정이 만료되었다고 판단.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        /*
            isAccountNonExpired와 마찬가지로 계정이 잠기지 않은 상태인가? 에 대한 처리 메소드.
            return 에 대한 의미는 NonExpired와 동일하다.
         */
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        /*
            비밀번호 유지 기간에 대한 처리 메소드.
            흔히 비밀번호를 바꾼지 90일이 지났습니다 뭐 이런거 처리할 때 사용하는 메소드
            비밀번호를 오래 사용하지 않았다면 Non 이기 때문에 true를 리턴.
            오래 사용했다면 false를 리턴하도록 한다.
         */
        return true;
    }

    @Override
    public boolean isEnabled() {
        /*
            계정 활성화에 대한 메소드.

            계정 활성화라는 의미는 휴면계정이 아닌지에 대한 여부다.
            한마디로 로그인이 가능한 계정인가에 대한 여부.

            보통 1년간 로그인을 하지 않은 페이지에 대해 휴면계정으로 전환이 되는데
            이때 휴면 계정이라면 여기서 false가 리턴이 되고
            휴면계정이 아니라면 true가 리턴이 되는것.

         */
        return true;
    }
}
