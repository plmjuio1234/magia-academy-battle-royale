package com.example.yugeup.game.monster;

/**
 * 몬스터 상태
 *
 * 몬스터의 현재 행동 상태를 나타냅니다.
 * 애니메이션과 AI 로직에 사용됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public enum MonsterState {
    /** 대기 상태 */
    IDLE,

    /** 이동 중 */
    MOVING,

    /** 추적 중 (서버에서 전송) */
    PURSUING,

    /** 공격 중 */
    ATTACKING,

    /** 피격 */
    HIT,

    /** 사망 */
    DEAD
}
