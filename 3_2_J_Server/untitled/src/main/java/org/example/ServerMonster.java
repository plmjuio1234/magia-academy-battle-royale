package org.example;

import java.util.List;
import java.util.Map;

/**
 * 서버 측 몬스터 구현
 * 클라이언트와 다르게 렌더링 없이 순수 AI와 상태 관리만 수행
 */
public abstract class ServerMonster {
    public int id;
    public float x, y;
    public float vx, vy;
    public int hp;
    public int maxHp;
    public String type;
    public boolean alive = true;

    // AI 파라미터
    protected float speed;
    protected float aggroRange;
    protected float attackRange;
    protected int attackDamage;
    protected float attackCooldown;
    protected float currentAttackCooldown = 0;

    // 상태
    protected String currentState = "IDLE";
    protected Integer targetPlayerId = null;
    private float syncTimer = 0;
    private static final float SYNC_INTERVAL = 0.1f;  // 100ms
    protected int lastAttackerId = -1;  // 마지막 공격자 ID (경험치 지급용)

    public ServerMonster(int id, float x, float y, int hp, String type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.maxHp = hp;
        this.hp = hp;
        this.type = type;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta, List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
        if (!alive) return;

        // AI 로직 실행 (플레이어 위치 기반 추적)
        updateAI(delta, activePlayers, playerPositions);

        // 위치 업데이트
        x += vx * delta;
        y += vy * delta;

        // 맵 경계 제한
        clampToMapBounds();

        // 쿨다운 감소
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= delta;
        }

        // 동기화 타이머
        syncTimer += delta;
    }

    /**
     * AI 로직 (하위 클래스가 구현)
     * @param delta 프레임 시간 (초)
     * @param activePlayers 활성 플레이어 ID 목록
     * @param playerPositions 플레이어 위치 맵 (key: playerId, value: [x, y])
     */
    protected abstract void updateAI(float delta, List<Integer> activePlayers, Map<Integer, float[]> playerPositions);

    /**
     * 위치 동기화 필요 여부
     */
    public boolean shouldSyncPosition() {
        return syncTimer >= SYNC_INTERVAL;
    }

    /**
     * 동기화 완료 표시
     */
    public void markSynced() {
        syncTimer = 0;
    }

    /**
     * 피해 입기
     */
    public void takeDamage(int damage, int attackerId) {
        hp -= damage;
        lastAttackerId = attackerId;  // 마지막 공격자 기록
        if (hp <= 0) {
            hp = 0;
            alive = false;
            currentState = "DEAD";
        }
    }

    /**
     * 마지막 공격자 ID 반환 (경험치 지급용)
     */
    public int getLastAttackerId() {
        return lastAttackerId;
    }

    /**
     * 맵 경계 제한 (4000x4000 맵)
     */
    protected void clampToMapBounds() {
        float radius = 20f;
        float mapWidth = 4000f;
        float mapHeight = 4000f;

        if (x < radius) x = radius;
        if (x > mapWidth - radius) x = mapWidth - radius;
        if (y < radius) y = radius;
        if (y > mapHeight - radius) y = mapHeight - radius;
    }

    public boolean isAlive() { return alive; }
    public String getType() { return type; }
    public String getState() { return currentState; }
    public String getElementType() { return ""; }  // Slime에서 오버라이드

    // ===== 콘크리트 몬스터 클래스 =====

    /**
     * 고스트 몬스터
     */
    public static class Ghost extends ServerMonster {
        private float invisibilityCooldown = 0;
        private final float INVISIBILITY_COOLDOWN = 5f;
        private final float INVISIBILITY_DURATION = 2f;
        private float currentInvisibilityDuration = 0;
        private boolean isInvisible = false;

        public Ghost(int id, float x, float y) {
            super(id, x, y, 60, "Ghost");
            this.speed = 120f;
            this.aggroRange = 3000f;  // 화면 크기만큼 확대
            this.attackRange = 300f;
            this.attackDamage = 25;
            this.attackCooldown = 2.0f;
        }

        @Override
        protected void updateAI(float delta, List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
            // 가시성 상태 업데이트
            if (isInvisible) {
                currentInvisibilityDuration -= delta;
                if (currentInvisibilityDuration <= 0) {
                    isInvisible = false;
                }
            } else {
                invisibilityCooldown -= delta;
            }

            // 가장 가까운 플레이어 찾기
            Integer nearestPlayer = findNearestPlayer(activePlayers, playerPositions);

            if (nearestPlayer == null) {
                // 배회
                currentState = "IDLE";
                vx = 0;
                vy = 0;
            } else {
                targetPlayerId = nearestPlayer;
                float[] targetPos = playerPositions.get(nearestPlayer);
                if (targetPos != null) {
                    float distSq = (x - targetPos[0]) * (x - targetPos[0]) + (y - targetPos[1]) * (y - targetPos[1]);

                    // 공격 범위 체크
                    if (distSq <= attackRange * attackRange && currentAttackCooldown <= 0) {
                        currentState = "ATTACKING";
                        currentAttackCooldown = attackCooldown;
                        vx = 0;
                        vy = 0;
                    } else {
                        // 추적
                        currentState = "PURSUING";
                        moveTowards(targetPos[0], targetPos[1]);
                    }
                }
            }

            // 10% 확률로 투명화 시작
            if (!isInvisible && Math.random() < 0.1f * delta && currentState.equals("IDLE")) {
                isInvisible = true;
                currentInvisibilityDuration = INVISIBILITY_DURATION;
                invisibilityCooldown = INVISIBILITY_COOLDOWN;
            }
        }

        /**
         * 타겟 위치로 이동
         */
        private void moveTowards(float targetX, float targetY) {
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                vx = (dx / distance) * speed;
                vy = (dy / distance) * speed;
            } else {
                vx = 0;
                vy = 0;
            }
        }
    }

    /**
     * 박쥐 몬스터
     */
    public static class Bat extends ServerMonster {
        public Bat(int id, float x, float y) {
            super(id, x, y, 50, "Bat");
            this.speed = 100f;
            this.aggroRange = 3000f;  // 화면 크기만큼 확대
            this.attackRange = 40f;
            this.attackDamage = 20;
            this.attackCooldown = 1.8f;
        }

        @Override
        protected void updateAI(float delta, List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
            Integer nearestPlayer = findNearestPlayer(activePlayers, playerPositions);

            if (nearestPlayer == null) {
                currentState = "IDLE";
                vx = 0;
                vy = 0;
            } else {
                targetPlayerId = nearestPlayer;
                float[] targetPos = playerPositions.get(nearestPlayer);
                if (targetPos != null) {
                    float distSq = (x - targetPos[0]) * (x - targetPos[0]) + (y - targetPos[1]) * (y - targetPos[1]);

                    // 공격 범위 체크
                    if (distSq <= attackRange * attackRange && currentAttackCooldown <= 0) {
                        currentState = "ATTACKING";
                        currentAttackCooldown = attackCooldown;
                        vx = 0;
                        vy = 0;
                    } else {
                        currentState = "PURSUING";
                        moveTowards(targetPos[0], targetPos[1]);
                    }
                }
            }
        }

        /**
         * 타겟 위치로 이동
         */
        private void moveTowards(float targetX, float targetY) {
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                vx = (dx / distance) * speed;
                vy = (dy / distance) * speed;
            } else {
                vx = 0;
                vy = 0;
            }
        }
    }

    /**
     * 골렘 몬스터
     */
    public static class Golem extends ServerMonster {
        private float chargingTime = 0;
        private final float CHARGING_DURATION = 3.0f;
        private boolean isCharging = false;

        public Golem(int id, float x, float y) {
            super(id, x, y, 150, "Golem");
            this.speed = 50f;
            this.aggroRange = 3000f;  // 화면 크기만큼 확대
            this.attackRange = 150f;
            this.attackDamage = 50;
            this.attackCooldown = 4.0f;
        }

        @Override
        protected void updateAI(float delta, List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
            Integer nearestPlayer = findNearestPlayer(activePlayers, playerPositions);

            if (nearestPlayer == null) {
                currentState = "IDLE";
                isCharging = false;
                chargingTime = 0;
                vx = 0;
                vy = 0;
            } else {
                targetPlayerId = nearestPlayer;
                float[] targetPos = playerPositions.get(nearestPlayer);
                if (targetPos != null) {
                    float distSq = (x - targetPos[0]) * (x - targetPos[0]) + (y - targetPos[1]) * (y - targetPos[1]);

                    if (currentAttackCooldown <= 0) {
                        if (!isCharging) {
                            // 충전 시작
                            isCharging = true;
                            chargingTime = 0;
                            currentState = "ATTACKING";
                            vx = 0;
                            vy = 0;
                        } else {
                            // 충전 중
                            chargingTime += delta;
                            if (chargingTime >= CHARGING_DURATION) {
                                // 충전 완료
                                isCharging = false;
                                chargingTime = 0;
                                currentAttackCooldown = attackCooldown;
                            }
                        }
                    } else {
                        // 추적
                        currentState = "PURSUING";
                        moveTowards(targetPos[0], targetPos[1]);
                    }
                }
            }
        }

        /**
         * 타겟 위치로 이동 (골렘은 느림)
         */
        private void moveTowards(float targetX, float targetY) {
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                vx = (dx / distance) * speed;
                vy = (dy / distance) * speed;
            } else {
                vx = 0;
                vy = 0;
            }
        }

        @Override
        public void takeDamage(int damage, int attackerId) {
            // 골렘 방어력: 입은 피해의 70%만 적용
            int actualDamage = (int)(damage * 0.7f);
            hp -= actualDamage;
            lastAttackerId = attackerId;  // 마지막 공격자 기록
            if (hp <= 0) {
                hp = 0;
                alive = false;
                currentState = "DEAD";
                isCharging = false;
            }
        }
    }

    /**
     * 가장 가까운 플레이어 ID 찾기 (어그로 범위 내)
     */
    protected Integer findNearestPlayer(List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
        if (activePlayers == null || activePlayers.isEmpty() || playerPositions == null) {
            return null;
        }

        Integer nearest = null;
        float minDistanceSq = aggroRange * aggroRange;

        for (Integer playerId : activePlayers) {
            float[] pos = playerPositions.get(playerId);
            if (pos != null) {
                float dx = x - pos[0];
                float dy = y - pos[1];
                float distSq = dx * dx + dy * dy;

                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    nearest = playerId;
                }
            }
        }

        return nearest;
    }
}
