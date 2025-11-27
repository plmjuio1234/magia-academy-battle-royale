package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 불 원소 스킬 클래스
 *
 * 파이어볼, 플레임 웨이브, 인페르노 3가지 스킬을 제공합니다.
 * 높은 데미지와 화상 효과가 특징입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillFire extends Skill {

    /**
     * SkillFire를 생성합니다.
     *
     * @param owner 스킬 소유자
     */
    public SkillFire(Player owner) {
        super("Fire Skill", 10, 2.0f, owner);
        setDescription("불 원소 스킬 (PHASE_14에서 구현 예정)");
    }

    @Override
    protected void use() {
        // TODO: PHASE_14에서 구현
        System.out.println("[SkillFire] 스킬 사용 (미구현)");
    }
}
