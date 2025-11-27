# PHASE_25_PVP_COMBAT.md - 플레이어 vs 플레이어 전투

---

## 🎯 목표
플레이어 간 전투 시스템 구현

---

## 📋 구현 범위

- ✅ 플레이어 간 스킬 충돌
- ✅ PVP 데미지 계산
- ✅ 플레이어 사망 처리
- ✅ 킬 로그 표시

---

## 🔧 구현 가이드

### 1. PVP 전투 확장

```java
/**
 * CollisionDetector에 추가
 */
public class CollisionDetector {
    /**
     * 플레이어 간 발사체 충돌
     */
    private void checkPlayerProjectileCollisions() {
        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        List<Player> players = GameManager.getInstance().getAllPlayers();

        for (Projectile projectile : projectiles) {
            if (!projectile.isAlive()) continue;

            Entity owner = projectile.getOwner();
            if (!(owner instanceof Player)) continue;

            for (Player target : players) {
                // 본인은 제외
                if (target.getId() == ((Player) owner).getId()) continue;

                // 무적 상태 제외
                if (target.hasBuff(BuffType.INVINCIBLE)) continue;

                if (isColliding(projectile, target)) {
                    projectile.onHit(target);
                }
            }
        }
    }
}
```

### 2. PVP 데미지 계산

```java
/**
 * CombatSystem에 추가
 */
public class CombatSystem {
    /**
     * PVP 데미지 계산 (감소 계수 적용)
     */
    private int calculatePVPDamage(Player attacker, Player defender, int baseDamage) {
        int damage = damageCalculator.calculate(attacker, defender, baseDamage);

        // PVP 데미지 70%로 감소
        damage = (int) (damage * 0.7f);

        // 방어력 적용
        damage = applyDefense(defender, damage);

        return damage;
    }
}
```

### 3. 킬 로그

```java
/**
 * 킬 로그 UI
 */
public class KillLog extends Table {
    private List<String> killMessages;
    private static final int MAX_MESSAGES = 5;

    public void addKill(String killerName, String victimName) {
        String message = killerName + " ← " + victimName;
        killMessages.add(0, message);

        if (killMessages.size() > MAX_MESSAGES) {
            killMessages.remove(MAX_MESSAGES);
        }

        refresh();
    }
}
```

---

## ✅ 완료 조건

- [ ] PVP 충돌 감지
- [ ] PVP 데미지 계산
- [ ] 킬 로그 UI
- [ ] 플레이어 사망 처리

---

## 🔗 다음 Phase

**PHASE_26: 게임 결과**
