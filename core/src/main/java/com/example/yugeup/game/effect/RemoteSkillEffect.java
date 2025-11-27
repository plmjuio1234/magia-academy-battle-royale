package com.example.yugeup.game.effect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.SkillEffectManager;

/**
 * 원격 플레이어의 스킬 이펙트 클래스
 *
 * 다른 플레이어가 시전한 스킬의 시각 이펙트만 표시합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RemoteSkillEffect {
    private int skillId;
    private Vector2 startPosition;
    private Vector2 direction;
    private Animation<TextureRegion> animation;
    private float animationTime;
    private float lifetime;
    private float maxLifetime = 3.0f;  // 최대 수명
    private boolean isAlive;
    private float speed = 200f;  // 기본 속도
    private Vector2 currentPosition;
    private float size = 48f;
    private boolean isProjectile = true;  // 투사체 여부 (false면 Zone)

    /**
     * 원격 스킬 이펙트 생성자
     *
     * @param skillId 스킬 ID
     * @param startX 시작 X 좌표
     * @param startY 시작 Y 좌표
     * @param targetX 목표 X 좌표
     * @param targetY 목표 Y 좌표
     */
    public RemoteSkillEffect(int skillId, float startX, float startY, float targetX, float targetY) {
        this.skillId = skillId;
        this.startPosition = new Vector2(startX, startY);
        this.animationTime = 0f;
        this.lifetime = 0f;
        this.isAlive = true;

        // 방향 계산
        this.direction = new Vector2(targetX - startX, targetY - startY).nor();

        // 스킬 ID에 따라 애니메이션 로드 (isProjectile 설정됨)
        loadAnimationForSkill(skillId);

        // Zone 스킬은 targetPosition에 생성, 투사체는 시작 위치에서 시작
        if (isProjectile) {
            this.currentPosition = new Vector2(startX, startY);
        } else {
            this.currentPosition = new Vector2(targetX, targetY);
        }
    }

    /**
     * 스킬 ID에 따라 애니메이션을 로드합니다.
     *
     * @param skillId 스킬 ID
     */
    private void loadAnimationForSkill(int skillId) {
        SkillEffectManager manager = SkillEffectManager.getInstance();

        // 스킬 ID 매핑
        switch (skillId) {
            // 불 원소
            case 5101: // Fireball - 투사체
                animation = manager.getAnimation("fireball-loop");
                isProjectile = true;
                break;
            case 5102: // FlameWave - Zone
                animation = manager.getAnimation("flame_wave-loop");
                isProjectile = false;
                size = 120f;
                maxLifetime = 1.5f;
                break;
            case 5103: // Inferno - Zone
                animation = manager.getAnimation("inferno");
                isProjectile = false;
                size = 150f;
                maxLifetime = 2.0f;
                break;

            // 물 원소
            case 5201: // WaterShot - 투사체
                animation = manager.getAnimation("water_ball-loop");
                isProjectile = true;
                break;
            case 5202: // IceSpike - 투사체
                animation = manager.getAnimation("ice_spike-loop");
                isProjectile = true;
                break;
            case 5203: // Flood - Zone
                animation = manager.getAnimation("flood_loop");
                isProjectile = false;
                size = 100f;
                maxLifetime = 2.0f;
                break;

            // 바람 원소
            case 5301: // AirSlash - 투사체
                animation = manager.getAnimation("air_slash-loop");
                isProjectile = true;
                break;
            case 5302: // Tornado - Zone
                animation = manager.getAnimation("tornado-loop");
                isProjectile = false;
                size = 80f;
                maxLifetime = 2.0f;
                break;
            case 5303: // Storm - Zone
                animation = manager.getAnimation("storm-loop");
                isProjectile = false;
                size = 150f;
                maxLifetime = 3.0f;
                break;

            // 번개 원소
            case 5401: // LightningBolt - Zone
                animation = manager.getAnimation("lightning_volt");
                isProjectile = false;
                size = 60f;
                maxLifetime = 0.5f;
                break;
            case 5402: // ChainLightning - 투사체
                animation = manager.getAnimation("chain_lightning-projectile");
                isProjectile = true;
                break;
            case 5403: // ThunderStorm - Zone
                animation = manager.getAnimation("thunder_storm-lightning");
                isProjectile = false;
                size = 150f;
                maxLifetime = 3.0f;
                break;

            // 땅 원소
            case 5001: // RockSmash - 투사체
                animation = manager.getAnimation("rock_smash-start");
                isProjectile = true;
                break;
            case 5002: // EarthSpike - Zone
                animation = manager.getAnimation("earth_spike");
                isProjectile = false;
                size = 60f;
                maxLifetime = 1.0f;
                break;
            case 5003: // StoneShield - Zone (플레이어 추적)
                animation = manager.getAnimation("stone_shield-loop");
                isProjectile = false;
                size = 80f;
                maxLifetime = 5.0f;
                break;
        }
    }

    /**
     * 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        lifetime += delta;
        animationTime += delta;

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 투사체만 위치 업데이트 (Zone은 고정)
        if (isProjectile) {
            currentPosition.add(direction.x * speed * delta, direction.y * speed * delta);
        }
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isAlive || animation == null) {
            return;
        }

        TextureRegion frame = animation.getKeyFrame(animationTime, true);
        float angle = direction.angleDeg();

        batch.draw(frame,
            currentPosition.x - size / 2,
            currentPosition.y - size / 2,
            size / 2,
            size / 2,
            size,
            size,
            1f,
            1f,
            angle);
    }

    /**
     * 이펙트가 활성화되어 있는지 확인합니다.
     *
     * @return 활성화 상태
     */
    public boolean isAlive() {
        return isAlive;
    }
}
