package com.example.yugeup.game.skill.lightning;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;

import java.util.List;
import java.util.Map;

/**
 * 라이트닝 볼트 지역 클래스
 *
 * 타겟 위에 낙뢰를 떨어뜨립니다.
 * 애니메이션 재생 후 자동 종료.
 * 각도 고정.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LightningBoltZone {

    // 존 상태
    private enum ZoneState {
        ACTIVE,     // 활성 (애니메이션 재생 중)
        FINISHED    // 종료
    }

    // 현재 상태
    private ZoneState state;

    // 존 위치
    private Vector2 position;

    // 데미지
    private int damage;

    // 활성 상태
    private boolean isActive;

    // 데미지 적용 여부
    private boolean damageApplied;

    // 히트박스 크기
    private float hitboxSize;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> animation;
    private float animationTime = 0f;
    private float maxAnimationTime = 0.56f;  // 7프레임 * 0.08초

    // 폴백 텍스처 (애니메이션 로드 실패 시)
    private static Texture fallbackTexture;

    // 몬스터 목록 참조
    private transient List<Monster> monsterList;

    // 네트워크 매니저 (서버 동기화용)
    private transient NetworkManager networkManager;

    // PVP 피격판정용
    private transient Map<Integer, Player> remotePlayers;
    private transient Player myPlayer;

    /**
     * 라이트닝 볼트 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param damage 데미지
     */
    public LightningBoltZone(float x, float y, int damage) {
        this.position = new Vector2(x, y);
        this.damage = damage;
        this.isActive = true;
        this.damageApplied = false;
        this.state = ZoneState.ACTIVE;

        // 히트박스 크기
        this.hitboxSize = Constants.LIGHTNING_BOLT_HITBOX_SIZE;

        // 렌더링 크기 (64 기본 * 스케일)
        this.renderSize = 64f * Constants.LIGHTNING_BOLT_SCALE;

        // 애니메이션 로드 (atlas에서 lightning_volt)
        this.animation = SkillEffectManager.getInstance().getAnimation("lightning_volt");

        // 폴백 텍스처 생성 (한 번만)
        if (fallbackTexture == null) {
            createFallbackTexture();
        }

        System.out.println("[LightningBoltZone] 생성! 위치: (" + x + ", " + y + "), 애니메이션: " + (animation != null));
    }

    /**
     * 폴백 텍스처 생성 (노란색 번개 모양)
     */
    private static void createFallbackTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        // 번개 모양 그리기
        pixmap.fillRectangle(28, 0, 8, 20);
        pixmap.fillRectangle(20, 20, 24, 8);
        pixmap.fillRectangle(28, 28, 8, 36);
        fallbackTexture = new Texture(pixmap);
        pixmap.dispose();
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
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        // 데미지 적용 (첫 프레임에서)
        if (!damageApplied) {
            applyDamage();
            applyDamageToPlayers();  // PVP 데미지
            damageApplied = true;
        }

        // 애니메이션 종료 체크
        boolean animFinished = false;
        if (animation != null) {
            animFinished = animation.isAnimationFinished(animationTime);
        } else {
            // 폴백: 시간 기반 종료
            animFinished = animationTime >= maxAnimationTime;
        }

        if (animFinished) {
            state = ZoneState.FINISHED;
            isActive = false;
            System.out.println("[LightningBoltZone] 종료!");
        }
    }

    /**
     * 범위 내 몬스터에게 데미지 적용
     */
    private void applyDamage() {
        if (monsterList == null) return;

        // 네트워크 매니저 가져오기
        if (networkManager == null) {
            networkManager = NetworkManager.getInstance();
        }

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;

            // 원형 거리 기반 충돌 판정
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 히트박스 반경 내 몬스터에게 데미지
            if (distance <= hitboxSize) {
                // 서버로 공격 메시지 전송 (클라이언트 측에서 직접 HP 변경 X)
                if (networkManager != null) {
                    networkManager.sendAttackMessage(
                        monster.getMonsterId(),
                        damage,
                        position.x,
                        position.y
                    );
                    System.out.println("[LightningBoltZone] 몬스터 ID=" + monster.getMonsterId() + "에게 " + damage + " 데미지 서버 전송!");
                }
            }
        }
    }

    /**
     * 범위 내 플레이어에게 데미지 적용 (PVP)
     */
    private void applyDamageToPlayers() {
        if (remotePlayers == null || remotePlayers.isEmpty()) return;

        // 네트워크 매니저 가져오기
        if (networkManager == null) {
            networkManager = NetworkManager.getInstance();
        }

        for (Player player : remotePlayers.values()) {
            if (player == null || player.isDead()) continue;

            // 자기 자신 제외
            if (myPlayer != null && player.getPlayerId() == myPlayer.getPlayerId()) continue;

            // 원형 거리 기반 충돌 판정
            float dx = player.getX() - position.x;
            float dy = player.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 히트박스 반경 내 플레이어에게 PVP 데미지
            if (distance <= hitboxSize) {
                int pvpDamage = (int) (damage * Constants.PVP_DAMAGE_MULTIPLIER);
                if (networkManager != null) {
                    networkManager.sendPvpAttack(player.getPlayerId(), pvpDamage, "LightningBolt");
                    System.out.println("[LightningBoltZone] PVP! 플레이어 ID=" + player.getPlayerId() + "에게 " + pvpDamage + " 데미지!");
                }
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

        if (animation != null) {
            TextureRegion frame = animation.getKeyFrame(animationTime, false);
            if (frame != null) {
                batch.draw(frame,
                    position.x - renderSize / 2,
                    position.y - renderSize / 2,
                    renderSize, renderSize);
                return;
            }
        }

        // 폴백 렌더링 (애니메이션 없을 때)
        if (fallbackTexture != null) {
            batch.setColor(1f, 1f, 0.5f, 1f);  // 밝은 노란색
            batch.draw(fallbackTexture,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
            batch.setColor(Color.WHITE);  // 색상 복원
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
     * 위치 반환
     *
     * @return 현재 위치
     */
    public Vector2 getPosition() {
        return position;
    }
}
