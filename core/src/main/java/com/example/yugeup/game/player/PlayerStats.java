package com.example.yugeup.game.player;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어 능력치 클래스
 *
 * 플레이어의 모든 능력치를 관리합니다.
 * HP, MP, 공격력, 방어력, 이동속도 등을 포함합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PlayerStats {
    // 체력 (Health Points)
    private int maxHealth;
    private int currentHealth;

    // 마나 (Mana Points)
    private int maxMana;
    private int currentMana;

    // 공격력 (Attack Power)
    private int attackPower;

    // 방어력 (Defense)
    private int defense;

    // 이동 속도 (Speed, 픽셀/초)
    private float speed;

    // 레벨
    private int level;

    // 능력치 변화 리스너
    private List<StatsChangeListener> listeners = new ArrayList<>();

    /**
     * 기본 생성자 (레벨 1 기준)
     */
    public PlayerStats() {
        this(1);
    }

    /**
     * 레벨 기반 생성자
     *
     * @param level 초기 레벨
     */
    public PlayerStats(int level) {
        this.level = level;
        calculateBaseStats();
    }

    /**
     * 레벨에 따른 기본 능력치 계산
     */
    private void calculateBaseStats() {
        // 기본 능력치 공식
        this.maxHealth = 100 + (level - 1) * 20;      // 100, 120, 140, ...
        this.maxMana = 50 + (level - 1) * 10;         // 50, 60, 70, ...
        this.attackPower = 10 + (level - 1) * 5;      // 10, 15, 20, ...
        this.defense = 5 + (level - 1) * 2;           // 5, 7, 9, ...
        this.speed = 300f + (level - 1) * 10f;        // 300, 310, 320, ...

        // 현재값 초기화 (최대값으로)
        this.currentHealth = maxHealth;
        this.currentMana = maxMana;
    }

    /**
     * 체력 감소
     *
     * @param amount 감소량
     * @return 실제 감소된 체력
     */
    public int decreaseHealth(int amount) {
        int oldHealth = currentHealth;
        currentHealth = Math.max(0, currentHealth - amount);
        int actualDecrease = oldHealth - currentHealth;

        notifyHealthChanged(oldHealth, currentHealth);
        return actualDecrease;
    }

    /**
     * 체력 회복
     *
     * @param amount 회복량
     * @return 실제 회복된 체력
     */
    public int increaseHealth(int amount) {
        int oldHealth = currentHealth;
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        int actualIncrease = currentHealth - oldHealth;

        notifyHealthChanged(oldHealth, currentHealth);
        return actualIncrease;
    }

    /**
     * 마나 소비
     *
     * @param amount 소비량
     * @return 소비 성공 여부
     */
    public boolean consumeMana(int amount) {
        if (currentMana < amount) {
            return false;
        }

        int oldMana = currentMana;
        currentMana -= amount;
        notifyManaChanged(oldMana, currentMana);
        return true;
    }

    /**
     * 마나 회복
     *
     * @param amount 회복량
     * @return 실제 회복된 마나
     */
    public int increaseMana(int amount) {
        int oldMana = currentMana;
        currentMana = Math.min(maxMana, currentMana + amount);
        int actualIncrease = currentMana - oldMana;

        notifyManaChanged(oldMana, currentMana);
        return actualIncrease;
    }

    /**
     * 데미지 계산 (방어력 고려)
     *
     * @param rawDamage 기본 데미지
     * @return 실제 적용될 데미지
     */
    public int calculateDamageReceived(int rawDamage) {
        // 방어력 공식: 데미지 감소 = 방어력 * 2
        int damageReduction = defense * 2;
        int actualDamage = Math.max(1, rawDamage - damageReduction);  // 최소 1 데미지
        return actualDamage;
    }

    /**
     * 공격 데미지 계산
     *
     * @return 공격력 기반 데미지
     */
    public int calculateAttackDamage() {
        // 기본 공격 데미지 = 공격력 * 1.0
        // 크리티컬, 스킬 보너스 등은 향후 추가
        return attackPower;
    }

    /**
     * 플레이어가 사망했는가?
     *
     * @return 사망 여부
     */
    public boolean isDead() {
        return currentHealth <= 0;
    }

    /**
     * 체력 비율 (0.0 ~ 1.0)
     *
     * @return 체력 비율
     */
    public float getHealthRatio() {
        if (maxHealth == 0) return 0f;
        return (float) currentHealth / maxHealth;
    }

    /**
     * 마나 비율 (0.0 ~ 1.0)
     *
     * @return 마나 비율
     */
    public float getManaRatio() {
        if (maxMana == 0) return 0f;
        return (float) currentMana / maxMana;
    }

    /**
     * 레벨업
     */
    public void levelUp() {
        level++;
        calculateBaseStats();
        notifyLevelUp(level);
    }

    // ===== 리스너 관리 =====

    public void addListener(StatsChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StatsChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyHealthChanged(int oldValue, int newValue) {
        for (StatsChangeListener listener : listeners) {
            listener.onHealthChanged(oldValue, newValue, maxHealth);
        }
    }

    private void notifyManaChanged(int oldValue, int newValue) {
        for (StatsChangeListener listener : listeners) {
            listener.onManaChanged(oldValue, newValue, maxMana);
        }
    }

    private void notifyLevelUp(int newLevel) {
        for (StatsChangeListener listener : listeners) {
            listener.onLevelUp(newLevel);
        }
    }

    // ===== Getter & Setter =====

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getDefense() {
        return defense;
    }

    public float getSpeed() {
        return speed;
    }

    public int getLevel() {
        return level;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = Math.min(currentHealth, maxHealth);
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        this.currentMana = Math.min(currentMana, maxMana);
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setCurrentHealth(int health) {
        int oldHealth = this.currentHealth;
        this.currentHealth = Math.max(0, Math.min(maxHealth, health));
        notifyHealthChanged(oldHealth, currentHealth);
    }

    public void setCurrentMana(int mana) {
        int oldMana = this.currentMana;
        this.currentMana = Math.max(0, Math.min(maxMana, mana));
        notifyManaChanged(oldMana, currentMana);
    }
}
