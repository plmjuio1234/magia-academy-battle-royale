package com.example.yugeup.game.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * 게임 맵 클래스
 *
 * 게임의 배경 맵을 관리하고 렌더링합니다.
 * Tiled 맵 에디터(.tmx)로 제작된 타일 맵을 로드합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class GameMap {
    // Tiled 맵
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    // 맵 크기 (픽셀 단위) - TMX에서 자동 계산
    private float mapWidth;
    private float mapHeight;
    private int tileWidth;
    private int tileHeight;
    private int mapWidthInTiles;
    private int mapHeightInTiles;

    // fog 그룹 레이어 (PHASE_24)
    private MapGroupLayer fogGroupLayer;

    /**
     * GameMap 생성자
     */
    public GameMap() {
        // Tiled 맵 로드
        loadTiledMap();

        System.out.println("[GameMap] Tiled 맵 로드 완료 - 크기: " + mapWidth + "x" + mapHeight +
                          ", 타일: " + mapWidthInTiles + "x" + mapHeightInTiles);
    }

    /**
     * Tiled 맵 로드
     */
    private void loadTiledMap() {
        try {
            // TMX 파일 로드
            TmxMapLoader mapLoader = new TmxMapLoader();
            tiledMap = mapLoader.load("maps/magical-school-map.tmx");

            // 맵 속성 읽기
            MapProperties properties = tiledMap.getProperties();
            mapWidthInTiles = properties.get("width", Integer.class);
            mapHeightInTiles = properties.get("height", Integer.class);
            tileWidth = properties.get("tilewidth", Integer.class);
            tileHeight = properties.get("tileheight", Integer.class);

            // 맵 크기 계산 (픽셀)
            mapWidth = mapWidthInTiles * tileWidth;
            mapHeight = mapHeightInTiles * tileHeight;

            // 맵 렌더러 생성 (1f = 1:1 스케일)
            mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1f);

            System.out.println("[GameMap] TMX 로드 성공");
            System.out.println("[GameMap] 타일 크기: " + tileWidth + "x" + tileHeight);
            System.out.println("[GameMap] 맵 타일 개수: " + mapWidthInTiles + "x" + mapHeightInTiles);
            System.out.println("[GameMap] 맵 픽셀 크기: " + mapWidth + "x" + mapHeight);
            System.out.println("[GameMap] 맵 중앙 좌표: (" + (mapWidth/2f) + ", " + (mapHeight/2f) + ")");

            // 레이어 목록 출력
            System.out.print("[GameMap] 로드된 레이어: ");
            for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
                System.out.print(tiledMap.getLayers().get(i).getName() + " ");
            }
            System.out.println();

            // fog 그룹 레이어 찾기 (PHASE_24)
            MapLayer fogLayer = tiledMap.getLayers().get("fog");
            if (fogLayer instanceof MapGroupLayer) {
                fogGroupLayer = (MapGroupLayer) fogLayer;
                System.out.println("[GameMap] fog 그룹 레이어 발견 - 자식 레이어 수: " + fogGroupLayer.getLayers().getCount());

                // 모든 fog 레이어를 비활성화 상태로 초기화
                for (MapLayer childLayer : fogGroupLayer.getLayers()) {
                    childLayer.setVisible(false);
                    System.out.println("[GameMap] fog 레이어 비활성화: " + childLayer.getName());
                }
            } else {
                System.out.println("[GameMap] 경고: fog 그룹 레이어를 찾을 수 없음");
            }

        } catch (Exception e) {
            System.err.println("[GameMap] 오류: TMX 파일 로드 실패 - " + e.getMessage());
            e.printStackTrace();

            // 폴백: 기본 값 설정
            mapWidthInTiles = 30;
            mapHeightInTiles = 30;
            tileWidth = 64;
            tileHeight = 64;
            mapWidth = mapWidthInTiles * tileWidth;
            mapHeight = mapHeightInTiles * tileHeight;
        }
    }

    /**
     * 업데이트 (자기장 시스템 등)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 자기장 업데이트 (PHASE_24에서 구현)
        // if (zoneManager != null) {
        //     zoneManager.update(delta);
        // }
    }

    /**
     * 맵 렌더링
     *
     * @param batch SpriteBatch (사용 안 함, mapRenderer 자체 batch 사용)
     * @param camera 카메라
     */
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        if (mapRenderer != null) {
            // 카메라 설정
            mapRenderer.setView(camera);

            // 맵 렌더링 (모든 레이어)
            mapRenderer.render();
        }
    }

    /**
     * 좌표가 맵 내부인지 확인
     *
     * @param x X 좌표
     * @param y Y 좌표
     * @return 맵 내부이면 true
     */
    public boolean isInsideMap(float x, float y) {
        return x >= 0 && x <= mapWidth && y >= 0 && y <= mapHeight;
    }

    /**
     * 좌표가 맵 경계인지 확인
     *
     * @param x X 좌표
     * @param y Y 좌표
     * @return 경계이면 true
     */
    public boolean isBorder(float x, float y) {
        int tileX = (int)(x / tileWidth);
        int tileY = (int)(y / tileHeight);

        return tileX <= 0 || tileY <= 0 ||
               tileX >= mapWidthInTiles - 1 ||
               tileY >= mapHeightInTiles - 1;
    }

    /**
     * 특정 좌표에 벽이 있는지 확인
     * 충돌 판정 대상 레이어:
     * - wall 그룹의 wall 레이어만
     * - item 그룹의 furniture1, furniture2, furniture3 레이어만
     *
     * @param x 월드 X 좌표 (픽셀)
     * @param y 월드 Y 좌표 (픽셀)
     * @return 벽이 있으면 true
     */
    public boolean isWall(float x, float y) {
        if (tiledMap == null) {
            System.out.println("[GameMap] isWall: tiledMap is null!");
            return false;
        }

        // 맵 범위 밖은 벽으로 처리
        if (x < 0 || y < 0 || x > mapWidth || y > mapHeight) {
            return true;
        }

        // 픽셀 좌표를 타일 좌표로 변환
        int tileX = (int)(x / tileWidth);
        int tileY = (int)(y / tileHeight);

        // 충돌 판정할 레이어 정의 (그룹명:레이어명 형식)
        String[][] collisionLayers = {
            {"wall", "wall"},              // wall 그룹의 wall 레이어만
            {"item", "furniture1"},         // item 그룹의 furniture1
            {"item", "furniture2"},         // item 그룹의 furniture2
            {"item", "furniture3"}          // item 그룹의 furniture3
        };
        // door-frame, open-door-wall, door는 통과 가능 (충돌 판정 제외)

        // 각 충돌 레이어 확인
        for (String[] layerInfo : collisionLayers) {
            String groupName = layerInfo[0];
            String targetLayerName = layerInfo[1];

            MapLayer groupLayer = tiledMap.getLayers().get(groupName);
            if (groupLayer == null) {
                continue;
            }

            // 그룹인 경우: 특정 레이어만 확인
            if (groupLayer instanceof MapGroupLayer) {
                MapGroupLayer group = (MapGroupLayer) groupLayer;
                MapLayer innerLayer = group.getLayers().get(targetLayerName);

                if (innerLayer != null && innerLayer instanceof TiledMapTileLayer) {
                    TiledMapTileLayer tileLayer = (TiledMapTileLayer) innerLayer;
                    TiledMapTileLayer.Cell cell = tileLayer.getCell(tileX, tileY);

                    if (cell != null && cell.getTile() != null) {
                        // 디버그 로그 (필요시 주석 해제)
                        // System.out.println("[GameMap] 벽 감지! 그룹:" + groupName +
                        //                  ", 레이어:" + targetLayerName +
                        //                  ", 좌표:(" + x + "," + y + "), 타일:(" + tileX + "," + tileY + ")");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 특정 영역에 벽이 있는지 확인 (원형 충돌체용)
     *
     * @param centerX 중심 X 좌표
     * @param centerY 중심 Y 좌표
     * @param radius 반경
     * @return 벽과 충돌하면 true
     */
    public boolean isWallInArea(float centerX, float centerY, float radius) {
        // 충돌체의 4방향 끝점 확인
        return isWall(centerX - radius, centerY) ||  // 왼쪽
               isWall(centerX + radius, centerY) ||  // 오른쪽
               isWall(centerX, centerY - radius) ||  // 아래
               isWall(centerX, centerY + radius);    // 위
    }

    // ===== Fog 시스템 (PHASE_24) =====

    /**
     * 특정 fog 레이어를 활성화합니다.
     *
     * @param zoneName 구역 이름 (TMX 레이어명)
     */
    public void activateFogLayer(String zoneName) {
        if (fogGroupLayer == null) {
            System.out.println("[GameMap] fog 그룹 레이어가 없습니다.");
            return;
        }

        MapLayer layer = fogGroupLayer.getLayers().get(zoneName);
        if (layer != null) {
            layer.setVisible(true);
            System.out.println("[GameMap] ★ fog 레이어 활성화: " + zoneName);
        } else {
            System.out.println("[GameMap] fog 레이어를 찾을 수 없음: " + zoneName);
        }
    }

    /**
     * 특정 fog 레이어를 비활성화합니다.
     *
     * @param zoneName 구역 이름 (TMX 레이어명)
     */
    public void deactivateFogLayer(String zoneName) {
        if (fogGroupLayer == null) {
            return;
        }

        MapLayer layer = fogGroupLayer.getLayers().get(zoneName);
        if (layer != null) {
            layer.setVisible(false);
            System.out.println("[GameMap] fog 레이어 비활성화: " + zoneName);
        }
    }

    /**
     * 특정 좌표가 활성화된 fog 구역 내에 있는지 확인합니다.
     *
     * @param x X 좌표 (픽셀)
     * @param y Y 좌표 (픽셀)
     * @return fog 구역 내에 있으면 해당 구역 이름, 아니면 null
     */
    public String isInActiveFog(float x, float y) {
        if (fogGroupLayer == null || tiledMap == null) {
            return null;
        }

        // 픽셀 좌표를 타일 좌표로 변환
        int tileX = (int)(x / tileWidth);
        int tileY = (int)(y / tileHeight);

        // 각 fog 레이어 확인
        for (MapLayer layer : fogGroupLayer.getLayers()) {
            // 비활성화된 레이어는 건너뜀
            if (!layer.isVisible()) {
                continue;
            }

            // TiledMapTileLayer인 경우 타일 확인
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(tileX, tileY);

                if (cell != null && cell.getTile() != null) {
                    return layer.getName(); // fog 구역 이름 반환
                }
            }
        }

        return null; // fog 구역에 없음
    }

    /**
     * fog 그룹 레이어가 있는지 확인합니다.
     *
     * @return fog 레이어 존재 여부
     */
    public boolean hasFogLayer() {
        return fogGroupLayer != null;
    }

    // ===== Getter =====

    public float getWidth() {
        return mapWidth;
    }

    public float getHeight() {
        return mapHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
    }
}
