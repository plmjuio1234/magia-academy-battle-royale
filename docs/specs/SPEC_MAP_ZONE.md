# SPEC_MAP_ZONE.md - 맵 및 자기장 시스템

> 자세한 타이밍은 총정리.md의 "🗺️ 게임 필드 & 자기장 시스템" 참조

---

## 🗺️ 게임 맵

### 맵 규모
```
크기: 1920 × 1920 픽셀 (정사각형)
중심: (960, 960)
플레이어 시야: 화면 크기(1080 × 1920)만큼만 렌더링
```

### 맵 경계
```
플레이어 경계 제한 (radius = 20):
  x: 20 ≤ x ≤ 1900
  y: 20 ≤ y ≤ 1900

몬스터 스폰 규칙:
  중심(960, 960)에서 300픽셀 이상 거리

맵 시각화:
  ┌─────────────────────┐
  │                     │
  │  (960, 960)         │ 1920
  │       +             │
  │                     │
  └─────────────────────┘
        1920
```

---

## 🔥 자기장 시스템

### 구역 분할
```
초기: 4개 구역 (초기 프로토타입)
최대: 5개 구역 (확장 가능)

분할 방식:
  ┌─────┬─────┐
  │  1  │  2  │
  ├─────┼─────┤
  │  3  │  4  │
  └─────┴─────┘

각 구역: 960 × 960 (1/4 맵)
```

### 자기장 진행 타이밍

| 시간 | 진행 상황 | 상태 |
|------|---------|------|
| 0:00~2:00 | 모든 구역 개방 | 안전 |
| 2:00~4:00 | 1구역 폐쇄 | 1개 구역 위험 |
| 4:00~6:00 | 2구역 폐쇄 | 2개 구역 위험 |
| 6:00~8:00 | 3구역 폐쇄 | 3개 구역 위험 |
| 8:00~10:00 | 1개 구역만 남음 | 최종 결판 |

### 폐쇄 구역 페널티
```
폐쇄된 구역 내부에 있으면:
  ├─ 초당 10 HP 지속 손상
  ├─ 손상 누적: 손상 × delta × 플레이어수
  └─ 구역을 벗어나면 손상 정지

예시:
  플레이어 HP: 100
  구역 폐쇄 상태 5초 동안 머물러 있음
  입은 손상: 10 × 5 = 50 HP
  결과: 남은 HP = 100 - 50 = 50
```

---

## 🎯 Zone 클래스

```
public class Zone {
    int zoneId;
    Rectangle bounds;  // x, y, width, height
    ZoneStatus status;  // OPEN, CLOSING, CLOSED
    float closureTimer;  // 폐쇄까지의 남은 시간
    int damagePerSecond = 10;  // 폐쇄 후 초당 손상

    // 플레이어가 이 구역 내에 있나?
    public boolean contains(Player player) {
        return bounds.contains(player.x, player.y);
    }

    // 플레이어가 입을 손상 계산
    public int getDamage(float delta) {
        if (status == ZoneStatus.CLOSED) {
            return (int)(damagePerSecond * delta);
        }
        return 0;
    }
}
```

---

## 🔄 ZoneManager

### 책임

```
public class ZoneManager {
    List<Zone> zones;
    float gameTimer = 0f;  // 게임 시작부터의 경과 시간
    float gameEndTime = 600f;  // 10분

    void update(float delta) {
        gameTimer += delta;

        // 시간에 따라 구역 폐쇄
        if (gameTimer >= 120f && !zones[0].isClosed())
            closeZone(0);  // 2분
        if (gameTimer >= 240f && !zones[1].isClosed())
            closeZone(1);  // 4분
        if (gameTimer >= 360f && !zones[2].isClosed())
            closeZone(2);  // 6분
        if (gameTimer >= 480f && !zones[3].isClosed())
            closeZone(3);  // 8분

        // 플레이어들에게 자기장 손상 적용
        for (Player player : players) {
            for (Zone zone : zones) {
                if (zone.contains(player) && zone.isClosed()) {
                    player.takeDamage(zone.getDamage(delta));
                }
            }
        }
    }
}
```

### 메서드

```
// 특정 위치의 구역 찾기
Zone getZoneAtPosition(float x, float y) {
    for (Zone zone : zones) {
        if (zone.contains(x, y)) {
            return zone;
        }
    }
    return null;
}

// 구역 폐쇄
void closeZone(int zoneId) {
    zones[zoneId].status = ZoneStatus.CLOSED;
    broadcastZoneClosureMessage(zoneId);
}

// 게임 종료 확인
boolean isGameEnded() {
    return gameTimer >= gameEndTime;
}

// 남은 시간
float getRemainingTime() {
    return gameEndTime - gameTimer;
}
```

---

## 📺 클라이언트 UI

### 자기장 타이머 표시
```
화면 중앙 상단 또는 하단:
  "다음 폐쇄: 2:15" (MM:SS 형식)

또는

  구역 폐쇄까지: ████░░░░ (프로그래스 바)
  "2분 15초"
```

### 폐쇄 된 구역 시각화
```
폐쇄 구역:
  ├─ 반투명 검정색 오버레이
  ├─ 또는 빨간 경계선
  └─ "폐쇄된 구역" 텍스트 표시
```

### 구역 정보 HUD
```
게임 화면 우측 상단에 표시:
  현재 구역: Zone 2
  안전 상태: ✓ (안전) / ✗ (위험)
```

---

## 🎯 맵 렌더링

### 카메라 시스템

```
카메라_중심 = 플레이어_위치
화면에 표시: 카메라_중심 ± (화면크기 / 2)

예시:
  플레이어 위치: (960, 960)
  화면 크기: 1080 × 1920
  렌더링 범위:
    x: 960 - 540 ~ 960 + 540 = [420, 1500]
    y: 960 - 960 ~ 960 + 960 = [0, 1920]
```

### 경계 처리
```
플레이어가 맵 끝에 가면:
  카메라가 맵을 벗어나지 않도록 조정

예시:
  플레이어가 x=100 (맵 왼쪽 끝)
  카메라_중심 = max(540, 100) = 540
```

---

## 🔬 ZoneStatus Enum

```
public enum ZoneStatus {
    OPEN,      // 안전 (플레이 가능)
    CLOSING,   // 폐쇄 중 (카운트다운, 선택사항)
    CLOSED     // 폐쇄됨 (진입 불가, 손상)
}
```

---

## 💾 구역 데이터 예시

```
구역 1 (좌상단):
  bounds: (0, 0, 960, 960)
  status: OPEN
  damagePerSecond: 10

구역 2 (우상단):
  bounds: (960, 0, 960, 960)
  status: OPEN
  damagePerSecond: 10

구역 3 (좌하단):
  bounds: (0, 960, 960, 960)
  status: OPEN
  damagePerSecond: 10

구역 4 (우하단):
  bounds: (960, 960, 960, 960)
  status: OPEN
  damagePerSecond: 10
```

---

**자기장 타이밍**: 총정리.md의 "🗺️ 게임 필드 & 자기장 시스템" 참조

