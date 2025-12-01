package com.example.yugeup.game.skill.wind;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 폭풍 지역 클래스 (플레이어 추적형)
 *
 * 8초 동안 플레이어 주위에 따라다니는 소용돌이입니다.
 * 히트박스 64x64, 도트딜, 각도 고정
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StormZone {

    // 플레이어 참조 (위치 추적용)
    private Player owner;

    // 존 위치 (플레이어 따라감)
    private Vector2 position;

    // 지속시간
    private float maxDuration;
    private float remainingDuration;
    private boolean isActive;

    // 데미지
    private int damagePerTick;

    // 도트딜 관련
    private float tickTimer = 0f;
    private float tickRate;
    private Set<Integer> recentlyHitMonsters;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> animation;
    private float animationTime = 0f;

    // 몬스터 리스트
    private List<Monster> monsterList;

    /**
     * 폭풍 지역 생성자
     *
     * @param owner 소유 플레이어 (위치 추적)
     * @param damagePerTick 틱당 데미지
     * @param duration 지속시간
     */
    public StormZone(Player owner, int damagePerTick, float duration) {
        this.owner = owner;
        this.position = new Vector2(owner.getX(), owner.getY());
        this.damagePerTick = damagePerTick;
        this.maxDuration = duration;
        this.remainingDuration = duration;
        this.isActive = true;
        this.tickRate = Constants.STORM_TICK_RATE;
        this.recentlyHitMonsters = new HashSet<>();

        // 히트박스 64x64에 스케일 적용
        this.renderSize = Constants.STORM_HITBOX_SIZE * Constants.STORM_SCALE;

        // 애니메이션 로드
        this.animation = SkillEffectManager.getInstance().getAnimation("storm-loop");

        System.out.println("[StormZone] 생성! 지속시간: " + duration + "초, 히트박스: " + renderSize);
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
     * 업데이트 (플레이어 추적 + 도트딜)
     *
     * @param delta 델타 타임
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        // 지속시간 감소
        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
            System.out.println("[StormZone] 종료!");
            return;
        }

        // 플레이어 위치 추적
        if (owner != null) {
            position.set(owner.getX(), owner.getY());
        }

        // 도트딜 타이머
        tickTimer += delta;
        if (tickTimer >= tickRate) {
            tickTimer = 0f;
            recentlyHitMonsters.clear();  // 새 틱에서 다시 맞을 수 있음
        }

        // 도트딜 적용
        applyDamageToNearbyMonsters();
    }

    /**
     * 주변 몬스터에게 데미지 적용
     */
    private void applyDamageToNearbyMonsters() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        float hitboxRadius = renderSize / 2f;

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
                    nm.sendAttackMessage(monster.getMonsterId(), damagePerTick, position.x, position.y);
                }
                recentlyHitMonsters.add(monster.getMonsterId());
                System.out.println("[Storm] 도트딜! 몬스터 " + monster.getMonsterId() + " 데미지: " + damagePerTick);
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
            position.x - renderSize / 2,
            position.y - renderSize / 2,
            renderSize, renderSize);
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
     * 렌더링 크기 반환
     *
     * @return 렌더링 크기
     */
    public float getRenderSize() {
        return renderSize;
    }
}
