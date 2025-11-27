package com.example.yugeup.game.monster;

import com.example.yugeup.utils.Constants;

/**
 * 고스트 몬스터
 *
 * 빠른 이동 속도와 투명화 능력을 가진 몬스터입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Ghost extends Monster {

    /**
     * 고스트를 생성합니다.
     */
    public Ghost() {
        super();
        initialize(MonsterType.GHOST);

        // 능력치 설정
        this.maxHealth = Constants.GHOST_HP;
        this.currentHealth = Constants.GHOST_HP;
        this.attack = Constants.GHOST_ATK;
        this.speed = Constants.GHOST_SPEED;

        // 크기 설정 (실제 스프라이트 크기에 맞춤)
        this.width = 32f;
        this.height = 32f;
    }

    /**
     * 특수 능력: 투명화
     *
     * 잠시 동안 투명해져서 무적 상태가 됩니다.
     */
    public void useInvisibility() {
        // TODO: 투명화 버프 추가
        // Buff invisibilityBuff = new InvisibilityBuff(3.0f);
        // addBuff(invisibilityBuff);
    }
}
