package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.game.buff.SpeedBuff;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 폭풍 스킬 클래스
 *
 * 바람 원소의 세 번째 스킬입니다.
 * 8초 동안 플레이어 주위에 따라다니는 소용돌이를 생성하고
 * 이동속도 50% 증가 버프를 부여합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Storm extends ElementalSkill {

    private transient List<StormZone> activeZones;

    /**
     * 폭풍 생성자
     *
     * @param owner 스킬 소유자
     */
    public Storm(Player owner) {
        super(5303, "폭풍", Constants.STORM_MANA_COST,
              Constants.STORM_COOLDOWN, Constants.STORM_DAMAGE,
              ElementType.WIND, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 폭풍을 시전합니다.
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

        // 플레이어에 가속 버프 적용 (50% 속도 증가)
        caster.addBuff(new SpeedBuff(Constants.STORM_DURATION, Constants.STORM_SPEED_MULTIPLIER));

        // 폭풍 지역 생성 (플레이어 추적형)
        StormZone zone = new StormZone(
            caster,  // 플레이어 참조 전달 (위치 추적용)
            getDamage(),
            Constants.STORM_DURATION
        );

        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[Storm] 폭풍 시전! 8초간 플레이어 추적 + 속도 50% 증가");
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Iterator<StormZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            StormZone zone = iterator.next();
            zone.update(delta);

            if (!zone.isActive()) {
                iterator.remove();
            }
        }
    }

    public List<StormZone> getActiveZones() {
        return activeZones;
    }
}
