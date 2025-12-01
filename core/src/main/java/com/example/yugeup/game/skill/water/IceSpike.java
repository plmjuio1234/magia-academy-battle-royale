package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 아이스 스파이크 스킬 클래스
 *
 * 물 원소의 두 번째 스킬입니다.
 * 관통 공격이 가능한 얼음 창을 발사합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class IceSpike extends ElementalSkill {

    // 활성 발사체 목록
    private transient List<IceSpikeProjectile> activeProjectiles;

    public IceSpike(Player owner) {
        super(5202, "아이스 스파이크", Constants.ICE_SPIKE_MANA_COST,
              Constants.ICE_SPIKE_COOLDOWN, Constants.ICE_SPIKE_DAMAGE,
              ElementType.WATER, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        // 시전 위치 및 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 기준 각도 계산
        float baseAngle = direction.angleDeg();

        // 3방향으로 얼음 가시 발사 (중앙, 왼쪽 20도, 오른쪽 20도)
        float[] angles = {
            baseAngle,                                    // 중앙
            baseAngle - Constants.ICE_SPIKE_ANGLE_SPREAD, // 왼쪽 20도
            baseAngle + Constants.ICE_SPIKE_ANGLE_SPREAD  // 오른쪽 20도
        };

        for (float angle : angles) {
            // 각도를 방향 벡터로 변환
            Vector2 dir = new Vector2(1, 0).setAngleDeg(angle);

            IceSpikeProjectile projectile = new IceSpikeProjectile(
                casterPos.cpy(),
                dir.x,
                dir.y,
                getDamage(),
                Constants.ICE_SPIKE_SPEED,
                Constants.ICE_SPIKE_RANGE
            );

            activeProjectiles.add(projectile);
        }

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[IceSpike] 아이스 스파이크 3방향 시전! 기준 각도: " + baseAngle);
    }

    /**
     * 스킬 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);
        updateProjectiles(delta);
    }

    /**
     * 발사체 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateProjectiles(float delta) {
        Iterator<IceSpikeProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            IceSpikeProjectile projectile = iterator.next();
            projectile.update(delta);

            if (!projectile.isAlive()) {
                projectile.dispose();
                iterator.remove();
            }
        }
    }

    /**
     * 활성 발사체 목록을 반환합니다.
     *
     * @return 발사체 리스트
     */
    public List<IceSpikeProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
