package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 플러드 스킬 클래스
 *
 * 물 원소의 세 번째 스킬입니다.
 * 선택한 방향으로 느리게 움직이는 관통형 소용돌이를 발사합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Flood extends ElementalSkill {

    // 활성 투사체 목록
    private transient List<FloodProjectile> activeProjectiles;

    public Flood(Player owner) {
        super(5203, "플러드", Constants.FLOOD_MANA_COST,
              Constants.FLOOD_COOLDOWN, Constants.FLOOD_DAMAGE,
              ElementType.WATER, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        // 시전 위치 및 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 플러드 투사체 생성 (관통형 도트딜)
        FloodProjectile projectile = new FloodProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            Constants.FLOOD_SPEED,
            Constants.FLOOD_RANGE
        );

        activeProjectiles.add(projectile);
        currentCooldown = getCooldown();

        // 네트워크 동기화 (확장 버전)
        float lifetime = Constants.FLOOD_RANGE / Constants.FLOOD_SPEED;
        sendProjectileSkillToNetwork(casterPos, targetPosition,
            Constants.FLOOD_SPEED, Constants.FLOOD_HITBOX_WIDTH, lifetime);

        System.out.println("[Flood] 플러드 시전! 방향: (" + direction.x + ", " + direction.y + ")");
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // 투사체 업데이트
        Iterator<FloodProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            FloodProjectile projectile = iterator.next();
            projectile.update(delta);

            if (!projectile.isAlive()) {
                iterator.remove();
            }
        }
    }

    /**
     * 활성 투사체 목록을 반환합니다.
     *
     * @return 투사체 리스트
     */
    public List<FloodProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
