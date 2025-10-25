package com.magicbr.game.systems.skills.electric;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 전기 충격 - 전기 원소 스킬 2
 * 즉시 대상에게 피해를 줌 (투사체 아님)
 */
public class Shock extends Skill {
    private static final int SKILL_ID = 502;
    private static final String SKILL_NAME = "전기 충격";
    private static final int BASE_DAMAGE = 30;
    private static final int MANA_COST = 18;
    private static final float COOLDOWN = 1.2f;
    private static final float CAST_TIME = 0.15f;
    private static final float AOE_RADIUS = 80f;
    private static final float STUN_DURATION = 0.5f;

    public Shock() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "전기", "전기");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // AoE 전기 충격 - 목표 위치에 정지된 투사체
        if (projectilePool != null) {
            projectilePool.obtain(
                targetX, targetY,
                0f, 0f,  // 정지 상태
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                "전기",
                0.8f  // 충격 지속 시간
            );
        }

        System.out.println("[전기 충격] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ", 기절: " + STUN_DURATION + "초)");
    }

    @Override
    public float getProjectileSpeed() {
        return 0f;
    }

    @Override
    public float getProjectileLifetime() {
        return 0.8f;
    }
}
