package com.example.yugeup.game.skill;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.network.messages.SkillCastMsg;

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
    // 스킬 타입 상수 (네트워크 동기화용)
    public static final int SKILL_TYPE_PROJECTILE = 0;           // 투사체 (Fireball, WaterShot 등)
    public static final int SKILL_TYPE_ZONE_FIXED = 1;           // 고정 Zone (Inferno, LightningBolt 등)
    public static final int SKILL_TYPE_ZONE_MOVING = 2;          // 이동형 Zone (ThunderStorm, EarthSpike)
    public static final int SKILL_TYPE_ZONE_PLAYER_FOLLOW = 3;   // 플레이어 추적 Zone (Storm, StoneShield)
    public static final int SKILL_TYPE_PROJECTILE_MULTI = 4;     // 다방향 투사체 (IceSpike)

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
     * 최종 데미지 계산 (업그레이드 + 공격력 스탯 적용)
     *
     * @return 업그레이드와 공격력이 적용된 데미지
     */
    public int getDamage() {
        // 공격력 스탯 보너스 (플레이어 공격력의 50%를 추가)
        int attackPowerBonus = 0;
        if (owner != null && owner.getStats() != null) {
            attackPowerBonus = owner.getStats().getAttackPower() / 2;
        }

        // 기본 데미지 * 배율 + 업그레이드 보너스 + 공격력 보너스
        return (int) (baseDamage * damageMultiplier) + damageBonus + attackPowerBonus;
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
     * 네트워크로 스킬 시전을 알립니다. (기본 - 하위 호환성)
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
     * 네트워크로 투사체 스킬 시전을 알립니다. (확장 버전)
     * 속도, 크기, 수명 정보를 포함하여 정확한 동기화를 지원합니다.
     *
     * @param casterPos 시전자 위치
     * @param targetPosition 목표 위치
     * @param speed 투사체 속도
     * @param radius 투사체 크기 (반지름)
     * @param lifetime 투사체 수명
     */
    protected void sendProjectileSkillToNetwork(Vector2 casterPos, Vector2 targetPosition,
                                                 float speed, float radius, float lifetime) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }

        SkillCastMsg msg = new SkillCastMsg();
        msg.skillId = skillId;
        msg.casterX = casterPos.x;
        msg.casterY = casterPos.y;
        msg.targetX = targetPosition.x;
        msg.targetY = targetPosition.y;
        msg.skillType = SKILL_TYPE_PROJECTILE;
        msg.projectileSpeed = speed;
        msg.projectileRadius = radius;
        msg.projectileLifetime = lifetime;

        // 방향 계산
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();
        msg.directionX = direction.x;
        msg.directionY = direction.y;

        msg.projectileCount = 1;

        networkManager.sendSkillCastFull(msg);
    }

    /**
     * 네트워크로 다방향 투사체 스킬 시전을 알립니다. (IceSpike 등)
     *
     * @param casterPos 시전자 위치
     * @param targetPosition 목표 위치
     * @param speed 투사체 속도
     * @param radius 투사체 크기 (반지름)
     * @param lifetime 투사체 수명
     * @param count 발사체 개수
     * @param angleSpread 발사 각도 간격 (도)
     */
    protected void sendMultiProjectileSkillToNetwork(Vector2 casterPos, Vector2 targetPosition,
                                                      float speed, float radius, float lifetime,
                                                      int count, float angleSpread) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }

        SkillCastMsg msg = new SkillCastMsg();
        msg.skillId = skillId;
        msg.casterX = casterPos.x;
        msg.casterY = casterPos.y;
        msg.targetX = targetPosition.x;
        msg.targetY = targetPosition.y;
        msg.skillType = SKILL_TYPE_PROJECTILE_MULTI;
        msg.projectileSpeed = speed;
        msg.projectileRadius = radius;
        msg.projectileLifetime = lifetime;

        // 방향 계산
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();
        msg.directionX = direction.x;
        msg.directionY = direction.y;

        msg.projectileCount = count;
        msg.angleSpread = angleSpread;

        networkManager.sendSkillCastFull(msg);
    }

    /**
     * 네트워크로 고정 Zone 스킬 시전을 알립니다. (Inferno, LightningBolt, RockSmash 등)
     *
     * @param zonePosition Zone 위치
     * @param radius Zone 크기 (반지름)
     * @param lifetime Zone 지속시간
     */
    protected void sendFixedZoneSkillToNetwork(Vector2 zonePosition, float radius, float lifetime) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }

        SkillCastMsg msg = new SkillCastMsg();
        msg.skillId = skillId;
        msg.casterX = owner.getX();
        msg.casterY = owner.getY();
        msg.targetX = zonePosition.x;
        msg.targetY = zonePosition.y;
        msg.skillType = SKILL_TYPE_ZONE_FIXED;
        msg.projectileRadius = radius;
        msg.projectileLifetime = lifetime;

        networkManager.sendSkillCastFull(msg);
    }

    /**
     * 네트워크로 이동형 Zone 스킬 시전을 알립니다. (ThunderStorm, EarthSpike 등)
     *
     * @param casterPos 시전자 위치
     * @param targetPosition 목표 위치 (방향 계산용)
     * @param speed Zone 이동 속도
     * @param radius Zone 크기 (반지름)
     * @param range Zone 이동 사거리
     */
    protected void sendMovingZoneSkillToNetwork(Vector2 casterPos, Vector2 targetPosition,
                                                 float speed, float radius, float range) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }

        SkillCastMsg msg = new SkillCastMsg();
        msg.skillId = skillId;
        msg.casterX = casterPos.x;
        msg.casterY = casterPos.y;
        msg.targetX = targetPosition.x;
        msg.targetY = targetPosition.y;
        msg.skillType = SKILL_TYPE_ZONE_MOVING;
        msg.projectileSpeed = speed;
        msg.projectileRadius = radius;
        msg.projectileLifetime = range / speed;  // 사거리/속도 = 수명

        // 방향 계산
        Vector2 direction = targetPosition.cpy().sub(casterPos).nor();
        msg.directionX = direction.x;
        msg.directionY = direction.y;

        networkManager.sendSkillCastFull(msg);
    }

    /**
     * 네트워크로 플레이어 추적형 Zone 스킬 시전을 알립니다. (Storm, StoneShield 등)
     *
     * @param radius Zone 크기 (반지름)
     * @param duration Zone 지속시간
     */
    protected void sendPlayerFollowZoneSkillToNetwork(float radius, float duration) {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }

        SkillCastMsg msg = new SkillCastMsg();
        msg.skillId = skillId;
        msg.casterX = owner.getX();
        msg.casterY = owner.getY();
        msg.targetX = owner.getX();
        msg.targetY = owner.getY();
        msg.skillType = SKILL_TYPE_ZONE_PLAYER_FOLLOW;
        msg.projectileRadius = radius;
        msg.projectileLifetime = duration;

        networkManager.sendSkillCastFull(msg);
    }

    /**
     * 업그레이드 타입
     */
    public enum UpgradeType {
        DAMAGE,      // 데미지 증가 (30%)
        COOLDOWN     // 쿨타임 감소 (20%)
    }
}
