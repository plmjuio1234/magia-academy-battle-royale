# 맵 및 게임 필드 시스템

---

## 맵 구조

### 기본 정보

- **맵 크기**: 1920 × 1920 픽셀
- **타일 크기**: 32 × 32 픽셀
- **그리드**: 60 × 60 타일
- **배경**: 흙 타일 (기본)
- **레이어**: Background, Collision, Objects, Effects

---

## TiledMap 기반 설계

### Tiled Map Editor 사용

**맵 파일 구조**:
```
assets/
└── maps/
    ├── map_01.tmx          # Tiled 맵 파일
    ├── tileset.tsx         # 타일셋
    └── background.png      # 타일 이미지
```

### 레이어 구성

#### 1. Background Layer
- 기본 바닥 타일 (모두 통행 가능)
- 시각적 다양성 추가

#### 2. Collision Layer
- 벽, 바위, 나무 등 장애물
- 통행 불가 영역 표시

#### 3. Objects Layer (포인트)
- 몬스터 스폰 포인트: (x, y) 좌표
- 아이템 스폰 포인트
- 플레이어 초기 스폰 포인트

#### 4. Effects Layer
- 축소 구역 시각화 (반투명 레이어)
- 독가스 표현

---

## 맵 로딩 및 렌더링

```java
public class MapRenderer {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Camera camera;

    public MapRenderer() {
        // Tiled 맵 로드
        tiledMap = new TmxMapLoader().load("maps/map_01.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    public void render(SpriteBatch batch, Camera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();  // Background + Collision 렌더링
    }

    public boolean isColliding(float x, float y, float radius) {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");

        // 타일 좌표로 변환 (32px 타일)
        int tileX = (int)(x / 32);
        int tileY = (int)(y / 32);

        if (tileX < 0 || tileX >= 60 || tileY < 0 || tileY >= 60) {
            return true;  // 맵 경계 밖
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;  // 타일이 있으면 충돌
    }

    public TiledMapTileLayer getObjectLayer() {
        return (TiledMapTileLayer) tiledMap.getLayers().get("Objects");
    }
}
```

---

## 던전 축소 시스템

### 축소 메커니즘

```java
public class ShrinkingZoneManager {
    private float gameTimer = 0;  // 게임 경과 시간 (초)
    private ShrinkPhase currentPhase = ShrinkPhase.PHASE_0;

    // 각 페이즈의 안전 지역 (원의 중심과 반지름)
    private Vector2 zoneCenter = new Vector2(960, 960);  // 맵 중앙
    private float zoneRadius = 960;  // 현재 안전 지역 반지름
    private float zoneDamagePerSecond = 0;

    public void update(float delta) {
        gameTimer += delta;

        // 페이즈 전환
        if (gameTimer < 180) {  // 0-3분
            currentPhase = ShrinkPhase.PHASE_0;
            zoneRadius = 960;
            zoneDamagePerSecond = 0;
        } else if (gameTimer < 360) {  // 3-6분
            currentPhase = ShrinkPhase.PHASE_1;
            zoneRadius = lerp(960, 480, (gameTimer - 180) / 180);
            zoneDamagePerSecond = 5;
        } else if (gameTimer < 480) {  // 6-8분
            currentPhase = ShrinkPhase.PHASE_2;
            zoneRadius = lerp(480, 240, (gameTimer - 360) / 120);
            zoneDamagePerSecond = 10;
        } else {  // 8-10분
            currentPhase = ShrinkPhase.PHASE_3;
            zoneRadius = 240;
            zoneDamagePerSecond = 20;
        }
    }

    public void applyZoneDamage(List<Player> players) {
        for (Player player : players) {
            float distance = Vector2.dst(
                player.x, player.y,
                zoneCenter.x, zoneCenter.y
            );

            if (distance > zoneRadius) {
                // 안전지역 밖에 있음
                int damage = (int)zoneDamagePerSecond;
                player.takeDamage(damage);
            }
        }
    }

    public void renderZone(ShapeRenderer shapeRenderer) {
        // 안전지역 경계 렌더링
        shapeRenderer.setColor(0, 1, 0, 0.3f);  // 반투명 초록색
        shapeRenderer.circle(zoneCenter.x, zoneCenter.y, zoneRadius);

        // 위험지역 (독가스) 렌더링
        shapeRenderer.setColor(0.5f, 0.5f, 0, 0.5f);  // 반투명 노란색
        shapeRenderer.circle(zoneCenter.x, zoneCenter.y, 960);
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * Math.min(1, t);
    }

    enum ShrinkPhase {
        PHASE_0, PHASE_1, PHASE_2, PHASE_3
    }
}
```

---

## 아이템 스폰 시스템

```java
public class ItemSpawner {
    private List<Item> items = new ArrayList<>();
    private float spawnTimer = 0;
    private static final float SPAWN_INTERVAL = 5.0f;  // 5초마다

    public void update(float delta) {
        spawnTimer -= delta;

        if (spawnTimer <= 0) {
            spawnItem();
            spawnTimer = SPAWN_INTERVAL;
        }

        // 수집된 아이템 제거
        items.removeIf(item -> !item.active);
    }

    private void spawnItem() {
        // 랜덤 위치 선택 (안전지역 내)
        Vector2 position = getRandomPositionInZone();
        ItemType type = getRandomItemType();

        Item item = new Item(position.x, position.y, type);
        items.add(item);
    }

    public Item checkItemCollision(Player player) {
        for (Item item : items) {
            float distance = Vector2.dst(
                player.x, player.y,
                item.x, item.y
            );

            if (distance <= 20) {  // 수집 반지름
                item.active = false;
                return item;
            }
        }
        return null;
    }

    private ItemType getRandomItemType() {
        float rand = (float)Math.random();
        if (rand < 0.4) return ItemType.HEALTH_POTION;
        if (rand < 0.7) return ItemType.MANA_POTION;
        if (rand < 0.85) return ItemType.SKILL_BOOK;
        return ItemType.MAGIC_ESSENCE;
    }

    enum ItemType {
        HEALTH_POTION,    // +30 HP
        MANA_POTION,      // +20 MP
        SKILL_BOOK,       // 스킬 쿨다운 50% 감소 (5초)
        MAGIC_ESSENCE     // 공격력 +10% (10초)
    }
}
```

---

## 플레이어 스폰 시스템

```java
public class PlayerSpawner {
    public static Vector2[] getSpawnPoints() {
        return new Vector2[]{
            new Vector2(300, 300),      // 좌상단
            new Vector2(1620, 300),     // 우상단
            new Vector2(300, 1620),     // 좌하단
            new Vector2(1620, 1620)     // 우하단
        };
    }

    public static Vector2 getPlayerSpawnPoint(int playerIndex) {
        Vector2[] points = getSpawnPoints();
        return points[playerIndex % points.length].cpy();
    }
}
```

---

## 충돌 감지

### 맵 경계 충돌
```java
public boolean checkMapBoundary(float x, float y, float radius) {
    // 맵 경계: 0 ~ 1920
    return x - radius < 0 || x + radius > 1920 ||
           y - radius < 0 || y + radius > 1920;
}
```

### 타일 기반 충돌
```java
public boolean checkTileCollision(float x, float y) {
    return mapRenderer.isColliding(x, y, 15);  // 플레이어 반지름 15
}
```

---

## 미니맵 렌더링

```java
public class MiniMap {
    private float minimapSize = 150;  // 150×150 px
    private float scale = minimapSize / 1920;  // 축소 비율

    public void render(ShapeRenderer shapeRenderer, Vector2 cameraPos) {
        // 미니맵 배경
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(10, 10, minimapSize, minimapSize);

        // 플레이어
        shapeRenderer.setColor(Color.BLUE);
        float playerX = 10 + cameraPos.x * scale;
        float playerY = 10 + cameraPos.y * scale;
        shapeRenderer.circle(playerX, playerY, 3);

        // 다른 플레이어
        shapeRenderer.setColor(Color.CYAN);
        for (OtherPlayer other : otherPlayers) {
            float otherX = 10 + other.x * scale;
            float otherY = 10 + other.y * scale;
            shapeRenderer.circle(otherX, otherY, 2);
        }

        // 안전지역 경계
        shapeRenderer.setColor(Color.GREEN);
        float radius = zoneRadius * scale;
        float centerX = 10 + 960 * scale;
        float centerY = 10 + 960 * scale;
        shapeRenderer.circle(centerX, centerY, radius);
    }
}
```

---

## 성능 최적화

### 조건부 렌더링
```java
public void renderOnlyVisibleTiles(TiledMap map, Camera camera) {
    // 카메라 범위 내의 타일만 렌더링
    // libGDX의 OrthogonalTiledMapRenderer가 자동으로 처리

    mapRenderer.setView(camera);
    mapRenderer.render();
}
```

### 데코레이션 최소화
- 복잡한 이펙트 제한
- 파티클 이펙트 개수 제한 (최대 50개)
- 투명도 블렌딩 최소화
