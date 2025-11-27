package com.example.yugeup.game.skill;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.network.NetworkManager;

/**
 * 원소 스킬 기본 클래스
 *
 * 모든 원소 스킬은 이 클래스를 상속합니다.
 * 업그레이드 시스템과 원소별 특성을 관리합니다.
 * (PHASE_14~18에서 각 원소별 스킬 구현)
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class ElementalSkill extends Skill {
    protected ElementType element;      // 원소 타입
    protected int skillLevel;           // 스킬 레벨 (1~3)

    // 업그레이드 가능한 속성
    protected float damageMultiplier;   // 데미지 배율
    protected float rangeMultiplier;    // 범위 배율
    protected float cooldownReduction;  // 쿨타임 감소

    // 업그레이드 누적 보너스 (PHASE_19 재설계)
    protected int damageBonus;          // 데미지 추가 보너스 (고정값)
    protected float cooldownReductionBonus;  // 쿨타임 감소 누적 (배율)

    /**
     * 원소 스킬 생성자
     *
     * @param skillId 스킬 ID
     * @param name 스킬 이름
     * @param manaCost 마나 소모량
     * @param cooldown 쿨타임 (초)
     * @param baseDamage 기본 데미지
     * @param element 원소 타입
     * @param owner 스킬 소유자
     */
    public ElementalSkill(int skillId, String name, int manaCost, float cooldown, int baseDamage, ElementType element, Player owner) {
        super(skillId, name, manaCost, cooldown, owner);
        this.element = element;
        this.baseDamage = baseDamage;
        this.skillLevel = 1;

        // 초기 배율 설정
        this.damageMultiplier = 1.0f;
        this.rangeMultiplier = 1.0f;
        this.cooldownReduction = 0f;

        // 업그레이드 보너스 초기화 (PHASE_19)
        this.damageBonus = 0;
        this.cooldownReductionBonus = 0f;
    }

    /**
     * 스킬 업그레이드 (PHASE_19에서 구현)
     *
     * @param upgradeType 업그레이드 타입 (DAMAGE/COOLDOWN)
     */
    public void upgrade(UpgradeType upgradeType) {
        skillLevel++;

        switch (upgradeType) {
            case DAMAGE:
                damageMultiplier += 0.3f;  // 30% 증가
                break;
            case COOLDOWN:
                cooldownReduction += 0.2f;  // 20% 감소
                break;
        }

        System.out.println("[ElementalSkill] " + name + " 업그레이드: " + upgradeType + " (레벨 " + skillLevel + ")");
    }

    /**
     * 최종 데미지 계산 (업그레이드 적용)
     *
     * @return 업그레이드가 적용된 데미지
     */
    public int getDamage() {
        // 기본 데미지 * 배율 + 업그레이드 보너스
        return (int) (baseDamage * damageMultiplier) + damageBonus;
    }

    /**
     * 최종 쿨타임 계산 (업그레이드 적용)
     *
     * @return 업그레이드가 적용된 쿨타임
     */
    @Override
    public float getCooldown() {
        // 기본 쿨타임 * (1 - 기존감소) * (1 - 보너스감소)
        return cooldown * (1.0f - cooldownReduction) * (1.0f - cooldownReductionBonus);
    }

    /**
     * 데미지 보너스 추가 (PHASE_19)
     *
     * @param bonus 추가할 데미지
     */
    public void addDamageBonus(int bonus) {
        this.damageBonus += bonus;
        System.out.println("[ElementalSkill] " + name + " 데미지 보너스 +" + bonus + " (총 +" + damageBonus + ")");
    }

    /**
     * 쿨타임 감소 보너스 추가 (PHASE_19)
     *
     * @param reduction 감소율 (예: 0.1f = 10% 감소)
     */
    public void addCooldownReduction(float reduction) {
        this.cooldownReductionBonus += reduction;
        System.out.println("[ElementalSkill] " + name + " 쿨타임 감소 +" + (int)(reduction * 100) + "% (총 -" + (int)(cooldownReductionBonus * 100) + "%)");
    }

    /**
     * 최종 범위 배율 반환
     *
     * @return 범위 배율
     */
    public float getRangeMultiplier() {
        return rangeMultiplier;
    }

    /**
     * 스킬 시전 (각 원소별로 오버라이드)
     * PHASE_14~18에서 구현
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치
     */
    public abstract void cast(Player caster, Vector2 targetPosition);

    /**
     * 스킬 실행 (Skill 추상 메서드 구현)
     */
    @Override
    protected void use() {
        // 기본 구현: cast 호출
        // 실제 targetPosition은 서브클래스에서 결정
        System.out.println("[ElementalSkill] " + name + " 사용 (PHASE_14~18에서 구현)");
    }

    // ===== Getters =====

    public ElementType getElement() {
        return element;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getCooldownReduction() {
        return cooldownReduction;
    }

    /**
     * 네트워크로 스킬 시전을 알립니다.
     *
     * @param targetPosition 목표 위치
     */
    protected void sendSkillCastToNetwork(Vector2 targetPosition) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendSkillCast(skillId, targetPosition.x, targetPosition.y);
        }
    }

    /**
     * 업그레이드 타입
     */
    public enum UpgradeType {
        DAMAGE,      // 데미지 증가 (30%)
        COOLDOWN     // 쿨타임 감소 (20%)
    }
}
