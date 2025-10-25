package com.magicbr.game.systems.skills.electric;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 번개 - 전기 원소 스킬 1
 */
public class LightningBolt extends Skill {
    private static final int SKILL_ID = 501;
    private static final String SKILL_NAME = "번개";
    private static final int BASE_DAMAGE = 35;
    private static final int MANA_COST = 17;
    private static final float COOLDOWN = 0.9f;
    private static final float CAST_TIME = 0.2f;
    private static final float PROJECTILE_SPEED = 500f;

    public LightningBolt() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "전기", "전기");
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
                "전기",
                1.5f  // 투사체 수명
            );
        }

        System.out.println("[번개] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ")");
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
