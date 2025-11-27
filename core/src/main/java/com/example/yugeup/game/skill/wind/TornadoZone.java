package com.example.yugeup.game.skill.wind;

import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;

/**
 * 토네이도 지역 클래스
 *
 * 회전하는 바람 지역입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class TornadoZone extends SkillZone {

    /**
     * 토네이도 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param damagePerTick 틱당 데미지
     * @param duration 지속시간
     */
    public TornadoZone(float x, float y, int damagePerTick, float duration) {
        super(x, y, Constants.TORNADO_RADIUS, duration, damagePerTick, "tornado-loop");  // 애니메이션 이름 전달
    }

    /**
     * 주변 몬스터에게 데미지를 적용합니다.
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        super.applyDamageToNearbyMonsters();  // 부모 클래스 구현 사용
    }

    /**
     * 몬스터가 범위 내에 있는지 확인하고 끌어당김을 적용합니다.
     *
     * @param monster 확인할 몬스터
     */
    public void damageMonsterIfInRange(Monster monster) {
        if (isMonsterInRange(monster)) {
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
