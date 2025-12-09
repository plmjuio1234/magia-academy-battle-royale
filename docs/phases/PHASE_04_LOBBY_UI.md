# PHASE_04_LOBBY_UI.md - 로비 UI 구현

---

## 🎯 목표
방 목록 및 캐릭터 커스터마이징 UI 구현 (좌측 2/3 + 우측 1/3 분할)

---

## 📋 구현 범위

**좌측 (2/3)**: 방 목록
- ✅ 방 목록 표시 (방 제목, 인원수)
- ✅ [참가] 버튼 (클릭 → WaitingRoom으로)
- ✅ [새로고침] (목록 업데이트)
- ✅ [타이틀로] (돌아가기)

**우측 (1/3)**: 캐릭터 미리보기
- ✅ 캐릭터 미리보기 렌더링
- ✅ 닉네임 표시/입력
- ✅ [외형변경] 버튼
- ✅ [게임시작] 버튼 (비활성 상태)

---

## 📁 필요 파일

```
screens/
  └─ LobbyScreen.java (새로 생성)

ui/lobby/
  ├─ RoomListPanel.java (새로 생성)
  └─ CharacterPreviewPanel.java (새로 생성)
```

---

## 🔧 구현 가이드

### LobbyScreen 구조

```java
public class LobbyScreen implements IScreen {
    private RoomListPanel roomListPanel;
    private CharacterPreviewPanel charPanel;

    @Override
    public void update(float delta) {
        // 입력 처리
        roomListPanel.update(delta);
        charPanel.update(delta);

        // [참가] 버튼 클릭 처리
        if (roomListPanel.isJoinButtonPressed()) {
            int roomId = roomListPanel.getSelectedRoomId();
            NetworkManager.getInstance().sendMessage(
                new JoinRoomMsg(roomId));
            // PHASE_05로 진행
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // 2/3 좌측: 방 목록
        roomListPanel.render(batch);

        // 1/3 우측: 캐릭터
        charPanel.render(batch);
    }
}
```

---

## 🧪 테스트 계획

```
[ ] 방 목록이 화면의 2/3를 차지한다
[ ] 캐릭터 미리보기가 우측 1/3에 표시된다
[ ] [참가] 버튼 클릭 시 서버로 JoinRoomMsg 전송
[ ] [새로고침] 버튼 클릭 시 GetRoomListMsg 전송
```

---

## ✅ 완료 조건

- [ ] LobbyScreen 레이아웃 구현
- [ ] RoomListPanel 구현
- [ ] CharacterPreviewPanel 구현
- [ ] 모든 버튼 상호작용 확인

