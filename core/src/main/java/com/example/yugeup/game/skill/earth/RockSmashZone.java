package com.example.yugeup.game.skill.earth;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 록 스매시 지역 클래스
 *
 * 바라보는 방향의 가장 가까운 적 위에 돌을 떨어뜨립니다.
 * start 애니메이션 → 바닥 도착 → end 애니메이션 → 1초 후 사라짐
 * 히트박스 48x48, 각도 고정
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RockSmashZone {

    // 존 상태
    private enum ZoneState {
        FALLING,    // 떨어지는 중 (start 애니메이션)
        IMPACT,     // 충돌 순간 (데미지 적용)
        LINGERING,  // 바닥에 유지 (end 애니메이션)
        FINISHED    // 종료
    }

    // 현재 상태
    private ZoneState state;

    // 존 위치 (타겟 위치)
    private Vector2 position;

    // 데미지
    private int damage;

    // 타이머
    private float fallTimer;
    private float lingerTimer;

    // 활성 상태
    private boolean isActive;

    // 렌더링 크기
    private float renderSize;

    // 애니메이션
    private Animation<TextureRegion> startAnimation;
    private Animation<TextureRegion> endAnimation;
    private float animationTime = 0f;

    // 몬스터 리스트 (충돌 감지용)
    private List<Monster> monsterList;

    // 이미 맞은 몬스터 (중복 데미지 방지)
    private Set<Integer> hitMonsters;

    /**
     * 록 스매시 지역 생성자
     *
     * @param targetX 타겟 X 좌표
     * @param targetY 타겟 Y 좌표
     * @param damage 데미지
     */
    public RockSmashZone(float targetX, float targetY, int damage) {
        this.position = new Vector2(targetX, targetY);
        this.damage = damage;
        this.state = ZoneState.FALLING;
        this.isActive = true;
        this.fallTimer = Constants.ROCK_SMASH_FALL_DURATION;
        this.lingerTimer = Constants.ROCK_SMASH_LINGER_DURATION;
        this.hitMonsters = new HashSet<>();

        // 히트박스 48x48에 스케일 적용
        this.renderSize = Constants.ROCK_SMASH_HITBOX_SIZE * Constants.ROCK_SMASH_SCALE;

        // 애니메이션 로드
        SkillEffectManager sem = SkillEffectManager.getInstance();
        this.startAnimation = sem.getAnimation("rock_smash-start");
        this.endAnimation = sem.getAnimation("rock_smash-end");

        System.out.println("[RockSmashZone] 생성! 위치: (" + targetX + ", " + targetY + ")");
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
     * 업데이트
     *
     * @param delta 델타 타임
     */
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        switch (state) {
            case FALLING:
                // 떨어지는 중
                fallTimer -= delta;
                if (fallTimer <= 0) {
                    // 충돌! 데미지 적용
                    state = ZoneState.IMPACT;
                    applyDamage();
                    // 즉시 LINGERING으로 전환
                    state = ZoneState.LINGERING;
                    animationTime = 0f;  // end 애니메이션 시작
                }
                break;

            case LINGERING:
                // 바닥에 유지
                lingerTimer -= delta;
                if (lingerTimer <= 0) {
                    state = ZoneState.FINISHED;
                    isActive = false;
                    System.out.println("[RockSmashZone] 종료!");
                }
                break;

            default:
                break;
        }
    }

    /**
     * 범위 내 몬스터에게 데미지 적용
     */
    private void applyDamage() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();
        float hitboxRadius = Constants.ROCK_SMASH_HITBOX_SIZE / 2f;

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;
            if (hitMonsters.contains(monster.getMonsterId())) continue;

            // 거리 계산
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정 (48x48 범위)
            if (distance <= hitboxRadius + 20f) {
                // 서버로 공격 메시지 전송
                if (nm != null) {
                    nm.sendAttackMessage(monster.getMonsterId(), damage, position.x, position.y);
                }
                hitMonsters.add(monster.getMonsterId());
                System.out.println("[RockSmash] 충돌! 몬스터 " + monster.getMonsterId() + " 데미지: " + damage);
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

        TextureRegion frame = null;

        if (state == ZoneState.FALLING && startAnimation != null) {
            // 떨어지는 중: start 애니메이션
            frame = startAnimation.getKeyFrame(animationTime, false);
        } else if ((state == ZoneState.IMPACT || state == ZoneState.LINGERING) && endAnimation != null) {
            // 바닥 충돌 후: end 애니메이션
            frame = endAnimation.getKeyFrame(animationTime, false);
        }

        if (frame != null) {
            // 각도 고정 (회전하지 않음)
            batch.draw(frame,
                position.x - renderSize / 2,
                position.y - renderSize / 2,
                renderSize, renderSize);
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

    /**
     * 렌더링 크기 반환
     *
     * @return 렌더링 크기
     */
    public float getRenderSize() {
        return renderSize;
    }
}
