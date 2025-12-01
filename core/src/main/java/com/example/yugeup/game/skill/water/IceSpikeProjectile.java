package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.utils.Constants;

/**
 * 아이스 스파이크 발사체 클래스
 *
 * 빠른 속도(150)로 날아가는 작은 얼음 가시입니다.
 * 타격 시 사라집니다. (관통 없음)
 * 각도에 따라 회전하지 않고 원본 방향 유지
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class IceSpikeProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> loopAnim;

    /**
     * 아이스 스파이크 발사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     * @param maxRange 최대 사거리
     */
    public IceSpikeProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "ice_spike-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);

        // 히트박스 10x10에 스케일 1배 적용
        this.renderSize = Constants.ICE_SPIKE_HITBOX_SIZE * Constants.ICE_SPIKE_SCALE;
        this.size = renderSize;

        // 애니메이션 로드
        this.loopAnim = SkillEffectManager.getInstance().getAnimation("ice_spike-loop");

        // 관통 (사거리 내 모든 적 타격)
        this.maxPierceCount = Integer.MAX_VALUE;

        // 얼음 원소: 청백색 (폴백용)
        setColor(0.6f, 0.9f, 1.0f);
    }

    /**
     * 업데이트 (사거리 체크 포함)
     */
    @Override
    public void update(float delta) {
        if (!isAlive) return;

        lifetime += delta;
        animationTime += delta;

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 사거리 체크
        traveledDistance = position.dst(startPosition);
        if (traveledDistance >= maxRange) {
            isAlive = false;
            return;
        }

        // 충돌 감지
        checkCollision();
    }

    /**
     * 렌더링 (각도 고정, 회전하지 않음)
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        if (loopAnim != null) {
            TextureRegion frame = loopAnim.getKeyFrame(animationTime, true);
            // 각도 고정 (회전하지 않음)
            batch.draw(frame,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
        } else if (texture != null) {
            batch.draw(texture,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
        }
    }

    /**
     * 충돌 감지 (부모 메서드 호출)
     */
    @Override
    protected void checkCollision() {
        super.checkCollision();
    }
}
