package com.example.yugeup.game.player;

/**
 * 능력치 변화 리스너
 *
 * 능력치가 변경될 때 UI 업데이트 등을 수행합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public interface StatsChangeListener {
    /**
     * 체력 변화
     *
     * @param oldValue 이전 체력
     * @param newValue 새 체력
     * @param maxValue 최대 체력
     */
    void onHealthChanged(int oldValue, int newValue, int maxValue);

    /**
     * 마나 변화
     *
     * @param oldValue 이전 마나
     * @param newValue 새 마나
     * @param maxValue 최대 마나
     */
    void onManaChanged(int oldValue, int newValue, int maxValue);

    /**
     * 레벨업
     *
     * @param newLevel 새 레벨
     */
    void onLevelUp(int newLevel);
}
