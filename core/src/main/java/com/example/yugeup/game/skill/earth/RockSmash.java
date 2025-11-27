package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RockSmash extends ElementalSkill {
    private transient List<RockSmashProjectile> activeProjectiles;

    public RockSmash(Player owner) {
        super(5001, "록 스매시", Constants.ROCK_SMASH_MANA_COST,
              Constants.ROCK_SMASH_COOLDOWN, Constants.ROCK_SMASH_DAMAGE,
              ElementType.EARTH, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        RockSmashProjectile projectile = new RockSmashProjectile(
            casterPos, direction.x, direction.y, getDamage(), Constants.ROCK_SMASH_SPEED
        );

        activeProjectiles.add(projectile);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<RockSmashProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            RockSmashProjectile projectile = iterator.next();
            projectile.update(delta);
            if (!projectile.isAlive()) {
                projectile.dispose();
                iterator.remove();
            }
        }
    }

    public List<RockSmashProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
