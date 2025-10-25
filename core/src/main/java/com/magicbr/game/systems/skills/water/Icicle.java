package com.magicbr.game.systems.skills.water;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 얼음창 - 물 원소 스킬 1
 * 느린 투사체 스킬, 저 MP, 빠른 쿨다운
 * 적을 2초간 50% 느리게 함 (슬로우 효과)
 *
 * 스펙:
 * - 피해: 25
 * - MP: 12
 * - 쿨다운: 0.6초
 * - 슬로우: 50% 감소, 2초간
 */
public class Icicle extends Skill {
    private static final int SKILL_ID = 201;
    private static final String SKILL_NAME = "얼음창";
    private static final int BASE_DAMAGE = 25;
    private static final int MANA_COST = 12;
    private static final float COOLDOWN = 0.6f;
    private static final float CAST_TIME = 0.15f;
    private static final float PROJECTILE_SPEED = 350f;
    private static final float SLOW_PERCENT = 0.5f;  // 50% 감소
    private static final float SLOW_DURATION = 2.0f;  // 2초
    private static final String ELEMENT_COLOR = "물";
    private static final String ELEMENT_NAME = "물";

    public Icicle() {
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
                2.0f  // 투사체 수명 (초)
            );
        }

        System.out.println("[얼음창] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (피해: " + BASE_DAMAGE + ", 슬로우: " + (int)(SLOW_PERCENT * 100) + "%)");
    }

    @Override
    public float getProjectileSpeed() {
        return PROJECTILE_SPEED;
    }

    @Override
    public float getProjectileLifetime() {
        return 2.0f;
    }
}
