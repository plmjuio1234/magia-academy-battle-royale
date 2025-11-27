package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillZone;

/**
 * 인페르노 지역 클래스
 *
 * 플레이어 중심의 원형 폭발 지역입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class InfernoZone extends SkillZone {

    /**
     * 인페르노 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param damagePerTick 틱당 데미지
     * @param duration 지속시간
     */
    public InfernoZone(float x, float y, int damagePerTick, float duration) {
        super(x, y, 200f, duration, damagePerTick, "inferno");  // 200px 반경, 애니메이션 이름 전달
    }

    /**
     * 주변 몬스터에게 데미지를 적용합니다.
     *
     * GameScreen에서 호출할 때 몬스터 목록을 받아야 함
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        // 부모 클래스의 구현 사용
        super.applyDamageToNearbyMonsters();
    }

    /**
     * 몬스터가 범위 내에 있는지 확인하고 데미지를 적용합니다.
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
