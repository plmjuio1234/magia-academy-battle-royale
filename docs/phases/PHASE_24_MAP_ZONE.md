# PHASE_24_MAP_ZONE.md - 맵 축소 시스템

---

## 🎯 목표
시간에 따라 맵이 축소되는 자기장 시스템 구현

---

## 📋 구현 범위

- ✅ Zone 시스템 (안전 구역)
- ✅ ZoneManager (자기장 진행)
- ✅ 시간별 구역 축소
- ✅ 구역 밖 데미지

---

## 📁 필요 파일

```
game/map/
  ├─ Zone.java
  ├─ ZoneManager.java
  └─ GameMap.java (수정)
```

---

## 🔧 구현 가이드

### 1. Zone 클래스

```java
public class Zone {
    private Rectangle bounds;
    private ZoneStatus status;
    private float closureTimer;
    private int damagePerSecond;

    public enum ZoneStatus {
        OPEN, CLOSING, CLOSED
    }

    public Zone(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.status = ZoneStatus.OPEN;
        this.damagePerSecond = 10;
    }

    public boolean contains(Vector2 position) {
        return bounds.contains(position);
    }

    public void startClosing(float duration) {
        this.status = ZoneStatus.CLOSING;
        this.closureTimer = duration;
    }

    public void update(float delta) {
        if (status == ZoneStatus.CLOSING) {
            closureTimer -= delta;
            if (closureTimer <= 0) {
                status = ZoneStatus.CLOSED;
            }
        }
    }
}
```

### 2. ZoneManager 클래스

```java
public class ZoneManager {
    private List<Zone> zones;
    private Zone currentZone;
    private float gameTime;
    private static final float GAME_DURATION = 600f;  // 10분

    // 구역 축소 일정
    private static final float[] ZONE_TIMES = {120f, 240f, 360f, 480f};

    public ZoneManager() {
        this.zones = new ArrayList<>();
        initializeZones();
    }

    private void initializeZones() {
        // 4개 구역: 1920 → 1400 → 900 → 500
        zones.add(new Zone(0, 0, 1920, 1920));
        zones.add(new Zone(260, 260, 1400, 1400));
        zones.add(new Zone(510, 510, 900, 900));
        zones.add(new Zone(710, 710, 500, 500));

        currentZone = zones.get(0);
    }

    public void update(float delta) {
        gameTime += delta;

        // 시간별 구역 전환
        for (int i = 0; i < ZONE_TIMES.length; i++) {
            if (gameTime >= ZONE_TIMES[i] && currentZone == zones.get(i)) {
                startZoneClosing(i + 1);
            }
        }

        // 현재 구역 업데이트
        currentZone.update(delta);

        // 플레이어 범위 밖 데미지
        applyZoneDamage();
    }

    private void startZoneClosing(int nextZoneIndex) {
        if (nextZoneIndex < zones.size()) {
            currentZone = zones.get(nextZoneIndex);
            currentZone.startClosing(30f);  // 30초 동안 축소
        }
    }

    private void applyZoneDamage() {
        List<Player> players = GameManager.getInstance().getAllPlayers();

        for (Player player : players) {
            if (!currentZone.contains(player.getPosition())) {
                // 구역 밖 - 데미지
                player.takeDamage(currentZone.getDamagePerSecond() / 20);  // 20Hz
            }
        }
    }

    public void render(SpriteBatch batch) {
        // 구역 경계 렌더링
        for (Zone zone : zones) {
            if (zone == currentZone) {
                // 빨간 테두리
            }
        }
    }
}
```

---

## ✅ 완료 조건

- [ ] Zone 클래스 구현
- [ ] ZoneManager 구현
- [ ] 시간별 축소 확인
- [ ] 구역 밖 데미지 확인

---

## 🔗 다음 Phase

**PHASE_25: PVP 전투**
