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
 * 워터 샷 스킬 클래스
 *
 * 물 원소의 첫 번째 스킬입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class WaterShot extends ElementalSkill {

    private transient List<WaterShotProjectile> activeProjectiles;

    public WaterShot(Player owner) {
        super(5201, "워터 샷", Constants.WATER_SHOT_MANA_COST,
              Constants.WATER_SHOT_COOLDOWN, Constants.WATER_SHOT_DAMAGE,
              ElementType.WATER, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        WaterShotProjectile projectile = new WaterShotProjectile(
            casterPos, direction.x, direction.y, getDamage(),
            Constants.WATER_SHOT_SPEED, Constants.WATER_SHOT_RANGE
        );

        activeProjectiles.add(projectile);
        currentCooldown = getCooldown();

        // 네트워크 동기화 (확장 버전)
        float lifetime = Constants.WATER_SHOT_RANGE / Constants.WATER_SHOT_SPEED;
        sendProjectileSkillToNetwork(casterPos, targetPosition,
            Constants.WATER_SHOT_SPEED, Constants.WATER_SHOT_HITBOX_SIZE, lifetime);

        System.out.println("[WaterShot] 워터 샷 시전!");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<WaterShotProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            WaterShotProjectile projectile = iterator.next();
            projectile.update(delta);
            if (!projectile.isAlive()) {
                projectile.dispose();
                iterator.remove();
            }
        }
    }

    public List<WaterShotProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
