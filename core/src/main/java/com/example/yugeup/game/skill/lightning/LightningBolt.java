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
 * 라이트닝 볼트 스킬 클래스
 *
 * 번개 원소의 첫 번째 스킬입니다.
 * 빠른 속도로 번개를 발사합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LightningBolt extends ElementalSkill {

    // 활성 발사체 목록
    private transient List<LightningBoltProjectile> activeProjectiles;

    /**
     * 라이트닝 볼트 생성자
     *
     * @param owner 스킬 소유자
     */
    public LightningBolt(Player owner) {
        super(5401, "라이트닝 볼트", Constants.LIGHTNING_BOLT_MANA_COST,
              Constants.LIGHTNING_BOLT_COOLDOWN, Constants.LIGHTNING_BOLT_DAMAGE,
              ElementType.LIGHTNING, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 라이트닝 볼트를 시전합니다.
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

        // 라이트닝 볼트 발사체 생성
        LightningBoltProjectile projectile = new LightningBoltProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            280f  // 빠른 속도
        );

        activeProjectiles.add(projectile);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[LightningBolt] 라이트닝 볼트 시전! 방향: (" + direction.x + ", " + direction.y + ")");
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
        Iterator<LightningBoltProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            LightningBoltProjectile projectile = iterator.next();
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
    public List<LightningBoltProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
