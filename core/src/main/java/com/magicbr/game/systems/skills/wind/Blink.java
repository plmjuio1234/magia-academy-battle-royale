package com.magicbr.game.systems.skills.wind;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.Skill;

/**
 * 순간이동 - 바람 원소 스킬 2
 * 자신을 이동시키는 스킬
 */
public class Blink extends Skill {
    private static final int SKILL_ID = 302;
    private static final String SKILL_NAME = "순간이동";
    private static final int BASE_DAMAGE = 0;
    private static final int MANA_COST = 15;
    private static final float COOLDOWN = 2.0f;
    private static final float CAST_TIME = 0.2f;
    private static final float BLINK_DISTANCE = 200f;

    public Blink() {
        super(SKILL_ID, SKILL_NAME, BASE_DAMAGE, MANA_COST, COOLDOWN, CAST_TIME, "바람", "바람");
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 방향 계산
        float dirX = targetX - caster.getPosition().x;
        float dirY = targetY - caster.getPosition().y;
        float distance = (float)Math.sqrt(dirX * dirX + dirY * dirY);

        if (distance == 0) {
            // 대상이 없으면 앞쪽으로 이동
            dirX = 1f;
            dirY = 0f;
        } else {
            dirX /= distance;
            dirY /= distance;
        }

        // 플레이어를 목표 방향으로 이동
        float newX = caster.getPosition().x + dirX * BLINK_DISTANCE;
        float newY = caster.getPosition().y + dirY * BLINK_DISTANCE;

        // 맵 경계 체크
        newX = Math.max(0, Math.min(newX, 1920f));
        newY = Math.max(0, Math.min(newY, 1920f));

        caster.getPosition().set(newX, newY);

        System.out.println("[순간이동] " + caster.getPlayerName() + " 시전! 새 위치: (" + newX + ", " + newY + ")");
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
