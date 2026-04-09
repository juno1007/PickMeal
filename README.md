<div align="center">
  <br />
  <h2>🍔 PICKMEAL (한끼뚝딱) 🍱</h2>
  <p><b>게임 요소를 결합한 맞춤형 식사 추천 & 커뮤니티 플랫폼</b></p>
  <br />
</div>

<div align="center">
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Amazon_S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white">
  <br>
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white">
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">
</div>

<br>

### 📄 프로젝트 문서
[👉 **프로젝트 문서(PDF) 보기**](./프로젝트%201차_1조%20-%20한끼뚝딱(PickMeal).pdf)

---

### 👨‍💻 팀원 소개 (Team 1조)
| 포지션 | 이름 | 담당 업무 | Github |
|:---:|:---:|---|:---:|
| **팀장** | **강한수** | 프로젝트 총괄, 브랜치 병합 관리, 아키텍처 설계, 백엔드 개발 | [@dlive838-maker](https://github.com/dlive838-maker) |
| **팀원** | **김승경** | AWS S3 이미지 인프라 구축, 카카오맵 API 연동 및 데이터 시각화 | [@iamsugu0106](https://github.com/iamsugu0106) |
| **팀원** | **김재민** | 프론트엔드 UI/UX 디자인, HTML/CSS/JS 위주 뷰페이지 구현 | [@woals10006-commits](https://github.com/woals10006-commits)|
| **팀원** | **황준호** | 월드컵 알고리즘 설계, OAuth2 연동, DB 및 카카오맵 API 구축 | [@juno1007](https://github.com/juno1007) |

---

### 💡 기획 배경 및 목적
매일 반복되는 "오늘 뭐 먹지?"라는 메뉴 결정의 피로도와 스트레스를 해결하기 위해 기획되었습니다. 룰렛, 스무고개, 이상형 월드컵 등의 **재미있는 게임 요소**를 도입하여 메뉴 고민을 즐겁게 해결하고, 추천받은 메뉴를 바탕으로 **주변 맛집 탐색 및 커뮤니티 활동**까지 이어갈 수 있는 종합 미식 플랫폼입니다.

---

### 🚀 주요 기능 (Key Features)

#### 🎮 게임 기반 메뉴 추천
* **음식 이상형 월드컵:** 토너먼트 방식으로 경쟁하여 현재 취향에 딱 맞는 음식 선정
* **스무고개:** 국물 여부, 맵기 등 속성 질문을 통해 최적의 메뉴 추론
* **룰렛 & 캡슐 뽑기:** 무작위성이 주는 직관적인 짜릿함으로 메뉴 결정

#### 🗺️ 장소 탐색 및 커뮤니티
* **카카오맵 탐색:** 추천받은 메뉴를 기반으로 주변 식당 위치 및 통계 정보 제공
* **커뮤니티 게시판:** 생생한 식사 후기와 맛집 추천, 이미지 업로드(AWS S3 연동) 기능
* **리뷰 시스템:** 상세한 식당 평가, 좋아요(찜하기), 댓글을 통한 유저 상호작용

#### 🔐 회원 관리 및 보안
* **OAuth2 소셜 로그인:** 카카오, 네이버, 구글 간편 로그인 지원
* **일반 회원가입:** 이메일 인증을 통한 안전한 회원가입
* **관리자 기능:** 유저 검색 및 권한(정지) 관리 모드 제공

---

### 💻 기술 스택 (Tech Stack)
* **Frontend:** HTML5, CSS3, JavaScript, Thymeleaf
* **Backend:** Java, Spring Framework
* **Database:** MySQL
* **Security & Auth:** Spring Security, OAuth2, JWT
* **Infrastructure:** AWS S3

---

### 📸 프로젝트 시연 (Demo Video)

각 이미지를 클릭하면 해당 기능의 유튜브 시연 영상으로 이동합니다.

| 01. 메인 & 소셜 로그인 | 02. 게임 기반 메뉴 추천 |
| :---: | :---: |
| [![시연 영상 1](https://img.youtube.com/vi/hvra-ooJCik/mqdefault.jpg)](https://youtu.be/hvra-ooJCik) | [![시연 영상 2](https://img.youtube.com/vi/sqcy-zeS3RM/mqdefault.jpg)](https://youtu.be/sqcy-zeS3RM) |
| 소셜 로그인 프로세스 및 <br>직관적인 메인 페이지 | 월드컵, 스무고개, 룰렛을 통한 <br>즐거운 메뉴 결정 과정 |

| 03. 주변 맛집 지도 탐색 | 04. 커뮤니티 및 리뷰 |
| :---: | :---: |
| [![시연 영상 3](https://img.youtube.com/vi/drSzSyq9T1Q/mqdefault.jpg)](https://youtu.be/drSzSyq9T1Q) | [![시연 영상 4](https://img.youtube.com/vi/Q4Oldm4fKNk/mqdefault.jpg)](https://youtu.be/Q4Oldm4fKNk) |
| 추천 메뉴 기반 카카오맵 <br>주변 식당 위치 확인 | S3 이미지 업로드 기반 게시판 및 <br>생생한 맛집 리뷰 작성 |

| 05. 관리자 제어 기능 |
| :---: |
| [![시연 영상 5](https://img.youtube.com/vi/t91Yp27cJhA/mqdefault.jpg)](https://youtu.be/t91Yp27cJhA) |
| 관리자 권한을 통한 <br>부적절한 게시글 삭제 및 회원 정지 |
---

### 🛠 트러블 슈팅 (Trouble Shooting)

**1. Git 병합 충돌 및 DB 스키마 불일치**
* **문제:** 동시 다발적인 기능 개발로 인해 공통 설정 파일(`pom.xml`, Mapper 등)에서 심각한 Merge Conflict 발생.
* **해결:** 팀장이 병합을 전담하여 `develop` 브랜치 위주로 안전하게 코드를 녹여내는 프로세스 확립. DB 컬럼 변경 시 SQL 스크립트를 즉각 공유해 환경 불일치 원천 차단.

**2. 소셜 로그인/일반 로그인 인증 정보 통합**
* **문제:** 인증 방식 차이로 인해 일관된 세션 및 권한 제어에 어려움 존재.
* **해결:** Spring Security의 `CustomOAuth2UserService`를 구현하여 공급자(Provider) 데이터를 하나로 정규화, 인증 객체를 통합적으로 관리.

**3. 파일 업로드 용량 및 참조 한계**
* **문제:** 로컬 저장소에 이미지를 저장할 경우 배포 시 경로 오류 및 서버 스토리지 부하 발생 우려.
* **해결:** AWS S3 클라우드 스토리지를 연동하여 파일 서버를 완전히 분리, 빠르고 안정적인 이미지 서빙 환경 구축.

---

### 🔮 향후 발전 방향 (Future Works)
* **SPA 기반 앱 고도화:** React 등을 도입해 모바일 환경에 최적화하고 푸시 알림 연동
* **지능형 추천 AI:** LLM API를 활용해 대화 형식으로 상황에 맞는 메뉴를 추천해 주는 기능
* **성능 및 캐싱 최적화:** Redis 캐싱과 데이터베이스 쿼리 튜닝을 통해 대용량 트래픽에 대응
