package com.example.yugeup.ui.hud;

import java.util.ArrayList;
import java.util.List;

/**
 * 킬 로그 UI 컴포넌트
 *
 * 플레이어 처치 내역을 화면에 표시합니다.
 * 일정 시간 후 자동으로 사라집니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class KillLogComponent {

    // 킬 로그 목록
    private List<String> killLogs;

    /**
     * KillLogComponent를 생성합니다.
     */
    public KillLogComponent() {
        this.killLogs = new ArrayList<>();
    }

    /**
     * 킬 로그를 추가합니다.
     *
     * @param killerName 처치자 이름
     * @param victimName 피해자 이름
     */
    public void addKillLog(String killerName, String victimName) {
        // TODO: PHASE_25에서 구현
    }

    /**
     * 킬 로그를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_25에서 구현
    }

    /**
     * 킬 로그를 렌더링합니다.
     */
    public void render() {
        // TODO: PHASE_25에서 구현
    }
}
