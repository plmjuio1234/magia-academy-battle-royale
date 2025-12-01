package com.example.yugeup.game.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.yugeup.game.monster.MonsterState;
import com.example.yugeup.game.monster.MonsterType;
import com.example.yugeup.utils.AssetManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 몬스터 애니메이션 시스템
 *
 * 몬스터의 상태별 애니메이션을 관리합니다.
 * IDLE, MOVING, ATTACKING, HIT, DEAD 상태에 따라 다른 프레임을 재생합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterAnimation {
    // 몬스터 타입
    private MonsterType type;

    // 텍스처 아틀라스
    private TextureAtlas atlas;

    // 상태별 애니메이션
    private Map<MonsterState, Animation<TextureRegion>> animations;

    // 현재 상태 경과 시간
    private float stateTime = 0f;

    // 현재 방향 (0=front, 1=back, 2=left, 3=right)
    private int direction = 0;

    /**
     * 몬스터 애니메이션 생성자
     *
     * @param type 몬스터 타입
     */
    public MonsterAnimation(MonsterType type) {
        this.type = type;
        this.animations = new HashMap<>();

        loadAtlas();
        loadAnimations();
    }

    /**
     * 아틀라스 로드
     */
    private void loadAtlas() {
        AssetManager assetManager = AssetManager.getInstance();
        String atlasName = type.name().toLowerCase();  // "ghost", "bat", "golem"

        try {
            atlas = assetManager.getAtlas(atlasName);
            System.out.println("[MonsterAnimation] 아틀라스 로드 성공: " + atlasName);
        } catch (Exception e) {
            System.out.println("[MonsterAnimation] 아틀라스 로드 실패: " + atlasName);
            e.printStackTrace();
        }
    }

    /**
     * 애니메이션 로드
     *
     * 각 몬스터 타입별로 상태에 맞는 애니메이션을 로드합니다.
     */
    private void loadAnimations() {
        if (atlas == null) {
            System.out.println("[MonsterAnimation] 아틀라스가 null입니다!");
            return;
        }

        // 몬스터 타입명 소문자로 변환
        String typePrefix = type.name().toLowerCase();

        // IDLE: front 프레임 사용 (골렘은 front가 없으므로 move_right 사용)
        if (type == MonsterType.GOLEM) {
            animations.put(MonsterState.IDLE,
                createAnimation(typePrefix + "-move_right-", 4, 0.3f));
        } else {
            animations.put(MonsterState.IDLE,
                createAnimation(typePrefix + "-front-", 4, 0.2f));
        }

        // MOVING: move_right 프레임 사용 (기본)
        animations.put(MonsterState.MOVING,
            createAnimation(typePrefix + "-move_right-", 4, 0.15f));

        // ATTACKING: 골렘만 attack 애니메이션 있음
        if (type == MonsterType.GOLEM) {
            animations.put(MonsterState.ATTACKING,
                createAnimation(typePrefix + "-attack_right-", 5, 0.1f));
        } else {
            // 다른 몬스터는 front 애니메이션 사용
            animations.put(MonsterState.ATTACKING,
                createAnimation(typePrefix + "-front-", 4, 0.1f));
        }

        // HIT, DEAD: 골렘은 move_right 재사용
        if (type == MonsterType.GOLEM) {
            animations.put(MonsterState.HIT,
                createAnimation(typePrefix + "-move_right-", 2, 0.1f));
            animations.put(MonsterState.DEAD,
                createAnimation(typePrefix + "-move_right-", 1, 0.15f));
        } else {
            animations.put(MonsterState.HIT,
                createAnimation(typePrefix + "-front-", 2, 0.1f));
            animations.put(MonsterState.DEAD,
                createAnimation(typePrefix + "-front-", 1, 0.15f));
        }
    }

    /**
     * 애니메이션 생성
     *
     * @param framePrefix 프레임 접두사 (예: "ghost-front-")
     * @param frameCount 프레임 수
     * @param frameDuration 프레임 지속 시간
     * @return 생성된 애니메이션
     */
    private Animation<TextureRegion> createAnimation(String framePrefix,
                                                     int frameCount, float frameDuration) {
        if (atlas == null) {
            System.out.println("[MonsterAnimation] 아틀라스가 null입니다!");
            return null;
        }

        // 텍스처 아틀라스에서 프레임 가져오기
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            String frameName = framePrefix + i;
            TextureRegion frame = atlas.findRegion(frameName);

            if (frame != null) {
                frames[i] = frame;
                // System.out.println("[MonsterAnimation] 프레임 로드 성공: " + frameName);
            } else {
                System.out.println("[MonsterAnimation] 프레임 로드 실패: " + frameName);
                // 실패 시 첫 프레임 재사용 (있다면)
                if (i > 0 && frames[0] != null) {
                    frames[i] = frames[0];
                } else {
                    // 첫 프레임도 없으면 기본 프레임 생성
                    frames[i] = createDefaultFrame();
                }
            }
        }

        return new Animation<>(frameDuration, frames);
    }

    /**
     * 기본 프레임 생성 (폴백)
     *
     * 실제 텍스처가 없을 때 사용할 기본 프레임입니다.
     *
     * @return 기본 텍스처 리전
     */
    private TextureRegion createDefaultFrame() {
        // 폴백: 단색 텍스처 생성 (몬스터 타입별로 다른 색상)
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        // 몬스터 타입별 색상
        switch (type) {
            case GHOST:
                pixmap.setColor(0.7f, 0.7f, 1.0f, 0.8f);  // 연한 파란색 (반투명)
                break;
            case BAT:
                pixmap.setColor(0.5f, 0.3f, 0.5f, 1.0f);  // 보라색
                break;
            case GOLEM:
                pixmap.setColor(0.6f, 0.5f, 0.4f, 1.0f);  // 갈색
                break;
            default:
                pixmap.setColor(1.0f, 0.0f, 0.0f, 1.0f);  // 빨간색
                break;
        }
        pixmap.fill();

        com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();

        return new TextureRegion(texture);
    }

    /**
     * 현재 상태의 프레임 가져오기
     *
     * @param state 몬스터 상태
     * @return 현재 프레임
     */
    public TextureRegion getCurrentFrame(MonsterState state) {
        return getCurrentFrame(state, 1);  // 기본: 오른쪽
    }

    /**
     * 현재 상태와 방향의 프레임 가져오기
     *
     * @param state 몬스터 상태
     * @param direction 방향 (0=left, 1=right)
     * @return 현재 프레임
     */
    public TextureRegion getCurrentFrame(MonsterState state, int direction) {
        if (atlas == null) {
            return createDefaultFrame();
        }

        String typePrefix = type.name().toLowerCase();
        String directionSuffix = (direction == 0) ? "left" : "right";

        // MOVING 상태: 방향별 이동 애니메이션
        if (state == MonsterState.MOVING) {
            String frameName = typePrefix + "-move_" + directionSuffix + "-0";

            // 해당 방향 프레임이 있는지 확인
            if (atlas.findRegion(frameName) != null) {
                Animation<TextureRegion> dirAnimation = createAnimation(
                    typePrefix + "-move_" + directionSuffix + "-", 4, 0.15f);
                if (dirAnimation != null) {
                    return dirAnimation.getKeyFrame(stateTime, true);
                }
            }
        }

        // ATTACKING 상태: 방향별 공격 애니메이션 (골렘용)
        if (state == MonsterState.ATTACKING && type == MonsterType.GOLEM) {
            String frameName = typePrefix + "-attack_" + directionSuffix + "-0";

            // 해당 방향 공격 프레임이 있는지 확인
            if (atlas.findRegion(frameName) != null) {
                Animation<TextureRegion> attackAnimation = createAnimation(
                    typePrefix + "-attack_" + directionSuffix + "-", 5, 0.1f);
                if (attackAnimation != null) {
                    return attackAnimation.getKeyFrame(stateTime, false);
                }
            }
        }

        // IDLE 상태: 방향별 idle 애니메이션 (있으면) 또는 move 재사용
        if (state == MonsterState.IDLE) {
            // 골렘은 front가 없으므로 현재 방향의 move 첫 프레임 사용
            if (type == MonsterType.GOLEM) {
                String frameName = typePrefix + "-move_" + directionSuffix + "-0";
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    return frame;
                }
            }
        }

        // 기본 애니메이션 사용
        Animation<TextureRegion> animation = animations.get(state);

        if (animation == null) {
            return createDefaultFrame();
        }

        // 루프 여부 (사망은 루프 안 함)
        boolean looping = (state != MonsterState.DEAD);

        return animation.getKeyFrame(stateTime, looping);
    }

    /**
     * 매 프레임 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        stateTime += delta;
    }

    /**
     * 상태 시간 리셋
     *
     * 상태가 변경될 때 호출하여 애니메이션을 처음부터 재생합니다.
     */
    public void resetStateTime() {
        stateTime = 0f;
    }

    /**
     * 애니메이션이 완료되었는지 확인
     *
     * @param state 확인할 상태
     * @return 애니메이션 완료 여부
     */
    public boolean isAnimationFinished(MonsterState state) {
        Animation<TextureRegion> animation = animations.get(state);

        if (animation == null) {
            return true;
        }

        return animation.isAnimationFinished(stateTime);
    }
}
