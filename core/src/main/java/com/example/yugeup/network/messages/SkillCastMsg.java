package com.example.yugeup.network.messages;

/**
 * 스킬 사용 메시지
 *
 * 플레이어가 스킬을 사용할 때 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillCastMsg {
    public int playerId;
    public int skillId;
    public float targetX;
    public float targetY;

    // 시전자 위치 (방향 계산에 필수)
    public float casterX;
    public float casterY;

    // 스킬의 모든 필요한 정보 (클라이언트에서 전송)
    public String skillName;
    public String elementColor;      // "불", "물", "바람" 등
    public int baseDamage;
    public float projectileSpeed;    // 스킬별 속도
    public float projectileRadius;   // 투사체 크기
    public float projectileLifetime; // 투사체 수명

    // 스킬 타입 (동기화 방식 결정용)
    public int skillType;            // 0: Projectile, 1: Zone(고정), 2: Zone(이동), 3: Zone(플레이어 추적)

    // 이동형 Zone용 추가 데이터
    public float directionX;         // 이동 방향 X
    public float directionY;         // 이동 방향 Y

    // 다방향 발사용 (IceSpike)
    public int projectileCount;      // 발사체 개수 (1이면 단일, 3이면 3방향 등)
    public float angleSpread;        // 발사 각도 간격 (도)

    public SkillCastMsg() {}
}
