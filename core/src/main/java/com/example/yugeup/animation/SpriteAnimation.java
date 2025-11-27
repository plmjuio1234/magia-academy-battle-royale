package com.example.yugeup.animation;

/**
 * 스프라이트 애니메이션 클래스
 *
 * 개별 스프라이트 애니메이션을 표현합니다.
 * 프레임, 재생 속도, 루프 등을 관리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SpriteAnimation {

    // 애니메이션 이름
    private String name;

    // 재생 시간
    private float duration;

    // 현재 시간
    private float elapsedTime;

    // 루프 여부
    private boolean looping;

    /**
     * 애니메이션을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_27에서 구현
    }

    /**
     * 애니메이션을 리셋합니다.
     */
    public void reset() {
        // TODO: PHASE_27에서 구현
    }

    /**
     * 애니메이션이 완료되었는지 확인합니다.
     *
     * @return 완료 여부
     */
    public boolean isFinished() {
        // TODO: PHASE_27에서 구현
        return false;
    }

    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getDuration() { return duration; }
    public void setDuration(float duration) { this.duration = duration; }

    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; }
}
