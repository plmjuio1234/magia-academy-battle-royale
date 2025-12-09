package com.example.yugeup.game.player;

/**
 * 캐릭터 스탯 업그레이드 관리자
 *
 * 플레이어의 능력치를 업그레이드합니다.
 * 공격력, 방어력, 체력, 마나 등을 강화할 수 있습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StatsUpgradeManager {
    private Player player;
    private PlayerStats stats;

    // 스탯 업그레이드 비용 (경험치)
    private static final int UPGRADE_COST = 75;

    // 스탯 업그레이드 배수
    private static final float ATK_UPGRADE_MULTIPLIER = 1.15f;    // 15% 증가
    private static final float DEF_UPGRADE_MULTIPLIER = 1.2f;     // 20% 증가
    private static final float HP_UPGRADE_VALUE = 20;             // 20 증가
    private static final float MP_UPGRADE_VALUE = 10;             // 10 증가

    /**
     * 스탯 업그레이드 매니저 생성자
     *
     * @param player 플레이어 엔티티
     */
    public StatsUpgradeManager(Player player) {
        this.player = player;
        this.stats = player.getStats();
    }

    /**
     * 스탯 업그레이드 가능 여부 확인
     *
     * @return 업그레이드 가능 여부
     */
    public boolean canUpgrade() {
        int currentExp = player.getLevelSystem().getCurrentExp();
        return currentExp >= UPGRADE_COST;
    }

    /**
     * 공격력 업그레이드
     *
     * @return 성공 여부
     */
    public boolean upgradeAttack() {
        if (!canUpgrade()) {
            return false;
        }

        // 경험치 소비
        player.getLevelSystem().gainExperience(-UPGRADE_COST);

        // 공격력 증가
        int currentAtk = stats.getAttackPower();
        int newAtk = (int) (currentAtk * ATK_UPGRADE_MULTIPLIER);
        stats.setAttackPower(newAtk);

        System.out.println("[StatsUpgradeManager] 공격력 업그레이드: " + currentAtk + " → " + newAtk);
        return true;
    }

    /**
     * 방어력 업그레이드
     *
     * @return 성공 여부
     */
    public boolean upgradeDefense() {
        if (!canUpgrade()) {
            return false;
        }

        // 경험치 소비
        player.getLevelSystem().gainExperience(-UPGRADE_COST);

        // 방어력 증가
        int currentDef = stats.getDefense();
        int newDef = (int) (currentDef * DEF_UPGRADE_MULTIPLIER);
        stats.setDefense(newDef);

        System.out.println("[StatsUpgradeManager] 방어력 업그레이드: " + currentDef + " → " + newDef);
        return true;
    }

    /**
     * 최대 체력 업그레이드
     *
     * @return 성공 여부
     */
    public boolean upgradeHealth() {
        if (!canUpgrade()) {
            return false;
        }

        // 경험치 소비
        player.getLevelSystem().gainExperience(-UPGRADE_COST);

        // 최대 체력 증가
        int currentMaxHp = stats.getMaxHealth();
        int newMaxHp = (int) (currentMaxHp + HP_UPGRADE_VALUE);
        stats.setMaxHealth(newMaxHp);

        // 현재 체력도 함께 회복
        stats.setCurrentHealth(newMaxHp);

        System.out.println("[StatsUpgradeManager] 최대 체력 업그레이드: " + currentMaxHp + " → " + newMaxHp);
        return true;
    }

    /**
     * 최대 마나 업그레이드
     *
     * @return 성공 여부
     */
    public boolean upgradeMana() {
        if (!canUpgrade()) {
            return false;
        }

        // 경험치 소비
        player.getLevelSystem().gainExperience(-UPGRADE_COST);

        // 최대 마나 증가
        int currentMaxMp = stats.getMaxMana();
        int newMaxMp = (int) (currentMaxMp + MP_UPGRADE_VALUE);
        stats.setMaxMana(newMaxMp);

        // 현재 마나도 함께 회복
        stats.setCurrentMana(newMaxMp);

        System.out.println("[StatsUpgradeManager] 최대 마나 업그레이드: " + currentMaxMp + " → " + newMaxMp);
        return true;
    }

    /**
     * 업그레이드 미리보기 (공격력)
     *
     * @return 미리보기 정보
     */
    public UpgradePreview getAttackUpgradePreview() {
        UpgradePreview preview = new UpgradePreview();
        int currentAtk = stats.getAttackPower();
        preview.beforeValue = currentAtk;
        preview.afterValue = (int) (currentAtk * ATK_UPGRADE_MULTIPLIER);
        preview.displayText = "공격력: " + preview.beforeValue + " → " + preview.afterValue;
        return preview;
    }

    /**
     * 업그레이드 미리보기 (방어력)
     *
     * @return 미리보기 정보
     */
    public UpgradePreview getDefenseUpgradePreview() {
        UpgradePreview preview = new UpgradePreview();
        int currentDef = stats.getDefense();
        preview.beforeValue = currentDef;
        preview.afterValue = (int) (currentDef * DEF_UPGRADE_MULTIPLIER);
        preview.displayText = "방어력: " + preview.beforeValue + " → " + preview.afterValue;
        return preview;
    }

    /**
     * 업그레이드 미리보기 (체력)
     *
     * @return 미리보기 정보
     */
    public UpgradePreview getHealthUpgradePreview() {
        UpgradePreview preview = new UpgradePreview();
        int currentMaxHp = stats.getMaxHealth();
        preview.beforeValue = currentMaxHp;
        preview.afterValue = (int) (currentMaxHp + HP_UPGRADE_VALUE);
        preview.displayText = "최대 체력: " + preview.beforeValue + " → " + preview.afterValue;
        return preview;
    }

    /**
     * 업그레이드 미리보기 (마나)
     *
     * @return 미리보기 정보
     */
    public UpgradePreview getManaUpgradePreview() {
        UpgradePreview preview = new UpgradePreview();
        int currentMaxMp = stats.getMaxMana();
        preview.beforeValue = currentMaxMp;
        preview.afterValue = (int) (currentMaxMp + MP_UPGRADE_VALUE);
        preview.displayText = "최대 마나: " + preview.beforeValue + " → " + preview.afterValue;
        return preview;
    }

    /**
     * 업그레이드 비용 반환
     *
     * @return 필요 경험치
     */
    public int getUpgradeCost() {
        return UPGRADE_COST;
    }

    /**
     * 업그레이드 미리보기 정보
     */
    public static class UpgradePreview {
        public int beforeValue;
        public int afterValue;
        public String displayText;
    }
}
