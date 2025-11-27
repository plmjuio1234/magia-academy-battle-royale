package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 스킬 기본 클래스
 *
 * 모든 스킬의 공통 속성과 메서드를 정의합니다.
 * 매직 미사일, 원소 스킬들이 이 클래스를 상속받습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class Skill {

    // 스킬 정보
    protected int skillId;         // 스킬 고유 ID
    protected String name;
    protected String description;
    protected int manaCost;
    protected int baseDamage;      // 기본 데미지

    // 쿨타임
    protected float cooldown;
    protected float currentCooldown;

    // 활성화 상태
    protected boolean isEnabled = true;

    // 소유자
    protected Player owner;

    /**
     * Skill 생성자
     *
     * @param skillId 스킬 ID
     * @param name 스킬 이름
     * @param manaCost 마나 소모량
     * @param cooldown 쿨타임 (초)
     * @param owner 스킬 소유자
     */
    public Skill(int skillId, String name, int manaCost, float cooldown, Player owner) {
        this.skillId = skillId;
        this.name = name;
        this.manaCost = manaCost;
        this.baseDamage = 0;
        this.cooldown = cooldown;
        this.currentCooldown = 0f;
        this.owner = owner;
    }

    /**
     * Skill 생성자 (하위 호환용, skillId 없이)
     *
     * @param name 스킬 이름
     * @param manaCost 마나 소모량
     * @param cooldown 쿨타임 (초)
     * @param owner 스킬 소유자
     */
    public Skill(String name, int manaCost, float cooldown, Player owner) {
        this(0, name, manaCost, cooldown, owner);
    }

    /**
     * 업데이트 (매 프레임)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 쿨타임 감소만 수행
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }

        // 자동 발동은 각 스킬에서 오버라이드하여 구현
        // (예: MagicMissile은 자동 발동, ElementalSkill은 수동 발동)
    }

    /**
     * 스킬 사용 시도
     */
    protected void tryUse() {
        // 마나 확인
        if (!owner.getStats().consumeMana(manaCost)) {
            return;
        }

        // 스킬 실행
        use();

        // 쿨타임 시작
        currentCooldown = cooldown;
    }

    /**
     * 스킬 실행 (서브클래스에서 구현)
     */
    protected abstract void use();

    /**
     * 스킬 준비 상태
     *
     * @return 사용 가능 여부
     */
    public boolean isReady() {
        return currentCooldown <= 0;
    }

    /**
     * 쿨타임 비율 (0.0 ~ 1.0)
     *
     * @return 쿨타임 진행 비율 (0.0 = 쿨타임 중, 1.0 = 준비 완료)
     */
    public float getCooldownRatio() {
        if (cooldown == 0) return 1.0f;
        return 1.0f - (currentCooldown / cooldown);
    }

    // ===== Getters & Setters =====

    public int getSkillId() { return skillId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getManaCost() { return manaCost; }
    public int getBaseDamage() { return baseDamage; }
    public float getCooldown() { return cooldown; }
    public float getCurrentCooldown() { return currentCooldown; }
    public boolean isEnabled() { return isEnabled; }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void toggleEnabled() {
        this.isEnabled = !this.isEnabled;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }
}
