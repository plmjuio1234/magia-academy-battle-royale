package com.example.yugeup.game.skill.earth;

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
 * 록 스매시 스킬 클래스
 *
 * 땅 원소의 첫 번째 스킬입니다.
 * 바라보는 방향의 가장 가까운 적 위에 돌을 떨어뜨립니다.
 * 바닥에 떨어질 때 주변 48x48 범위에 피해를 줍니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RockSmash extends ElementalSkill {

    // 활성 Zone 목록
    private transient List<RockSmashZone> activeZones;

    // 몬스터 목록 (타겟팅용)
    private transient List<Monster> monsterList;

    /**
     * 록 스매시 생성자
     *
     * @param owner 스킬 소유자
     */
    public RockSmash(Player owner) {
        super(5001, "록 스매시", Constants.ROCK_SMASH_MANA_COST,
              Constants.ROCK_SMASH_COOLDOWN, Constants.ROCK_SMASH_DAMAGE,
              ElementType.EARTH, owner);
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
     * 록 스매시를 시전합니다.
     *
     * @param caster 시전자
     * @param targetPosition 목표 방향 좌표
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (!isReady()) {
            return;
        }

        if (!caster.getStats().consumeMana(getManaCost())) {
            return;
        }

        Vector2 casterPos = new Vector2(caster.getX(), caster.getY());
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();

        // 바라보는 방향의 가장 가까운 적 찾기
        Monster target = findNearestMonsterInDirection(casterPos, direction);

        Vector2 dropPosition;
        if (target != null) {
            // 타겟 위에 돌 떨어뜨리기
            dropPosition = new Vector2(target.getX(), target.getY());
            System.out.println("[RockSmash] 타겟 발견! 몬스터 " + target.getMonsterId() + " 위치에 돌 투하");
        } else {
            // 타겟 없으면 바라보는 방향 100픽셀 앞에 떨어뜨리기
            dropPosition = casterPos.cpy().add(direction.scl(100f));
            System.out.println("[RockSmash] 타겟 없음. 전방 100픽셀 위치에 돌 투하");
        }

        // 록 스매시 Zone 생성
        RockSmashZone zone = new RockSmashZone(
            dropPosition.x,
            dropPosition.y,
            getDamage()
        );
        zone.setMonsterList(monsterList);

        activeZones.add(zone);
        currentCooldown = getCooldown();

        // 네트워크 동기화 (고정 Zone)
        float totalDuration = Constants.ROCK_SMASH_FALL_DURATION + Constants.ROCK_SMASH_LINGER_DURATION;
        sendFixedZoneSkillToNetwork(dropPosition, Constants.ROCK_SMASH_HITBOX_SIZE, totalDuration);

        System.out.println("[RockSmash] 록 스매시 시전! 위치: (" + dropPosition.x + ", " + dropPosition.y + ")");
    }

    /**
     * 바라보는 방향의 가장 가까운 몬스터를 찾습니다.
     *
     * @param casterPos 시전자 위치
     * @param direction 바라보는 방향
     * @return 가장 가까운 몬스터 (없으면 null)
     */
    private Monster findNearestMonsterInDirection(Vector2 casterPos, Vector2 direction) {
        if (monsterList == null || monsterList.isEmpty()) {
            return null;
        }

        Monster nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;

            // 거리 계산
            float dx = monster.getX() - casterPos.x;
            float dy = monster.getY() - casterPos.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 타겟팅 범위 내인지 확인
            if (distance > Constants.ROCK_SMASH_TARGETING_RANGE) continue;

            // 방향 확인 (내적으로 60도 이내인지)
            Vector2 toMonster = new Vector2(dx, dy).nor();
            float dot = direction.dot(toMonster);

            // dot > 0.5 = 약 60도 이내
            if (dot > 0.5f && distance < nearestDistance) {
                nearest = monster;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        Iterator<RockSmashZone> iterator = activeZones.iterator();
        while (iterator.hasNext()) {
            RockSmashZone zone = iterator.next();
            zone.update(delta);

            if (!zone.isActive()) {
                iterator.remove();
            }
        }
    }

    /**
     * 활성 Zone 목록을 반환합니다.
     *
     * @return Zone 리스트
     */
    public List<RockSmashZone> getActiveZones() {
        return activeZones;
    }
}
