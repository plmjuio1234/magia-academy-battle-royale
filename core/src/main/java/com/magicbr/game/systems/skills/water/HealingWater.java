package com.magicbr.game.systems.skills.water;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 치유의 물 - 물 원소 스킬 3
 * 자기 자신을 회복하는 스킬
 *
 * 스펙:
 * - 회복: +25 HP
 * - MP: 20
 * - 쿨다운: 3.0초
 */
public class HealingWater extends Skill {
    private static final int SKILL_ID = 203;
    private static final String SKILL_NAME = "치유의 물";
    private static final int BASE_DAMAGE = 25;  // 실제로는 회복량
    private static final int MANA_COST = 20;
    private static final float COOLDOWN = 3.0f;
    private static final float CAST_TIME = 0.5f;
    private static final String ELEMENT_COLOR = "물";
    private static final String ELEMENT_NAME = "물";

    public HealingWater() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, ELEMENT_COLOR, ELEMENT_NAME);
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();

        // 자신을 회복
        int healAmount = BASE_DAMAGE;
        int newHp = Math.min(caster.getHp() + healAmount, caster.getMaxHp());
        int actualHeal = newHp - caster.getHp();

        caster.setHp(newHp);

        // 치유 효과 시각화 (중앙에 정지된 투사체)
        if (projectilePool != null) {
            projectilePool.obtain(
                casterPos.x, casterPos.y,
                0f, 0f,  // 정지 상태
                actualHeal,  // 회복량을 damage 필드로 사용 (시각적 표현용)
                caster.getId(),
                SKILL_NAME,
                ELEMENT_COLOR,
                1.0f  // 치유 효과 지속 시간 (초)
            );
        }

        System.out.println("[치유의 물] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (회복: +" + actualHeal + " HP)");
    }

    @Override
    public float getProjectileSpeed() {
        return 0f;
    }

    @Override
    public float getProjectileLifetime() {
        return 1.5f;
    }
}
