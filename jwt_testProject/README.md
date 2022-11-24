# 프로젝트 설명.

---

인프런 jwt 강의에서 postman만을 사용해 테스트 했으니   
html 파일을 만들어서 실제 프로젝트에 어떻게 적용이 되는지에 대한 테스트.   

---

# 강의와의 차이점

---

현재 시점에서 WebSecurityConfigurerAdapter는 Deprecated 상태이므로   
현 상황에 맞게 Adapter를 사용하지 않고 구현.
> 이 방법의 문제점
> > SecurityConfig에서 필터를 추가할때 각 필터에
> > authenticationManager()를 넣어주었는데 Adapter를 사용하지 않다보니
> > 여기서 오류가 발생함.   
> > 그래서 문제 해결로 Adapter에 들어가서 authenticationManager() 메소드를
> > SecurityConfig 하단에 작성해주는 방법으로 해결.
> > 근데 이건 1차원 적인 해결방법으로 보임.   
> > authenticationManager() 메소드를 호출하지 않거나 다르게 사용하는 방법으로
> > 변경하는 것이 좀 더 나은 방법이지 않나 싶음.
> > 일단 강의내용까지의 테스트에서는 정상적으로 작동.

---

# 테스트 전 체크사항.

---

1. html 페이지 연결 체크
2. 로그인 페이지에서 /login으로 데이터를 넘겨줄때 json을 사용.

---

# 테스트 사항

---

1. 로그인이 정상적으로 처리되는지 체크
2. 로그인 후 각 페이지로 접근시에 권한 문제에 대해 정상적으로 처리되는지 체크


# 문제점

---

로그인 처리 완료.
정상적인 로그인 처리가 되는것으로 보이나 토큰이 생성이 되지 않은 것인지   
아니면 토큰이 헤더로 넘어오지 않은것인지 헤더로 넘어온 토큰이 같이 넘어가지 않은 것인지 403발생함.   
전체적으로 로그 찍어서 확인하기.

> ### 오류 정리   
> 토큰이 ResponseHeader로 정상적으로 넘어왔으나 RequestHeader에 토큰을 담지 못해서 발생한 문제.   
> JQuery - Ajax 기반에서 ReqeustHeader에 담는 방법은 아래 두가지.
> ``` javascript
>   XMLHttpRequest xhr = new XMLHttpRequest();
>   xhr.open("post", "/api/v1/user", true);
>   xhr.setRequestHeader("Authorization", token);
>   xhr.send();
> 
> 
>   //Ajax
>   $.ajax({
>       type: "post",
>       url: "/login",
>       data: data,
>       beforeSend: function(xhr){
>           xhr.setRequestHeader("Authorization", token);
>       },
>       success: function(data){
>           console.log("success");
>       }
>   });
> ``` 
> 하지만 이 방법의 문제점은 GET, POST 요청 모두 가능하지만 페이지 이동이 불가하다는 문제가 있다.   
> location.href 를 통해 페이지 이동을 할 수는 있지만 권한이 있는 페이지에서는 RequestHeader에 토큰을 담을 수가 없어서   
> 근본적인 해결책이 되진 않는다고 보임.


로그인 시 토큰 생성 후 정상적으로 리턴 될 시
ajax에서 success를 통해 페이지 이동 전 토큰을 localStorage에 저장하도록 함.
여기서 문제점.
request.getHeader()는 localStorage를 체크하지 않아
Authorization을 가져오지 못함.
그럼 Header에 계속 넣어주는 방법을 택해야 하는것인지
아니면 localStorage에서 꺼내오는 방법이 있는지
아니면 Cookie에 넣어 여기서 꺼내 사용할 것인지
방법 찾아봐야함.

일단 생각하는 대로 적어보면
로그인 처리 -> 토큰 생성 -> 리턴 -> 토큰 storage저장 -> 
권한 필요한 페이지 접근 시 storage에서 토큰 추출 -> 
토큰 검증 후 인증 처리.

이렇게 진행이 되어야 할 것 같은데 다 구현이 되어있는 상태이지만
토큰을 어디에 저장하고 어떻게 넘겨줘야 할지가 고민.
매번 헤더에 넣어서 넘겨준다 라는 개념으로는 부족한게 로그인을 해놓고
버튼이동이 아닌 주소 이동을 하는 경우 그럼 헤더에 담는 부분이 빠지기 때문에
문제가 발생할 수 있음.

> ### 오류 정리    
> ``` javascript
>   $.ajax({
>       type: "post",
>       url: "/login",
>       data: data,
>       success: function(data, textStatus, request, xhr){
>          console.log("success");
>
>          var token = request.getResponseHeader('Authorization');
>
>          xhr.setRequestHeader("Authorization", token);
>
>          localStorage.setItem("Authorization", token);
>
>          location.href="/api/v1/user";
>          
>       }
>       });
> ```
> 이렇게 localStorage에 넣는 방법을 선택했으나 결과적으로는 권한 페이지인 /api/v1/user에 접근할 때   
> RequestHeader에 토큰이 실리지도 않을 뿐더러 beforeSend에서 사용할때와 달리 success에서 사용하는 경우에는   
> xhr.open()이 필요하다. 그래서 오류가 발생.   
> 그리고 xhr.open()을 추가해서 처리하게 되면 이미 해당 페이지에 헤더를 넘겨준 상태이기 때문에 다시 헤더가 비어있게 되고   
> 결국 locaion.href로 이동할때는 Header에 토큰이 담기지 않게 된다.   
> 여러 방법을 찾아봤지만 결국 마땅한 해결책을 찾지 못함.   
> 이 방법 역시 Rest 방식에서는 사용이 가능하지만 권한 페이지 이동에 대해서는 해결 방법을 찾지 못했다.   
> localStorage를 서버에서 꺼낼 방법도 없고 한가지 방법이 있어 보이는 것은 페이지 이동시에 form을 동적으로 생성해서   
> 이 form에 토큰을 담아서 보내는 방법정도가 있을 것 같다.   
> 하지만 이 방법의 문제점은 버튼을 눌러 이동하는 형태가 아닌 주소 입력으로 이동하는 방식에서는 불가능하다는 점.   
> 그래서 localStorage에 저장하는 방식이 아닌 Cookie에 저장하는 방식으로 변경하기로 결정.



쿠키로 토큰을 받아 처리하는데 있어서 기본적인 문제는 해결.
쿠키에서 토큰값을 꺼내 검증까지 구현.
그럼 이제 문제점.
쿠키가 삭제되어야 하는 시점을 정해야 한다.
1. 로그아웃 시. (처리 완료.)
2. 브라우저 종료 시. 

아니면 토큰 시간 도 짧으니 토큰 시간보다 1분정도만 쿠키 기간을 더 주도록 잡아서
토큰이 만료될때 쿠키 역시 만료되도록 시간을 설정.
일단 제일 포인트는 로그아웃 시에는 쿠키가 삭제되도록 처리는 해야될듯.

> ### 1차 문제해결   
> 토큰을 저장하는 방법에 있어서 거론되는 방법은 localStorage에 저장하는 방법과 Cookie에 저장하는 방법 두가지가 대표적.   
> localStorage에 저장하는 방법으로 처리하고 토큰을 헤더에 담아 처리하도록 하고 싶었지만 restController에 접근하는 것은 해결이 되었으나   
> 권한 페이지 이동에 대한 문제점을 해결하지 못해 Cookie방식으로 변경.
> 토큰 생성 후 response.addCookie()로 쿠키를 클라이언트에게 넘겨주고 요청이 들어왔을때는 헤더를 확인하는 것이 아닌 쿠키에서 토큰을 확인하도록 구현.   
> 권한페이지 접근 테스트 시 정상적으로 처리가 되는것을 확인.   
> 그리고 쿠키에 대한 보안을 강화하기 위해 쿠키를 넘겨주기 전 httpOnly를 설정해 넘겨주도록 구현.   
> 사용자가 로그인하는 경우 토큰이 새로 발급 되기 때문에 로그아웃을 하는 경우 쿠키를 삭제하도록 구현.   
> 현재 refreshToken에 대한 구현이 없는 상황인데 이걸 구현하고 나면 쿠키 역시 만료 시간을 재설정 해야 한다.   
> 그리고 쿠키 만료 시간은 토큰 만료시간보다 살짝만 길게 설정하면 되지 않을까 한다.   
> 이렇게 구현하는 경우 쿠키에 토큰이 담겨있기 때문에 클라이언트에서 요청시에 따로 토큰을 담는다거나 하는데에 대한 필요성이 줄어든다는 장점이 있다고 보임.

#

> ### 남은 문제점 및 궁금한점
> 1. localStorage에 저장을 하고 토큰을 RequestHeader에 담아서 페이지 이동을 하고자 한다면 어떻게 처리를 해야 하는가
> 2. Cookie에 저장해 처리하는 방법을 구현했는데 httpOnly를 설정한 것 만으로 괜찮은 것인가
> 3. Cookie에는 공백을 넣을 수 없어서 Bearer 뒤에 공백을 제거한 후 토큰을 붙여 넘겨주는 방법을 택했는데 이에 대한 문제가 발생하지 않는가
