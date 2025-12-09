package com.example.yugeup.game.level;

import com.example.yugeup.game.monster.MonsterType;

/**
 * 경험치 관리자
 *
 * 몬스터 처치 시 경험치 분배 및 보정을 담당합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ExperienceManager {
    /**
     * 몬스터 처치 경험치 계산
     *
     * @param monsterType 몬스터 종류
     * @return 경험치량
     */
    public static int getExpForMonster(MonsterType monsterType) {
        switch (monsterType) {
            case GHOST:
                return 50;   // 고스트: 50 exp
            case BAT:
                return 25;   // 박쥐: 25 exp
            case GOLEM:
                return 100;  // 골렘: 100 exp
            default:
                return 10;
        }
    }

    /**
     * 레벨 차이에 따른 경험치 보정
     *
     * @param baseExp 기본 경험치
     * @param playerLevel 플레이어 레벨
     * @param monsterLevel 몬스터 레벨
     * @return 보정된 경험치
     */
    public static int adjustExpByLevelDifference(int baseExp, int playerLevel, int monsterLevel) {
        int levelDiff = monsterLevel - playerLevel;

        if (levelDiff >= 5) {
            // 5레벨 이상 높으면 150%
            return (int)(baseExp * 1.5f);
        } else if (levelDiff >= 2) {
            // 2~4레벨 높으면 120%
            return (int)(baseExp * 1.2f);
        } else if (levelDiff <= -5) {
            // 5레벨 이상 낮으면 50%
            return (int)(baseExp * 0.5f);
        } else if (levelDiff <= -2) {
            // 2~4레벨 낮으면 80%
            return (int)(baseExp * 0.8f);
        }

        // 레벨 차이 -1 ~ 1: 100%
        return baseExp;
    }

    /**
     * 파티 경험치 분배 (향후 구현)
     *
     * @param totalExp 총 경험치
     * @param partySize 파티 인원 수
     * @return 개인당 경험치
     */
    public static int calculatePartyExp(int totalExp, int partySize) {
        // 파티원 수에 따라 경험치 분배
        // 예: 2명 = 60% 씩, 3명 = 50% 씩
        if (partySize <= 1) {
            return totalExp;
        } else if (partySize == 2) {
            return (int)(totalExp * 0.6f);
        } else if (partySize == 3) {
            return (int)(totalExp * 0.5f);
        } else {
            return (int)(totalExp * 0.4f);
        }
    }
}
