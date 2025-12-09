package com.example.yugeup.game.monster;

import com.example.yugeup.utils.Constants;

/**
 * 골렘 몬스터
 *
 * 높은 체력과 공격력을 가진 보스 몬스터입니다.
 * 느린 이동 속도가 단점입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Golem extends Monster {

    /**
     * 골렘을 생성합니다.
     */
    public Golem() {
        super();
        initialize(MonsterType.GOLEM);

        // 능력치 설정
        this.maxHealth = Constants.GOLEM_HP;
        this.currentHealth = Constants.GOLEM_HP;
        this.attack = Constants.GOLEM_ATK;
        this.speed = Constants.GOLEM_SPEED;

        // 크기 설정 (2배 증가)
        this.width = Constants.GOLEM_SIZE;   // 32 → 64 (2배)
        this.height = Constants.GOLEM_SIZE;  // 32 → 64 (2배)
    }

    /**
     * 특수 능력: 강타
     *
     * 강력한 일격을 가하여 2배의 데미지를 입힙니다.
     */
    public void heavyStrike() {
        // TODO: 강타 공격 구현
        // 현재 공격력의 2배 데미지
    }
}
