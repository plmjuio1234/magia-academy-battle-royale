package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.BaseProjectile;
import java.util.List;

/**
 * 파이어볼 발사체 클래스
 *
 * 직선으로 날아가는 불 원소 발사체입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FireballProjectile extends BaseProjectile {

    /**
     * 파이어볼 발사체 생성자 (직진)
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     */
    public FireballProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed) {
        super(origin, directionX, directionY, damage, speed, "fireball-loop");  // 애니메이션 이름 전달
        // 불 원소: 주황-빨강색 (폴백용)
        setColor(1.0f, 0.4f, 0.0f);
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
