package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 토네이도 스킬 클래스
 *
 * 바람 원소의 두 번째 스킬입니다.
 * 보는 방향으로 빠르게 날아가는 토네이도를 발사합니다.
 * 사거리 500, 속도 200, 히트박스 18x18
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Tornado extends ElementalSkill {

    // 활성 투사체 목록
    private transient List<TornadoProjectile> activeProjectiles;

    /**
     * 토네이도 생성자
     *
     * @param owner 스킬 소유자
     */
    public Tornado(Player owner) {
        super(5302, "토네이도", Constants.TORNADO_MANA_COST,
              Constants.TORNADO_COOLDOWN, Constants.TORNADO_DAMAGE,
              ElementType.WIND, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 토네이도를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        // 시전 위치 및 방향 계산
        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 토네이도 투사체 생성
        TornadoProjectile projectile = new TornadoProjectile(
            casterPos,
            direction.x,
            direction.y,
            getDamage(),
            Constants.TORNADO_SPEED,
            Constants.TORNADO_RANGE
        );

        activeProjectiles.add(projectile);
        currentCooldown = getCooldown();

        // 네트워크 동기화
        sendSkillCastToNetwork(targetPosition);

        System.out.println("[Tornado] 토네이도 시전! 방향: (" + direction.x + ", " + direction.y + ")");
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        Iterator<TornadoProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            TornadoProjectile projectile = iterator.next();
            projectile.update(delta);

            if (!projectile.isAlive()) {
                projectile.dispose();
                iterator.remove();
            }
        }
    }

    /**
     * 활성 투사체 목록을 반환합니다.
     *
     * @return 투사체 리스트
     */
    public List<TornadoProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
