package com.example.yugeup.game.monster;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 몬스터 관리 클래스
 *
 * 게임 내 모든 몬스터를 관리합니다.
 * 서버로부터 받은 몬스터 정보를 기반으로 생성/업데이트/삭제를 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterManager {

    // 활성화된 몬스터 목록
    private List<Monster> monsters;

    // GameMap 참조 (몬스터 벽 충돌 감지용)
    private com.example.yugeup.game.map.GameMap gameMap;

    /**
     * MonsterManager를 생성합니다.
     */
    public MonsterManager() {
        this.monsters = new ArrayList<>();
    }

    /**
     * GameMap 설정 (몬스터 벽 충돌 감지용)
     *
     * @param gameMap 게임 맵
     */
    public void setGameMap(com.example.yugeup.game.map.GameMap gameMap) {
        this.gameMap = gameMap;
        // 기존 몬스터들에게도 GameMap 설정
        for (Monster monster : monsters) {
            monster.setGameMap(gameMap);
        }
    }

    /**
     * 몬스터를 추가합니다.
     *
     * @param monster 추가할 몬스터
     */
    public void addMonster(Monster monster) {
        if (monster == null) {
            System.out.println("[MonsterManager] null 몬스터는 추가할 수 없습니다.");
            return;
        }

        // 이미 존재하는지 확인
        for (Monster m : monsters) {
            if (m.getMonsterId() == monster.getMonsterId()) {
                System.out.println("[MonsterManager] 이미 존재하는 몬스터 ID: " + monster.getMonsterId());
                return;
            }
        }

        // GameMap 설정 (벽 충돌 감지용)
        if (gameMap != null) {
            monster.setGameMap(gameMap);
        }

        monsters.add(monster);
        System.out.println("[MonsterManager] 몬스터 추가: ID=" + monster.getMonsterId() +
                          ", Type=" + monster.getType());
    }

    /**
     * 몬스터를 제거합니다.
     *
     * @param monsterId 제거할 몬스터 ID
     */
    public void removeMonster(int monsterId) {
        Iterator<Monster> iterator = monsters.iterator();
        while (iterator.hasNext()) {
            Monster monster = iterator.next();
            if (monster.getMonsterId() == monsterId) {
                iterator.remove();
                System.out.println("[MonsterManager] 몬스터 제거: ID=" + monsterId);
                return;
            }
        }
        System.out.println("[MonsterManager] 제거할 몬스터를 찾을 수 없음: ID=" + monsterId);
    }

    /**
     * 몬스터 객체로 제거
     *
     * @param monster 제거할 몬스터
     */
    public void removeMonster(Monster monster) {
        if (monsters.remove(monster)) {
            System.out.println("[MonsterManager] 몬스터 제거: ID=" + monster.getMonsterId());
        }
    }

    /**
     * ID로 몬스터를 찾습니다.
     *
     * @param monsterId 몬스터 ID
     * @return 몬스터 객체 (없으면 null)
     */
    public Monster getMonster(int monsterId) {
        for (Monster monster : monsters) {
            if (monster.getMonsterId() == monsterId) {
                return monster;
            }
        }
        return null;
    }

    // 몬스터 사망 리스너
    private MonsterDeathListener deathListener;

    /**
     * 몬스터 사망 리스너 인터페이스
     */
    public interface MonsterDeathListener {
        void onMonsterDeath(Monster monster);
    }

    /**
     * 몬스터 사망 리스너 설정
     *
     * @param listener 리스너
     */
    public void setDeathListener(MonsterDeathListener listener) {
        this.deathListener = listener;
    }

    /**
     * 모든 몬스터를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 사망한 몬스터를 임시 리스트에 저장
        java.util.List<Monster> deadMonsters = new java.util.ArrayList<>();

        // 모든 몬스터 업데이트 및 사망 체크
        for (Monster monster : monsters) {
            monster.update(delta);

            if (monster.isDead() && !deadMonsters.contains(monster)) {
                deadMonsters.add(monster);
            }
        }

        // 사망한 몬스터 제거 및 리스너 호출
        for (Monster deadMonster : deadMonsters) {
            System.out.println("[MonsterManager] 사망한 몬스터 제거: ID=" + deadMonster.getMonsterId());

            // 먼저 리스트에서 제거
            monsters.remove(deadMonster);

            // 그 다음 리스너 호출 (경험치 획득 등)
            if (deathListener != null) {
                deathListener.onMonsterDeath(deadMonster);
            }
        }
    }

    /**
     * 모든 몬스터를 렌더링합니다.
     *
     * @param batch 스프라이트 배치
     */
    public void render(SpriteBatch batch) {
        for (Monster monster : monsters) {
            if (!monster.isDead()) {
                monster.render(batch);
            }
        }
    }

    /**
     * 모든 몬스터를 제거합니다.
     */
    public void clear() {
        monsters.clear();
        System.out.println("[MonsterManager] 모든 몬스터 제거됨");
    }

    /**
     * 몬스터 수를 반환합니다.
     *
     * @return 현재 몬스터 수
     */
    public int getMonsterCount() {
        return monsters.size();
    }

    // Getter
    public List<Monster> getMonsters() { return monsters; }
}
