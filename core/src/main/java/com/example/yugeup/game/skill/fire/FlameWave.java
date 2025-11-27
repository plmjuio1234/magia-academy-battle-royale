package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 플레임 웨이브 스킬 클래스
 *
 * 불 원소의 두 번째 스킬입니다.
 * 선택한 방향으로 부채꼴 범위의 불 지역을 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FlameWave extends ElementalSkill {

    // 활성 지역 목록
    private transient List<FlameWaveZone> activeZones;

    /**
     * 플레임 웨이브 생성자
     *
     * @param owner 스킬 소유자
     */
    public FlameWave(Player owner) {
        super(5102, "플레임 웨이브", Constants.FLAME_WAVE_MANA_COST,
              Constants.FLAME_WAVE_COOLDOWN, 50,
              ElementType.FIRE, owner);
        this.activeZones = new ArrayList<>();
    }

    /**
     * 플레임 웨이브를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
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

        // 시전 위치
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 플레임 웨이브 지역 생성
        FlameWaveZone zone = new FlameWaveZone(
            casterPos.x,
            casterPos.y,
            direction,
            getDamage(),
            1.5f  // 지속 시간
        );

        activeZones.add(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[FlameWave] 플레임 웨이브 시전! 방향: (" + direction.x + ", " + direction.y + ")");
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
        Iterator<FlameWaveZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            FlameWaveZone zone = iterator.next();
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
    public List<FlameWaveZone> getActiveZones() {
        return activeZones;
    }
}
