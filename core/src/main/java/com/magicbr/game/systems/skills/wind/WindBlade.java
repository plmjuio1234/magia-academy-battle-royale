package com.magicbr.game.systems.skills.wind;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 바람 칼날 - 바람 원소 스킬 3
 */
public class WindBlade extends Skill {
    private static final int SKILL_ID = 303;
    private static final String SKILL_NAME = "바람 칼날";
    private static final int BASE_DAMAGE = 32;
    private static final int MANA_COST = 18;
    private static final float COOLDOWN = 0.8f;
    private static final float CAST_TIME = 0.15f;
    private static final float PROJECTILE_SPEED = 450f;

    public WindBlade() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "바람", "바람");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();
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
                "바람",
                2.0f  // 투사체 수명
            );
        }

        System.out.println("[바람 칼날] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ")");
    }

    @Override
    public float getProjectileSpeed() {
        return 400f;
    }

    @Override
    public float getProjectileLifetime() {
        return 2.0f;
    }
}
