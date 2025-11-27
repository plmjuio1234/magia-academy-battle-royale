package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;

/**
 * 아이스 스파이크 발사체 클래스
 *
 * 관통 공격이 가능한 얼음 창입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class IceSpikeProjectile extends BaseProjectile {

    /**
     * 아이스 스파이크 발사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     * @param pierceCount 관통 횟수
     */
    public IceSpikeProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed, int pierceCount) {
        super(origin, directionX, directionY, damage, speed, "ice_spike-loop");
        this.maxPierceCount = pierceCount;  // 관통 설정
        // 얼음 원소: 청백색 (폴백용)
        setColor(0.6f, 0.9f, 1.0f);
    }

    /**
     * 충돌 감지 (미사용 - BaseProjectile의 자동 충돌 감지 사용)
     */
    @Override
    protected void checkCollision() {
        // BaseProjectile의 checkCollision() 사용
        super.checkCollision();
    }
}
