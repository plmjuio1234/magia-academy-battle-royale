package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * 파이어볼 스킬 클래스
 *
 * 불 원소의 첫 번째 스킬입니다.
 * 선택한 방향으로 직선 발사체를 날립니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Fireball extends ElementalSkill {

    // 발사체 목록
    private transient List<FireballProjectile> activeProjectiles;

    /**
     * 파이어볼 생성자
     *
     * @param owner 스킬 소유자
     */
    public Fireball(Player owner) {
        super(5101, "파이어볼", Constants.FIREBALL_MANA_COST,
              Constants.FIREBALL_COOLDOWN, Constants.FIREBALL_DAMAGE,
              ElementType.FIRE, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 파이어볼을 시전합니다.
     *
     * 선택한 방향(targetPosition)으로 발사체를 생성합니다.
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

        // 발사 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 발사체 생성
        FireballProjectile projectile = new FireballProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            220f  // 발사 속도
        );

        activeProjectiles.add(projectile);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[Fireball] 파이어볼 시전! 방향: (" + direction.x + ", " + direction.y + ")");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 발사체 업데이트
        updateProjectiles(delta);
    }

    /**
     * 활성 발사체를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateProjectiles(float delta) {
        Iterator<FireballProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            FireballProjectile projectile = iterator.next();
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
    public List<FireballProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
