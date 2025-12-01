package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.utils.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 썬더 스톰 지역 클래스
 *
 * 보는 방향으로 이동하는 비구름과 그 아래 번개를 생성합니다.
 * 구름: 속도 20, 사거리 200, 각도 고정
 * 번개: 구름 아래 60칸, 54x54 히트박스, 각도 고정
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ThunderStormZone {

    // 존 상태
    private enum ZoneState {
        MOVING,     // 이동 중
        FINISHED    // 종료
    }

    // 현재 상태
    private ZoneState state;

    // 구름 위치 (상단)
    private Vector2 cloudPosition;

    // 번개 위치 (구름 아래 60칸)
    private Vector2 lightningPosition;

    // 시작 위치
    private Vector2 startPosition;

    // 방향 및 속도
    private float directionX;
    private float directionY;
    private float speed;
    private float range;
    private float traveledDistance;

    // 데미지
    private int damage;

    // 활성 상태
    private boolean isActive;

    // 구름-번개 오프셋 (60칸)
    private float cloudOffsetY;

    // 히트박스 크기 (타원형)
    private float lightningHitboxWidth;
    private float lightningHitboxHeight;

    // 렌더링 크기 (가로/세로 각각)
    private float cloudRenderWidth;
    private float cloudRenderHeight;
    private float lightningRenderWidth;
    private float lightningRenderHeight;

    // 애니메이션
    private Animation<TextureRegion> cloudAnimation;
    private Animation<TextureRegion> lightningAnimation;
    private float animationTime = 0f;

    // 몬스터 목록 참조
    private transient List<Monster> monsterList;

    // 이미 피격한 몬스터 (중복 피격 방지 - 프레임당)
    private Set<Monster> hitMonstersThisFrame;

    // 데미지 틱 타이머
    private float damageTickTimer = 0f;
    private static final float DAMAGE_TICK_INTERVAL = 0.3f;

    /**
     * 썬더 스톰 지역 생성자
     *
     * @param startX 시작 X 좌표
     * @param startY 시작 Y 좌표
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     */
    public ThunderStormZone(float startX, float startY, float directionX, float directionY, int damage) {
        // 구름은 플레이어 위 60칸에서 시작
        this.cloudOffsetY = Constants.THUNDER_STORM_CLOUD_OFFSET_Y;
        this.cloudPosition = new Vector2(startX, startY + cloudOffsetY);
        this.lightningPosition = new Vector2(startX, startY);
        this.startPosition = new Vector2(startX, startY);

        this.directionX = directionX;
        this.directionY = directionY;
        this.damage = damage;
        this.speed = Constants.THUNDER_STORM_CLOUD_SPEED;
        this.range = Constants.THUNDER_STORM_RANGE;
        this.traveledDistance = 0f;
        this.isActive = true;
        this.state = ZoneState.MOVING;

        // 히트박스 크기 (타원형 - 렌더링과 동일)
        this.lightningHitboxWidth = Constants.THUNDER_STORM_LIGHTNING_HITBOX_WIDTH;
        this.lightningHitboxHeight = Constants.THUNDER_STORM_LIGHTNING_HITBOX_HEIGHT;

        // 렌더링 크기 (64 기본 * 가로/세로 별도 스케일)
        this.cloudRenderWidth = 64f * Constants.THUNDER_STORM_CLOUD_SCALE_X;
        this.cloudRenderHeight = 64f * Constants.THUNDER_STORM_CLOUD_SCALE_Y;
        this.lightningRenderWidth = 64f * Constants.THUNDER_STORM_LIGHTNING_SCALE_X;
        this.lightningRenderHeight = 64f * Constants.THUNDER_STORM_LIGHTNING_SCALE_Y;

        // 초기화
        this.hitMonstersThisFrame = new HashSet<>();

        // 애니메이션 로드
        SkillEffectManager sem = SkillEffectManager.getInstance();
        this.cloudAnimation = sem.getAnimation("thunder_storm-cloud");
        this.lightningAnimation = sem.getAnimation("thunder_storm-lightning");

        System.out.println("[ThunderStormZone] 생성! 방향: (" + directionX + ", " + directionY + ")");
    }

    /**
     * 몬스터 목록 설정
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        if (state == ZoneState.MOVING) {
            // 구름 이동
            float moveDistance = speed * delta;
            cloudPosition.x += directionX * moveDistance;
            cloudPosition.y += directionY * moveDistance;
            traveledDistance += moveDistance;

            // 번개 위치 업데이트 (구름 아래 60칸)
            lightningPosition.x = cloudPosition.x;
            lightningPosition.y = cloudPosition.y - cloudOffsetY;

            // 데미지 틱 처리
            damageTickTimer += delta;
            if (damageTickTimer >= DAMAGE_TICK_INTERVAL) {
                damageTickTimer = 0f;
                hitMonstersThisFrame.clear();
                applyDamage();
            }

            // 사거리 초과 시 종료
            if (traveledDistance >= range) {
                state = ZoneState.FINISHED;
                isActive = false;
                System.out.println("[ThunderStormZone] 사거리 도달, 종료!");
            }
        }
    }

    /**
     * 범위 내 몬스터에게 데미지 적용
     */
    private void applyDamage() {
        if (monsterList == null) return;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (hitMonstersThisFrame.contains(monster)) continue;

            // 타원형 히트박스 충돌 판정 (렌더링과 동일)
            float dx = monster.getX() - lightningPosition.x;
            float dy = monster.getY() - lightningPosition.y;

            // 타원 방정식: (dx/a)^2 + (dy/b)^2 <= 1
            float halfWidth = lightningHitboxWidth / 2;
            float halfHeight = lightningHitboxHeight / 2;
            float normalizedDist = (dx * dx) / (halfWidth * halfWidth) + (dy * dy) / (halfHeight * halfHeight);

            // 히트박스 내 몬스터에게 데미지
            if (normalizedDist <= 1.0f) {
                monster.takeDamage(damage);
                hitMonstersThisFrame.add(monster);
                System.out.println("[ThunderStormZone] 몬스터에게 " + damage + " 데미지!");
            }
        }
    }

    /**
     * 렌더링 (각도 고정)
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isActive) return;

        // 번개 렌더링 (아래) - 가로/세로 별도 크기
        if (lightningAnimation != null) {
            TextureRegion lightningFrame = lightningAnimation.getKeyFrame(animationTime, true);
            if (lightningFrame != null) {
                batch.draw(lightningFrame,
                    lightningPosition.x - lightningRenderWidth / 2,
                    lightningPosition.y - lightningRenderHeight / 2,
                    lightningRenderWidth, lightningRenderHeight);
            }
        }

        // 구름 렌더링 (위) - 가로/세로 별도 크기
        if (cloudAnimation != null) {
            TextureRegion cloudFrame = cloudAnimation.getKeyFrame(animationTime, true);
            if (cloudFrame != null) {
                batch.draw(cloudFrame,
                    cloudPosition.x - cloudRenderWidth / 2,
                    cloudPosition.y - cloudRenderHeight / 2,
                    cloudRenderWidth, cloudRenderHeight);
            }
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
     * 구름 위치 반환
     *
     * @return 구름 위치
     */
    public Vector2 getCloudPosition() {
        return cloudPosition;
    }

    /**
     * 번개 위치 반환
     *
     * @return 번개 위치
     */
    public Vector2 getLightningPosition() {
        return lightningPosition;
    }
}
