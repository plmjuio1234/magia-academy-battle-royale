package com.magicbr.game.systems.skills.earth;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 방어막 - 땅 원소 스킬 2
 * 임시 방어 효과 제공
 */
public class StoneShield extends Skill {
    private static final int SKILL_ID = 402;
    private static final String SKILL_NAME = "방어막";
    private static final int BASE_DAMAGE = 0;
    private static final int MANA_COST = 20;
    private static final float COOLDOWN = 2.5f;
    private static final float CAST_TIME = 0.3f;
    private static final float SHIELD_DURATION = 4.0f;
    private static final int DEFENSE_BONUS = 20;

    public StoneShield() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "땅", "땅");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 방어 효과 시각화 - 자신 위치에 정지된 투사체
        if (projectilePool != null) {
            projectilePool.obtain(
                caster.getPosition().x, caster.getPosition().y,
                0f, 0f,  // 정지 상태
                DEFENSE_BONUS,  // 방어력을 damage로 표현
                caster.getId(),
                SKILL_NAME,
                "땅",
                SHIELD_DURATION  // 방어 지속 시간
            );
        }

        System.out.println("[방어막] " + caster.getPlayerName() + " 시전! 방어력 +" + DEFENSE_BONUS + " (" + SHIELD_DURATION + "초)");
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
