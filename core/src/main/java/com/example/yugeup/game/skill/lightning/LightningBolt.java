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
 * 라이트닝 볼트 스킬 클래스
 *
 * 번개 원소의 첫 번째 스킬입니다.
 * 400 사거리 내 가장 가까운 적 위에 낙뢰를 떨어뜨립니다.
 * 파이어볼 대비 데미지가 30% 낮음.
 * 각도 고정.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LightningBolt extends ElementalSkill {

    // 활성 지역 목록
    private transient List<LightningBoltZone> activeZones;

    // 몬스터 목록 참조 (타겟팅용)
    private transient List<Monster> monsterList;

    /**
     * 라이트닝 볼트 생성자
     *
     * @param owner 스킬 소유자
     */
    public LightningBolt(Player owner) {
        super(5401, "라이트닝 볼트", Constants.LIGHTNING_BOLT_MANA_COST,
              Constants.LIGHTNING_BOLT_COOLDOWN, Constants.LIGHTNING_BOLT_DAMAGE,
              ElementType.LIGHTNING, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 몬스터 목록 설정 (타겟팅용)
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 라이트닝 볼트를 시전합니다.
     * 사용자가 터치한 위치에 즉시 낙뢰를 떨어뜨립니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치 (사용자 터치 위치)
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) {
            return;
        }

        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        // 낙뢰 생성 (사용자가 터치한 위치 그대로)
        LightningBoltZone zone = new LightningBoltZone(
            targetPosition.x,
            targetPosition.y,
            getDamage()
        );
        zone.setMonsterList(monsterList);
        activeZones.add(zone);

        System.out.println("[LightningBolt] 라이트닝 볼트 시전! 목표 위치: (" + targetPosition.x + ", " + targetPosition.y + ")");

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화 (고정 Zone)
        // 번개 애니메이션 지속시간 (약 0.5초)
        float animDuration = 0.5f;
        sendFixedZoneSkillToNetwork(targetPosition, Constants.LIGHTNING_BOLT_HITBOX_SIZE, animDuration);
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
        Iterator<LightningBoltZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            LightningBoltZone zone = iterator.next();
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
    public List<LightningBoltZone> getActiveZones() {
        return activeZones;
    }
}
