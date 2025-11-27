package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import java.util.List;
import com.example.yugeup.game.monster.Monster;

public class RockSmashProjectile extends BaseProjectile {
    public RockSmashProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed) {
        super(origin, directionX, directionY, damage, speed, "rock_smash-start");  // 애니메이션 이름 전달
        // 땅 원소: 갈색 (폴백용)
        setColor(0.6f, 0.4f, 0.2f);
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
                return;
            }
        }
    }

    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
