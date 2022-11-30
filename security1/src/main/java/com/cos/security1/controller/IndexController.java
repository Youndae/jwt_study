package com.cos.security1.controller;

import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping({"", "/"})
    public String index(){
        // Mustache의 path는 src/main/resources 이다.
        // 그래서 viewResolver를 설정할 때 : (prefix)/templates/, (suffix).mustache로 잡으면 된다.
        // dependencies에 mustache를 넣어주면 기본적으로 저 경로로 잡히기 때문에 굳이 yml에 설정할 필요는 없다.
        // index페이지의 경우 html로 작성한다. mustache로 만들어주면 복잡해지기 때문이라고 한다.
        // 근데 그럼 index.mustache가 아닌데 어떻게 index 페이지를 찾는가?
        // 이것에 대한 설정을 WebMvcConfig에서 설정해준다. 하지만 여기서 설정하는것과 yml에서 설정하는것의 차이가 무엇인지 알아볼것.

        return "index";
    }

    @GetMapping("/user")
    @ResponseBody
    public String user(){



        return "user";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String admin(){

        return "admin";
    }

    @GetMapping("/manager")
    @ResponseBody
    public String manager(){

        return "manager";
    }

    @GetMapping("/loginForm")
    public String loginForm(){

        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm(){

        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user){
        System.out.println(user);

        user.setRole("ROLE_USER");

        String rawPwd = user.getPassword();
        String encPwd = bCryptPasswordEncoder.encode(rawPwd);
        user.setPassword(encPwd);

        userRepository.save(user);

        return "redirect:/loginForm";
    }

    /*
        @Secured를 사용하기 위해서는 SecurityConfig에서
        @EnableGlobalMethodSecurity(securedEnabled = true)
        이 어노테이션이 필요하다.
        @PreAuthorize와 동일한 기능.
        그리고 @PreAuthorize를 사용하기 위해서는
        @EnableGlobalMethodSecurity(prePostEnabled = true)
        로 써야 가능하다고 강의에서 나오는데 내가 진행한 프로젝트들을 보면이게 없이 사용했다.

        그래서 여기서 테스트해보니 어노테이션을 주석처리하면 권한 설정이 제대로 동작하지 않았고
        어노테이션 설정을 해야 제대로 동작하는것으로 확인.

        @PostAuthorize도 존재하는데 메소드가 종료한 뒤의 권한 처리다.
        설정의 경우 prePostEnabled = true 하나로 @PreAuthorize와 @PostAuthorize 모두 사용이 가능하다.
        속성명을 보면 prePost로 되어있기 때문에 두개 다에 해당한다는 것을 알 수 있다.
        @PostAuthorize는 요즘엔 잘 사용하지 않는다고 한다.


        권한을 하나만 설정하고자 하면 @Secured()
        여러 권한을 지정하고자 하면 @PreAuthorize() 를 사용하면 된다.
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    @ResponseBody
    public String info(){
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/data")
    @ResponseBody
    public String data(){
        return "데이터정보";
    }

}
