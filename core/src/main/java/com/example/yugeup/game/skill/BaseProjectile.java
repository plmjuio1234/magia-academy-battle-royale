package com.example.yugeup.game.skill;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.network.NetworkManager;
import java.util.ArrayList;
import java.util.List;

/**
 * 발사체 기본 클래스
 *
 * 스킬의 발사체를 나타내는 추상 클래스입니다.
 * 유도, 직진, 관통 등 다양한 발사체 타입을 지원합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class BaseProjectile {

    // 위치 및 이동
    protected Vector2 position;
    protected Vector2 velocity;
    protected float speed;

    // 방향 (비유도 발사체용)
    protected Vector2 direction;

    // 타겟
    protected Monster target;
    protected boolean isHoming;  // 유도 미사일 여부

    // 데미지
    protected int damage;

    // 관통 기능
    protected int maxPierceCount;  // 최대 관통 수
    protected int currentPierceCount;  // 현재 관통 수
    protected List<Integer> hitMonsterIds;  // 이미 타격한 몬스터 ID

    // 상태
    protected boolean isAlive;
    protected float lifetime;
    protected float maxLifetime = 5.0f;  // 최대 수명 (5초)

    // 렌더링
    protected Texture texture;
    protected float size = 48f;  // 발사체 크기 (16 → 48, 3배 증가)
    protected static final float COLLISION_RADIUS = 20f;  // 충돌 반경
    protected float red = 0.5f;    // 기본 색상: 파란색
    protected float green = 0.5f;
    protected float blue = 1.0f;
    protected float alpha = 1.0f;

    // 애니메이션 (PHASE_24)
    protected Animation<TextureRegion> animation;
    protected float animationTime = 0f;
    protected String animationName;

    // 피격판정용 (PHASE_24)
    protected List<Monster> monsterList;
    protected NetworkManager networkManager;

    /**
     * 발사체 생성자 (유도 미사일)
     *
     * @param origin 발사 위치
     * @param target 타겟 몬스터
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     */
    public BaseProjectile(Vector2 origin, Monster target, int damage, float speed) {
        this.position = new Vector2(origin);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.isHoming = true;
        this.isAlive = true;
        this.lifetime = 0f;

        // 초기 방향 설정
        this.velocity = new Vector2();
        this.direction = new Vector2();
        updateVelocity();

        // 관통 초기화
        this.maxPierceCount = 1;  // 기본값: 관통 안 함
        this.currentPierceCount = 0;
        this.hitMonsterIds = new ArrayList<>();

        // 텍스처 (임시)
        createTexture();
    }

    /**
     * 발사체 생성자 (직진 미사일)
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     */
    public BaseProjectile(Vector2 origin, float directionX, float directionY, int damage, float speed) {
        this(origin, directionX, directionY, damage, speed, null);
    }

    /**
     * 발사체 생성자 (직진 미사일, 애니메이션 지원)
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param animationName 애니메이션 이름 (null이면 기본 텍스처 사용)
     */
    public BaseProjectile(Vector2 origin, float directionX, float directionY, int damage, float speed, String animationName) {
        this.position = new Vector2(origin);
        this.target = null;
        this.damage = damage;
        this.speed = speed;
        this.isHoming = false;
        this.isAlive = true;
        this.lifetime = 0f;

        // 방향 설정 및 정규화
        this.direction = new Vector2(directionX, directionY).nor();
        this.velocity = new Vector2(direction).scl(speed);

        // 관통 초기화
        this.maxPierceCount = 1;  // 기본값: 관통 안 함
        this.currentPierceCount = 0;
        this.hitMonsterIds = new ArrayList<>();

        // 애니메이션 로드
        this.animationName = animationName;
        if (animationName != null) {
            this.animation = SkillEffectManager.getInstance().getAnimation(animationName);
        }

        // 애니메이션이 없으면 폴백으로 Pixmap 텍스처 생성
        if (this.animation == null) {
            createTexture();
        }
    }

    /**
     * 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        lifetime += delta;
        animationTime += delta;  // 애니메이션 시간 증가

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 타겟 사망 시 직진
        if (target != null && target.isDead()) {
            isHoming = false;
            target = null;
        }

        // 유도 미사일
        if (isHoming && target != null) {
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
    protected void updateVelocity() {
        if (target == null || target.isDead()) {
            return;
        }

        // 몬스터 중앙 좌표 계산
        float centerX = target.getX() + target.getWidth() / 2;
        float centerY = target.getY() + target.getHeight() / 2;
        Vector2 targetPos = new Vector2(centerX, centerY);

        Vector2 dir = targetPos.sub(position).nor();
        velocity.set(dir).scl(speed);
    }

    /**
     * 몬스터 목록 주입 메서드
     *
     * @param monsters 몬스터 목록
     */
    public void setMonsterList(List<Monster> monsters) {
        this.monsterList = monsters;
    }

    /**
     * 충돌 감지 (서버 동기화)
     */
    protected void checkCollision() {
        if (monsterList == null) {
            System.out.println("[BaseProjectile] checkCollision: monsterList is null!");
            return;
        }

        if (monsterList.isEmpty()) {
            System.out.println("[BaseProjectile] checkCollision: monsterList is empty!");
            return;
        }

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (hitMonsterIds.contains(monster.getMonsterId())) continue;

            // 거리 계산
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= COLLISION_RADIUS) {
                System.out.println("[BaseProjectile] 충돌 감지! 몬스터 ID=" + monster.getMonsterId() +
                    ", 거리=" + distance + ", 데미지=" + damage);

                // 서버로 공격 메시지 전송
                if (networkManager == null) {
                    networkManager = NetworkManager.getInstance();
                }
                if (networkManager != null) {
                    networkManager.sendAttackMessage(
                        monster.getMonsterId(),
                        damage,
                        position.x,
                        position.y);
                    System.out.println("[BaseProjectile] 서버로 공격 메시지 전송 완료");
                } else {
                    System.out.println("[BaseProjectile] ERROR: NetworkManager is null!");
                }

                // 관통 처리
                hitMonsterIds.add(monster.getMonsterId());
                currentPierceCount++;

                if (currentPierceCount >= maxPierceCount) {
                    isAlive = false;
                    break;
                }
            }
        }
    }

    /**
     * 몬스터와의 충돌을 처리합니다.
     *
     * @param monster 충돌한 몬스터
     * @return 충돌 처리 성공 여부
     */
    protected boolean handleMonsterCollision(Monster monster) {
        if (monster == null || monster.isDead()) {
            return false;
        }

        // 이미 타격한 몬스터 확인 (관통 기능)
        if (hitMonsterIds.contains(monster.getMonsterId())) {
            return false;
        }

        // 데미지 적용
        monster.takeDamage(damage);
        hitMonsterIds.add(monster.getMonsterId());
        currentPierceCount++;

        // 관통 한계 도달
        if (currentPierceCount >= maxPierceCount) {
            isAlive = false;
        }

        return true;
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        if (animation != null) {
            // 애니메이션 렌더링 (방향에 맞춰 회전)
            TextureRegion frame = animation.getKeyFrame(animationTime);
            float angle = velocity.angleDeg();

            // TextureRegion용 draw 메서드 사용
            batch.draw(frame,
                position.x - size / 2,  // x
                position.y - size / 2,  // y
                size / 2,               // originX
                size / 2,               // originY
                size,                   // width
                size,                   // height
                1f,                     // scaleX
                1f,                     // scaleY
                angle);                 // rotation
        } else if (texture != null) {
            // 폴백: Pixmap 텍스처
            batch.draw(texture,
                position.x - size / 2,
                position.y - size / 2,
                size, size);
        }
    }

    /**
     * 텍스처 생성 (임시 - 색상이 적용된 원)
     */
    protected void createTexture() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(red, green, blue, alpha);
        pixmap.fillCircle(8, 8, 6);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * 발사체 색상을 설정합니다.
     *
     * @param r 빨강 (0.0 ~ 1.0)
     * @param g 초록 (0.0 ~ 1.0)
     * @param b 파랑 (0.0 ~ 1.0)
     */
    protected void setColor(float r, float g, float b) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = 1.0f;
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    // ===== Getters & Setters =====

    public boolean isAlive() {
        return isAlive;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getDamage() {
        return damage;
    }

    public void setMaxPierceCount(int count) {
        this.maxPierceCount = Math.max(1, count);
    }

    public int getMaxPierceCount() {
        return maxPierceCount;
    }

    public int getCurrentPierceCount() {
        return currentPierceCount;
    }
}
