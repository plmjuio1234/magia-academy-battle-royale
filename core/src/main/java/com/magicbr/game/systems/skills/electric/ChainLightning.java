package com.magicbr.game.systems.skills.electric;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 체인 라이트닝 - 전기 원소 스킬 3
 * 연쇄 피해 스킬
 */
public class ChainLightning extends Skill {
    private static final int SKILL_ID = 503;
    private static final String SKILL_NAME = "체인 라이트닝";
    private static final int BASE_DAMAGE = 40;
    private static final int MANA_COST = 26;
    private static final float COOLDOWN = 2.5f;
    private static final float CAST_TIME = 0.4f;
    private static final int CHAIN_TARGETS = 3;
    private static final float CHAIN_RADIUS = 200f;

    public ChainLightning() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "전기", "전기");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 목표 위치에 연쇄 라이트닝 투사체 생성
        if (projectilePool != null) {
            projectilePool.obtain(
                targetX, targetY,
                0f, 0f,  // 정지 상태
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                "전기",
                1.0f  // 연쇄 지속 시간
            );
        }

        System.out.println("[체인 라이트닝] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ", 연쇄: " + CHAIN_TARGETS + "회)");
    }

    @Override
    public float getProjectileSpeed() {
        return 0f;
    }

    @Override
    public float getProjectileLifetime() {
        return 1.0f;
    }
}
