package com.magicbr.game.systems.skills.fire;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 화염 폭발 - 불 원소 스킬 2
 * 광역 피해 스킬, 중간 MP, 중간 쿨다운
 *
 * 스펙:
 * - 피해: 40
 * - MP: 20
 * - 쿨다운: 1.2초
 * - 범위: 100px 반지름
 */
public class Explosion extends Skill {
    private static final int SKILL_ID = 102;
    private static final String SKILL_NAME = "화염 폭발";
    private static final int BASE_DAMAGE = 40;
    private static final int MANA_COST = 20;
    private static final float COOLDOWN = 1.2f;
    private static final float CAST_TIME = 0.3f;
    private static final float AOE_RADIUS = 100f;
    private static final String ELEMENT_COLOR = "불";
    private static final String ELEMENT_NAME = "불";

    public Explosion() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, ELEMENT_COLOR, ELEMENT_NAME);
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();

        // AOE 폭발 - 투사체를 목표 위치에 정지 상태로 생성
        if (projectilePool != null) {
            projectilePool.obtain(
                targetX, targetY,
                0f, 0f,  // 정지 상태 (속도 없음)
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                ELEMENT_COLOR,
                0.5f  // 폭발 지속 시간 (초)
            );
        }

        System.out.println("[화염 폭발] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (피해: " + BASE_DAMAGE + ", 범위: " + AOE_RADIUS + "px)");
    }

    @Override
    public float getProjectileSpeed() {
        return 0f;  // AoE - 정지 상태
    }

    @Override
    public float getProjectileLifetime() {
        return 0.5f;  // 폭발 지속 시간
    }
}
