package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 인페르노 스킬 클래스
 *
 * 불 원소의 세 번째 스킬입니다.
 * 플레이어 주변에 폭발 지역을 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Inferno extends ElementalSkill {

    // 활성 지역 목록
    private transient List<InfernoZone> activeZones;

    /**
     * 인페르노 생성자
     *
     * @param owner 스킬 소유자
     */
    public Inferno(Player owner) {
        super(5103, "인페르노", Constants.INFERNO_MANA_COST,
              Constants.INFERNO_COOLDOWN, 100,
              ElementType.FIRE, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 인페르노를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치 (사용되지 않음 - 플레이어 중심)
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

        // 인페르노 지역 생성 (플레이어 중심, 즉발 폭발)
        // 애니메이션 재생시간 계산 (18프레임 * 0.08초 = 약 1.44초)
        float animDuration = 18 * 0.08f;
        InfernoZone zone = new InfernoZone(
            caster.getX(),
            caster.getY(),
            getDamage(),
            animDuration  // 애니메이션 지속시간
        );

        activeZones.add(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화 (고정 Zone)
        Vector2 zonePos = new Vector2(caster.getX(), caster.getY());
        sendFixedZoneSkillToNetwork(zonePos, Constants.INFERNO_RANGE, animDuration);

        System.out.println("[Inferno] 인페르노 시전! 위치: (" + caster.getX() + ", " + caster.getY() + ")");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 지역 업데이트
        updateZones(delta);
    }

    /**
     * 활성 지역을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateZones(float delta) {
        Iterator<InfernoZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            InfernoZone zone = iterator.next();
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
    public List<InfernoZone> getActiveZones() {
        return activeZones;
    }
}
