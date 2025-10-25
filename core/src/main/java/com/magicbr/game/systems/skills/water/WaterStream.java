package com.magicbr.game.systems.skills.water;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 물줄기 - 물 원소 스킬 2
 * 넉백 스킬, 중간 MP, 중간 쿨다운
 * 적을 뒤로 밀어냄
 *
 * 스펙:
 * - 피해: 20
 * - MP: 18
 * - 쿨다운: 1.0초
 * - 넉백: 300px
 */
public class WaterStream extends Skill {
    private static final int SKILL_ID = 202;
    private static final String SKILL_NAME = "물줄기";
    private static final int BASE_DAMAGE = 20;
    private static final int MANA_COST = 18;
    private static final float COOLDOWN = 1.0f;
    private static final float CAST_TIME = 0.2f;
    private static final float PROJECTILE_SPEED = 400f;
    private static final float KNOCKBACK_DISTANCE = 300f;
    private static final String ELEMENT_COLOR = "물";
    private static final String ELEMENT_NAME = "물";

    public WaterStream() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, ELEMENT_COLOR, ELEMENT_NAME);
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
                ELEMENT_COLOR,
                2.5f  // 투사체 수명 (초)
            );
        }

        System.out.println("[물줄기] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (피해: " + BASE_DAMAGE + ", 넉백: " + KNOCKBACK_DISTANCE + "px)");
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
