package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 에어 슬래시 스킬 클래스
 *
 * 바람 원소의 첫 번째 스킬입니다.
 * 빠른 관통 발사체를 날립니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class AirSlash extends ElementalSkill {

    // 발사체 목록
    private transient List<AirSlashProjectile> activeProjectiles;

    /**
     * 에어 슬래시 생성자
     *
     * @param owner 스킬 소유자
     */
    public AirSlash(Player owner) {
        super(5301, "에어 슬래시", Constants.AIR_SLASH_MANA_COST,
              Constants.AIR_SLASH_COOLDOWN, Constants.AIR_SLASH_DAMAGE,
              ElementType.WIND, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 에어 슬래시를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        AirSlashProjectile projectile = new AirSlashProjectile(
            casterPos, direction.x, direction.y, getDamage(), Constants.AIR_SLASH_SPEED
        );
        projectile.setMaxPierceCount(5);  // 최대 5명 관통

        activeProjectiles.add(projectile);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[AirSlash] 에어 슬래시 시전!");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<AirSlashProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            AirSlashProjectile projectile = iterator.next();
            projectile.update(delta);
            if (!projectile.isAlive()) {
                projectile.dispose();
                iterator.remove();
            }
        }
    }

    public List<AirSlashProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
