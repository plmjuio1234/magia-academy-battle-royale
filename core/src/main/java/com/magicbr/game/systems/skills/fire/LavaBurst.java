package com.magicbr.game.systems.skills.fire;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 용암 분출 - 불 원소 스킬 3
 * DoT(지속 피해) 스킬, 높은 MP, 긴 쿨다운
 *
 * 스펙:
 * - 기본 피해: 35
 * - DoT: 초당 5 피해, 3초간
 * - MP: 25
 * - 쿨다운: 2.0초
 * - 속도: 느림 (범위형 투사체)
 */
public class LavaBurst extends Skill {
    private static final int SKILL_ID = 103;
    private static final String SKILL_NAME = "용암 분출";
    private static final int BASE_DAMAGE = 35;
    private static final int MANA_COST = 25;
    private static final float COOLDOWN = 2.0f;
    private static final float CAST_TIME = 0.4f;
    private static final float PROJECTILE_SPEED = 250f;  // 더 느린 속도
    private static final int DOT_DAMAGE = 5;             // 초당 피해
    private static final float DOT_DURATION = 3.0f;      // 3초간 지속
    private static final String ELEMENT_COLOR = "불";
    private static final String ELEMENT_NAME = "불";

    public LavaBurst() {
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

        // 투사체 속도
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
                3.0f  // 투사체 수명 (초) - DoT 지속 시간과 맞춤
            );
        }

        System.out.println("[용암 분출] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (피해: " + BASE_DAMAGE + " + DoT " + DOT_DAMAGE + "/초)");
    }

    @Override
    public float getProjectileSpeed() {
        return PROJECTILE_SPEED;  // 250f - 느린 속도
    }

    @Override
    public float getProjectileLifetime() {
        return 3.0f;  // DoT 지속 시간과 맞춤
    }
}
