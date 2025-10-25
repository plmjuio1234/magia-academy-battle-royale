package com.magicbr.game.systems.skills;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * 투사체 오브젝트 풀
 * 수많은 투사체를 효율적으로 관리하기 위해 오브젝트 재사용
 */
public class ProjectilePool {
    private List<Projectile> activeProjectiles = new ArrayList<>();
    private List<Projectile> inactiveProjectiles = new ArrayList<>();

    // 미리 생성해둘 투사체 개수
    private static final int INITIAL_POOL_SIZE = 100;
    private static final int MAX_POOL_SIZE = 500;

    public ProjectilePool() {
        // 초기 투사체 풀 생성
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            inactiveProjectiles.add(new Projectile(0, 0, 0, 0, 0, 0, "테스트", "불", 1f));
        }
    }

    /**
     * 풀에서 투사체 가져오기
     */
    public Projectile obtain(float x, float y, float vx, float vy, int damage,
                             int ownerId, String skillName, String elementColor, float lifetime) {
        Projectile projectile;

        System.out.println("[ProjectilePool] 투사체 생성: " + skillName +
            " (색상: " + elementColor + ", 지속: " + lifetime + "초)");

        if (inactiveProjectiles.isEmpty()) {
            // 풀이 비었으면 새로 생성
            projectile = new Projectile(x, y, vx, vy, damage, ownerId, skillName, elementColor, lifetime);
        } else {
            // 풀에서 가져오기
            projectile = inactiveProjectiles.remove(inactiveProjectiles.size() - 1);
            projectile.reset(x, y, vx, vy, damage, ownerId, skillName, elementColor, lifetime);
        }

        activeProjectiles.add(projectile);
        return projectile;
    }

    /**
     * 풀에 투사체 반환
     */
    public void free(Projectile projectile) {
        activeProjectiles.remove(projectile);
        if (inactiveProjectiles.size() < MAX_POOL_SIZE) {
            projectile.active = false;
            inactiveProjectiles.add(projectile);
        }
    }

    /**
     * 모든 활성 투사체 업데이트
     */
    public void updateAll(float delta) {
        for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = activeProjectiles.get(i);
            projectile.update(delta);

            // 비활성 투사체는 풀에 반환
            if (!projectile.active) {
                free(projectile);
            }
        }
    }

    /**
     * 모든 활성 투사체 렌더링
     */
    public void renderAll(ShapeRenderer shapeRenderer) {
        for (Projectile projectile : activeProjectiles) {
            projectile.render(shapeRenderer);
        }
    }

    /**
     * 활성 투사체 목록 반환 (읽기 전용)
     */
    public List<Projectile> getActiveProjectiles() {
        return new ArrayList<>(activeProjectiles);
    }

    /**
     * 활성 투사체 개수
     */
    public int getActiveProjectileCount() {
        return activeProjectiles.size();
    }

    /**
     * 풀 상태 리셋 (게임 재시작 시)
     */
    public void clear() {
        activeProjectiles.clear();
        inactiveProjectiles.clear();

        // 풀 재구성
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            inactiveProjectiles.add(new Projectile(0, 0, 0, 0, 0, 0, "테스트", "불", 1f));
        }
    }

    /**
     * 디버그: 풀 상태 출력
     */
    public void printPoolStats() {
        System.out.println("[ProjectilePool] Active: " + activeProjectiles.size() +
                           " / Inactive: " + inactiveProjectiles.size() +
                           " / Total: " + (activeProjectiles.size() + inactiveProjectiles.size()));
    }
}
