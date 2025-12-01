package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 어스 스파이크 스킬 클래스
 *
 * 땅 원소의 두 번째 스킬입니다.
 * 보는 방향으로 바닥에서 가시가 솟아오릅니다.
 * 사거리 200, 속도 100, 히트박스 24x16
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class EarthSpike extends ElementalSkill {

    // 활성 Zone 목록
    private transient List<EarthSpikeZone> activeZones;

    // 몬스터 목록 (충돌 감지용)
    private transient List<Monster> monsterList;

    /**
     * 어스 스파이크 생성자
     *
     * @param owner 스킬 소유자
     */
    public EarthSpike(Player owner) {
        super(5002, "어스 스파이크", Constants.EARTH_SPIKE_MANA_COST,
              Constants.EARTH_SPIKE_COOLDOWN, Constants.EARTH_SPIKE_DAMAGE,
              ElementType.EARTH, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 몬스터 목록 설정
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 어스 스파이크를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) {
            return;
        }

        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        // 시전 위치 및 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 어스 스파이크 Zone 생성
        EarthSpikeZone zone = new EarthSpikeZone(
            casterPos,
            direction.x,
            direction.y,
            getDamage()
        );
        zone.setMonsterList(monsterList);

        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화 (이동형 Zone)
        sendMovingZoneSkillToNetwork(casterPos, targetPosition,
            Constants.EARTH_SPIKE_SPEED,
            Constants.EARTH_SPIKE_HITBOX_WIDTH,
            Constants.EARTH_SPIKE_RANGE);

        System.out.println("[EarthSpike] 어스 스파이크 시전! 방향: (" + direction.x + ", " + direction.y + ")");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        Iterator<EarthSpikeZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            EarthSpikeZone zone = iterator.next();
            zone.update(delta);

            if (!zone.isActive()) {
                iterator.remove();
            }
        }
    }

    /**
     * 활성 Zone 목록을 반환합니다.
     *
     * @return Zone 리스트
     */
    public List<EarthSpikeZone> getActiveZones() {
        return activeZones;
    }
}
