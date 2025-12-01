package com.example.yugeup.game.skill.lightning;

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
 * 썬더 스톰 스킬 클래스
 *
 * 번개 원소의 세 번째 스킬입니다.
 * 보는 방향으로 이동하는 비구름과 그 아래 번개를 생성합니다.
 * 구름: 속도 20, 사거리 200
 * 번개: 구름 아래 60칸, 54x54 히트박스
 * 각도 고정.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ThunderStorm extends ElementalSkill {

    // 활성 지역 목록
    private transient List<ThunderStormZone> activeZones;

    // 몬스터 목록 참조
    private transient List<Monster> monsterList;

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
     * 몬스터 목록 설정
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 썬더 스톰을 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치 (방향 계산용)
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

        // 썬더 스톰 지역 생성 (플레이어 위치에서 시작)
        ThunderStormZone zone = new ThunderStormZone(
            casterPos.x,
            casterPos.y,
            direction.x,
            direction.y,
            getDamage()
        );
        zone.setMonsterList(monsterList);
        activeZones.add(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[ThunderStorm] 썬더 스톰 시전! 방향: (" + direction.x + ", " + direction.y + ")");
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
