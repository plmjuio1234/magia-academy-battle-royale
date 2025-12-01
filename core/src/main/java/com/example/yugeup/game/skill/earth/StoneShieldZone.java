package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.utils.Constants;

/**
 * 스톤 실드 지역 클래스
 *
 * 플레이어를 따라다니며 방어막 이펙트를 표시합니다.
 * start 애니메이션 후 loop 애니메이션 진행
 * 각도 고정
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StoneShieldZone {

    // 존 상태
    private enum ZoneState {
        START,      // 시작 애니메이션
        LOOP,       // 루프 애니메이션
        FINISHED    // 종료
    }

    // 현재 상태
    private ZoneState state;

    // 플레이어 참조 (따라다니기 위함)
    private Player owner;

    // 존 위치
    private Vector2 position;

    // 지속시간
    private float maxDuration;
    private float remainingDuration;
    private boolean isActive;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> startAnimation;
    private Animation<TextureRegion> loopAnimation;
    private float animationTime = 0f;

    /**
     * 스톤 실드 지역 생성자
     *
     * @param owner 스킬 소유자 (플레이어)
     * @param duration 지속시간
     */
    public StoneShieldZone(Player owner, float duration) {
        this.owner = owner;
        this.position = new Vector2(owner.getX(), owner.getY());
        this.maxDuration = duration;
        this.remainingDuration = duration;
        this.isActive = true;
        this.state = ZoneState.START;

        // 렌더링 크기
        this.renderSize = 64f * Constants.STONE_SHIELD_SCALE;

        // 애니메이션 로드
        SkillEffectManager sem = SkillEffectManager.getInstance();
        this.startAnimation = sem.getAnimation("stone_shield-start");
        this.loopAnimation = sem.getAnimation("stone_shield-loop");

        System.out.println("[StoneShieldZone] 생성! 지속시간: " + duration + "초");
    }

    /**
     * 업데이트 시 플레이어 위치를 따라갑니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        // 지속시간 감소
        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
            state = ZoneState.FINISHED;
            System.out.println("[StoneShieldZone] 종료!");
            return;
        }

        // 상태 전환: start → loop
        if (state == ZoneState.START) {
            if (startAnimation != null && startAnimation.isAnimationFinished(animationTime)) {
                state = ZoneState.LOOP;
                animationTime = 0f;
                System.out.println("[StoneShieldZone] loop 애니메이션 시작");
            }
        }

        // 플레이어 위치 추적
        if (owner != null) {
            position.set(owner.getX(), owner.getY());
        }
    }

    /**
     * 렌더링 (각도 고정)
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isActive) return;

        TextureRegion frame = null;

        if (state == ZoneState.START && startAnimation != null) {
            frame = startAnimation.getKeyFrame(animationTime, false);
        } else if (state == ZoneState.LOOP && loopAnimation != null) {
            frame = loopAnimation.getKeyFrame(animationTime, true);
        }

        if (frame != null) {
            // 각도 고정 (회전하지 않음)
            batch.draw(frame,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
        }
    }

    /**
     * 활성 상태 확인
     *
     * @return 활성 여부
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 위치 반환
     *
     * @return 현재 위치
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * 렌더링 크기 반환
     *
     * @return 렌더링 크기
     */
    public float getRenderSize() {
        return renderSize;
    }
}
