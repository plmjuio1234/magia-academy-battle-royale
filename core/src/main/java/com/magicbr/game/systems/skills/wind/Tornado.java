package com.magicbr.game.systems.skills.wind;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 회오리 - 바람 원소 스킬 1
 * 광역 피해 스킬
 */
public class Tornado extends Skill {
    private static final int SKILL_ID = 301;
    private static final String SKILL_NAME = "회오리";
    private static final int BASE_DAMAGE = 28;
    private static final int MANA_COST = 16;
    private static final float COOLDOWN = 1.0f;
    private static final float CAST_TIME = 0.25f;
    private static final float AOE_RADIUS = 120f;

    public Tornado() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "바람", "바람");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();

        // AOE 회오리 - 목표 위치에 투사체 생성
        if (projectilePool != null) {
            projectilePool.obtain(
                targetX, targetY,
                0f, 0f,  // 정지 상태
                BASE_DAMAGE,
                caster.getId(),
                SKILL_NAME,
                "바람",
                1.5f  // 회오리 지속 시간
            );
        }

        System.out.println("[회오리] " + caster.getPlayerName() + " 시전! (피해: " + BASE_DAMAGE + ", 범위: " + AOE_RADIUS + "px)");
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
