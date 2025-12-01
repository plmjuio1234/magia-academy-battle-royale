package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import com.example.yugeup.game.player.Player;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 어스 스파이크 지역 클래스 (이동형)
 *
 * 보는 방향으로 진행하는 바닥 가시입니다.
 * 사거리 200, 속도 100, 히트박스 24x16
 * 애니메이션이 끝나면 바로 사라짐
 * 각도 고정
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class EarthSpikeZone {

    // 위치 및 속도
    private Vector2 position;
    private Vector2 startPosition;
    private Vector2 velocity;

    // 데미지
    private int damage;

    // 이동 거리 제한
    private float maxRange;
    private float traveledDistance = 0f;

    // 활성 상태
    private boolean isActive;

    // 렌더링 크기
    private float renderWidth;
    private float renderHeight;

    // 애니메이션
    private Animation<TextureRegion> animation;
    private float animationTime = 0f;

    // 몬스터 리스트 (충돌 감지용)
    private List<Monster> monsterList;

    // 이미 맞은 몬스터 (관통 데미지)
    private Set<Integer> hitMonsters;

    // PVP 피격판정용
    private Map<Integer, Player> remotePlayers;
    private Player myPlayer;
    private Set<Integer> hitPlayers;

    /**
     * 어스 스파이크 지역 생성자
     *
     * @param origin 시작 위치
     * @param directionX 방향 X
     * @param directionY 방향 Y
     * @param damage 데미지
     */
    public EarthSpikeZone(Vector2 origin, float directionX, float directionY, int damage) {
        this.position = new Vector2(origin);
        this.startPosition = new Vector2(origin);
        this.damage = damage;
        this.maxRange = Constants.EARTH_SPIKE_RANGE;
        this.isActive = true;
        this.hitMonsters = new HashSet<>();
        this.hitPlayers = new HashSet<>();

        // 속도 벡터 설정
        float speed = Constants.EARTH_SPIKE_SPEED;
        this.velocity = new Vector2(directionX * speed, directionY * speed);

        // 히트박스 24x16에 스케일 적용
        this.renderWidth = Constants.EARTH_SPIKE_HITBOX_WIDTH * Constants.EARTH_SPIKE_SCALE;
        this.renderHeight = Constants.EARTH_SPIKE_HITBOX_HEIGHT * Constants.EARTH_SPIKE_SCALE;

        // 애니메이션 로드
        this.animation = SkillEffectManager.getInstance().getAnimation("earth_spike");

        System.out.println("[EarthSpikeZone] 생성! 방향: (" + directionX + ", " + directionY + ")");
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
     * 원격 플레이어 목록 설정 (PVP용)
     *
     * @param remotePlayers 원격 플레이어 맵
     * @param myPlayer 스킬 시전자
     */
    public void setPlayerList(Map<Integer, Player> remotePlayers, Player myPlayer) {
        this.remotePlayers = remotePlayers;
        this.myPlayer = myPlayer;
    }

    /**
     * 업데이트
     *
     * @param delta 델타 타임
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 사거리 체크
        traveledDistance = position.dst(startPosition);
        if (traveledDistance >= maxRange) {
            isActive = false;
            System.out.println("[EarthSpikeZone] 사거리 도달, 종료!");
            return;
        }

        // 애니메이션 종료 체크
        if (animation != null && animation.isAnimationFinished(animationTime)) {
            isActive = false;
            System.out.println("[EarthSpikeZone] 애니메이션 종료!");
            return;
        }

        // 충돌 감지
        checkCollision();
        checkPlayerCollision();  // PVP
    }

    /**
     * 충돌 감지 (관통)
     */
    private void checkCollision() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        float hitboxHalfWidth = Constants.EARTH_SPIKE_HITBOX_WIDTH / 2f;
        float hitboxHalfHeight = Constants.EARTH_SPIKE_HITBOX_HEIGHT / 2f;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (hitMonsters.contains(monster.getMonsterId())) continue;

            // 거리 계산 (AABB 대신 원형 충돌)
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= hitboxHalfWidth + 20f) {
                // 서버로 공격 메시지 전송
                if (nm != null) {
                    nm.sendAttackMessage(monster.getMonsterId(), damage, position.x, position.y);
                }
                hitMonsters.add(monster.getMonsterId());
                System.out.println("[EarthSpike] 충돌! 몬스터 " + monster.getMonsterId() + " 데미지: " + damage);
            }
        }
    }

    /**
     * 플레이어 충돌 감지 (PVP, 관통)
     */
    private void checkPlayerCollision() {
        if (remotePlayers == null || remotePlayers.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        float hitboxHalfWidth = Constants.EARTH_SPIKE_HITBOX_WIDTH / 2f;

        for (Player player : remotePlayers.values()) {
            if (player == null || player.isDead()) continue;

            // 자기 자신 제외
            if (myPlayer != null && player.getPlayerId() == myPlayer.getPlayerId()) continue;

            // 이미 맞은 플레이어 제외
            if (hitPlayers.contains(player.getPlayerId())) continue;

            // 거리 계산
            float dx = player.getX() - position.x;
            float dy = player.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (distance <= hitboxHalfWidth + 20f) {
                // PVP 데미지 (0.5배)
                int pvpDamage = (int) (damage * Constants.PVP_DAMAGE_MULTIPLIER);
                if (nm != null) {
                    nm.sendPvpAttack(player.getPlayerId(), pvpDamage, "EarthSpike");
                    System.out.println("[EarthSpike] PVP 충돌! 플레이어 " + player.getPlayerId() + " 데미지: " + pvpDamage);
                }
                hitPlayers.add(player.getPlayerId());
            }
        }
    }

    /**
     * 렌더링 (각도 고정)
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isActive || animation == null) return;

        TextureRegion frame = animation.getKeyFrame(animationTime, true);
        // 각도 고정 (회전하지 않음)
        batch.draw(frame,
            position.x - renderWidth / 2,
            position.y - renderHeight / 2,
            renderWidth, renderHeight);
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
     * 위치 반환
     *
     * @return 현재 위치
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * 렌더링 너비 반환
     *
     * @return 렌더링 너비
     */
    public float getRenderWidth() {
        return renderWidth;
    }

    /**
     * 렌더링 높이 반환
     *
     * @return 렌더링 높이
     */
    public float getRenderHeight() {
        return renderHeight;
    }
}
