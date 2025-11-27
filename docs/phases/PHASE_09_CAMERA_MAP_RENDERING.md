# PHASE_09_CAMERA_MAP_RENDERING.md - 카메라 & 맵 렌더링

---

## 🎯 목표
게임맵 객체 생성 및 렌더링 시스템 구현
(배경 타일맵, 맵 경계, 렌더링 최적화)

---

## 📋 구현 범위

### 게임맵 시스템
- ✅ GameMap 클래스 구현
- ✅ 타일 기반 맵 렌더링
- ✅ 맵 크기: 1920x1920 픽셀

### 렌더링 최적화
- ✅ 화면 밖 타일 컬링 (Culling)
- ✅ 배치 렌더링 (SpriteBatch)
- ✅ 맵 경계 표시

### 카메라 개선
- ✅ 맵 경계 내로 카메라 제한
- ✅ 줌 인/아웃 (선택사항)
- ✅ 화면 비율 대응

---

## 📁 필요 파일

### 생성할 파일
```
game/map/
  ├─ GameMap.java                (새로 생성)
  ├─ Tile.java                   (새로 생성)
  └─ MapRenderer.java            (새로 생성)

camera/
  └─ CameraController.java       (수정 - 경계 확인 개선)
```

### 기존 파일 수정
```
GameScreen.java                  (수정 - 맵 렌더링 추가)
Constants.java                    (수정 - 맵 상수 추가)
```

---

## 🔧 구현 가이드

### 1. GameMap 클래스

```java
/**
 * 게임 맵 클래스
 *
 * 게임의 배경 맵을 관리하고 렌더링합니다.
 * 타일 기반 맵 시스템으로 구현됩니다.
 */
public class GameMap {
    // 맵 크기 (픽셀 단위)
    public static final float MAP_WIDTH = 1920f;
    public static final float MAP_HEIGHT = 1920f;

    // 타일 크기
    public static final int TILE_SIZE = 64;  // 64x64 픽셀
    public static final int MAP_TILES_X = (int)(MAP_WIDTH / TILE_SIZE);   // 30
    public static final int MAP_TILES_Y = (int)(MAP_HEIGHT / TILE_SIZE);  // 30

    // 타일 배열
    private Tile[][] tiles;

    // 렌더링
    private MapRenderer mapRenderer;
    private Texture tileTexture;
    private Texture borderTexture;

    // 자기장 (나중에 구현)
    private ZoneManager zoneManager;

    public GameMap() {
        this.tiles = new Tile[MAP_TILES_X][MAP_TILES_Y];
        this.mapRenderer = new MapRenderer();

        // 에셋 로드
        loadAssets();

        // 맵 초기화
        initializeTiles();
    }

    /**
     * 에셋 로드
     */
    private void loadAssets() {
        // 기본 타일 텍스처 (임시)
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.25f, 0.3f, 1f);  // 어두운 파란색
        pixmap.fill();
        tileTexture = new Texture(pixmap);
        pixmap.dispose();

        // 경계 텍스처
        Pixmap borderPixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        borderPixmap.setColor(0.8f, 0.2f, 0.2f, 1f);  // 빨간색
        borderPixmap.fill();
        borderTexture = new Texture(borderPixmap);
        borderPixmap.dispose();
    }

    /**
     * 타일 초기화
     */
    private void initializeTiles() {
        for (int x = 0; x < MAP_TILES_X; x++) {
            for (int y = 0; y < MAP_TILES_Y; y++) {
                // 경계 타일 판별
                boolean isBorder = (x == 0 || y == 0 ||
                                   x == MAP_TILES_X - 1 ||
                                   y == MAP_TILES_Y - 1);

                Texture texture = isBorder ? borderTexture : tileTexture;
                tiles[x][y] = new Tile(x, y, texture);
            }
        }
    }

    /**
     * 업데이트 (자기장 시스템 등)
     */
    public void update(float delta) {
        // 자기장 업데이트 (PHASE_24에서 구현)
        if (zoneManager != null) {
            zoneManager.update(delta);
        }
    }

    /**
     * 맵 렌더링
     */
    public void render(SpriteBatch batch, OrthogonalCamera camera) {
        // 화면에 보이는 타일만 렌더링 (컬링)
        int startX = Math.max(0, (int)(camera.position.x - camera.viewportWidth / 2) / TILE_SIZE - 1);
        int endX = Math.min(MAP_TILES_X, (int)(camera.position.x + camera.viewportWidth / 2) / TILE_SIZE + 1);
        int startY = Math.max(0, (int)(camera.position.y - camera.viewportHeight / 2) / TILE_SIZE - 1);
        int endY = Math.min(MAP_TILES_Y, (int)(camera.position.y + camera.viewportHeight / 2) / TILE_SIZE + 1);

        // 타일 렌더링
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                tiles[x][y].render(batch);
            }
        }

        // 맵 경계선 렌더링
        renderBorder(batch);
    }

    /**
     * 맵 경계선 렌더링
     */
    private void renderBorder(SpriteBatch batch) {
        // 경계선은 이미 경계 타일로 표시됨
        // 추가적인 경계 표시가 필요하면 여기에 구현
    }

    /**
     * 좌표가 맵 내부인지 확인
     */
    public boolean isInsideMap(float x, float y) {
        return x >= 0 && x <= MAP_WIDTH && y >= 0 && y <= MAP_HEIGHT;
    }

    /**
     * 좌표가 맵 경계인지 확인
     */
    public boolean isBorder(float x, float y) {
        int tileX = (int)(x / TILE_SIZE);
        int tileY = (int)(y / TILE_SIZE);

        return tileX <= 0 || tileY <= 0 ||
               tileX >= MAP_TILES_X - 1 ||
               tileY >= MAP_TILES_Y - 1;
    }

    public float getWidth() {
        return MAP_WIDTH;
    }

    public float getHeight() {
        return MAP_HEIGHT;
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= MAP_TILES_X || y < 0 || y >= MAP_TILES_Y) {
            return null;
        }
        return tiles[x][y];
    }

    public void dispose() {
        tileTexture.dispose();
        borderTexture.dispose();
    }
}
```

### 2. Tile 클래스

```java
/**
 * 타일 클래스
 *
 * 맵의 개별 타일을 나타냅니다.
 */
public class Tile {
    // 타일 좌표 (그리드)
    private int gridX;
    private int gridY;

    // 월드 좌표 (픽셀)
    private float worldX;
    private float worldY;

    // 텍스처
    private Texture texture;

    // 타일 타입 (향후 확장)
    private TileType type;

    public enum TileType {
        NORMAL,   // 일반 타일
        BORDER,   // 경계 타일
        BLOCKED   // 막힌 타일 (장애물)
    }

    public Tile(int gridX, int gridY, Texture texture) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.texture = texture;

        // 월드 좌표 계산
        this.worldX = gridX * GameMap.TILE_SIZE;
        this.worldY = gridY * GameMap.TILE_SIZE;

        // 타입 설정
        boolean isBorder = (gridX == 0 || gridY == 0 ||
                           gridX == GameMap.MAP_TILES_X - 1 ||
                           gridY == GameMap.MAP_TILES_Y - 1);
        this.type = isBorder ? TileType.BORDER : TileType.NORMAL;
    }

    /**
     * 타일 렌더링
     */
    public void render(SpriteBatch batch) {
        batch.draw(texture, worldX, worldY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }
}
```

### 3. MapRenderer 클래스

```java
/**
 * 맵 렌더러
 *
 * 맵 렌더링을 최적화합니다.
 */
public class MapRenderer {
    // 렌더링 통계
    private int tilesRendered = 0;
    private int tilesCulled = 0;

    /**
     * 디버그 정보 표시
     */
    public void renderDebugInfo(SpriteBatch batch, BitmapFont font) {
        font.draw(batch, "Tiles Rendered: " + tilesRendered, 10, 100);
        font.draw(batch, "Tiles Culled: " + tilesCulled, 10, 80);
    }

    public void resetStats() {
        tilesRendered = 0;
        tilesCulled = 0;
    }

    public void incrementRendered() {
        tilesRendered++;
    }

    public void incrementCulled() {
        tilesCulled++;
    }
}
```

### 4. CameraController 수정 (경계 개선)

```java
/**
 * 카메라가 맵 경계를 벗어나지 않도록 제한
 */
private void clampCameraToMapBounds() {
    float mapWidth = GameMap.MAP_WIDTH;
    float mapHeight = GameMap.MAP_HEIGHT;
    float halfScreenWidth = camera.viewportWidth / 2;
    float halfScreenHeight = camera.viewportHeight / 2;

    // 최소 경계
    camera.position.x = Math.max(halfScreenWidth, camera.position.x);
    camera.position.y = Math.max(halfScreenHeight, camera.position.y);

    // 최대 경계
    camera.position.x = Math.min(mapWidth - halfScreenWidth, camera.position.x);
    camera.position.y = Math.min(mapHeight - halfScreenHeight, camera.position.y);
}
```

### 5. GameScreen 수정 (맵 렌더링 추가)

```java
/**
 * 게임 월드 렌더링 (카메라 적용)
 */
private void renderGameWorld(SpriteBatch batch) {
    // 맵 렌더링
    if (gameManager.getGameMap() != null) {
        gameManager.getGameMap().render(batch, camera);
    }

    // 몬스터 렌더링
    for (Monster monster : gameManager.getMonsters()) {
        monster.render(batch);
    }

    // 원격 플레이어 렌더링
    for (Player player : remotePlayers.values()) {
        player.render(batch);
    }

    // 로컬 플레이어 렌더링 (중앙)
    if (localPlayer != null) {
        localPlayer.render(batch);
    }

    // 스킬 이펙트 렌더링
    gameManager.getSkillEffects().forEach(effect -> effect.render(batch));
}
```

---

## 🧪 테스트 계획

```java
/**
 * GameMap 테스트
 */
public class TestGameMap {
    private GameMap gameMap;

    @BeforeEach
    public void setUp() {
        gameMap = new GameMap();
    }

    @Test
    public void 맵이_초기화된다() {
        assertNotNull(gameMap);
        assertEquals(1920f, gameMap.getWidth());
        assertEquals(1920f, gameMap.getHeight());
    }

    @Test
    public void 타일_개수가_정확하다() {
        assertEquals(30, GameMap.MAP_TILES_X);
        assertEquals(30, GameMap.MAP_TILES_Y);
    }

    @Test
    public void 모든_타일이_초기화된다() {
        for (int x = 0; x < GameMap.MAP_TILES_X; x++) {
            for (int y = 0; y < GameMap.MAP_TILES_Y; y++) {
                assertNotNull(gameMap.getTile(x, y));
            }
        }
    }

    @Test
    public void 경계_타일이_정확히_설정된다() {
        // 좌상단 경계
        Tile borderTile = gameMap.getTile(0, 0);
        assertEquals(Tile.TileType.BORDER, borderTile.getType());

        // 중앙 일반 타일
        Tile normalTile = gameMap.getTile(15, 15);
        assertEquals(Tile.TileType.NORMAL, normalTile.getType());

        // 우하단 경계
        Tile bottomRightBorder = gameMap.getTile(29, 29);
        assertEquals(Tile.TileType.BORDER, bottomRightBorder.getType());
    }

    @Test
    public void 맵_내부_좌표_판별() {
        assertTrue(gameMap.isInsideMap(100, 100));
        assertTrue(gameMap.isInsideMap(1000, 1000));
        assertFalse(gameMap.isInsideMap(-10, 100));
        assertFalse(gameMap.isInsideMap(2000, 100));
    }

    @Test
    public void 맵_경계_판별() {
        assertTrue(gameMap.isBorder(0, 100));
        assertTrue(gameMap.isBorder(1920, 100));
        assertFalse(gameMap.isBorder(960, 960));
    }
}

/**
 * Tile 테스트
 */
public class TestTile {
    private Tile tile;

    @BeforeEach
    public void setUp() {
        Texture texture = new Texture(new Pixmap(64, 64, Pixmap.Format.RGBA8888));
        tile = new Tile(5, 10, texture);
    }

    @Test
    public void 타일_그리드_좌표가_정확하다() {
        assertEquals(5, tile.getGridX());
        assertEquals(10, tile.getGridY());
    }

    @Test
    public void 타일_월드_좌표가_정확하다() {
        assertEquals(5 * 64, tile.getWorldX(), 0.01f);
        assertEquals(10 * 64, tile.getWorldY(), 0.01f);
    }

    @Test
    public void 경계_타일_판별() {
        Texture texture = new Texture(new Pixmap(64, 64, Pixmap.Format.RGBA8888));

        Tile borderTile = new Tile(0, 0, texture);
        assertEquals(Tile.TileType.BORDER, borderTile.getType());

        Tile normalTile = new Tile(15, 15, texture);
        assertEquals(Tile.TileType.NORMAL, normalTile.getType());
    }
}

/**
 * 맵 렌더링 최적화 테스트
 */
public class TestMapRendering {
    private GameMap gameMap;
    private OrthogonalCamera camera;

    @BeforeEach
    public void setUp() {
        gameMap = new GameMap();
        camera = new OrthogonalCamera();
        camera.setToOrtho(false, 1080, 1920);
        camera.position.set(960, 960, 0);  // 맵 중앙
    }

    @Test
    public void 화면_밖_타일은_렌더링_안_함() {
        // 컬링 테스트
        // (실제 렌더링 카운트 확인 필요)
    }

    @Test
    public void 카메라가_맵_경계를_벗어나지_않음() {
        CameraController controller = new CameraController(camera);

        // 맵 왼쪽 끝으로 이동 시도
        controller.update(new Vector2(0, 960));
        assertTrue(camera.position.x >= camera.viewportWidth / 2);

        // 맵 오른쪽 끝으로 이동 시도
        controller.update(new Vector2(1920, 960));
        assertTrue(camera.position.x <= 1920 - camera.viewportWidth / 2);
    }
}
```

---

## ✅ 완료 조건

- [ ] GameMap 클래스 구현
- [ ] Tile 클래스 구현
- [ ] MapRenderer 클래스 구현
- [ ] 타일 기반 맵 렌더링 확인
- [ ] 맵 경계 표시 확인
- [ ] 화면 밖 타일 컬링 작동 확인
- [ ] 카메라 맵 경계 제한 확인
- [ ] 모든 단위 테스트 통과

---

## 🔗 다음 Phase 연결점

**PHASE_10: 캐릭터 능력치 시스템**
- PlayerStats 클래스 구현
- HP, MP, ATK, DEF, SPEED 등 능력치
- 능력치 기반 전투 로직
