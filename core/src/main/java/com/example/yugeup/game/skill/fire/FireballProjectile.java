package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.utils.Constants;
import java.util.List;

/**
 * 파이어볼 발사체 클래스
 *
 * 직선으로 날아가는 불 원소 발사체입니다.
 * 새 스펙: 사거리 300, 속도 100, 히트박스 12x12
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FireballProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    /**
     * 파이어볼 발사체 생성자 (직진, 사거리 제한)
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param maxRange 최대 사거리 (픽셀)
     */
    public FireballProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "fireball-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);
        // 히트박스 12x12에 스케일 4배 적용 = 48x48 렌더링
        this.size = Constants.FIREBALL_HITBOX_SIZE * Constants.FIREBALL_SCALE;
        // 불 원소: 주황-빨강색 (폴백용)
        setColor(1.0f, 0.4f, 0.0f);
    }

    /**
     * 기존 생성자 (하위 호환용)
     */
    public FireballProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed) {
        this(origin, directionX, directionY, damage, speed, Constants.FIREBALL_RANGE);
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
     * 충돌 감지 (몬스터 목록에서)
     *
     * GameManager의 몬스터 목록을 받아서 충돌 판정합니다.
     *
     * @param monsters 게임의 모든 몬스터 목록
     */
    public void checkCollision(List<Monster> monsters) {
        if (monsters == null || !isAlive) {
            return;
        }

        for (Monster monster : monsters) {
            if (monster == null || monster.isDead()) {
                continue;
            }

            // 거리 계산
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= COLLISION_RADIUS) {
                handleMonsterCollision(monster);
                return;  // 파이어볼은 첫 타격 후 소멸
            }
        }
    }

    /**
     * 충돌 감지 (부모 메서드 호출)
     */
    @Override
    protected void checkCollision() {
        // 부모 클래스의 충돌 감지 사용 (서버 동기화 포함)
        super.checkCollision();
    }
}
