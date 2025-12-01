package com.example.yugeup.game.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fog 구역 관리자
 *
 * TMX 맵의 fog 레이어들을 관리하고 시간에 따라 활성화합니다.
 * 2분마다 랜덤으로 한 구역씩 fog가 활성화되며,
 * town-square(중앙 광장)는 마지막(10분)에 활성화됩니다.
 *
 * @author YuGeup Development Team
 * @version 2.0
 */
public class ZoneManager {

    // 모든 fog 구역 (이름 -> Zone)
    private Map<String, Zone> zones;

    // fog 활성화 순서 (랜덤, town-square는 마지막)
    private List<String> activationOrder;

    // 현재 활성화된 구역 인덱스
    private int currentActivationIndex;

    // 게임 경과 시간 (초)
    private float gameTime;

    // fog 활성화 간격 (초) - 2분
    private static final float FOG_ACTIVATION_INTERVAL = 120f;

    // fog 구역 이름들
    private static final String[] FOG_ZONE_NAMES = {
        "town-square",    // 중앙 광장 (마지막에 활성화)
        "dormitory",      // 기숙사
        "library",        // 도서관
        "classroom",      // 교실
        "alchemy-room"    // 연금술실
    };

    // 리스너 (fog 활성화 시 GameMap에 알림)
    private FogActivationListener listener;

    /**
     * ZoneManager 생성자
     */
    public ZoneManager() {
        this.zones = new HashMap<>();
        this.activationOrder = new ArrayList<>();
        this.currentActivationIndex = 0;
        this.gameTime = 0f;

        // Zone 객체 생성
        for (String name : FOG_ZONE_NAMES) {
            zones.put(name, new Zone(name));
        }

        // 활성화 순서 생성 (town-square 제외, 랜덤 셔플)
        generateActivationOrder();

        System.out.println("[ZoneManager] 초기화 완료");
        System.out.println("[ZoneManager] fog 활성화 순서: " + activationOrder);
    }

    /**
     * fog 활성화 순서를 생성합니다.
     * town-square는 마지막에 활성화됩니다.
     */
    private void generateActivationOrder() {
        activationOrder.clear();

        // town-square 제외한 나머지 구역들
        for (String name : FOG_ZONE_NAMES) {
            if (!name.equals("town-square")) {
                activationOrder.add(name);
            }
        }

        // 랜덤 셔플
        Collections.shuffle(activationOrder);

        // town-square는 마지막에 추가
        activationOrder.add("town-square");
    }

    /**
     * ZoneManager를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        gameTime += delta;

        // 각 Zone 업데이트
        for (Zone zone : zones.values()) {
            zone.update(delta);
        }

        // fog 활성화 체크 (2분마다)
        checkFogActivation();
    }

    /**
     * fog 활성화 시점을 체크합니다.
     */
    private void checkFogActivation() {
        // 아직 활성화할 구역이 남아있는지 확인
        if (currentActivationIndex >= activationOrder.size()) {
            return;
        }

        // 다음 활성화 시간 계산
        float nextActivationTime = (currentActivationIndex + 1) * FOG_ACTIVATION_INTERVAL;

        // 시간이 되면 다음 구역 활성화
        if (gameTime >= nextActivationTime) {
            String zoneName = activationOrder.get(currentActivationIndex);
            activateFog(zoneName);
            currentActivationIndex++;
        }
    }

    /**
     * 특정 구역의 fog를 활성화합니다.
     *
     * @param zoneName 구역 이름
     */
    public void activateFog(String zoneName) {
        Zone zone = zones.get(zoneName);
        if (zone != null && !zone.isActive()) {
            zone.activate();

            // 리스너에게 알림 (GameMap이 TMX 레이어 표시)
            if (listener != null) {
                listener.onFogActivated(zoneName);
            }

            int activatedCount = getActivatedCount();
            System.out.println("[ZoneManager] ★ fog 활성화: " + zoneName +
                " (" + activatedCount + "/" + activationOrder.size() + ")");
        }
    }

    /**
     * 특정 구역의 fog 활성화 상태를 설정합니다. (서버 동기화용)
     *
     * @param zoneName 구역 이름
     * @param active 활성화 여부
     */
    public void setFogActive(String zoneName, boolean active) {
        Zone zone = zones.get(zoneName);
        if (zone != null) {
            if (active && !zone.isActive()) {
                zone.activate();
                if (listener != null) {
                    listener.onFogActivated(zoneName);
                }
            } else if (!active && zone.isActive()) {
                zone.setActive(false);
            }
        }
    }

    /**
     * 특정 좌표가 활성화된 fog 구역 안에 있는지 확인합니다.
     * (실제 충돌 판정은 GameMap에서 TMX 타일 기반으로 수행)
     *
     * @param zoneName 구역 이름
     * @return 해당 구역의 fog가 활성화되어 있으면 true
     */
    public boolean isFogActive(String zoneName) {
        Zone zone = zones.get(zoneName);
        return zone != null && zone.isActive();
    }

    /**
     * 활성화된 fog 구역 개수를 반환합니다.
     *
     * @return 활성화된 구역 수
     */
    public int getActivatedCount() {
        int count = 0;
        for (Zone zone : zones.values()) {
            if (zone.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 활성화된 모든 fog 구역 이름을 반환합니다.
     *
     * @return 활성화된 구역 이름 리스트
     */
    public List<String> getActivatedZoneNames() {
        List<String> activeNames = new ArrayList<>();
        for (Zone zone : zones.values()) {
            if (zone.isActive()) {
                activeNames.add(zone.getName());
            }
        }
        return activeNames;
    }

    /**
     * 게임 경과 시간을 반환합니다.
     *
     * @return 게임 시간 (초)
     */
    public float getGameTime() {
        return gameTime;
    }

    /**
     * 게임 시간을 설정합니다. (서버 동기화용)
     *
     * @param time 게임 시간 (초)
     */
    public void setGameTime(float time) {
        this.gameTime = time;
    }

    /**
     * 다음 fog 활성화까지 남은 시간을 반환합니다.
     *
     * @return 남은 시간 (초), 모두 활성화되었으면 -1
     */
    public float getTimeUntilNextFog() {
        if (currentActivationIndex >= activationOrder.size()) {
            return -1;
        }
        float nextActivationTime = (currentActivationIndex + 1) * FOG_ACTIVATION_INTERVAL;
        return nextActivationTime - gameTime;
    }

    /**
     * fog 활성화 리스너를 설정합니다.
     *
     * @param listener 리스너
     */
    public void setListener(FogActivationListener listener) {
        this.listener = listener;
    }

    /**
     * 모든 fog 구역을 반환합니다.
     *
     * @return Zone 맵
     */
    public Map<String, Zone> getZones() {
        return zones;
    }

    /**
     * fog 활성화 순서를 반환합니다.
     *
     * @return 활성화 순서 리스트
     */
    public List<String> getActivationOrder() {
        return activationOrder;
    }

    /**
     * Fog 활성화 리스너 인터페이스
     */
    public interface FogActivationListener {
        /**
         * fog가 활성화되었을 때 호출됩니다.
         *
         * @param zoneName 활성화된 구역 이름
         */
        void onFogActivated(String zoneName);
    }
}
