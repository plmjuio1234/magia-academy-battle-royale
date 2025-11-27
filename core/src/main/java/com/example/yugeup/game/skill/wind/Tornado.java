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
 * 토네이도 스킬 클래스
 *
 * 바람 원소의 두 번째 스킬입니다.
 * 회전하는 바람 지역을 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Tornado extends ElementalSkill {

    // 활성 지역 목록
    private transient List<TornadoZone> activeZones;

    /**
     * 토네이도 생성자
     *
     * @param owner 스킬 소유자
     */
    public Tornado(Player owner) {
        super(5302, "토네이도", Constants.TORNADO_MANA_COST,
              Constants.TORNADO_COOLDOWN, Constants.TORNADO_DAMAGE,
              ElementType.WIND, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 토네이도를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) {
            return;
        }

        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        TornadoZone zone = new TornadoZone(
            targetPosition.x,
            targetPosition.y,
            getDamage(),
            Constants.TORNADO_DURATION
        );

        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[Tornado] 토네이도 시전!");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        Iterator<TornadoZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            TornadoZone zone = iterator.next();
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
    public List<TornadoZone> getActiveZones() {
        return activeZones;
    }
}
