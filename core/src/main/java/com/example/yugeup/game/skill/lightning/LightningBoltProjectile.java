package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;

/**
 * 라이트닝 볼트 발사체 클래스
 *
 * 빠른 속도로 날아가는 번개 원소 발사체입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LightningBoltProjectile extends BaseProjectile {

    /**
     * 라이트닝 볼트 발사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     */
    public LightningBoltProjectile(Vector2 origin, float directionX, float directionY,
                                   int damage, float speed) {
        super(origin, directionX, directionY, damage, speed, "lightning_volt");
        // 번개 원소: 밝은 노란색 (폴백용)
        setColor(1.0f, 1.0f, 0.3f);
    }

    /**
     * 충돌 감지 (BaseProjectile 자동 처리)
     */
    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
