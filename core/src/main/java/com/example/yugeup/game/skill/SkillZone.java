package com.example.yugeup.game.skill;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 스킬 존(지속형 범위 공격) 기본 클래스
 *
 * 일정 시간 동안 지정된 위치에서 범위 내 적에게 데미지를 주는 영역입니다.
 * 불 원소의 '플레임 웨이브', 번개 원소의 '전자기장' 등에서 사용됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class SkillZone {

    // 스킬 존 고유 ID (GameManager에서 관리)
    protected int zoneId;

    // 존의 중심 위치
    protected Vector2 position;

    // 존의 반경 (픽셀)
    protected float radius;

    // 존의 지속시간 (초)
    protected float maxDuration;

    // 남은 지속시간 (초)
    protected float remainingDuration;

    // 존이 활성화되어 있는지
    protected boolean isActive;

    // 틱당 데미지 (매번 영향을 줄 때 대미지)
    protected int damagePerTick;

    // 다음 데미지까지 남은 시간 (초)
    protected float damageInterval;

    // 틱 간격 (초, 기본값: 0.5f)
    protected float tickRate;

    // 이미 데미지를 받은 몬스터 목록 (중복 방지)
    protected List<Integer> hitMonsters;

    // 애니메이션 (PHASE_24)
    protected Animation<TextureRegion> animation;
    protected float animationTime = 0f;
    protected String animationName;

    // 피격판정용 (PHASE_24)
    protected List<Monster> monsterList;
    protected NetworkManager networkManager;

    // PVP 피격판정용 (원격 플레이어)
    protected Map<Integer, Player> remotePlayers;
    protected Player myPlayer;  // 스킬 시전자 (자기 자신 제외용)
    protected Set<Integer> hitPlayersThisTick;  // 이번 틱에 피격한 플레이어 (중복 방지)

    /**
     * 스킬 존 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param radius 존의 반경
     * @param duration 지속시간 (초)
     * @param damagePerTick 틱당 데미지
     */
    public SkillZone(float x, float y, float radius, float duration, int damagePerTick) {
        this(x, y, radius, duration, damagePerTick, null);
    }

    /**
     * 스킬 존 생성자 (애니메이션 지원)
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param radius 존의 반경
     * @param duration 지속시간 (초)
     * @param damagePerTick 틱당 데미지
     * @param animationName 애니메이션 이름 (null이면 애니메이션 없음)
     */
    public SkillZone(float x, float y, float radius, float duration, int damagePerTick, String animationName) {
        this.position = new Vector2(x, y);
        this.radius = radius;
        this.maxDuration = duration;
        this.remainingDuration = duration;
        this.isActive = true;
        this.damagePerTick = damagePerTick;
        this.damageInterval = 0f;
        this.tickRate = 0.5f;  // 기본값: 0.5초마다 데미지
        this.hitMonsters = new ArrayList<>();
        this.hitPlayersThisTick = new HashSet<>();

        // 애니메이션 로드
        this.animationName = animationName;
        if (animationName != null) {
            this.animation = SkillEffectManager.getInstance().getAnimation(animationName);
        }
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
     * 원격 플레이어 목록 주입 메서드 (PVP용)
     *
     * @param remotePlayers 원격 플레이어 맵 (playerId -> Player)
     * @param myPlayer 스킬 시전자 (자기 자신, 제외용)
     */
    public void setPlayerList(Map<Integer, Player> remotePlayers, Player myPlayer) {
        this.remotePlayers = remotePlayers;
        this.myPlayer = myPlayer;
    }

    /**
     * 스킬 존을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;  // 애니메이션 시간 증가

        // 지속시간 감소
        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
            onEnd();  // 종료 처리
            return;
        }

        // 데미지 틱 처리
        damageInterval -= delta;
        if (damageInterval <= 0) {
            damageInterval = tickRate;
            hitPlayersThisTick.clear();  // 틱마다 초기화
            applyDamageToNearbyMonsters();
            applyDamageToNearbyPlayers();  // PVP 데미지
        }
    }

    /**
     * 주변 몬스터에게 데미지를 적용합니다 (서버 동기화).
     *
     * 존의 범위 내에 있는 모든 몬스터를 찾아 데미지를 입힙니다.
     */
    public void applyDamageToNearbyMonsters() {
        if (monsterList == null) return;

        for (Monster monster : monsterList) {
            if (isMonsterInRange(monster)) {
                if (!hitMonsters.contains(monster.getMonsterId())) {
                    // 서버로 공격 메시지 전송
                    if (networkManager == null) {
                        networkManager = NetworkManager.getInstance();
                    }
                    if (networkManager != null) {
                        networkManager.sendAttackMessage(
                            monster.getMonsterId(),
                            damagePerTick,
                            position.x, position.y);
                    }

                    hitMonsters.add(monster.getMonsterId());
                }
            } else {
                // 범위 벗어나면 다시 맞을 수 있도록
                hitMonsters.remove((Integer) monster.getMonsterId());
            }
        }
    }

    /**
     * 주변 플레이어에게 데미지를 적용합니다 (PVP).
     *
     * 존의 범위 내에 있는 모든 원격 플레이어를 찾아 데미지를 입힙니다.
     */
    public void applyDamageToNearbyPlayers() {
        if (remotePlayers == null || remotePlayers.isEmpty()) return;

        if (networkManager == null) {
            networkManager = NetworkManager.getInstance();
        }

        for (Player player : remotePlayers.values()) {
            if (player == null || player.isDead()) continue;

            // 자기 자신 제외
            if (myPlayer != null && player.getPlayerId() == myPlayer.getPlayerId()) continue;

            // 이번 틱에 이미 피격한 플레이어는 제외
            if (hitPlayersThisTick.contains(player.getPlayerId())) continue;

            // 범위 체크
            if (isPlayerInRange(player)) {
                // PVP 데미지 계산 (0.5배)
                int pvpDamage = (int) (damagePerTick * Constants.PVP_DAMAGE_MULTIPLIER);

                // 서버로 PVP 공격 메시지 전송
                if (networkManager != null) {
                    networkManager.sendPvpAttack(player.getPlayerId(), pvpDamage, "ZoneSkill");
                    System.out.println("[SkillZone] PVP 데미지! 플레이어 ID=" + player.getPlayerId() + ", 데미지=" + pvpDamage);
                }

                hitPlayersThisTick.add(player.getPlayerId());
            }
        }
    }

    /**
     * 특정 플레이어가 범위 내에 있는지 확인합니다.
     *
     * @param player 확인할 플레이어
     * @return 범위 내에 있으면 true
     */
    protected boolean isPlayerInRange(Player player) {
        if (player == null || player.isDead()) {
            return false;
        }

        float dx = player.getX() - position.x;
        float dy = player.getY() - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance <= radius;
    }

    /**
     * 렌더링 (애니메이션)
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        if (!isActive || animation == null) return;

        TextureRegion frame = animation.getKeyFrame(animationTime);
        batch.draw(frame,
            position.x - radius,
            position.y - radius,
            radius * 2, radius * 2);
    }

    /**
     * 특정 몬스터와의 거리를 확인합니다.
     *
     * @param monster 확인할 몬스터
     * @return 거리 내에 있으면 true
     */
    protected boolean isMonsterInRange(Monster monster) {
        if (monster == null || monster.isDead()) {
            return false;
        }

        float dx = monster.getX() - position.x;
        float dy = monster.getY() - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance <= radius;
    }

    /**
     * 몬스터에게 데미지를 입힙니다.
     *
     * @param monster 데미지를 받을 몬스터
     */
    protected void damageMonster(Monster monster) {
        if (monster == null || monster.isDead()) {
            return;
        }

        monster.takeDamage(damagePerTick);
    }

    /**
     * 존이 종료될 때 호출됩니다.
     *
     * 하위 클래스에서 종료 처리를 구현할 수 있습니다.
     */
    protected abstract void onEnd();

    /**
     * 존이 활성화되어 있는지 확인합니다.
     *
     * @return 활성화 상태
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 존의 중심 위치를 반환합니다.
     *
     * @return 중심 위치
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * 존의 반경을 반환합니다.
     *
     * @return 반경
     */
    public float getRadius() {
        return radius;
    }

    /**
     * 남은 지속시간을 반환합니다.
     *
     * @return 남은 지속시간 (초)
     */
    public float getRemainingDuration() {
        return remainingDuration;
    }

    /**
     * 최대 지속시간을 반환합니다.
     *
     * @return 최대 지속시간 (초)
     */
    public float getMaxDuration() {
        return maxDuration;
    }

    /**
     * 존의 진행률을 반환합니다 (0.0 ~ 1.0).
     *
     * @return 진행률
     */
    public float getProgress() {
        if (maxDuration == 0) return 0f;
        return remainingDuration / maxDuration;
    }

    /**
     * 존의 ID를 설정합니다.
     *
     * @param zoneId 존의 ID
     */
    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * 존의 ID를 반환합니다.
     *
     * @return 존의 ID
     */
    public int getZoneId() {
        return zoneId;
    }
}
