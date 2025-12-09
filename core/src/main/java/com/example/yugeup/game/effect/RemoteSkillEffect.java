package com.example.yugeup.game.effect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.messages.SkillCastMsg;

/**
 * 원격 플레이어의 스킬 이펙트 클래스
 *
 * 다른 플레이어가 시전한 스킬의 시각 이펙트만 표시합니다.
 * 다양한 스킬 타입을 지원합니다:
 * - Projectile (투사체): 방향으로 이동
 * - Zone Fixed (고정 Zone): 특정 위치에 고정
 * - Zone Moving (이동 Zone): 방향으로 이동하는 Zone
 * - Zone Player Follow (플레이어 추적 Zone): 플레이어를 따라다님
 * - Projectile Multi (다방향 투사체): 여러 방향으로 발사
 *
 * @author YuGeup Development Team
 * @version 2.0
 */
public class RemoteSkillEffect {
    private int skillId;
    private Vector2 startPosition;
    private Vector2 direction;
    private Animation<TextureRegion> animation;
    private float animationTime;
    private float lifetime;
    private float maxLifetime = 3.0f;
    private boolean isAlive;
    private float speed = 200f;
    private Vector2 currentPosition;
    private float size = 48f;
    private float width = 48f;   // 렌더링 너비 (비율 지원)
    private float height = 48f;  // 렌더링 높이 (비율 지원)

    // 스프라이트 뒤집기 (왼쪽 방향 시 Y축 플립)
    private boolean flipY = false;

    // 각도 고정 여부 (이동은 하지만 회전 안함)
    private boolean fixedAngle = false;

    // 스킬 타입 (새로운 동기화 방식)
    private int skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;

    // 플레이어 추적용 (플레이어 추적형 Zone)
    private Player followPlayer;

    // 다방향 발사체용 (메인 이펙트만 표시, 추가 발사체는 별도 생성)
    private int projectileIndex = 0;  // 다방향 발사 시 몇 번째 발사체인지

    /**
     * 원격 스킬 이펙트 생성자 (레거시)
     *
     * @param skillId 스킬 ID
     * @param startX 시작 X 좌표
     * @param startY 시작 Y 좌표
     * @param targetX 목표 X 좌표
     * @param targetY 목표 Y 좌표
     * @deprecated SkillCastMsg를 받는 생성자 사용 권장
     */
    @Deprecated
    public RemoteSkillEffect(int skillId, float startX, float startY, float targetX, float targetY) {
        this.skillId = skillId;
        this.startPosition = new Vector2(startX, startY);
        this.animationTime = 0f;
        this.lifetime = 0f;
        this.isAlive = true;

        // 방향 계산
        this.direction = new Vector2(targetX - startX, targetY - startY).nor();

        // 스킬 ID에 따라 애니메이션 로드
        loadAnimationForSkill(skillId);

        // 스킬 타입에 따라 시작 위치 결정
        determineStartPosition(startX, startY, targetX, targetY);
    }

    /**
     * 원격 스킬 이펙트 생성자 (SkillCastMsg 버전)
     * 스킬의 상세 정보를 SkillCastMsg에서 받아서 동적으로 설정합니다.
     *
     * @param msg 스킬 시전 메시지
     * @param startX 시작 X 좌표 (원격 플레이어 위치)
     * @param startY 시작 Y 좌표 (원격 플레이어 위치)
     */
    public RemoteSkillEffect(SkillCastMsg msg, float startX, float startY) {
        this.skillId = msg.skillId;
        this.animationTime = 0f;
        this.lifetime = 0f;
        this.isAlive = true;

        // 스킬 타입 설정 (메시지에서 전달된 값 사용)
        this.skillType = msg.skillType;

        // 시전자 위치 사용 (메시지에 포함된 경우)
        float casterX = msg.casterX > 0 ? msg.casterX : startX;
        float casterY = msg.casterY > 0 ? msg.casterY : startY;
        this.startPosition = new Vector2(casterX, casterY);

        // 방향 계산 (메시지에 directionX/Y가 있으면 사용, 없으면 계산)
        if (msg.directionX != 0 || msg.directionY != 0) {
            this.direction = new Vector2(msg.directionX, msg.directionY).nor();
        } else {
            this.direction = new Vector2(msg.targetX - casterX, msg.targetY - casterY).nor();
        }

        // 스킬 ID에 따라 애니메이션, 크기, 속도 등 모든 속성 로드
        // (원본 클라이언트와 동일한 값 사용 - msg 값으로 덮어쓰지 않음)
        loadAnimationForSkill(msg.skillId);

        // 스킬 타입에 따라 시작 위치 결정
        determineStartPositionByType(msg, casterX, casterY);
    }

    /**
     * 원격 스킬 이펙트 생성자 (플레이어 추적형)
     *
     * @param msg 스킬 시전 메시지
     * @param player 추적할 플레이어
     */
    public RemoteSkillEffect(SkillCastMsg msg, Player player) {
        this(msg, player.getX(), player.getY());
        this.followPlayer = player;
        this.skillType = ElementalSkill.SKILL_TYPE_ZONE_PLAYER_FOLLOW;
    }

    /**
     * 다방향 발사체용 생성자
     *
     * @param msg 스킬 시전 메시지
     * @param startX 시작 X
     * @param startY 시작 Y
     * @param index 발사체 인덱스 (0: 중앙, 1: 왼쪽, 2: 오른쪽 등)
     */
    public RemoteSkillEffect(SkillCastMsg msg, float startX, float startY, int index) {
        this(msg, startX, startY);
        this.projectileIndex = index;

        // 다방향 발사체의 방향 조정
        if (msg.projectileCount > 1 && msg.angleSpread > 0) {
            float baseAngle = direction.angleDeg();
            float angleOffset = 0;

            // 중앙(0), 왼쪽(-angleSpread), 오른쪽(+angleSpread) 순서로 계산
            if (index == 1) {
                angleOffset = -msg.angleSpread;
            } else if (index == 2) {
                angleOffset = msg.angleSpread;
            }

            float newAngle = baseAngle + angleOffset;
            this.direction = new Vector2(1, 0).setAngleDeg(newAngle);
        }
    }

    /**
     * 스킬 타입에 따라 시작 위치를 결정합니다. (새 버전)
     */
    private void determineStartPositionByType(SkillCastMsg msg, float casterX, float casterY) {
        switch (skillType) {
            case ElementalSkill.SKILL_TYPE_PROJECTILE:
            case ElementalSkill.SKILL_TYPE_PROJECTILE_MULTI:
                // 투사체: 시전자 위치에서 시작
                this.currentPosition = new Vector2(casterX, casterY);
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_FIXED:
                // 고정 Zone: 목표 위치에 생성
                this.currentPosition = new Vector2(msg.targetX, msg.targetY);
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_MOVING:
                // 이동 Zone: 시전자 위치에서 시작
                this.currentPosition = new Vector2(casterX, casterY);
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_PLAYER_FOLLOW:
                // 플레이어 추적 Zone: 시전자 위치에서 시작 (이후 플레이어 따라감)
                this.currentPosition = new Vector2(casterX, casterY);
                break;

            default:
                // 기본: 시전자 위치
                this.currentPosition = new Vector2(casterX, casterY);
                break;
        }
    }

    /**
     * 스킬 타입에 따라 시작 위치를 결정합니다. (레거시 호환)
     */
    private void determineStartPosition(float startX, float startY, float targetX, float targetY) {
        // 레거시: isProjectile 플래그 기반으로 판단
        boolean isProjectile = (skillType == ElementalSkill.SKILL_TYPE_PROJECTILE ||
                                skillType == ElementalSkill.SKILL_TYPE_PROJECTILE_MULTI ||
                                skillType == ElementalSkill.SKILL_TYPE_ZONE_MOVING);

        if (isProjectile) {
            this.currentPosition = new Vector2(startX, startY);
        } else {
            this.currentPosition = new Vector2(targetX, targetY);
        }
    }

    /**
     * 스킬 ID에 따라 애니메이션과 기본 속성을 로드합니다.
     *
     * @param skillId 스킬 ID
     */
    private void loadAnimationForSkill(int skillId) {
        SkillEffectManager manager = SkillEffectManager.getInstance();

        switch (skillId) {
            // ===== 불 원소 =====
            case 5101: // Fireball - 투사체
                // 원본: FIREBALL_HITBOX_SIZE(12) * FIREBALL_SCALE(4) = 48px
                animation = manager.getAnimation("fireball-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = height = size = 48f;
                speed = 240f;  // 원본 200 * 1.2 (네트워크 보정)
                break;
            case 5102: // FlameWave - 투사체 (도트딜)
                // 원본: FLAME_WAVE_HITBOX_SIZE(16) * FLAME_WAVE_SCALE(4) = 64px
                // 원본: 왼쪽 방향(90~270도) 시 scaleY = -1
                animation = manager.getAnimation("flame_wave-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = height = size = 64f;
                speed = 240f;  // 원본 200 * 1.2 (네트워크 보정)
                flipY = true;  // 왼쪽 방향 시 Y축 플립
                break;
            case 5103: // Inferno - 고정 Zone
                // 원본: INFERNO_HITBOX_SIZE(60) * INFERNO_SCALE(3) = 180px
                // 원본: 각도 고정, Y오프셋 40f (여기서는 생략)
                animation = manager.getAnimation("inferno");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_FIXED;
                width = height = size = 180f;
                maxLifetime = 1.5f;
                break;

            // ===== 물 원소 =====
            case 5201: // WaterShot - 투사체
                // 원본: WATER_SHOT_HITBOX_SIZE(24) * WATER_SHOT_SCALE(2.5) = 60px
                animation = manager.getAnimation("water_ball-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = height = size = 60f;
                speed = 180f;  // 원본 150 * 1.2 (네트워크 보정)
                break;
            case 5202: // IceSpike - 다방향 투사체
                // 원본: ICE_SPIKE_HITBOX_SIZE(10) * ICE_SPIKE_SCALE(3) = 30px
                // 원본: 이동은 하지만 각도 고정 (회전 안함)
                animation = manager.getAnimation("ice_spike-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE_MULTI;
                width = height = size = 30f;
                speed = 360f;  // 원본 300 * 1.2 (네트워크 보정)
                fixedAngle = true;  // 각도 고정
                break;
            case 5203: // Flood - 투사체 (관통 도트딜)
                // 원본: FLOOD_HITBOX_WIDTH(60)*3=180, FLOOD_HITBOX_HEIGHT(90)*3=270
                // 원본: 이동은 하지만 각도 고정 (회전 안함)
                animation = manager.getAnimation("flood_loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = 180f;   // 60 * 3
                height = 270f;  // 90 * 3
                size = 270f;
                speed = 96f;   // 원본 80 * 1.2 (네트워크 보정)
                fixedAngle = true;  // 각도 고정
                break;

            // ===== 바람 원소 =====
            case 5301: // AirSlash - 투사체
                // 원본: AIR_SLASH_HITBOX_WIDTH(24)*3=72, AIR_SLASH_HITBOX_HEIGHT(10)*3=30
                // 원본: 회전 O, 왼쪽(90~270도) 시 scaleY = -1
                animation = manager.getAnimation("air_slash-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = 72f;   // 24 * 3
                height = 30f;  // 10 * 3
                size = 72f;
                speed = 240f;  // 원본 200 * 1.2 (네트워크 보정)
                flipY = true;  // 왼쪽 방향 시 Y축 플립
                break;
            case 5302: // Tornado - 투사체
                // 원본: TORNADO_HITBOX_SIZE(18) * TORNADO_SCALE(3) = 54px
                // 원본: 이동은 하지만 각도 고정
                animation = manager.getAnimation("tornado-loop");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = height = size = 54f;
                speed = 240f;  // 원본 200 * 1.2 (네트워크 보정)
                fixedAngle = true;  // 각도 고정
                break;
            case 5303: // Storm - 플레이어 추적 Zone
                // 원본: STORM_HITBOX_SIZE(64) * STORM_SCALE(2) = 128px
                // 원본: 플레이어 추적, 각도 고정
                animation = manager.getAnimation("storm-loop");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_PLAYER_FOLLOW;
                width = height = size = 128f;
                maxLifetime = 8.0f;
                break;

            // ===== 번개 원소 =====
            case 5401: // LightningBolt - 고정 Zone
                // 원본: 64 * LIGHTNING_BOLT_SCALE(1.5) = 96px
                // 원본: 고정 Zone, 각도 고정
                animation = manager.getAnimation("lightning_volt");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_FIXED;
                width = height = size = 96f;
                maxLifetime = 0.56f;  // 7프레임 * 0.08초
                break;
            case 5402: // ChainLightning - 투사체
                // 원본: CHAIN_LIGHTNING_HITBOX_SIZE(40) * CHAIN_LIGHTNING_SCALE(2.5) = 100px
                // 원본: 회전 O (방향각)
                animation = manager.getAnimation("chain_lightning-projectile");
                skillType = ElementalSkill.SKILL_TYPE_PROJECTILE;
                width = height = size = 100f;
                speed = 216f;  // 원본 180 * 1.2 (네트워크 보정)
                break;
            case 5403: // ThunderStorm - 이동 Zone
                // 원본: 64 * THUNDER_STORM_LIGHTNING_SCALE_X(3.5) = 224, 64 * _Y(1.8) = 115
                // 원본: 이동 Zone, 각도 고정
                animation = manager.getAnimation("thunder_storm-lightning");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_MOVING;
                width = 224f;   // 64 * 3.5
                height = 115f;  // 64 * 1.8
                size = 224f;
                speed = 20f;
                maxLifetime = 10.0f;  // 사거리 200 / 속도 20
                fixedAngle = true;  // 각도 고정
                break;

            // ===== 땅 원소 =====
            case 5001: // RockSmash - 고정 Zone
                // 원본: ROCK_SMASH_HITBOX_SIZE(48) * ROCK_SMASH_SCALE(2) = 96px
                // 원본: 고정 Zone, 각도 고정
                animation = manager.getAnimation("rock_smash-start");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_FIXED;
                width = height = size = 96f;
                maxLifetime = 1.5f;
                break;
            case 5002: // EarthSpike - 이동 Zone
                // 원본: EARTH_SPIKE_HITBOX_WIDTH(36)*3=108, _HEIGHT(24)*3=72
                // 원본: 이동 Zone, 각도 고정
                animation = manager.getAnimation("earth_spike");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_MOVING;
                width = 108f;   // 36 * 3
                height = 72f;   // 24 * 3
                size = 108f;
                speed = 300f;  // 원본 250 * 1.2 (네트워크 보정)
                fixedAngle = true;  // 각도 고정
                break;
            case 5003: // StoneShield - 플레이어 추적 Zone
                // 원본: 64 * STONE_SHIELD_SCALE(1.2) = 약 77px
                // 원본: 플레이어 추적, 각도 고정
                animation = manager.getAnimation("stone_shield-loop");
                skillType = ElementalSkill.SKILL_TYPE_ZONE_PLAYER_FOLLOW;
                width = height = size = 77f;
                maxLifetime = 8.0f;
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

        // 스킬 타입에 따른 위치 업데이트
        switch (skillType) {
            case ElementalSkill.SKILL_TYPE_PROJECTILE:
            case ElementalSkill.SKILL_TYPE_PROJECTILE_MULTI:
                // 투사체: 방향으로 이동
                currentPosition.add(direction.x * speed * delta, direction.y * speed * delta);
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_FIXED:
                // 고정 Zone: 위치 변경 없음
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_MOVING:
                // 이동 Zone: 방향으로 이동
                currentPosition.add(direction.x * speed * delta, direction.y * speed * delta);
                break;

            case ElementalSkill.SKILL_TYPE_ZONE_PLAYER_FOLLOW:
                // 플레이어 추적 Zone: 플레이어 위치로 이동
                if (followPlayer != null) {
                    currentPosition.set(followPlayer.getX(), followPlayer.getY());
                }
                break;
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

        // 투사체와 이동 Zone은 방향에 따라 회전, 고정 Zone 또는 fixedAngle은 회전 없음
        float angle = 0;
        if (!fixedAngle && (skillType == ElementalSkill.SKILL_TYPE_PROJECTILE ||
            skillType == ElementalSkill.SKILL_TYPE_PROJECTILE_MULTI ||
            skillType == ElementalSkill.SKILL_TYPE_ZONE_MOVING)) {
            angle = direction.angleDeg();
        }

        // 렌더링 크기 (원본 스킬과 동일한 비율 사용)
        float renderWidth = this.width;
        float renderHeight = this.height;

        // Y축 플립 결정 (왼쪽 방향 90~270도 시)
        float scaleY = 1f;
        if (flipY && angle > 90 && angle < 270) {
            scaleY = -1f;
        }

        batch.draw(frame,
            currentPosition.x - renderWidth / 2,
            currentPosition.y - renderHeight / 2,
            renderWidth / 2,
            renderHeight / 2,
            renderWidth,
            renderHeight,
            1f,
            scaleY,
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

    /**
     * 추적할 플레이어를 설정합니다. (플레이어 추적형 Zone용)
     *
     * @param player 추적할 플레이어
     */
    public void setFollowPlayer(Player player) {
        this.followPlayer = player;
    }

    /**
     * 스킬 타입을 반환합니다.
     *
     * @return 스킬 타입
     */
    public int getSkillType() {
        return skillType;
    }

    /**
     * 스킬 ID를 반환합니다.
     *
     * @return 스킬 ID
     */
    public int getSkillId() {
        return skillId;
    }
}
