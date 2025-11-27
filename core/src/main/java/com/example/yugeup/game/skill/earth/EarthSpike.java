package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EarthSpike extends ElementalSkill {
    private transient List<EarthSpikeZone> activeZones;

    public EarthSpike(Player owner) {
        super(5002, "어스 스파이크", Constants.EARTH_SPIKE_MANA_COST,
              Constants.EARTH_SPIKE_COOLDOWN, Constants.EARTH_SPIKE_DAMAGE,
              ElementType.EARTH, owner);
        this.activeZones = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        EarthSpikeZone zone = new EarthSpikeZone(targetPosition.x, targetPosition.y, getDamage());
        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<EarthSpikeZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            EarthSpikeZone zone = iterator.next();
            zone.update(delta);
            if (!zone.isActive()) iterator.remove();
        }
    }

    public List<EarthSpikeZone> getActiveZones() {
        return activeZones;
    }
}
