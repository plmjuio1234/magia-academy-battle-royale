package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.utils.Constants;

/**
 * 워터샷 투사체 클래스
 *
 * 느린 속도(50)지만 큰 히트박스(24x24)를 가진 직선 투사체입니다.
 * 타격 시 사라집니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class WaterShotProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    /**
     * 워터샷 투사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param maxRange 최대 사거리 (픽셀)
     */
    public WaterShotProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "water_ball-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);
        // 히트박스 24x24에 스케일 1배 적용
        this.size = Constants.WATER_SHOT_HITBOX_SIZE * Constants.WATER_SHOT_SCALE;
        // 물 원소: 청록색 (폴백용)
        setColor(0.0f, 0.7f, 1.0f);

        System.out.println("[WaterShotProjectile] 생성! 방향: (" + directionX + ", " + directionY + "), 속도: " + speed);
    }

    /**
     * 업데이트 (사거리 체크 포함)
     */
    @Override
    public void update(float delta) {
        if (!isAlive) return;

        lifetime += delta;
        animationTime += delta;

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 사거리 체크
        traveledDistance = position.dst(startPosition);
        if (traveledDistance >= maxRange) {
            isAlive = false;
            return;
        }

        // 충돌 감지
        checkCollision();
    }

    /**
     * 충돌 감지 (부모 메서드 호출)
     */
    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
