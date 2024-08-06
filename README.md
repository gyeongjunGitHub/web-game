Project name : Scatch Master  

[링크]
http://54.180.115.155:8080/


설명 : 사용자가 주어진 단어를 그림으로 설명하고, 다른 사용자가 그림과 단어의 초성을보고 정답을 맞추는 게임입니다.
(최소 2인, 최대 3인)😊😊


[게임 화면]
![image-43](https://github.com/user-attachments/assets/cba43cd0-dd12-4d5d-8a00-87b669f21574)


-------------------------------------------------------------------------------------------------------------

[사용 언어 및 개발환경]

java 17.ver, Spring Boot(3.2.6 ver), MySQL, JavaScript, JPA, AWS EC2, AWS RDS, Web Socket 

-------------------------------------------------------------------------------------------------------------

[인력구성 및 기여도]

 - 총 2명. BackEnd 1명, Front End 1명(디자인 위주)

 - 기여도 : 90% (디자인 제외 나머지 모두)

-------------------------------------------------------------------------------------------------------------

[주요업무 및 상세역할]

- API 설계

- 로그인 / 회원가입 기능 구현

- 관리자 페이지 / 마이페이지 구현

- 간단한 매칭 기능(인원수만 충족하면 매칭성공 알고리즘 x)

- 실시간 스케치 기능 및 GameManager 클레스를 통한 게임 로직 구현

- 친구추가 기능 구현

- 상점 페이지, 아이템 구매 기능 구현

- 내 아이템 페이지, 아이템 사용 기능 구현

- 1대1 채팅 시스템 구현

- AWS EC2(프리티어) 사용하여 배포 -> 메모리부족 문제 메모리스왑 하드디스크를 사용해 속도 느림.

-------------------------------------------------------------------------------------------------------------

[architecture]

![architecture](https://github.com/user-attachments/assets/96c97e61-6679-470f-a196-4d7632ea000a)

-------------------------------------------------------------------------------------------------------------

[ERD]

![image-45](https://github.com/user-attachments/assets/5ef5bda2-a09b-4efa-93a8-2ddf0d0a7f5c)

-------------------------------------------------------------------------------------------------------------

[로그인]

![image](https://github.com/user-attachments/assets/91489c82-ba5c-4f6d-9206-0c5564511aa6)

-------------------------------------------------------------------------------------------------------------

[회원가입]

![image](https://github.com/user-attachments/assets/831139c1-648a-43d3-bc73-6254aeec26bb)

-------------------------------------------------------------------------------------------------------------

[관리자페이지 / 마이페이지]

![image](https://github.com/user-attachments/assets/df0302ac-947e-4b00-971c-581e18fe2cef)

-------------------------------------------------------------------------------------------------------------

[게임시스템]

![image](https://github.com/user-attachments/assets/5f6374f6-7fa1-465b-b18a-bed5a084939b)

![image](https://github.com/user-attachments/assets/cc312e91-b91d-4336-8521-9379b8e74a36)

![image](https://github.com/user-attachments/assets/ddbce86e-1130-4dbb-8aec-4da411664615)

![image](https://github.com/user-attachments/assets/9ea8cb48-4517-4e3c-a12a-7f0d85c31b0f)

step. 4 까지 완료 후 게임화면 세팅까지 끝나면 정해진 게임의 규칙에 따라 게임을 진행합니다.


[규칙]

- 게임이 시작되면 정해진 순서에 따라 그림을 그리는 역할을 수행합니다.

- 그림을 그리는 인원을 제외한 나머지 인원은 그림과 퀴즈 단어의 초성을 보고 정답을 맞추면 됩니다.

- 그림을 잘 그려 정답을 맞추기 쉽게 한 유저는 칭찬을 통해 추가 점수를 획득 할 수 있습니다.

-------------------------------------------------------------------------------------------------------------

[1대1 채팅 기능]

![image](https://github.com/user-attachments/assets/6fdf23f6-46ca-40c9-bde1-3d2a75c5b32c)
![image](https://github.com/user-attachments/assets/0770170b-42a9-46e9-92e9-8a981e699010)

-------------------------------------------------------------------------------------------------------------

[친구추가 기능]

![image](https://github.com/user-attachments/assets/574b0146-2aea-428d-b3f2-7b190c43918a)

![image](https://github.com/user-attachments/assets/a79ee837-cf10-45db-b84b-73b881e0eae8)

![image](https://github.com/user-attachments/assets/93cc1da7-9299-462b-b450-1cdaa1c55bac)

![image](https://github.com/user-attachments/assets/13726090-6818-448e-8a01-056021d5878f)

-------------------------------------------------------------------------------------------------------------

[상점]

![image](https://github.com/user-attachments/assets/008b2bdb-bb75-429b-8e89-c37726424905)

![image](https://github.com/user-attachments/assets/2e407141-a6d6-4eb6-bad8-b4d85c5f0fe7)

-------------------------------------------------------------------------------------------------------------

↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

[내 아이템] 화면입니다. 아이템을 사용하면 개수가 차감됩니다.

![image-9](https://github.com/user-attachments/assets/046043f4-80ad-4727-b304-0072546b7092)

![image-12](https://github.com/user-attachments/assets/1e112f3b-d153-4871-ba4b-d70a473e0161)

![image-13](https://github.com/user-attachments/assets/80c8cabc-3f3e-4480-bd1f-9965b7714bff)

------------------------------------------------------------------------------

[마이페이지]

회원정보 수정 기능은 이후 만들 예정입니다.

![image-14](https://github.com/user-attachments/assets/7b34a8ef-6ab9-4334-bce4-48f0b262498e)

------------------------------------------------------------------------------

[친구추가]

![image-19](https://github.com/user-attachments/assets/2ec5862e-6af4-4125-acbd-211047a531f6)

![image-18](https://github.com/user-attachments/assets/944d155f-51d8-4353-a07a-932e85c9ffa9)

------------------------------------------------------------------------------

[관리자 페이지]

권한이 ROLE_ADMIN일 경우만 접근 가능합니다.

![image-25](https://github.com/user-attachments/assets/579aa0f6-3bfa-4454-bf95-fdebcf496fd0)

-프로필 사진 선택 : 회원가입 시 기본 프로필 사진 이미지를 설정할 수 있습니다.

-상점 아이템 등록 : 관리자가 상점에 아이템을 등록 할 수 있습니다.

![image-26](https://github.com/user-attachments/assets/a89c319f-d3bf-4226-b269-14c986669ad3)

-상점 아이템 삭제 : 관리자가 상점에 아이템을 삭제 할 수 있습니다.


![image-27](https://github.com/user-attachments/assets/7ea0eb37-1f24-4130-8188-a03e26ef528c)

![image-28](https://github.com/user-attachments/assets/cd93a49c-af6b-447a-bf6c-d1897b143e2f)

![image-29](https://github.com/user-attachments/assets/fe43b589-0da6-468f-a1ac-7fa2b6c9be09)


-게임 설정 세팅 : 관리자가 게임 라운드의 시간 제한을 설정 할 수 있습니다.

![image-30](https://github.com/user-attachments/assets/be1e5d0b-f333-4785-b2c4-08f4c980f240)

------------------------------------------------------------------------------


