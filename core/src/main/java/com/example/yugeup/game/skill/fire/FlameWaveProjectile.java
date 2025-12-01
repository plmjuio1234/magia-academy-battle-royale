package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.BaseProjectile;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.HashSet;
import java.util.Set;

/**
 * 플레임 웨이브 투사체 클래스
 *
 * 방향으로 날아가면서 지나가는 적에게 도트딜을 줍니다.
 * 스펙: 사거리 200, 속도 150, 히트박스 16x16, 도트딜 0.3초 간격
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class FlameWaveProjectile extends BaseProjectile {

    // 사거리 제한
    private float maxRange;
    private float traveledDistance = 0f;
    private Vector2 startPosition;

    // 도트딜 관련
    private float tickTimer = 0f;
    private float tickRate;
    private Set<Integer> recentlyHitMonsters;  // 최근 히트한 몬스터 (틱 내)
    private Set<Integer> recentlyHitPlayers;   // 최근 히트한 플레이어 (틱 내)

    // 렌더링 크기 (스케일 적용)
    private float renderSize;

    // 발사 각도
    private float angle;

    // 애니메이션
    private Animation<TextureRegion> loopAnim;

    /**
     * 플레임 웨이브 투사체 생성자
     *
     * @param origin 발사 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 틱당 데미지
     * @param speed 이동 속도 (픽셀/초)
     * @param maxRange 최대 사거리 (픽셀)
     */
    public FlameWaveProjectile(Vector2 origin, float directionX, float directionY,
                               int damage, float speed, float maxRange) {
        super(origin, directionX, directionY, damage, speed, "flame_wave-loop");
        this.maxRange = maxRange;
        this.startPosition = new Vector2(origin);
        this.tickRate = Constants.FLAME_WAVE_TICK_RATE;
        this.recentlyHitMonsters = new HashSet<>();
        this.recentlyHitPlayers = new HashSet<>();

        // 렌더링 크기: 히트박스 16x16에 스케일 3배 = 48x48
        this.renderSize = Constants.FLAME_WAVE_HITBOX_SIZE * Constants.FLAME_WAVE_SCALE;
        this.size = renderSize;

        // 발사 각도 계산
        this.angle = new Vector2(directionX, directionY).angleDeg();

        // 애니메이션 로드
        this.loopAnim = SkillEffectManager.getInstance().getAnimation("flame_wave-loop");

        // 관통 무제한 (도트딜이므로)
        this.maxPierceCount = Integer.MAX_VALUE;

        // 불 원소: 주황-빨강색 (폴백용)
        setColor(1.0f, 0.4f, 0.0f);

        System.out.println("[FlameWaveProjectile] 생성! 방향: (" + directionX + ", " + directionY + "), 속도: " + speed + ", velocity: (" + velocity.x + ", " + velocity.y + ")");
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
            recentlyHitPlayers.clear();
        }

        // 충돌 감지 (도트딜)
        checkDotDamage();
        checkDotDamageToPlayers();  // PVP 도트딜
    }

    /**
     * 도트딜 충돌 감지
     */
    private void checkDotDamage() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        // 렌더링 크기의 절반을 히트 반경으로 사용 (시각적 크기와 일치)
        float hitboxRadius = renderSize / 2f;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (recentlyHitMonsters.contains(monster.getMonsterId())) continue;

            // 거리 계산
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정 (렌더링 크기 기준 + 몬스터 반경)
            if (distance <= hitboxRadius + 20f) {
                // 서버로 공격 메시지 전송
                if (nm != null) {
                    nm.sendAttackMessage(monster.getMonsterId(), damage, position.x, position.y);
                }
                recentlyHitMonsters.add(monster.getMonsterId());
                System.out.println("[FlameWave] 도트딜! 몬스터 " + monster.getMonsterId() + " 데미지: " + damage);
            }
        }
    }

    /**
     * 플레이어에게 도트딜 (PVP)
     */
    private void checkDotDamageToPlayers() {
        if (playerList == null || playerList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        float hitboxRadius = renderSize / 2f;

        for (Player player : playerList) {
            if (player == null || player.isDead()) continue;
            if (player.getPlayerId() == ownerPlayerId) continue;  // 자기 자신 제외
            if (recentlyHitPlayers.contains(player.getPlayerId())) continue;

            // 거리 계산
            float dx = player.getX() - position.x;
            float dy = player.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= hitboxRadius + 20f) {
                int pvpDamage = (int) (damage * Constants.PVP_DAMAGE_MULTIPLIER);
                if (nm != null) {
                    nm.sendPvpAttack(player.getPlayerId(), pvpDamage, "FlameWave");
                }
                recentlyHitPlayers.add(player.getPlayerId());
                System.out.println("[FlameWave] PVP 도트딜! 플레이어 " + player.getPlayerId() + " 데미지: " + pvpDamage);
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
     * 렌더링 (발사 방향으로 회전, 왼쪽 방향 시 Y플립)
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        if (loopAnim != null) {
            TextureRegion frame = loopAnim.getKeyFrame(animationTime, true);

            // 왼쪽 방향(90~270도)일 때 Y축 플립으로 상하 반전 방지
            float scaleY = 1f;
            float renderAngle = angle;
            if (angle > 90 && angle < 270) {
                scaleY = -1f;  // Y축 플립
            }

            batch.draw(frame,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize / 2,
                renderSize / 2,
                renderSize,
                renderSize,
                1f, scaleY,
                renderAngle);
        } else if (texture != null) {
            batch.draw(texture,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
        }
    }
}
