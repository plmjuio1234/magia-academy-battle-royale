package com.magicbr.game.systems.skills.earth;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 돌덩이 - 땅 원소 스킬 1
 */
public class RockThrow extends Skill {
    private static final int SKILL_ID = 401;
    private static final String SKILL_NAME = "돌덩이";
    private static final int BASE_DAMAGE = 33;
    private static final int MANA_COST = 14;
    private static final float COOLDOWN = 0.7f;
    private static final float CAST_TIME = 0.2f;
    private static final float PROJECTILE_SPEED = 380f;

    public RockThrow() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "땅", "땅");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();

        // 방향 계산
        float dirX = targetX - casterPos.x;
        float dirY = targetY - casterPos.y;
        float distance = (float)Math.sqrt(dirX * dirX + dirY * dirY);

        if (distance == 0) return;

        dirX /= distance;
        dirY /= distance;

        float vx = dirX * PROJECTILE_SPEED;
        float vy = dirY * PROJECTILE_SPEED;

        // 투사체 생성
        if (projectilePool != null) {
            projectilePool.obtain(
                casterPos.x, casterPos.y,
                vx, vy,
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                "땅",
                2.0f  // 투사체 수명
            );
        }

        System.out.println("[돌덩이] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ")");
    }

    @Override
    public float getProjectileSpeed() {
        return 350f;
    }

    @Override
    public float getProjectileLifetime() {
        return 2.0f;
    }
}
