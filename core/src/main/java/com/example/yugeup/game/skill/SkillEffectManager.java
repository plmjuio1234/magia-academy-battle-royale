package com.example.yugeup.game.skill;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * 스킬 이펙트 애니메이션 관리자
 *
 * skills.atlas에서 애니메이션을 로드하고 관리합니다.
 * 싱글톤 패턴으로 구현되어 전역적으로 접근 가능합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillEffectManager {
    private static SkillEffectManager instance;
    private TextureAtlas skillsAtlas;
    private Map<String, Animation<TextureRegion>> animations = new HashMap<>();

    /**
     * private 생성자 (싱글톤 패턴)
     */
    private SkillEffectManager() {
    }

    /**
     * 싱글톤 인스턴스 반환
     *
     * @return SkillEffectManager 인스턴스
     */
    public static SkillEffectManager getInstance() {
        if (instance == null) {
            instance = new SkillEffectManager();
        }
        return instance;
    }

    /**
     * 아틀라스 로드 및 애니메이션 초기화
     *
     * @param atlas 스킬 이펙트 아틀라스
     */
    public void loadAtlas(TextureAtlas atlas) {
        this.skillsAtlas = atlas;
        loadAllAnimations();
    }

    /**
     * 모든 애니메이션 로드
     */
    private void loadAllAnimations() {
        System.out.println("[SkillEffectManager] 애니메이션 로드 시작");

        // Fireball (4프레임, 루프)
        loadAnimation("fireball-loop", 0, 3, 0.08f, Animation.PlayMode.LOOP);

        // Ice Spike (10프레임, 루프)
        loadAnimation("ice_spike-loop", 0, 9, 0.08f, Animation.PlayMode.LOOP);

        // Flame Wave (6프레임, 루프)
        loadAnimation("flame_wave-loop", 0, 5, 0.1f, Animation.PlayMode.LOOP);

        // Water Ball (17프레임, 루프)
        loadAnimation("water_ball-loop", 0, 16, 0.06f, Animation.PlayMode.LOOP);

        // Air Slash (6프레임, 루프)
        loadAnimation("air_slash-loop", 0, 5, 0.08f, Animation.PlayMode.LOOP);

        // Lightning Volt (7프레임, 루프)
        loadAnimation("lightning_volt", 0, 6, 0.08f, Animation.PlayMode.LOOP);

        // Chain Lightning (발사체 5프레임, 체인 4프레임)
        loadAnimation("chain_lightning-projectile", 0, 4, 0.08f, Animation.PlayMode.LOOP);
        loadAnimation("chain_lightning-chain", 0, 3, 0.08f, Animation.PlayMode.LOOP);

        // Storm (9프레임, 루프)
        loadAnimation("storm-loop", 0, 8, 0.12f, Animation.PlayMode.LOOP);

        // Tornado (5프레임, 루프)
        loadAnimation("tornado-loop", 0, 4, 0.1f, Animation.PlayMode.LOOP);

        // Inferno (18프레임, 루프)
        loadAnimation("inferno", 0, 17, 0.08f, Animation.PlayMode.LOOP);

        // Flood (8프레임, 루프)
        loadAnimation("flood_loop", 0, 7, 0.1f, Animation.PlayMode.LOOP);

        // Thunder Storm Lightning (12프레임, 루프)
        loadAnimation("thunder_storm-lightning", 0, 11, 0.1f, Animation.PlayMode.LOOP);

        // Earth Spike (8프레임, 일회)
        loadAnimation("earth_spike", 0, 7, 0.1f, Animation.PlayMode.NORMAL);

        // Rock Smash (6프레임, 일회)
        loadAnimation("rock_smash-start", 0, 5, 0.08f, Animation.PlayMode.NORMAL);

        // Stone Shield (9프레임, 루프)
        loadAnimation("stone_shield-loop", 0, 8, 0.12f, Animation.PlayMode.LOOP);

        System.out.println("[SkillEffectManager] 총 " + animations.size() + "개 애니메이션 로드 완료");
    }

    /**
     * 개별 애니메이션 로드
     *
     * @param baseName      애니메이션 기본 이름 (예: "fireball-loop")
     * @param start         시작 프레임 번호
     * @param end           끝 프레임 번호
     * @param frameDuration 프레임 지속 시간 (초)
     * @param mode          재생 모드 (LOOP, NORMAL 등)
     */
    private void loadAnimation(String baseName, int start, int end,
                                float frameDuration, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = start; i <= end; i++) {
            String frameName = baseName + "-" + i;
            TextureRegion frame = skillsAtlas.findRegion(frameName);
            if (frame != null) {
                frames.add(frame);
            }
        }

        if (frames.size > 0) {
            Animation<TextureRegion> animation =
                    new Animation<>(frameDuration, frames, mode);
            animations.put(baseName, animation);
            System.out.println("[SkillEffectManager] 애니메이션 로드: " + baseName +
                    " (" + frames.size + " 프레임)");
        } else {
            System.out.println("[SkillEffectManager] 애니메이션 로드 실패: " + baseName);
        }
    }

    /**
     * 애니메이션 가져오기
     *
     * @param name 애니메이션 이름
     * @return Animation 객체 (없으면 null)
     */
    public Animation<TextureRegion> getAnimation(String name) {
        return animations.get(name);
    }

    /**
     * 로드된 애니메이션 개수 반환
     *
     * @return 애니메이션 개수
     */
    public int getAnimationCount() {
        return animations.size();
    }
}
