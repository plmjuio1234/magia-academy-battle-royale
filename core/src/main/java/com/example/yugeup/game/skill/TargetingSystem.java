package com.example.yugeup.game.skill;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;

import java.util.ArrayList;
import java.util.List;

/**
 * 타게팅 시스템
 *
 * 범위 내 몬스터를 탐지하고 타겟을 선택합니다.
 * 스킬의 자동 타게팅, 범위 공격 등에 사용됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class TargetingSystem {
    // 몬스터 목록 (외부에서 주입받음)
    private List<Monster> monsters;

    /**
     * TargetingSystem을 생성합니다.
     */
    public TargetingSystem() {
        this.monsters = new ArrayList<>();
    }

    /**
     * 몬스터 목록을 설정합니다.
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsters(List<Monster> monsters) {
        this.monsters = monsters;
    }

    /**
     * 가장 가까운 몬스터를 찾습니다.
     *
     * @param origin 기준 위치
     * @param range 탐지 범위 (픽셀)
     * @return 가장 가까운 몬스터 (없으면 null)
     */
    public Monster findNearestMonster(Vector2 origin, float range) {
        Monster nearest = null;
        float minDistance = range;

        for (Monster monster : monsters) {
            // 사망한 몬스터 제외
            if (monster.isDead()) {
                continue;
            }

            // 거리 계산
            float dx = monster.getX() - origin.x;
            float dy = monster.getY() - origin.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 범위 내이고 더 가까우면 업데이트
            if (distance <= range && distance < minDistance) {
                nearest = monster;
                minDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * 범위 내 모든 몬스터를 찾습니다.
     *
     * @param origin 기준 위치
     * @param range 탐지 범위 (픽셀)
     * @return 범위 내 몬스터 목록
     */
    public List<Monster> findMonstersInRange(Vector2 origin, float range) {
        List<Monster> result = new ArrayList<>();

        for (Monster monster : monsters) {
            // 사망한 몬스터 제외
            if (monster.isDead()) {
                continue;
            }

            // 거리 계산
            float dx = monster.getX() - origin.x;
            float dy = monster.getY() - origin.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 범위 내에 있으면 추가
            if (distance <= range) {
                result.add(monster);
            }
        }

        return result;
    }

    /**
     * 방향 내 몬스터를 찾습니다. (원뿔 형태)
     *
     * @param origin 기준 위치
     * @param direction 방향 벡터 (정규화 필수)
     * @param range 탐지 범위 (픽셀)
     * @param angle 각도 범위 (도 단위, 예: 60도)
     * @return 방향 내 몬스터 목록
     */
    public List<Monster> findMonstersInCone(Vector2 origin, Vector2 direction, float range, float angle) {
        List<Monster> result = new ArrayList<>();

        for (Monster monster : monsters) {
            // 사망한 몬스터 제외
            if (monster.isDead()) {
                continue;
            }

            // 몬스터로 향하는 벡터
            Vector2 toMonster = new Vector2(monster.getX() - origin.x, monster.getY() - origin.y);
            float distance = toMonster.len();

            // 범위 확인
            if (distance > range) {
                continue;
            }

            // 각도 확인
            toMonster.nor(); // 정규화
            float angleBetween = (float) Math.toDegrees(Math.acos(direction.dot(toMonster)));

            if (angleBetween <= angle / 2) {
                result.add(monster);
            }
        }

        return result;
    }
}
