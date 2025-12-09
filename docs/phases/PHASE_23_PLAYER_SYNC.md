# PHASE_23_PLAYER_SYNC.md - 원격 플레이어 동기화

---

## 🎯 목표
원격 플레이어 렌더링 및 실시간 동기화

---

## 📋 구현 범위

- ✅ PlayerUpdateMsg (위치/상태 전송)
- ✅ 원격 플레이어 렌더링
- ✅ 보간(Interpolation)으로 부드러운 이동
- ✅ 원격 플레이어 스킬 시전 동기화

---

## 📁 필요 파일

```
network/messages/
  └─ PlayerUpdateMsg.java

game/player/
  └─ RemotePlayer.java
```

---

## 🔧 구현 가이드

### 1. PlayerUpdateMsg

```java
public class PlayerUpdateMsg {
    public int playerId;
    public float x, y;
    public int state;           // PlayerState
    public int health;
    public int element;         // 선택한 원소
}
```

### 2. RemotePlayer 클래스

```java
/**
 * 원격 플레이어
 */
public class RemotePlayer extends Player {
    private Vector2 targetPosition;    // 보간 목표
    private float interpolationSpeed = 10f;

    public RemotePlayer(int playerId) {
        super(playerId);
        this.targetPosition = new Vector2();
    }

    /**
     * 서버로부터 위치 업데이트
     */
    public void updateFromServer(float x, float y, int stateOrdinal, int health) {
        this.targetPosition.set(x, y);
        this.setState(PlayerState.values()[stateOrdinal]);
        this.setHealth(health);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // 보간으로 부드럽게 이동
        position.lerp(targetPosition, interpolationSpeed * delta);
    }
}
```

---

## ✅ 완료 조건

- [ ] PlayerUpdateMsg 구현
- [ ] RemotePlayer 렌더링
- [ ] 보간 이동 구현
- [ ] 동기화 확인

---

## 🔗 다음 Phase

**PHASE_24: 맵 축소 시스템**
