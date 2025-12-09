package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.game.buff.DamageReductionBuff;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 스톤 실드 스킬 클래스
 *
 * 땅 원소의 세 번째 스킬입니다.
 * 8초 동안 피해를 50% 덜 받습니다.
 * start 애니메이션 후 7초간 loop 애니메이션
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StoneShield extends ElementalSkill {

    // 활성 지역 목록 (시각 이펙트용)
    private transient List<StoneShieldZone> activeZones;

    /**
     * 스톤 실드 생성자
     *
     * @param owner 스킬 소유자
     */
    public StoneShield(Player owner) {
        super(5003, "스톤 실드", Constants.STONE_SHIELD_MANA_COST,
              Constants.STONE_SHIELD_COOLDOWN, 0,
              ElementType.EARTH, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 스톤 실드를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치 (사용되지 않음)
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) {
            return;
        }

        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        // 피해 감소 버프 적용 (50% 감소)
        caster.addBuff(new DamageReductionBuff(
            Constants.STONE_SHIELD_DURATION,
            Constants.STONE_SHIELD_DAMAGE_REDUCTION
        ));

        // 시각 이펙트 존 생성
        StoneShieldZone zone = new StoneShieldZone(
            caster,
            Constants.STONE_SHIELD_DURATION
        );
        activeZones.add(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화 (플레이어 추적형 Zone)
        // 스톤 실드는 크기가 플레이어와 동일 (고정 크기 사용)
        sendPlayerFollowZoneSkillToNetwork(64f, Constants.STONE_SHIELD_DURATION);

        System.out.println("[StoneShield] 스톤 실드 시전! 8초간 피해 50% 감소");
    }

    /**
     * 스킬 업데이트
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
        Iterator<StoneShieldZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            StoneShieldZone zone = iterator.next();
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
    public List<StoneShieldZone> getActiveZones() {
        return activeZones;
    }
}
