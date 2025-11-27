package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import java.util.List;
import com.example.yugeup.game.monster.Monster;

/**
 * 에어 슬래시 발사체 클래스
 */
public class AirSlashProjectile extends BaseProjectile {
    public AirSlashProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed) {
        super(origin, directionX, directionY, damage, speed, "air_slash-loop");  // 애니메이션 이름 전달
        // 바람 원소: 밝은 회색/흰색 (폴백용)
        setColor(0.9f, 0.9f, 0.95f);
    }

    public void checkCollision(List<Monster> monsters) {
        if (monsters == null || !isAlive) return;
        for (Monster monster : monsters) {
            if (monster == null || monster.isDead()) continue;
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance <= COLLISION_RADIUS) {
                handleMonsterCollision(monster);
                if (!isAlive) return;
            }
        }
    }

    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
