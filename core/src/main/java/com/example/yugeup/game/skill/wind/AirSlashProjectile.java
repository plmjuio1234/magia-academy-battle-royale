package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.utils.Constants;

/**
 * 에어 슬래시 검기 발사체 클래스
 *
 * 근접 공격이 실패했을 때 발사되는 검기입니다.
 * 사거리 120, 속도 100, 히트박스 24x10
 * 각도에 따라 회전하며 날아갑니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class AirSlashProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    // 렌더링 크기
    private float renderWidth;
    private float renderHeight;

    // 애니메이션
    private Animation<TextureRegion> loopAnim;

    // 발사 각도
    private float angle;

    /**
     * 에어 슬래시 검기 투사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도
     * @param maxRange 최대 사거리
     */
    public AirSlashProjectile(Vector2 origin, float directionX, float directionY,
                              int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "air_slash-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);

        // 히트박스 24x10에 스케일 적용
        this.renderWidth = Constants.AIR_SLASH_HITBOX_WIDTH * Constants.AIR_SLASH_SCALE;
        this.renderHeight = Constants.AIR_SLASH_HITBOX_HEIGHT * Constants.AIR_SLASH_SCALE;
        this.size = Math.max(renderWidth, renderHeight);

        // 애니메이션 로드
        this.loopAnim = SkillEffectManager.getInstance().getAnimation("air_slash-loop");

        // 발사 각도 계산
        this.angle = new Vector2(directionX, directionY).angleDeg();

        // 타격 시 사라짐 (관통 없음)
        this.maxPierceCount = 1;

        // 바람 원소: 밝은 회색/흰색 (폴백용)
        setColor(0.9f, 0.9f, 0.95f);

        System.out.println("[AirSlashProjectile] 검기 생성! 방향: (" + directionX + ", " + directionY + "), 각도: " + angle);
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
     * 렌더링 (발사 방향으로 회전)
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        if (loopAnim != null) {
            TextureRegion frame = loopAnim.getKeyFrame(animationTime, true);

            // 왼쪽 방향(90~270도)일 때 Y축 플립
            float scaleY = 1f;
            float renderAngle = angle;
            if (angle > 90 && angle < 270) {
                scaleY = -1f;
            }

            batch.draw(frame,
                position.x - renderWidth / 2,
                position.y - renderHeight / 2,
                renderWidth / 2,
                renderHeight / 2,
                renderWidth,
                renderHeight,
                1f, scaleY,
                renderAngle);
        } else if (texture != null) {
            batch.draw(texture,
                position.x - renderWidth / 2,
                position.y - renderHeight / 2,
                renderWidth, renderHeight);
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
