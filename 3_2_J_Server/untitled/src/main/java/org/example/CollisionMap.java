// 서버 - org.example.CollisionMap.java
package org.example;

import java.util.HashMap;
import java.util.Map;

public class CollisionMap {
    private boolean[][] walkable;
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;

    // ===== Fog 구역 데이터 (PHASE_24) =====
    // 각 fog 구역별 타일 데이터 (zoneName -> boolean[height][width])
    private Map<String, boolean[][]> fogZones = new HashMap<>();

    // ⭐ TMX 파서에서 사용할 생성자
    public CollisionMap(boolean[][] walkable, int width, int height,
                        int tileWidth, int tileHeight) {
        this.walkable = walkable;
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        System.out.println("[CollisionMap] 생성 완료");
        System.out.println("  맵 크기: " + width + "x" + height);
        System.out.println("  타일 크기: " + tileWidth + "x" + tileHeight);
    }

    /**
     * 클라이언트 GameMap.isWall()과 동일한 로직
     * TMX 좌표계(Y=0이 상단)와 LibGDX 좌표계(Y=0이 하단)의 차이를 보정합니다.
     */
    public boolean isWall(float x, float y) {
        float mapWidth = width * tileWidth;
        float mapHeight = height * tileHeight;

        if (x < 0 || y < 0 || x > mapWidth || y > mapHeight) {
            return true;
        }

        int tileX = (int)(x / tileWidth);
        int libgdxTileY = (int)(y / tileHeight);

        // TMX 좌표계로 변환 (Y축 반전)
        int tileY = height - 1 - libgdxTileY;

        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return true;
        }

        return !walkable[tileY][tileX];
    }

    /**
     * 클라이언트 GameMap.isWallInArea()와 동일한 로직
     * TMX 좌표계(Y=0이 상단)와 LibGDX 좌표계(Y=0이 하단)의 차이를 보정합니다.
     */
    public boolean isWallInArea(float centerX, float centerY, float radius) {
        // 체크할 타일 범위 계산 (LibGDX 좌표계)
        int minTileX = (int)((centerX - radius) / tileWidth);
        int maxTileX = (int)((centerX + radius) / tileWidth);
        int minLibgdxTileY = (int)((centerY - radius) / tileHeight);
        int maxLibgdxTileY = (int)((centerY + radius) / tileHeight);

        // 범위 내 모든 타일 체크
        for (int libgdxTy = minLibgdxTileY; libgdxTy <= maxLibgdxTileY; libgdxTy++) {
            // TMX 좌표계로 변환 (Y축 반전)
            int ty = height - 1 - libgdxTy;

            for (int tx = minTileX; tx <= maxTileX; tx++) {
                if (tx < 0 || tx >= width || ty < 0 || ty >= height) {
                    continue;
                }

                // 타일 중심 좌표 (LibGDX 좌표계)
                float tileCenterX = (tx + 0.5f) * tileWidth;
                float tileCenterY = (libgdxTy + 0.5f) * tileHeight;

                // 원과 타일의 거리 체크
                float dx = centerX - tileCenterX;
                float dy = centerY - tileCenterY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);

                // 몬스터 반지름 내에 벽 타일이 있으면 true
                if (distance < radius && !walkable[ty][tx]) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }

    // 픽셀 단위 맵 크기 반환
    public float getMapWidth() { return width * tileWidth; }
    public float getMapHeight() { return height * tileHeight; }

    // ===== Fog 구역 메서드 (PHASE_24) =====

    /**
     * fog 구역 데이터를 추가합니다.
     *
     * @param zoneName 구역 이름 (TMX 레이어명)
     * @param zoneData 해당 구역의 타일 데이터 (true = fog 영역)
     */
    public void addFogZone(String zoneName, boolean[][] zoneData) {
        fogZones.put(zoneName, zoneData);
        System.out.println("[CollisionMap] fog 구역 추가: " + zoneName);
    }

    /**
     * 특정 좌표가 어떤 fog 구역에 있는지 확인합니다.
     *
     * TMX 좌표계(Y=0이 상단)와 LibGDX 좌표계(Y=0이 하단)의 차이를 보정합니다.
     *
     * @param x 월드 X 좌표 (픽셀, LibGDX 좌표계)
     * @param y 월드 Y 좌표 (픽셀, LibGDX 좌표계)
     * @return 해당 fog 구역 이름, 없으면 null
     */
    public String getFogZoneAt(float x, float y) {
        // 픽셀 좌표를 타일 좌표로 변환
        int tileX = (int)(x / tileWidth);
        int libgdxTileY = (int)(y / tileHeight);

        // TMX 좌표계로 변환 (Y축 반전)
        int tileY = height - 1 - libgdxTileY;

        // 범위 체크
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return null;
        }

        // 각 fog 구역 확인
        for (Map.Entry<String, boolean[][]> entry : fogZones.entrySet()) {
            String zoneName = entry.getKey();
            boolean[][] zoneData = entry.getValue();

            if (zoneData[tileY][tileX]) {
                return zoneName; // 이 구역에 있음
            }
        }

        return null; // fog 구역에 없음
    }

    /**
     * fog 구역 데이터가 있는지 확인합니다.
     *
     * @return fog 구역이 있으면 true
     */
    public boolean hasFogZones() {
        return !fogZones.isEmpty();
    }

    /**
     * fog 구역 개수를 반환합니다.
     */
    public int getFogZoneCount() {
        return fogZones.size();
    }

    /**
     * 특정 좌표가 어떤 fog 구역에든 있는지 확인합니다.
     * 몬스터 스폰 시 건물 내부에서만 스폰하도록 제한하는데 사용됩니다.
     *
     * @param x 월드 X 좌표 (픽셀, LibGDX 좌표계)
     * @param y 월드 Y 좌표 (픽셀, LibGDX 좌표계)
     * @return fog 구역 내에 있으면 true
     */
    public boolean isInsideAnyFogZone(float x, float y) {
        return getFogZoneAt(x, y) != null;
    }
}
