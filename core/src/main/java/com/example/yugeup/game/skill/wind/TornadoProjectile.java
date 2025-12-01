package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.utils.Constants;

/**
 * 토네이도 투사체 클래스
 *
 * 빠른 속도(200)로 날아가는 회오리바람입니다.
 * 사거리 500, 히트박스 18x18
 * 각도에 따라 회전하지 않고 원본 방향 유지
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class TornadoProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> loopAnim;

    /**
     * 토네이도 투사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     * @param maxRange 최대 사거리
     */
    public TornadoProjectile(Vector2 origin, float directionX, float directionY,
                             int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "tornado-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);

        // 히트박스 18x18에 스케일 적용
        this.renderSize = Constants.TORNADO_HITBOX_SIZE * Constants.TORNADO_SCALE;
        this.size = renderSize;

        // 애니메이션 로드
        this.loopAnim = SkillEffectManager.getInstance().getAnimation("tornado-loop");

        // 관통 (사거리 내 모든 적 타격)
        this.maxPierceCount = Integer.MAX_VALUE;

        // 바람 원소: 연한 청록색 (폴백용)
        setColor(0.8f, 0.95f, 1.0f);

        System.out.println("[TornadoProjectile] 생성! 방향: (" + directionX + ", " + directionY + "), 속도: " + speed);
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
