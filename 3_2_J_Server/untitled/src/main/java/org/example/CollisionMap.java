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
        return isWall(centerX - radius, centerY) ||
            isWall(centerX + radius, centerY) ||
            isWall(centerX, centerY - radius) ||
            isWall(centerX, centerY + radius);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
}
