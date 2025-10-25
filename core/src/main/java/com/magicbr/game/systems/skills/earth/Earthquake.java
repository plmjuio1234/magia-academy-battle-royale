package com.magicbr.game.systems.skills.earth;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 지진 - 땅 원소 스킬 3
 * 광역 피해 스킬
 */
public class Earthquake extends Skill {
    private static final int SKILL_ID = 403;
    private static final String SKILL_NAME = "지진";
    private static final int BASE_DAMAGE = 45;
    private static final int MANA_COST = 28;
    private static final float COOLDOWN = 3.0f;
    private static final float CAST_TIME = 0.6f;
    private static final float AOE_RADIUS = 150f;

    public Earthquake() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "땅", "땅");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 광역 지진 - 목표 위치에 정지된 투사체
        if (projectilePool != null) {
            projectilePool.obtain(
                targetX, targetY,
                0f, 0f,  // 정지 상태
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                "땅",
                1.0f  // 지진 지속 시간
            );
        }

        System.out.println("[지진] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ", 범위: " + AOE_RADIUS + "px)");
    }

    @Override
    public float getProjectileSpeed() {
        return 0f;
    }

    @Override
    public float getProjectileLifetime() {
        return 0.5f;
    }
}
