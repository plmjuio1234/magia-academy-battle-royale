package com.magicbr.game.systems.skills.fire;

import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 화염구 - 불 원소 스킬 1
 * 기본 공격 스킬, 낮은 MP, 빠른 쿨다운
 *
 * 스펙:
 * - 피해: 30
 * - MP: 15
 * - 쿨다운: 0.5초
 * - 속도: 빠름
 */
public class Fireball extends Skill {
    private static final int SKILL_ID = 101;
    private static final String SKILL_NAME = "화염구";
    private static final int BASE_DAMAGE = 30;
    private static final int MANA_COST = 15;
    private static final float COOLDOWN = 0.5f;
    private static final float CAST_TIME = 0.1f;
    private static final float PROJECTILE_SPEED = 400f;
    private static final String ELEMENT_COLOR = "불";
    private static final String ELEMENT_NAME = "불";

    public Fireball() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, ELEMENT_COLOR, ELEMENT_NAME);
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        Vector2 casterPos = caster.getPosition();

        // 방향 계산 (플레이어 위치 → 목표 위치)
        float dirX = targetX - casterPos.x;
        float dirY = targetY - casterPos.y;
        float distance = (float)Math.sqrt(dirX * dirX + dirY * dirY);

        if (distance == 0) return;

        // 방향 정규화
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
                2.0f  // 투사체 수명 (초)
            );
        }

        System.out.println("[화염구] " + caster.getPlayerName() + "이(가) " + SKILL_NAME + " 시전! (피해: " + BASE_DAMAGE + ")");
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
