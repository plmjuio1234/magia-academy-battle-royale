# 몬스터 AI 시스템 구현 가이드

**참고**: @docs/prd/game-concept.md 의 던전 축소 시스템을 함께 확인하세요.

---

## 몬스터 종류

### 1. 마법 골렘 (Magic Golem)

```java
public class MagicGolem extends Monster {
    public MagicGolem(float x, float y) {
        super(x, y, 80, "마법 골렘");
        attackDamage = 30;
        attackRange = 30;  // 근접
        attackCooldown = 1.5f;
        speed = 80;        // 픽셀/초
        aggroRange = 200;  // 시야 범위
    }

    @Override
    public void update(float delta) {
        updateState(delta);
        performAction(delta);
    }

    private void performAction(float delta) {
        Player nearestPlayer = findNearestPlayer();

        if (nearestPlayer == null) {
            patrol();  // 배회
        } else {
            if (canReachTarget(nearestPlayer)) {
                attack(nearestPlayer);
            } else {
                pursue(nearestPlayer);
            }
        }
    }

    private void pursue(Player target) {
        Vector2 direction = new Vector2(target.x - x, target.y - y).nor();
        vx = direction.x * speed;
        vy = direction.y * speed;
    }

    private void attack(Player target) {
        // 근접 공격
        vx = 0;
        vy = 0;
        // 서버에 공격 이벤트 전송
    }
}
```

**드롭 아이템**:
- 체력 포션: 70% 확률
- 마나 포션: 30% 확률

---

### 2. 팬텀 북 (Phantom Book)

```java
public class PhantomBook extends Monster {
    public PhantomBook(float x, float y) {
        super(x, y, 50, "팬텀 북");
        attackDamage = 25;
        attackRange = 300;  // 원거리
        attackCooldown = 2.0f;
        speed = 100;
        aggroRange = 250;
    }

    @Override
    public void update(float delta) {
        updateState(delta);
        handleTeleport(delta);
        performAction(delta);
    }

    private void handleTeleport(float delta) {
        // 2초마다 텔레포트
        if (teleportCooldown <= 0) {
            Vector2 newPos = getRandomTeleportPoint();
            x = newPos.x;
            y = newPos.y;
            teleportCooldown = 2.0f;
        }
        teleportCooldown -= delta;
    }

    private void performAction(float delta) {
        Player target = findNearestPlayer();
        if (target != null && Vector2.dst(x, y, target.x, target.y) <= attackRange) {
            castMagic(target);
        }
    }

    private void castMagic(Player target) {
        // 투사체 생성 후 발사
        Vector2 direction = new Vector2(target.x - x, target.y - y).nor();
        createProjectile(direction, attackDamage);
    }
}
```

**드롭 아이템**:
- 스킬북: 60% 확률
- 마법 에센스: 40% 확률

---

### 3. 수정 거미 (Crystal Spider)

```java
public class CrystalSpider extends Monster {
    public CrystalSpider(float x, float y) {
        super(x, y, 40, "수정 거미");
        attackDamage = 5;  // DoT로 처리
        attackRange = 100;
        attackCooldown = 3.0f;
        speed = 120;
        aggroRange = 200;
    }

    @Override
    public void update(float delta) {
        updateState(delta);
        performAction(delta);
    }

    private void performAction(float delta) {
        Player target = findNearestPlayer();

        if (target == null) {
            patrol();
            return;
        }

        // 거리에 따른 행동
        float distance = Vector2.dst(x, y, target.x, target.y);

        if (distance > 150) {
            // 멀면 추적
            pursue(target);
        } else {
            // 가까우면 웹 트랩 설치
            setWebTrap(target);
        }
    }

    private void setWebTrap(Player target) {
        // DoT (지속 피해)
        // 초당 5 피해, 3초 동안
        vx = 0;
        vy = 0;
        // 서버에 웹 트랩 이벤트 전송
    }
}
```

**드롭 아이템**:
- 독 저항 포션: 100% 확률

---

## 기본 몬스터 클래스

```java
public abstract class Monster {
    protected float x, y;
    protected float vx, vy;
    protected int maxHp;
    protected int hp;
    protected String name;
    protected float speed;

    // AI 파라미터
    protected int attackDamage;
    protected float attackRange;
    protected float attackCooldown;
    protected float currentAttackCooldown;
    protected float aggroRange;  // 시야 범위

    // 상태
    protected MonsterState state = MonsterState.IDLE;
    protected float patrolTimer = 0;
    protected Vector2 patrolTarget = new Vector2();

    public Monster(float x, float y, int hp, String name) {
        this.x = x;
        this.y = y;
        this.maxHp = hp;
        this.hp = hp;
        this.name = name;
    }

    protected void updateState(float delta) {
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= delta;
        }
    }

    protected Player findNearestPlayer() {
        Player nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Player player : activePlayers) {
            float distance = Vector2.dst(x, y, player.x, player.y);
            if (distance <= aggroRange && distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    protected void patrol() {
        patrolTimer -= 0.016f;  // 60fps 가정
        if (patrolTimer <= 0) {
            patrolTarget = getRandomPoint();
            patrolTimer = 3.0f;
        }

        Vector2 direction = new Vector2(patrolTarget.x - x, patrolTarget.y - y).nor();
        vx = direction.x * speed * 0.5f;  // 배회는 느리게
        vy = direction.y * speed * 0.5f;
    }

    protected void pursue(Player target) {
        Vector2 direction = new Vector2(target.x - x, target.y - y).nor();
        vx = direction.x * speed;
        vy = direction.y * speed;
    }

    protected boolean canReachTarget(Player target) {
        float distance = Vector2.dst(x, y, target.x, target.y);
        return distance <= attackRange;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            // 드롭 아이템 생성
            dropLoot();
        }
    }

    protected abstract void performAction(float delta);
    protected abstract void dropLoot();

    enum MonsterState {
        IDLE, PATROL, PURSUING, ATTACKING, DEAD
    }
}
```

---

## 몬스터 스폰 시스템

```java
public class MonsterSpawner {
    private static final int SPAWN_RATE = 3;  // 3초마다

    private float spawnTimer = SPAWN_RATE;
    private List<Monster> monsters = new ArrayList<>();

    public void update(float delta) {
        spawnTimer -= delta;

        if (spawnTimer <= 0) {
            spawnMonster();
            spawnTimer = SPAWN_RATE;
        }

        // 모든 몬스터 업데이트
        for (Monster monster : monsters) {
            monster.update(delta);

            if (monster.hp <= 0) {
                // 제거 (드롭 처리됨)
                monsters.remove(monster);
            }
        }
    }

    private void spawnMonster() {
        // 랜덤 위치에 스폰 (안전지역 밖)
        float x = (float)(Math.random() * 1920);
        float y = (float)(Math.random() * 1920);

        int type = (int)(Math.random() * 3);
        Monster monster;

        switch (type) {
            case 0:
                monster = new MagicGolem(x, y);
                break;
            case 1:
                monster = new PhantomBook(x, y);
                break;
            case 2:
                monster = new CrystalSpider(x, y);
                break;
            default:
                monster = new MagicGolem(x, y);
        }

        monsters.add(monster);
    }

    public List<Monster> getMonsters() {
        return monsters;
    }
}
```

---

## AI 경로 찾기 (간단한 A* 구현)

```java
public class PathFinder {
    public static List<Vector2> findPath(Vector2 start, Vector2 goal, int[][] grid) {
        // 그리드 기반 A* 알고리즘
        // grid[y][x] = 0 (통행 가능), 1 (장애물)

        List<Vector2> path = new ArrayList<>();

        // 1. 시작과 목표가 가까우면 직선 경로
        float distance = Vector2.dst(start.x, start.y, goal.x, goal.y);
        if (distance < 100) {
            path.add(goal);
            return path;
        }

        // 2. A* 알고리즘 (복잡하므로 간단한 버전)
        Vector2 current = start.cpy();
        while (Vector2.dst(current.x, current.y, goal.x, goal.y) > 50) {
            Vector2 direction = new Vector2(goal.x - current.x, goal.y - current.y).nor();
            current.add(direction.x * 50, direction.y * 50);
            path.add(current.cpy());

            if (path.size() > 20) break;  // 무한 루프 방지
        }

        return path;
    }
}
```

---

## 충돌 및 피해 처리

### 플레이어-몬스터 충돌
```java
public void checkMonsterCollisions() {
    for (Monster monster : monsters) {
        for (Player player : players) {
            float distance = Vector2.dst(
                monster.x, monster.y,
                player.x, player.y
            );

            if (distance <= 30 + 15) {  // 충돌 반지름
                // 데미지 처리
                player.takeDamage(monster.attackDamage);

                // 넉백
                Vector2 knockback = new Vector2(
                    player.x - monster.x,
                    player.y - monster.y
                ).nor();
                player.velocity.add(knockback.x * 100, knockback.y * 100);
            }
        }
    }
}
```

### 투사체-플레이어 충돌
```java
public void checkProjectileCollisions() {
    for (Projectile proj : projectiles) {
        for (Player player : players) {
            float distance = Vector2.dst(
                proj.x, proj.y,
                player.x, player.y
            );

            if (distance <= 5 + 15) {  // 투사체 반지름 5
                player.takeDamage((int)proj.damage);
                proj.active = false;
            }
        }
    }
}
```
