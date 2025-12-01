package com.example.yugeup.game.skill.water;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.HashSet;
import java.util.Set;

/**
 * 플러드 투사체 클래스
 *
 * 매우 느린 속도(25)로 이동하는 관통형 소용돌이입니다.
 * 타격 시 사라지지 않고 도트딜을 줍니다.
 * 각도에 따라 회전하지 않고 원본 방향 유지
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FloodProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    // 도트딜 관련
    private float tickTimer = 0f;
    private float tickRate;
    private Set<Integer> recentlyHitMonsters;

    // 렌더링 크기
    private float renderWidth;
    private float renderHeight;

    // 애니메이션
    private Animation<TextureRegion> loopAnim;

    /**
     * 플러드 투사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 틱당 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param maxRange 최대 사거리 (픽셀)
     */
    public FloodProjectile(Vector2 origin, float directionX, float directionY,
                           int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "flood_loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);
        this.tickRate = Constants.FLOOD_TICK_RATE;
        this.recentlyHitMonsters = new HashSet<>();

        // 히트박스 30x45에 스케일 1배 적용
        this.renderWidth = Constants.FLOOD_HITBOX_WIDTH * Constants.FLOOD_SCALE;
        this.renderHeight = Constants.FLOOD_HITBOX_HEIGHT * Constants.FLOOD_SCALE;
        this.size = Math.max(renderWidth, renderHeight);

        // 애니메이션 로드
        this.loopAnim = SkillEffectManager.getInstance().getAnimation("flood_loop");

        // 관통 무제한 (도트딜이므로)
        this.maxPierceCount = Integer.MAX_VALUE;

        // 물 원소: 청록색 (폴백용)
        setColor(0.0f, 0.5f, 1.0f);

        System.out.println("[FloodProjectile] 생성! 방향: (" + directionX + ", " + directionY + "), 속도: " + speed);
    }

    /**
     * 업데이트 (도트딜 처리 포함)
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

        // 도트딜 타이머
        tickTimer += delta;
        if (tickTimer >= tickRate) {
            tickTimer = 0f;
            recentlyHitMonsters.clear();  // 새 틱에서 다시 맞을 수 있음
        }

        // 충돌 감지 (도트딜)
        checkDotDamage();
    }

    /**
     * 도트딜 충돌 감지
     */
    private void checkDotDamage() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        // 히트박스 크기 기준
        float hitboxRadius = Math.max(renderWidth, renderHeight) / 2f;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (recentlyHitMonsters.contains(monster.getMonsterId())) continue;

            // 거리 계산
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= hitboxRadius + 20f) {
                // 서버로 공격 메시지 전송
                if (nm != null) {
                    nm.sendAttackMessage(monster.getMonsterId(), damage, position.x, position.y);
                }
                recentlyHitMonsters.add(monster.getMonsterId());
                System.out.println("[Flood] 도트딜! 몬스터 " + monster.getMonsterId() + " 데미지: " + damage);
            }
        }
    }

    /**
     * 충돌 감지 오버라이드 (기본 충돌은 비활성화, 도트딜로 처리)
     */
    @Override
    protected void checkCollision() {
        // 도트딜은 checkDotDamage에서 처리
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
                position.x - renderWidth / 2,
                position.y - renderHeight / 2,
                renderWidth, renderHeight);
        } else if (texture != null) {
            batch.draw(texture,
                position.x - renderWidth / 2,
                position.y - renderHeight / 2,
                renderWidth, renderHeight);
        }
    }
}
