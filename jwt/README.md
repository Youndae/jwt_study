# inflearn jwt 실습

jwt 라이브러리는 Java JWT로 사용.
강의에서는 3.10.2를 사용했지만
22/11/17 기준 4.2.1 까지 나온 상태이고
종속성 취약점이 발생했다는 문구가 MVNRepository에 나와있어서
4.2.1로 사용.

# 강의 내에서의 몇가지 의문? 궁금점?

---
1. BCryptPasswordEncoder Bean의 위치.
    > 기존 프로젝트 진행할때도 그렇고 보통 SecurityConfig 내에   
   > 빈을 생성해서 사용했으나 강의 중간에 위치변경(충돌문제)라는 섹션으로
   > 위치를 securityconfig에서 JwtApplication으로 변경하라는
   > 부분이 있었다. 그래서 securityConfig에 그대로 작성해서 사용했으나
   > 문제가 발생하지 않았다.
   > 당연히 JwtApplication에 빈을 작성해 둔 경우도 문제가 발생하지 않았다.
   > 추후 진행하는 강의에서 꼬인다면 그때 다시 한번 확인.

2. JwtAuthenticationFilter 클래스에서 principalDetails.get... 에 대한 의문점
    > authentication.getPrincipal() 로 PrincipalDetails에 정보를 담아주었는데
   > 이때 강의내에서는 PrincipalDetails에 @Data 어노테이션을 부여해 getter, setter를 생성하고
   > getUser() 를 통해 username과 userpassword를 가져온다.   
   > 하지만 이미 get까지만 찍어봤을 때 getUsername과 getUserPassword()가
   > 존재하는 상황이었고 그래서 테스트를 수행해본 결과   
   > @Data 어노테이션을 그대로 유지한 상태에서 문제없음.   
   > @Data 어노테이션을 삭제한 상태에서도 문제가 발생하지 않았다.   
   > 그럼 굳이 @Data를 사용하지 않아도 된다는 얘긴데 왜 굳이 사용해야 하는지??   
   > 갑자기 든 생각. getUser().getUsername()은 User Entity에서 가져온다는 개념이고
   > getUsername()은 UserDetails에서 가져온다는 개념인데
   > DetailsService에서는 어차피 username만 받으니까 일단은 문제가 발생하지 않는다고 볼 수 있을 것 같고
   > 추후 진행하는 강의에서 User 엔티티를 통해 처리해야하는 과정이 있다면 @Data 어노테이션이 필요할것으로 보인다.
   > 토큰을 생성할 때 getUser().getId()를 사용해야 하기 때문에 @Data 어노테이션을 사용하고 가져와야 할것같다.


# 11/21 강의 시청 완료.

---

## 확인할 사항
1. 계속 postman으로만 테스트했으니 html파일을 제대로 만들어서 테스트 할 것.
2. jwt의 구조를 정리하고 동작 방식에 대해 정리할 것.
3. 강의에서 구축한 방법에 대한 순서 및 내용 총 정리할 것.
4. 좀 더 다른 방법이 있는지에 대해 알아볼 것.