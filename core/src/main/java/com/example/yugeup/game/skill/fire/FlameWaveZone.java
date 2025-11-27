package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillZone;
import java.util.List;

/**
 * 플레임 웨이브 지역 클래스
 *
 * 부채꼴 모양으로 확장되는 불 지역입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FlameWaveZone extends SkillZone {

    // 발사 방향
    private Vector2 direction;

    // 부채꼴 각도
    private float spreadAngle;  // 90도 부채꼴

    /**
     * 플레임 웨이브 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param direction 발사 방향
     * @param damagePerTick 틱당 데미지
     * @param duration 지속시간
     */
    public FlameWaveZone(float x, float y, Vector2 direction, int damagePerTick, float duration) {
        super(x, y, 150f, duration, damagePerTick, "flame_wave-loop");  // 애니메이션 이름 전달
        this.direction = direction.cpy().nor();
        this.spreadAngle = 90f;  // 90도 부채꼴
    }

    /**
     * 주변 몬스터에게 데미지를 적용합니다.
     *
     * 부채꼴 범위 내의 몬스터만 데미지를 입습니다.
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        // 부모 클래스의 구현 사용 (isMonsterInRange 체크)
        super.applyDamageToNearbyMonsters();
    }

    /**
     * 부채꼴 범위 내에 몬스터가 있는지 확인합니다.
     *
     * @param monster 확인할 몬스터
     * @return 범위 내에 있으면 true
     */
    public boolean isMonsterInCone(Monster monster) {
        if (monster == null || monster.isDead()) {
            return false;
        }

        // 몬스터와의 상대 위치 계산
        float dx = monster.getX() - position.x;
        float dy = monster.getY() - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 범위 내인지 확인
        if (distance > radius) {
            return false;
        }

        // 부채꼴 범위 내인지 확인
        Vector2 monsterDir = new Vector2(dx, dy).nor();
        float angle = (float) Math.acos(direction.dot(monsterDir)) * 180 / (float) Math.PI;

        return angle <= spreadAngle / 2f;
    }

    /**
     * 몬스터에게 데미지를 적용합니다.
     *
     * @param monster 데미지를 받을 몬스터
     */
    public void damageMonsterIfInCone(Monster monster) {
        if (isMonsterInCone(monster)) {
            damageMonster(monster);
        }
    }

    /**
     * 종료 처리
     */
    @Override
    protected void onEnd() {
        // 특별한 처리 없음
    }
}
