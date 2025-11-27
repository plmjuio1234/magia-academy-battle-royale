package com.example.yugeup.game.level;

import com.example.yugeup.game.player.PlayerStats;
import com.example.yugeup.game.player.StatsCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * 레벨 시스템
 *
 * 플레이어의 레벨과 경험치를 관리합니다.
 * 경험치 획득, 레벨업 처리, 능력치 증가를 담당합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LevelSystem {
    // 현재 레벨
    private int currentLevel;

    // 경험치
    private int currentExp;
    private int expForNextLevel;

    // 플레이어 Stats 참조
    private PlayerStats playerStats;

    // 레벨업 리스너
    private List<LevelUpListener> listeners = new ArrayList<>();

    /**
     * 생성자
     *
     * @param playerStats 플레이어 능력치
     */
    public LevelSystem(PlayerStats playerStats) {
        this.playerStats = playerStats;
        this.currentLevel = 1;
        this.currentExp = 0;
        this.expForNextLevel = StatsCalculator.calculateExpForLevel(2);
    }

    /**
     * 경험치 획득
     *
     * @param amount 경험치량
     */
    public void gainExperience(int amount) {
        currentExp += amount;

        // 레벨업 체크 (여러 레벨 동시 상승 가능)
        while (currentExp >= expForNextLevel) {
            levelUp();
        }

        notifyExpGained(amount);
    }

    /**
     * 레벨업 처리
     */
    private void levelUp() {
        // 남은 경험치 계산
        int remainingExp = currentExp - expForNextLevel;

        // 레벨 증가
        currentLevel++;
        currentExp = remainingExp;

        // 다음 레벨 경험치 계산
        expForNextLevel = StatsCalculator.calculateExpForLevel(currentLevel + 1);

        // 능력치 증가
        playerStats.levelUp();

        // 레벨업 효과
        notifyLevelUp(currentLevel);
    }

    /**
     * 경험치 비율 (0.0 ~ 1.0)
     *
     * @return 경험치 진행도 비율
     */
    public float getExpRatio() {
        if (expForNextLevel == 0) return 1.0f;
        return (float) currentExp / expForNextLevel;
    }

    /**
     * 현재 레벨의 경험치 진행도 (백분율)
     *
     * @return 경험치 백분율 (0~100)
     */
    public int getExpPercentage() {
        return (int)(getExpRatio() * 100);
    }

    // ===== 리스너 관리 =====

    public void addListener(LevelUpListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LevelUpListener listener) {
        listeners.remove(listener);
    }

    private void notifyExpGained(int amount) {
        for (LevelUpListener listener : listeners) {
            listener.onExpGained(amount, currentExp, expForNextLevel);
        }
    }

    private void notifyLevelUp(int newLevel) {
        for (LevelUpListener listener : listeners) {
            listener.onLevelUp(newLevel);
        }
    }

    // ===== Getters =====

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public int getExpForNextLevel() {
        return expForNextLevel;
    }

    public int getRemainingExp() {
        return expForNextLevel - currentExp;
    }
}
