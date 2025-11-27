package com.example.yugeup.game.skill;

import java.util.HashMap;
import java.util.Map;

/**
 * 스킬 관리 클래스
 *
 * 플레이어의 모든 스킬을 관리합니다.
 * 스킬 등록, 사용, 업그레이드 등을 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillManager {

    // 스킬 목록 (스킬ID -> Skill 객체)
    private Map<Integer, Skill> skills;

    /**
     * SkillManager를 생성합니다.
     */
    public SkillManager() {
        this.skills = new HashMap<>();
    }

    /**
     * 스킬을 등록합니다.
     *
     * @param skill 등록할 스킬
     */
    public void registerSkill(Skill skill) {
        // TODO: PHASE_12에서 구현
    }

    /**
     * 스킬을 사용합니다.
     *
     * @param skillId 스킬 ID
     * @param casterX 시전자 X 좌표
     * @param casterY 시전자 Y 좌표
     * @param targetX 타겟 X 좌표
     * @param targetY 타겟 Y 좌표
     */
    public void useSkill(int skillId, float casterX, float casterY, float targetX, float targetY) {
        // TODO: PHASE_12에서 구현
    }

    /**
     * 모든 스킬을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_12에서 구현
    }

    /**
     * 스킬을 업그레이드합니다.
     *
     * @param skillId 업그레이드할 스킬 ID
     */
    public void upgradeSkill(int skillId) {
        // TODO: PHASE_19에서 구현
    }

    // Getter
    public Map<Integer, Skill> getSkills() { return skills; }
}
