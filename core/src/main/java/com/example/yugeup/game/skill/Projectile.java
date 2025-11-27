package com.example.yugeup.game.skill;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.network.NetworkManager;

/**
 * 발사체 클래스
 *
 * 스킬의 발사체를 나타냅니다.
 * 위치, 속도, 타겟 추적, 충돌 감지를 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Projectile {
    // 위치 및 이동
    private Vector2 position;
    private Vector2 velocity;
    private float speed;

    // 타겟
    private Monster target;
    private boolean isHoming;  // 유도 미사일 여부

    // 데미지
    private int damage;

    // 상태
    private boolean isAlive;
    private float lifetime;
    private float maxLifetime = 5.0f;  // 최대 수명 (5초)

    // 렌더링
    private Texture texture;
    private float size = 24f;  // 16 → 24로 50% 증가
    private static final float COLLISION_RADIUS = 12f;  // 8 → 12로 증가

    // 네트워크
    private Vector2 playerPosition;  // 발사 시점의 플레이어 위치 (서버 검증용)

    /**
     * Projectile 생성자
     *
     * @param origin 발사 위치
     * @param target 타겟 몬스터
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param playerPos 플레이어 위치 (서버 검증용)
     */
    public Projectile(Vector2 origin, Monster target, int damage, float speed, Vector2 playerPos) {
        this.position = new Vector2(origin);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.isHoming = true;
        this.isAlive = true;
        this.lifetime = 0f;
        this.playerPosition = new Vector2(playerPos);

        // 초기 방향 설정
        this.velocity = new Vector2();
        updateVelocity();

        // 텍스처 (임시)
        createTexture();
    }

    /**
     * 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        lifetime += delta;

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 타겟 사망 시 직진
        if (target == null || target.isDead()) {
            isHoming = false;
        }

        // 유도 미사일
        if (isHoming) {
            updateVelocity();
        }

        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 충돌 감지
        checkCollision();
    }

    /**
     * 속도 업데이트 (유도)
     */
    private void updateVelocity() {
        if (target == null || target.isDead()) {
            return;
        }

        // 몬스터 중앙 좌표 계산
        float centerX = target.getX() + target.getWidth() / 2;
        float centerY = target.getY() + target.getHeight() / 2;
        Vector2 targetPos = new Vector2(centerX, centerY);

        Vector2 direction = targetPos.sub(position).nor();
        velocity.set(direction).scl(speed);
    }

    /**
     * 충돌 감지 (피격 즉시 소멸)
     */
    private void checkCollision() {
        if (target == null || target.isDead()) {
            // 타겟이 이미 죽었으면 즉시 발사체 소멸
            isAlive = false;
            return;
        }

        // 몬스터 중앙 좌표 계산
        float centerX = target.getX() + target.getWidth() / 2;
        float centerY = target.getY() + target.getHeight() / 2;

        // 거리 계산 (몬스터 중앙 기준)
        float dx = centerX - position.x;
        float dy = centerY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 충돌 판정
        if (distance <= COLLISION_RADIUS) {
            // 서버로 공격 메시지 전송 (서버가 데미지 처리)
            NetworkManager networkManager = NetworkManager.getInstance();
            if (networkManager != null) {
                networkManager.sendAttackMessage(target.getMonsterId(), damage, playerPosition.x, playerPosition.y);
            }

            // 발사체 즉시 소멸 (피격 이펙트 남지 않음)
            isAlive = false;
        }
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (texture != null && isAlive) {
            // 날아가는 방향 계산 (velocity 벡터의 각도)
            float angle = velocity.angleDeg();

            // 회전 적용하여 렌더링
            // draw(Texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY)
            batch.draw(texture,
                position.x - size / 2,  // x
                position.y - size / 2,  // y
                size / 2,               // originX (회전 중심)
                size / 2,               // originY (회전 중심)
                size,                   // width
                size,                   // height
                1f,                     // scaleX
                1f,                     // scaleY
                angle,                  // rotation (degrees)
                0, 0,                   // srcX, srcY
                texture.getWidth(),     // srcWidth
                texture.getHeight(),    // srcHeight
                false, false);          // flipX, flipY
        }
    }

    /**
     * 텍스처 생성 (매직 미사일 이미지 로드)
     */
    private void createTexture() {
        try {
            // PNG 파일 로드
            texture = new Texture(com.badlogic.gdx.Gdx.files.internal("skills/magicmissile.png"));
            System.out.println("[Projectile] magicmissile.png 로드 성공");
        } catch (Exception e) {
            // 폴백: 기존 Pixmap 방식 (보라색 원)
            System.out.println("[Projectile] magicmissile.png 로드 실패, Pixmap 사용: " + e.getMessage());
            Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.7f, 0.3f, 1.0f, 1.0f);  // 보라색 (매직 미사일)
            pixmap.fillCircle(8, 8, 6);
            texture = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    // ===== Getters =====

    public boolean isAlive() {
        return isAlive;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getDamage() {
        return damage;
    }
}
