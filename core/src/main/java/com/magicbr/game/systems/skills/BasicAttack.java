package com.magicbr.game.systems.skills;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;

/**
 * 기본공격 - 모든 플레이어가 1레벨부터 사용 가능
 * 빠른 쿨다운, 낮은 MP 소비
 *
 * 스펙:
 * - 피해: 15
 * - MP: 5
 * - 쿨다운: 0.3초
 */
public class BasicAttack extends Skill {
    private static final int SKILL_ID = 0;  // 기본공격은 ID 0
    private static final String SKILL_NAME = "기본공격";
    private static final int BASE_DAMAGE = 15;
    private static final int MANA_COST = 5;
    private static final float COOLDOWN = 0.3f;
    private static final float CAST_TIME = 0.05f;
    private static final float PROJECTILE_SPEED = 350f;

    public BasicAttack(String elementName) {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, elementName, elementName);
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
                elementName,
                1.5f  // 투사체 수명 (초)
            );
        }

        System.out.println("[기본공격] " + caster.getPlayerName() + " 시전! 피해: " + BASE_DAMAGE);
    }
}
