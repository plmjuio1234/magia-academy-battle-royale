package com.magicbr.game.systems.skills;

import com.magicbr.game.entities.Player;

/**
 * 모든 스킬의 베이스 추상 클래스
 * 각 원소별 스킬은 이 클래스를 상속받아 구현
 */
public abstract class Skill {
    // 기본 스킬 정보
    protected int skillId;
    protected String skillName;
    protected int baseDamage;
    protected int manaCost;
    protected float cooldown;          // 전체 쿨다운 시간 (초)
    protected float currentCooldown;   // 남은 쿨다운 시간 (초)
    protected float castTime;          // 시전 시간 (초)

    // 원소 색상 (UI 표시용)
    protected String elementColor;
    protected String elementName;

    // 투사체 풀 (스킬이 투사체를 생성할 때 필요)
    protected ProjectilePool projectilePool;

    /**
     * 스킬 생성자
     */
    public Skill(int skillId, String skillName, int baseDamage, int manaCost,
                 float cooldown, float castTime, String elementColor, String elementName) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.castTime = castTime;
        this.elementColor = elementColor;
        this.elementName = elementName;
    }

    /**
     * 매 프레임마다 호출되어 쿨다운 감소
     */
    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) {
                currentCooldown = 0;
            }
        }
    }

    /**
     * 스킬을 사용할 수 있는지 확인
     * - 쿨다운이 끝났는가
     * - 플레이어 MP가 충분한가
     */
    public boolean canCast(Player caster) {
        return currentCooldown <= 0 && caster.getMp() >= manaCost;
    }

    /**
     * 쿨다운 초기화
     */
    public void resetCooldown() {
        this.currentCooldown = cooldown;
    }

    /**
     * 스킬 시전 (각 스킬별로 구현)
     * targetX, targetY는 마우스 위치 등 플레이어가 지정한 위치
     */
    public abstract void execute(Player caster, float targetX, float targetY);

    // Getter 메서드들
    public int getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getManaCost() {
        return manaCost;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }

    public float getCooldownPercent() {
        return currentCooldown / cooldown;
    }

    public float getCastTime() {
        return castTime;
    }

    public String getElementColor() {
        return elementColor;
    }

    public String getElementName() {
        return elementName;
    }

    public boolean isOnCooldown() {
        return currentCooldown > 0;
    }

    /**
     * ProjectilePool 설정 (SkillManager에서 호출)
     */
    public void setProjectilePool(ProjectilePool pool) {
        this.projectilePool = pool;
    }

    /**
     * 투사체 속도 반환 (기본값: 400f, 서브클래스에서 오버라이드)
     */
    public float getProjectileSpeed() {
        return 400f;
    }

    /**
     * 투사체 반지름 반환 (기본값: 10f, 서브클래스에서 오버라이드)
     */
    public float getProjectileRadius() {
        return 10f;
    }

    /**
     * 투사체 수명 반환 (기본값: 2.0f, 서브클래스에서 오버라이드)
     */
    public float getProjectileLifetime() {
        return 2.0f;
    }
}
