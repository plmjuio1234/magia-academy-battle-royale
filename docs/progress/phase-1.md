# Phase 1: 기반 구축 (3주)

**목표**: libGDX 프로젝트 구조, Kryonet 연결, 기본 플레이어 시스템 완성

**기간**: 주차 1-3

---

## 체크리스트

### 1.1 libGDX 프로젝트 구조 (✅ 완료)

- [x] Core 모듈 (공유 코드)
- [x] Desktop 모듈 (테스트용)
- [x] Android 모듈 (배포용)
- [x] 의존성 설정 (libGDX, Kryonet)
- [x] Gradle 빌드 설정

**담당**: 전체 팀

**완료 일시**: 2025-10-16

---

### 1.2 Kryonet 서버-클라이언트 연결 (✅ 완료)

#### 서버 구현
- [x] Kryonet Server 초기화
- [x] TCP 포트 설정 (5000)
- [x] UDP 포트 설정 (5001)
- [x] 메시지 클래스 등록 (Kryo)
- [x] 연결 리스너 구현

#### 클라이언트 구현
- [x] Kryonet Client 초기화
- [x] 서버 연결 로직
- [x] 메시지 수신 리스너
- [x] 기본 테스트 메시지 송수신

**담당**: 김동연 (서버), 신은성 (클라이언트)

**완료 일시**: 2025-10-23

---

### 1.3 기본 플레이어 이동 및 애니메이션 (✅ 완료)

- [x] Player 엔티티 클래스 구현
- [x] 위치, 속도, HP/MP 관리
- [x] 맵 경계 제한
- [x] 가상 조이스틱 (VirtualJoystick)
- [x] 키보드 입력 처리 (Desktop)
- [x] 플레이어 렌더링 (ShapeRenderer)
- [x] 원소 선택 시 테두리 색상 변경
- [x] 네트워크 동기화 (위치 브로드캐스트)

**담당**: 신은성

**완료 일시**: 2025-10-20

---

### 1.4 Scene2D UI 기본 틀 (✅ 완료)

- [x] BaseScreen 클래스 구현
- [x] 한글 폰트 로드 (NanumGothic, BlackHanSans)
- [x] FontManager 구현
- [x] UIHelper 유틸리티
- [x] 기본 버튼/라벨 스타일
- [x] Scene2D Stage 설정

**담당**: 신은성

**완료 일시**: 2025-10-18

---

### 1.5 화면 흐름 구현

#### 기본 화면들
- [x] SplashScreen: 로딩 화면
- [x] MenuScreen: 메인 메뉴 + 배경 이미지
- [x] RoomListScreen: 방 목록 조회/생성
- [x] LobbyScreen: 매칭 대기 (부분 완료)
- [x] CharacterSelectScreen: 캐릭터 선택
- [x] GameScreen: 게임플레이 (기본 구조)
- [x] ResultScreen: 결과 화면

**담당**: 신은성

**완료 일시**: 2025-10-22

---

### 1.6 멀티플레이어 기능 (✅ 완료)

#### 방 시스템
- [x] 방 생성 (CreateRoomMsg)
- [x] 방 목록 조회 (GetRoomListMsg)
- [x] 방 입장 (JoinRoomMsg)
- [x] 방 퇴장 (LeaveRoomMsg)
- [x] 플레이어 목록 동기화 (RoomUpdateMsg)

#### 게임 시작
- [x] 게임 시작 요청 (StartGameMsg)
- [x] 게임 시작 알림 (GameStartNotification)

#### 기타
- [x] 채팅 시스템 (ChatMsg)
- [x] 플레이어 이동 동기화 (PlayerMoveMsg)

**담당**: 김동연 (서버), 신은성 (클라이언트)

**완료 일시**: 2025-10-25

---

## 요구사항 분석

### 기술 요구사항
- libGDX 1.12+
- Kryonet 2.22.0-RC1
- Java 11+

### 성능 요구사항
- 초당 60 FPS (Desktop)
- 30+ FPS (Android)
- 네트워크 지연 < 200ms

### 사용성 요구사항
- 한글 UI 완벽 지원
- 부드러운 화면 전환
- 명확한 오류 메시지

---

## 위험 요소 및 해결책

| 위험 요소 | 영향 | 대처 방안 |
|----------|------|----------|
| 한글 폰트 렌더링 실패 | 높음 | FontManager로 일원화 관리 |
| Kryonet 연결 불안정 | 높음 | TCP/UDP 선택식 사용, 재연결 로직 |
| 네트워크 지연 큼 | 중간 | 클라이언트 예측 (보간) 적용 |
| 메모리 부족 (Android) | 중간 | 텍스처 압축, Object Pooling |

---

## 진행 상황 요약

**상태**: ✅ **완료**

현재 Phase 1은 완벽하게 완료되었습니다. 모든 기본 인프라가 준비되었으며, 클라이언트와 서버 간 통신이 정상 작동합니다.

다음 단계인 **Phase 2**로 진행할 준비가 완료되었습니다.
