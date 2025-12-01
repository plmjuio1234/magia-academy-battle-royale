package org.example;

import java.util.*;

/**
 * 서버 측 몬스터 관리자
 * 게임 룸별로 몬스터를 관리하고 클라이언트로 동기화
 */
public class ServerMonsterManager {
    private Map<Integer, List<ServerMonster>> roomMonsters = new HashMap<>();
    private Map<Integer, Float> roomSpawnTimers = new HashMap<>();  // 각 방별 스폰 타이머
    private static final float SPAWN_INTERVAL = 0.5f;  // 0.5초마다 스폰 체크
    private static final int MAX_MONSTERS_PER_ROOM = 50;  // 최대 50마리 유지
    private static final float MAP_WIDTH = 4000f;  // 전체 맵 너비
    private static final float MAP_HEIGHT = 4000f;  // 전체 맵 높이
    private int nextMonsterId = 1000;  // 몬스터 ID는 1000부터 시작 (플레이어 ID와 구분)

    // NEW : 충돌 맵 추가
    private CollisionMap collisionMap;

    // 콜백: 메시지를 방의 모든 플레이어에게 전송
    public interface MessageCallback {
        void broadcast(int roomId, Object message);
        void sendToPlayer(int playerId, Object message);  // 특정 플레이어에게 전송
    }

    private MessageCallback messageCallback;

    // 플레이어 HP 관리 (roomId -> (playerId -> HP))
    private Map<Integer, Map<Integer, Integer>> roomPlayerHp = new HashMap<>();
    private static final int PLAYER_MAX_HP = 100;

    // NEW : 생성자에 CollisionMap 매개변수 추가
    public ServerMonsterManager(MessageCallback callback, CollisionMap collisionMap) {
        this.messageCallback = callback;
        this.collisionMap = collisionMap;
    }

    // NEW : 클라이언트 GameMap.isWall()과 동일한 로직
    public boolean isWall(float x, float y) {
        if (collisionMap == null) {
            return false;
        }
        return collisionMap.isWall(x, y);
    }

    // NEW : 클라이언트 GameMap.isWallInArea()와 동일한 로직
    public boolean isWallInArea(float centerX, float centerY, float radius) {
        if (collisionMap == null) {
            return false;
        }
        return collisionMap.isWallInArea(centerX, centerY, radius);
    }

    /**
     * 룸의 몬스터 리스트 초기화
     */
    public void initializeRoom(int roomId) {
        roomMonsters.put(roomId, new ArrayList<>());
        roomSpawnTimers.put(roomId, 0.0f);  // 각 방의 스폰 타이머 초기화
        roomPlayerHp.put(roomId, new HashMap<>());  // 플레이어 HP 맵 초기화
        System.out.println("[ServerMonsterManager] 룸 " + roomId + " 몬스터 시스템 초기화됨");
    }

    /**
     * 플레이어 HP 초기화 (게임 시작 시)
     */
    public void initializePlayerHp(int roomId, int playerId) {
        Map<Integer, Integer> hpMap = roomPlayerHp.get(roomId);
        if (hpMap != null) {
            hpMap.put(playerId, PLAYER_MAX_HP);
            System.out.println("[ServerMonsterManager] 플레이어 " + playerId + " HP 초기화: " + PLAYER_MAX_HP);
        }
    }

    /**
     * 플레이어 HP 가져오기
     */
    public int getPlayerHp(int roomId, int playerId) {
        Map<Integer, Integer> hpMap = roomPlayerHp.get(roomId);
        if (hpMap != null && hpMap.containsKey(playerId)) {
            return hpMap.get(playerId);
        }
        return PLAYER_MAX_HP;
    }

    /**
     * 플레이어 HP 설정
     */
    public void setPlayerHp(int roomId, int playerId, int hp) {
        Map<Integer, Integer> hpMap = roomPlayerHp.get(roomId);
        if (hpMap != null) {
            hpMap.put(playerId, Math.max(0, Math.min(hp, PLAYER_MAX_HP)));
        }
    }

    /**
     * 룸 종료 시 몬스터 정리
     */
    public void cleanupRoom(int roomId) {
        roomMonsters.remove(roomId);
        roomSpawnTimers.remove(roomId);
        roomPlayerHp.remove(roomId);
    }

    /**
     * 매 프레임 업데이트 (메인 게임 루프에서 호출)
     * @param delta 프레임 시간 (초)
     * @param roomId 업데이트할 룸 ID
     * @param activePlayers 룸의 활성 플레이어들 (AI 판정용)
     * @param playerPositions 플레이어 위치 맵 (key: playerId, value: [x, y])
     */
    public void update(float delta, int roomId, List<Integer> activePlayers, Map<Integer, float[]> playerPositions) {
        List<ServerMonster> monsters = roomMonsters.get(roomId);
        if (monsters == null) {
            System.err.println("[ServerMonsterManager] 룸 " + roomId + " 찾을 수 없음!");
            return;
        }

        // 새 몬스터 스폰: 50마리 미만이면 계속 스폰
        Float spawnTimer = roomSpawnTimers.get(roomId);
        if (spawnTimer == null) spawnTimer = 0.0f;
        spawnTimer += delta;

        if (spawnTimer >= SPAWN_INTERVAL) {
            // 50마리 미만이면 부족한 만큼 스폰
            int monstersToSpawn = MAX_MONSTERS_PER_ROOM - monsters.size();
            if (monstersToSpawn > 0) {
                // 한 번에 최대 5마리까지만 스폰 (서버 부하 방지)
                int spawnCount = Math.min(monstersToSpawn, 5);
                System.out.println("[몬스터 스폰] 룸 " + roomId + ": " + spawnCount + "마리 스폰 시도 (현재: " + monsters.size() + "/" + MAX_MONSTERS_PER_ROOM + ")");

                for (int i = 0; i < spawnCount; i++) {
                    spawnMonster(roomId, activePlayers);
                }
            } else {
                // 50마리 다 찼을 때는 로그 출력 안 함 (너무 많음)
            }
            spawnTimer = 0.0f;
        }

        roomSpawnTimers.put(roomId, spawnTimer);

        // NEW : 모든 몬스터 업데이트 (플레이어 위치 + 충돌 맵 전달)
        Iterator<ServerMonster> iterator = monsters.iterator();
        while (iterator.hasNext()) {
            ServerMonster monster = iterator.next();
            monster.update(delta, activePlayers, playerPositions, this);  // NEW : this(ServerMonsterManager) 전달

            // ===== 몬스터 → 플레이어 공격 처리 (PHASE_25) =====
            if (monster.canAttackNow()) {
                Integer targetId = monster.getTargetPlayerId();
                if (targetId != null) {
                    // fog 구역 검증: 플레이어가 건물(fog 구역) 안에 있을 때만 공격
                    float[] targetPos = playerPositions.get(targetId);
                    if (targetPos != null && collisionMap != null) {
                        String targetZone = collisionMap.getFogZoneAt(targetPos[0], targetPos[1]);
                        if (targetZone == null) {
                            // 플레이어가 fog 구역 밖에 있으면 공격하지 않음
                            continue;
                        }
                    }

                    int damage = monster.getAttackDamage();
                    int currentHp = getPlayerHp(roomId, targetId);
                    int newHp = Math.max(0, currentHp - damage);
                    setPlayerHp(roomId, targetId, newHp);

                    // 공격 메시지 전송
                    MonsterAttackPlayerMsg attackMsg = new MonsterAttackPlayerMsg(
                        monster.id, targetId, damage, newHp, PLAYER_MAX_HP
                    );
                    messageCallback.broadcast(roomId, attackMsg);

                    System.out.println("[몬스터 공격] " + monster.getType() + "(ID:" + monster.id +
                        ") → 플레이어 " + targetId + " (데미지: " + damage + ", HP: " + newHp + "/" + PLAYER_MAX_HP + ")");
                }
            }

            // 위치 동기화 (100ms마다)
            if (monster.shouldSyncPosition()) {
                sendMonsterUpdate(roomId, monster);
            }

            // 죽은 몬스터 처리
            if (!monster.isAlive()) {
                System.out.println("[몬스터 사망] 룸 " + roomId + ": ID=" + monster.id + " 타입=" + monster.getType() + " (남은 몬스터: " + (monsters.size() - 1) + "마리)");
                // 아이템 드롭 위치 전송
                sendMonsterDeath(roomId, monster);
                iterator.remove();
            }
        }
    }

    /**
     * 랜덤 몬스터 스폰
     * fog 구역(건물 내부) 안에서만 스폰하여 잔디/외부에 스폰되지 않도록 합니다.
     */
    private void spawnMonster(int roomId, List<Integer> activePlayers) {
        if (activePlayers.isEmpty()) return;

        // 실제 맵 크기 가져오기 (CollisionMap에서)
        float actualMapWidth = (collisionMap != null) ? collisionMap.getMapWidth() : MAP_WIDTH;
        float actualMapHeight = (collisionMap != null) ? collisionMap.getMapHeight() : MAP_HEIGHT;

        // 벽이 아닌 위치를 찾을 때까지 반복 (최대 50번 시도)
        float x = 0, y = 0;
        boolean validPosition = false;
        int attempts = 0;

        while (!validPosition && attempts < 50) {
            // 실제 맵 범위에서 랜덤 스폰 (맵 가장자리 100픽셀 제외)
            float margin = 100f;
            x = margin + (float)(Math.random() * (actualMapWidth - margin * 2));
            y = margin + (float)(Math.random() * (actualMapHeight - margin * 2));

            // 중앙에서 멀리 떨어진 곳만 스폰 (시작 위치 보호)
            float centerX = actualMapWidth / 2f;
            float centerY = actualMapHeight / 2f;
            float distance = (float)Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));

            // 조건: 중앙 보호 + 벽이 아닌 곳 + fog 구역 내부(건물 내부)
            boolean notNearCenter = distance >= 400f;
            boolean notWall = !isWallInArea(x, y, 32f);
            boolean insideBuilding = (collisionMap != null && collisionMap.isInsideAnyFogZone(x, y));

            if (notNearCenter && notWall && insideBuilding) {
                validPosition = true;
            }

            attempts++;
        }

        // 유효한 위치를 못 찾으면 스폰 포기
        if (!validPosition) {
            System.err.println("[몬스터 스폰] 룸 " + roomId + ": 유효한 스폰 위치를 찾지 못함 (50번 시도 실패)");
            return;
        }

        ServerMonster monster = createRandomMonster(nextMonsterId++, x, y);
        if (monster != null) {
            List<ServerMonster> monsters = roomMonsters.get(roomId);
            if (monsters != null) {
                monsters.add(monster);
                sendMonsterSpawn(roomId, monster);
            }
        }
    }

    /**
     * 랜덤 몬스터 타입 생성
     */
    private ServerMonster createRandomMonster(int id, float x, float y) {
        int typeRoll = (int)(Math.random() * 3);

        switch (typeRoll) {
            case 0:  // Ghost
                return new ServerMonster.Ghost(id, x, y);
            case 1:  // Bat
                return new ServerMonster.Bat(id, x, y);
            case 2:  // Golem
                return new ServerMonster.Golem(id, x, y);
            default:
                return new ServerMonster.Ghost(id, x, y);
        }
    }

    /**
     * 몬스터 스폰 메시지 전송
     */
    private void sendMonsterSpawn(int roomId, ServerMonster monster) {
        MonsterSpawnMsg msg = new MonsterSpawnMsg();
        msg.monsterId = monster.id;
        msg.x = monster.x;
        msg.y = monster.y;
        msg.monsterType = monster.getType();
        msg.elementType = monster.getElementType();

        System.out.println("[서버] 몬스터 스폰 메시지 전송 - ID: " + monster.id + ", 타입: " + monster.getType() +
            ", 위치: (" + monster.x + "," + monster.y + ")");
        messageCallback.broadcast(roomId, msg);
    }

    /**
     * 몬스터 위치/상태 업데이트 메시지 전송
     */
    private void sendMonsterUpdate(int roomId, ServerMonster monster) {
        MonsterUpdateMsg msg = new MonsterUpdateMsg();
        msg.monsterId = monster.id;
        msg.x = monster.x;
        msg.y = monster.y;
        msg.vx = monster.vx;
        msg.vy = monster.vy;
        msg.hp = monster.hp;
        msg.maxHp = monster.maxHp;
        msg.state = monster.getState();

        messageCallback.broadcast(roomId, msg);
        monster.markSynced();
    }

    /**
     * 몬스터 사망 메시지 전송
     */
    private void sendMonsterDeath(int roomId, ServerMonster monster) {
        MonsterDeathMsg msg = new MonsterDeathMsg();
        msg.monsterId = monster.id;
        msg.dropX = monster.x;
        msg.dropY = monster.y;
        msg.killerId = monster.getLastAttackerId();  // 막타친 플레이어 ID

        System.out.println("[몬스터 사망] 룸 " + roomId + ": 몬스터 ID=" + monster.id +
            ", 타입=" + monster.getType() + ", killerId=" + msg.killerId + " (lastAttackerId=" + monster.getLastAttackerId() + ")");

        messageCallback.broadcast(roomId, msg);
    }

    /**
     * 몬스터 피해 처리 (스킬 맞았을 때 서버에서 호출)
     * @param roomId 룸 ID
     * @param monsterId 몬스터 ID
     * @param damage 데미지
     * @param attackerId 공격자 ID
     */
    public void damageMonster(int roomId, int monsterId, int damage, int attackerId) {
        List<ServerMonster> monsters = roomMonsters.get(roomId);
        if (monsters == null) return;

        for (ServerMonster monster : monsters) {
            if (monster.id == monsterId) {
                // 데미지 적용 (attackerId 전달)
                monster.takeDamage(damage, attackerId);

                // 데미지 결과를 모든 클라이언트에게 브로드캐스트
                MonsterDamageMsg damageMsg = new MonsterDamageMsg();
                damageMsg.monsterId = monster.id;
                damageMsg.newHp = monster.hp;
                damageMsg.damageAmount = damage;
                damageMsg.attackerId = attackerId;

                messageCallback.broadcast(roomId, damageMsg);

                System.out.println("[ServerMonsterManager] 몬스터 피격: ID=" + monster.id +
                    ", 데미지=" + damage + ", 공격자 ID=" + attackerId +
                    ", 남은 HP=" + monster.hp + "/" + monster.maxHp + ", lastAttackerId=" + monster.getLastAttackerId());
                break;
            }
        }
    }

    /**
     * 룸의 활성 몬스터 개수 반환
     */
    public int getMonsterCount(int roomId) {
        List<ServerMonster> monsters = roomMonsters.get(roomId);
        return monsters != null ? monsters.size() : 0;
    }

    /**
     * 룸의 모든 활성 몬스터 반환
     */
    public List<ServerMonster> getAllMonsters(int roomId) {
        List<ServerMonster> monsters = roomMonsters.get(roomId);
        return monsters != null ? new java.util.ArrayList<>(monsters) : new java.util.ArrayList<>();
    }
}
