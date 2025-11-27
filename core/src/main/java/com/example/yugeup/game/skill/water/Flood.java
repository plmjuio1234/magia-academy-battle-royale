package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Flood extends ElementalSkill {
    private transient List<FloodZone> activeZones;

    public Flood(Player owner) {
        super(5203, "플러드", Constants.FLOOD_MANA_COST,
              Constants.FLOOD_COOLDOWN, Constants.FLOOD_DAMAGE,
              ElementType.WATER, owner);
        this.activeZones = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        FloodZone zone = new FloodZone(targetPosition.x, targetPosition.y, getDamage(), Constants.FLOOD_DURATION);
        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<FloodZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            FloodZone zone = iterator.next();
            zone.update(delta);
            if (!zone.isActive()) iterator.remove();
        }
    }

    public List<FloodZone> getActiveZones() {
        return activeZones;
    }
}
