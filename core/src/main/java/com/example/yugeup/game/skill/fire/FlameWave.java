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
 * 플레임 웨이브 스킬 클래스
 *
 * 불 원소의 두 번째 스킬입니다.
 * 선택한 방향으로 투사체가 날아가며 지나가는 적에게 도트딜을 줍니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FlameWave extends ElementalSkill {

    // 활성 투사체 목록
    private transient List<FlameWaveProjectile> activeProjectiles;

    /**
     * 플레임 웨이브 생성자
     *
     * @param owner 스킬 소유자
     */
    public FlameWave(Player owner) {
        super(5102, "플레임 웨이브", Constants.FLAME_WAVE_MANA_COST,
              Constants.FLAME_WAVE_COOLDOWN, Constants.FLAME_WAVE_DAMAGE,
              ElementType.FIRE, owner);
        this.activeProjectiles = new ArrayList<>();
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

        // 플레임 웨이브 투사체 생성 (도트딜)
        FlameWaveProjectile projectile = new FlameWaveProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            Constants.FLAME_WAVE_SPEED,
            Constants.FLAME_WAVE_RANGE
        );

        activeProjectiles.add(projectile);

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

        // 투사체 업데이트
        updateProjectiles(delta);
    }

    /**
     * 활성 투사체를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateProjectiles(float delta) {
        Iterator<FlameWaveProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            FlameWaveProjectile projectile = iterator.next();
            projectile.update(delta);

            if (!projectile.isAlive()) {
                iterator.remove();
            }
        }
    }

    /**
     * 활성 투사체 목록을 반환합니다.
     *
     * @return 투사체 리스트
     */
    public List<FlameWaveProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
