package org.example;

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

    // 스킬의 모든 필요한 정보 (클라이언트에서 전송)
    public String skillName;
    public String elementColor;      // "불", "물", "바람" 등
    public int baseDamage;
    public float projectileSpeed;    // 스킬별 속도
    public float projectileRadius;   // 투사체 크기
    public float projectileLifetime; // 투사체 수명

    public SkillCastMsg() {}
}
