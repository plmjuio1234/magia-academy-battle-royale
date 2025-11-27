package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 썬더 스톰 스킬 클래스
 *
 * 번개 원소의 세 번째 스킬입니다.
 * 지속적으로 낙뢰를 쏟아붓는 지역을 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ThunderStorm extends ElementalSkill {

    // 활성 지역 목록
    private transient List<ThunderStormZone> activeZones;

    /**
     * 썬더 스톰 생성자
     *
     * @param owner 스킬 소유자
     */
    public ThunderStorm(Player owner) {
        super(5403, "썬더 스톰", Constants.THUNDER_STORM_MANA_COST,
              Constants.THUNDER_STORM_COOLDOWN, Constants.THUNDER_STORM_DAMAGE,
              ElementType.LIGHTNING, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 썬더 스톰을 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        // 쿨타임 확인
        if (!isReady()) {
            return;
        }

        // 마나 확인 및 소모
        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        // 썬더 스톰 지역 생성
        ThunderStormZone zone = new ThunderStormZone(
            targetPosition.x,
            targetPosition.y,
            getDamage(),
            Constants.THUNDER_STORM_DURATION
        );

        activeZones.add(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[ThunderStorm] 썬더 스톰 시전!");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);
        updateZones(delta);
    }

    /**
     * 활성 지역을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateZones(float delta) {
        Iterator<ThunderStormZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            ThunderStormZone zone = iterator.next();
            zone.update(delta);

            if (!zone.isActive()) {
                iterator.remove();
            }
        }
    }

    /**
     * 활성 지역 목록을 반환합니다.
     *
     * @return 지역 리스트
     */
    public List<ThunderStormZone> getActiveZones() {
        return activeZones;
    }
}
