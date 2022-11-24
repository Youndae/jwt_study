package com.cos.jwt.config.auth;

import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
    http://localhost:8080/login
    으로 로그인 요청이 올 때 동작을 한다.
    시큐리티가 기본적으로 로그인 요청 주소가 /login이기 때문.

    하지만 securityConfig에서 formlogin().disalbed()를 설정해두었기 때문에 /login으로 요청을 전달할 수 없다.
    원래는 formlogin().loginProcessingUrl()을 통해 설정하거나 설정하지 않으면 defualt로 /login으로 되도록 되어있지만
    그게 막힌 상황이다.

    그래서 login요청을 보냈을 때 직접 PricipalDetailsService를 호출해 처리하도록 구현해야 한다.

    구현을 위해 필터를 하나 만들어준다.
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("PrincipalDetailsService 의 loadUserByUsername");
        User userEntity = userRepository.findByUsername(username);

        System.out.println("userEntity : " + userEntity);


        return new PrincipalDetails(userEntity);
    }
}
