package com.example.yugeup.game.monster;

import com.example.yugeup.utils.Constants;

/**
 * 박쥐 몬스터
 *
 * 낮은 체력과 매우 빠른 이동 속도를 가진 몬스터입니다.
 * 빠른 공격 패턴으로 플레이어를 괴롭힙니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Bat extends Monster {

    /**
     * 박쥐를 생성합니다.
     */
    public Bat() {
        super();
        initialize(MonsterType.BAT);

        // 능력치 설정
        this.maxHealth = Constants.BAT_HP;
        this.currentHealth = Constants.BAT_HP;
        this.attack = Constants.BAT_ATK;
        this.speed = Constants.BAT_SPEED;

        // 크기 설정 (실제 스프라이트 크기에 맞춤)
        this.width = 32f;
        this.height = 32f;
    }

    /**
     * 특수 능력: 급습
     *
     * 빠르게 플레이어에게 돌진하여 공격합니다.
     */
    public void dash() {
        // TODO: 급습 공격 구현
        // 짧은 시간 동안 속도 2배 증가
    }
}
