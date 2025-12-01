package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.utils.Constants;

/**
 * 체인 라이트닝 발사체 클래스
 *
 * 단순 직선 투사체입니다 (연쇄 공격 제거됨).
 * 작은 크기, 빠른 속도, 긴 사거리.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ChainLightningProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    /**
     * 체인 라이트닝 발사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     * @param maxRange 최대 사거리
     */
    public ChainLightningProjectile(Vector2 origin, float directionX, float directionY,
                                    int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "chain_lightning-projectile");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);
        // 히트박스 크기에 스케일 적용
        this.size = Constants.CHAIN_LIGHTNING_HITBOX_SIZE * Constants.CHAIN_LIGHTNING_SCALE;
        // 번개 원소: 청백색 (폴백용)
        setColor(0.5f, 0.8f, 1.0f);
    }

    /**
     * 기존 생성자 (하위 호환용)
     */
    public ChainLightningProjectile(Vector2 origin, float directionX, float directionY,
                                    int damage, float speed) {
        this(origin, directionX, directionY, damage, speed, Constants.CHAIN_LIGHTNING_RANGE);
    }

    /**
     * 업데이트 (사거리 체크 포함)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 사거리 체크
        traveledDistance = position.dst(startPosition);
        if (traveledDistance >= maxRange) {
            isAlive = false;
        }
    }

    /**
     * 충돌 감지 (BaseProjectile 자동 처리)
     */
    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
