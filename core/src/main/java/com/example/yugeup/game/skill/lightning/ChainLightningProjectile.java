package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;

/**
 * 체인 라이트닝 발사체 클래스
 *
 * 적에게 도달한 후 주변 적으로 연쇄되는 번개입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ChainLightningProjectile extends BaseProjectile {

    /**
     * 체인 라이트닝 발사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     */
    public ChainLightningProjectile(Vector2 origin, float directionX, float directionY,
                                    int damage, float speed) {
        super(origin, directionX, directionY, damage, speed, "chain_lightning-projectile");
        // 번개 원소: 청백색 (폴백용)
        setColor(0.5f, 0.8f, 1.0f);
    }

    /**
     * 충돌 감지 (BaseProjectile 자동 처리)
     */
    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
