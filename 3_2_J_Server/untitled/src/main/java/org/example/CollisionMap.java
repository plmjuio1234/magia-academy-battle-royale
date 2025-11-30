// 서버 - org.example.CollisionMap.java
package org.example;

public class CollisionMap {
    private boolean[][] walkable;
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;

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
     */
    public boolean isWall(float x, float y) {
        float mapWidth = width * tileWidth;
        float mapHeight = height * tileHeight;

        if (x < 0 || y < 0 || x > mapWidth || y > mapHeight) {
            return true;
        }

        int tileX = (int)(x / tileWidth);
        int tileY = (int)(y / tileHeight);

        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return true;
        }

        return !walkable[tileY][tileX];
    }

    /**
     * 클라이언트 GameMap.isWallInArea()와 동일한 로직
     */
    public boolean isWallInArea(float centerX, float centerY, float radius) {
        // 체크할 타일 범위 계산
        int minTileX = (int)((centerX - radius) / tileWidth);
        int maxTileX = (int)((centerX + radius) / tileWidth);
        int minTileY = (int)((centerY - radius) / tileHeight);
        int maxTileY = (int)((centerY + radius) / tileHeight);

        // 범위 내 모든 타일 체크
        for (int ty = minTileY; ty <= maxTileY; ty++) {
            for (int tx = minTileX; tx <= maxTileX; tx++) {
                if (tx < 0 || tx >= width || ty < 0 || ty >= height) {
                    continue;
                }

                // 타일 중심 좌표
                float tileCenterX = (tx + 0.5f) * tileWidth;
                float tileCenterY = (ty + 0.5f) * tileHeight;

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
}
