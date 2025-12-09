package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 바람 원소 스킬 클래스
 *
 * 바람 원소 기반 스킬을 제공합니다.
 * 넓은 범위와 빠른 발사 속도가 특징입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillWind extends Skill {

    /**
     * SkillWind를 생성합니다.
     *
     * @param owner 스킬 소유자
     */
    public SkillWind(Player owner) {
        super("Wind Skill", 10, 2.0f, owner);
        setDescription("바람 원소 스킬 (PHASE_16에서 구현 예정)");
    }

    @Override
    protected void use() {
        // TODO: PHASE_16에서 구현
        System.out.println("[SkillWind] 스킬 사용 (미구현)");
    }
}
