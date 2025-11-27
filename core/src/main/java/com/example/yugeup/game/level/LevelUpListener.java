package com.example.yugeup.game.level;

/**
 * 레벨업 리스너
 *
 * 경험치 획득 및 레벨업 이벤트를 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public interface LevelUpListener {
    /**
     * 경험치 획득 시 호출
     *
     * @param amount 획득한 경험치량
     * @param currentExp 현재 경험치
     * @param maxExp 다음 레벨까지 필요한 경험치
     */
    void onExpGained(int amount, int currentExp, int maxExp);

    /**
     * 레벨업 시 호출
     *
     * @param newLevel 새로운 레벨
     */
    void onLevelUp(int newLevel);
}
