package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 체인 라이트닝 스킬 클래스
 *
 * 번개 원소의 두 번째 스킬입니다.
 * 적을 타격한 후 주변 적으로 연쇄합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ChainLightning extends ElementalSkill {

    // 활성 발사체 목록
    private transient List<ChainLightningProjectile> activeProjectiles;

    /**
     * 체인 라이트닝 생성자
     *
     * @param owner 스킬 소유자
     */
    public ChainLightning(Player owner) {
        super(5402, "체인 라이트닝", Constants.CHAIN_LIGHTNING_MANA_COST,
              Constants.CHAIN_LIGHTNING_COOLDOWN, Constants.CHAIN_LIGHTNING_DAMAGE,
              ElementType.LIGHTNING, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 체인 라이트닝을 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치
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

        // 시전 위치 및 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 체인 라이트닝 발사체 생성
        ChainLightningProjectile projectile = new ChainLightningProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            260f  // 중간 속도
        );

        // 연쇄 공격 설정 (최대 4번 연쇄)
        projectile.setMaxPierceCount(Constants.CHAIN_LIGHTNING_MAX_JUMPS);

        activeProjectiles.add(projectile);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[ChainLightning] 체인 라이트닝 시전! 방향: (" + direction.x + ", " + direction.y + ")");
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
        Iterator<ChainLightningProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            ChainLightningProjectile projectile = iterator.next();
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
    public List<ChainLightningProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
