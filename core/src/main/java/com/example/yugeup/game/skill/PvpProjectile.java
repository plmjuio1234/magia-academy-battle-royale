package com.example.yugeup.game.skill;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;

/**
 * PVP 발사체 클래스
 *
 * 플레이어를 타겟으로 하는 발사체입니다.
 * 다른 플레이어에게 데미지를 입히며, 서버로 공격 메시지를 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PvpProjectile {
    // 위치 및 이동
    private Vector2 position;
    private Vector2 velocity;
    private float speed;

    // 타겟 플레이어
    private Player target;
    private boolean isHoming;  // 유도 미사일 여부

    // 데미지
    private int damage;

    // 공격자 ID
    private int attackerId;

    // 상태
    private boolean isAlive;
    private float lifetime;
    private float maxLifetime = 5.0f;  // 최대 수명 (5초)

    // 렌더링
    private Texture texture;
    private float size = 24f;

    // 충돌 반경 (동적 계산)
    private float collisionRadius;

    /**
     * PvpProjectile 생성자
     *
     * @param origin 발사 위치
     * @param target 타겟 플레이어
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param attackerId 공격자 플레이어 ID
     */
    public PvpProjectile(Vector2 origin, Player target, int damage, float speed, int attackerId) {
        this.position = new Vector2(origin);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.attackerId = attackerId;
        this.isHoming = true;
        this.isAlive = true;
        this.lifetime = 0f;

        // 발사체 충돌 반경 사용 (몬스터/플레이어 공통)
        this.collisionRadius = Constants.PROJECTILE_COLLISION_RADIUS;

        // 초기 방향 설정
        this.velocity = new Vector2();
        updateVelocity();

        // 텍스처 생성
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

        // 플레이어 좌표 (원격 플레이어는 targetPosition 사용)
        // 플레이어 position은 이미 스프라이트 중앙 기준이므로 오프셋 불필요
        Vector2 playerPos = target.isRemote() ? target.getTargetPosition() : target.getPosition();

        // 타겟 플레이어 좌표 (position이 이미 중앙)
        float centerX = playerPos.x;
        float centerY = playerPos.y;
        Vector2 targetPosVec = new Vector2(centerX, centerY);

        // 방향 계산
        Vector2 dir = targetPosVec.cpy().sub(position).nor();

        velocity.set(dir).scl(speed);
    }

    /**
     * 충돌 감지 (플레이어와 충돌 시 서버로 PVP 공격 메시지 전송)
     */
    private void checkCollision() {
        if (target == null || target.isDead()) {
            isAlive = false;
            return;
        }

        // 플레이어 좌표 (position이 이미 중앙, 원격 플레이어는 targetPosition 사용)
        Vector2 playerPos = target.isRemote() ? target.getTargetPosition() : target.getPosition();
        float centerX = playerPos.x;
        float centerY = playerPos.y;

        // 거리 계산
        float dx = centerX - position.x;
        float dy = centerY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 충돌 판정
        if (distance <= collisionRadius) {
            // 서버로 PVP 공격 메시지 전송
            sendPvpAttackMessage();

            // 발사체 즉시 소멸
            isAlive = false;
        }
    }

    /**
     * PVP 공격 메시지를 서버로 전송합니다.
     */
    private void sendPvpAttackMessage() {
        NetworkManager networkManager = NetworkManager.getInstance();
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendPvpAttack(target.getPlayerId(), damage, "MagicMissile");
            System.out.println("[PvpProjectile] PVP 공격 메시지 전송: 타겟=" + target.getPlayerId() + ", 데미지=" + damage);
        }
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (texture != null && isAlive) {
            // 날아가는 방향 계산
            float angle = velocity.angleDeg();

            // 회전 적용하여 렌더링
            batch.draw(texture,
                position.x - size / 2,
                position.y - size / 2,
                size / 2,
                size / 2,
                size,
                size,
                1f,
                1f,
                angle,
                0, 0,
                texture.getWidth(),
                texture.getHeight(),
                false, false);
        }
    }

    /**
     * 텍스처 생성 (빨간색 원 - PVP 공격 표시)
     */
    private void createTexture() {
        try {
            // PNG 파일 로드 시도
            texture = new Texture(com.badlogic.gdx.Gdx.files.internal("skills/magicmissile.png"));
        } catch (Exception e) {
            // 폴백: 빨간색 원
            Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pixmap.setColor(1.0f, 0.3f, 0.3f, 1.0f);  // 빨간색 (PVP 공격)
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
