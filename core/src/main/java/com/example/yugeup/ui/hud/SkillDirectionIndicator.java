package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * 스킬 방향 표시기
 *
 * 스킬 시전 시 플레이어 중심에서 마우스/터치 방향으로 화살표를 그립니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillDirectionIndicator {
    private Vector2 playerPosition;
    private Vector2 targetPosition;
    private boolean isActive;

    // 화살표 길이 및 스타일
    private static final float ARROW_LENGTH = 100f;  // 화살표 길이
    private static final float ARROW_HEAD_SIZE = 20f;  // 화살표 머리 크기
    private static final float ARROW_WIDTH = 4f;  // 화살표 선 두께

    // 범위 표시 모드 (인페르노 등 광역 스킬용)
    private boolean isRangeMode;
    private float rangeRadius;

    /**
     * 생성자
     */
    public SkillDirectionIndicator() {
        this.playerPosition = new Vector2();
        this.targetPosition = new Vector2();
        this.isActive = false;
        this.isRangeMode = false;
        this.rangeRadius = 0f;
    }

    /**
     * 방향 표시기 활성화
     *
     * @param playerX 플레이어 X 좌표
     * @param playerY 플레이어 Y 좌표
     */
    public void activate(float playerX, float playerY) {
        this.playerPosition.set(playerX, playerY);
        this.targetPosition.set(playerX, playerY);
        this.isActive = true;
        this.isRangeMode = false;
        this.rangeRadius = 0f;
    }

    /**
     * 범위 표시 모드로 활성화 (광역 스킬용)
     *
     * @param playerX 플레이어 X 좌표
     * @param playerY 플레이어 Y 좌표
     * @param radius 범위 반경
     */
    public void activateRangeMode(float playerX, float playerY, float radius) {
        this.playerPosition.set(playerX, playerY);
        this.targetPosition.set(playerX, playerY);
        this.isActive = true;
        this.isRangeMode = true;
        this.rangeRadius = radius;
    }

    /**
     * 방향 표시기 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 목표 위치 업데이트 (마우스/터치 위치)
     *
     * @param targetX 목표 X 좌표
     * @param targetY 목표 Y 좌표
     */
    public void updateTarget(float targetX, float targetY) {
        this.targetPosition.set(targetX, targetY);
    }

    /**
     * 방향 표시기 렌더링
     *
     * @param shapeRenderer ShapeRenderer
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (!isActive) {
            return;
        }

        // 범위 표시 모드일 경우 원 그리기
        if (isRangeMode) {
            renderRangeCircle(shapeRenderer);
            return;
        }

        // 방향 벡터 계산
        Vector2 direction = targetPosition.cpy().sub(playerPosition);
        if (direction.len() < 10f) {
            return;  // 너무 짧으면 그리지 않음
        }

        direction.nor();  // 정규화

        // 화살표 끝점 계산
        Vector2 arrowEnd = playerPosition.cpy().add(direction.cpy().scl(ARROW_LENGTH));

        // 화살표 선 그리기
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.YELLOW);

        // 화살표 몸체 (굵은 선)
        drawThickLine(shapeRenderer, playerPosition.x, playerPosition.y,
                     arrowEnd.x, arrowEnd.y, ARROW_WIDTH);

        // 화살표 머리 그리기 (삼각형)
        drawArrowHead(shapeRenderer, arrowEnd, direction);

        shapeRenderer.end();
    }

    /**
     * 범위 원 렌더링 (광역 스킬용)
     *
     * @param shapeRenderer ShapeRenderer
     */
    private void renderRangeCircle(ShapeRenderer shapeRenderer) {
        // 블렌딩 활성화 (투명도 적용)
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 반투명 원 내부
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.5f, 0f, 0.2f);  // 반투명 주황색
        shapeRenderer.circle(playerPosition.x, playerPosition.y, rangeRadius, 48);
        shapeRenderer.end();

        // 원 테두리
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.6f, 0f, 0.8f);  // 주황색 테두리
        shapeRenderer.circle(playerPosition.x, playerPosition.y, rangeRadius, 48);
        shapeRenderer.end();

        // 블렌딩 비활성화
        com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    /**
     * 굵은 선 그리기
     */
    private void drawThickLine(ShapeRenderer sr, float x1, float y1, float x2, float y2, float width) {
        Vector2 direction = new Vector2(x2 - x1, y2 - y1);
        Vector2 perpendicular = new Vector2(-direction.y, direction.x).nor().scl(width / 2);

        float[] vertices = {
            x1 + perpendicular.x, y1 + perpendicular.y,
            x1 - perpendicular.x, y1 - perpendicular.y,
            x2 - perpendicular.x, y2 - perpendicular.y,
            x2 + perpendicular.x, y2 + perpendicular.y
        };

        sr.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
        sr.triangle(vertices[0], vertices[1], vertices[4], vertices[5], vertices[6], vertices[7]);
    }

    /**
     * 화살표 머리 그리기
     */
    private void drawArrowHead(ShapeRenderer sr, Vector2 tip, Vector2 direction) {
        // 화살표 머리는 역방향으로 삼각형
        Vector2 back = direction.cpy().scl(-ARROW_HEAD_SIZE);
        Vector2 perpendicular = new Vector2(-direction.y, direction.x).scl(ARROW_HEAD_SIZE / 2);

        Vector2 left = tip.cpy().add(back).add(perpendicular);
        Vector2 right = tip.cpy().add(back).sub(perpendicular);

        sr.triangle(tip.x, tip.y, left.x, left.y, right.x, right.y);
    }

    /**
     * 현재 방향 벡터 반환
     *
     * @return 정규화된 방향 벡터
     */
    public Vector2 getDirection() {
        Vector2 dir = targetPosition.cpy().sub(playerPosition);
        if (dir.len() < 10f) {
            return new Vector2(1, 0);  // 기본 방향 (오른쪽)
        }
        return dir.nor();
    }

    /**
     * 활성 상태 반환
     *
     * @return 활성 여부
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 목표 위치 반환
     *
     * @return 목표 위치
     */
    public Vector2 getTargetPosition() {
        return targetPosition.cpy();
    }
}
