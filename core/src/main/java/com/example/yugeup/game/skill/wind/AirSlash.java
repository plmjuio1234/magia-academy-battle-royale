package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 에어 슬래시 스킬 클래스
 *
 * 바람 원소의 첫 번째 스킬입니다.
 * 보는 방향 24칸 이내의 가까운 적들에게 근접 공격합니다.
 * 아무도 맞지 않았다면 검기를 발사합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class AirSlash extends ElementalSkill {

    // 발사체 목록 (근접 실패 시 발사)
    private transient List<AirSlashProjectile> activeProjectiles;

    // 몬스터 목록 (근접 공격용)
    private transient List<Monster> monsterList;

    /**
     * 에어 슬래시 생성자
     *
     * @param owner 스킬 소유자
     */
    public AirSlash(Player owner) {
        super(5301, "에어 슬래시", Constants.AIR_SLASH_MANA_COST,
              Constants.AIR_SLASH_COOLDOWN, Constants.AIR_SLASH_DAMAGE,
              ElementType.WIND, owner);
        this.activeProjectiles = new ArrayList<>();
    }

    /**
     * 몬스터 목록 설정 (근접 공격 판정용)
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 에어 슬래시를 시전합니다.
     * 근접 범위 내 적이 있으면 근접 공격, 없으면 검기 발사
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) return;
        if (!caster.getStats().consumeMana(getManaCost())) return;

        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 근접 범위 내 적 찾기 (보는 방향 24칸 이내)
        boolean meleeHit = tryMeleeAttack(casterPos, direction);

        if (!meleeHit) {
            // 근접 실패 시 검기 발사
            AirSlashProjectile projectile = new AirSlashProjectile(
                casterPos,
                direction.x,
                direction.y,
                getDamage(),
                Constants.AIR_SLASH_SPEED,
                Constants.AIR_SLASH_RANGE
            );
            activeProjectiles.add(projectile);
            System.out.println("[AirSlash] 근접 실패! 검기 발사!");
        } else {
            System.out.println("[AirSlash] 근접 공격 성공!");
        }

        currentCooldown = getCooldown();

        // 네트워크 동기화 (확장 버전)
        float lifetime = Constants.AIR_SLASH_RANGE / Constants.AIR_SLASH_SPEED;
        sendProjectileSkillToNetwork(casterPos, targetPosition,
            Constants.AIR_SLASH_SPEED, Constants.AIR_SLASH_HITBOX_WIDTH, lifetime);
    }

    /**
     * 근접 공격 시도
     *
     * @param casterPos 시전자 위치
     * @param direction 공격 방향
     * @return 적중 여부
     */
    private boolean tryMeleeAttack(Vector2 casterPos, Vector2 direction) {
        if (monsterList == null || monsterList.isEmpty()) {
            return false;
        }

        boolean hitAny = false;
        NetworkManager nm = NetworkManager.getInstance();

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;

            // 몬스터까지의 거리 및 방향 계산
            float dx = monster.getX() - casterPos.x;
            float dy = monster.getY() - casterPos.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 24칸 이내인지 확인
            if (distance > Constants.AIR_SLASH_MELEE_RANGE) continue;

            // 보는 방향인지 확인 (내적 > 0 = 앞쪽)
            Vector2 toMonster = new Vector2(dx, dy).nor();
            float dot = direction.dot(toMonster);
            if (dot < 0.5f) continue;  // 대략 60도 이내의 적만

            // 근접 공격 적중!
            if (nm != null) {
                nm.sendAttackMessage(monster.getMonsterId(), getDamage(), casterPos.x, casterPos.y);
            }
            hitAny = true;
            System.out.println("[AirSlash] 근접 타격! 몬스터 " + monster.getMonsterId() + " 거리: " + distance);
        }

        return hitAny;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Iterator<AirSlashProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            AirSlashProjectile projectile = iterator.next();
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
    public List<AirSlashProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }
}
