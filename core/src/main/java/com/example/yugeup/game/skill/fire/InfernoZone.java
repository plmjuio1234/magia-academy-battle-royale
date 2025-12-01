package com.example.yugeup.game.skill.fire;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillEffectManager;
import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.utils.Constants;

/**
 * 인페르노 지역 클래스
 *
 * 플레이어 주변 60칸 이내에 원형으로 순간 폭발합니다.
 * 새 스펙: 범위 60, 히트박스 60x60, 스케일 1.5, 즉발 데미지
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class InfernoZone extends SkillZone {

    // 데미지가 이미 적용되었는지
    private boolean damageApplied = false;

    // 애니메이션
    private Animation<TextureRegion> infernoAnim;

    // 이펙트 스케일
    private float effectScale;

    /**
     * 인페르노 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param damage 데미지 (즉발)
     * @param duration 이펙트 지속시간 (애니메이션용)
     */
    public InfernoZone(float x, float y, int damage, float duration) {
        super(x, y, Constants.INFERNO_HITBOX_SIZE, duration, damage, "inferno");
        this.effectScale = Constants.INFERNO_SCALE;

        // 애니메이션 로드
        this.infernoAnim = SkillEffectManager.getInstance().getAnimation("inferno");

        // 즉발 데미지이므로 틱 간격 매우 짧게 (첫 프레임에 데미지)
        this.tickRate = 0.01f;
    }

    /**
     * 업데이트 (즉발 데미지 후 애니메이션만 재생)
     */
    @Override
    public void update(float delta) {
        if (!isActive) return;

        animationTime += delta;

        // 지속시간 감소
        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
            onEnd();
            return;
        }

        // 즉발 데미지 (monsterList 주입 후 첫 프레임에만)
        if (!damageApplied && monsterList != null) {
            applyInstantDamage();
            damageApplied = true;
        }
    }

    /**
     * 즉발 데미지 적용 (범위 내 모든 몬스터에게 한 번만)
     */
    private void applyInstantDamage() {
        if (monsterList == null || monsterList.isEmpty()) return;

        NetworkManager nm = NetworkManager.getInstance();

        for (Monster monster : monsterList) {
            if (monster == null || monster.isDead()) continue;

            // 범위 체크
            float dx = monster.getX() - position.x;
            float dy = monster.getY() - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= Constants.INFERNO_RANGE) {
                // 서버로 공격 메시지 전송
                if (nm != null) {
                    nm.sendAttackMessage(monster.getMonsterId(), damagePerTick, position.x, position.y);
                }
                System.out.println("[InfernoZone] 몬스터 " + monster.getMonsterId() + " 히트! 데미지: " + damagePerTick);
            }
        }
    }

    /**
     * 기존 틱 데미지 비활성화 (즉발이므로)
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        // 즉발 데미지는 applyInstantDamage에서 처리
    }

    /**
     * 렌더링 (히트박스 60x60에 scale 1.5 적용, Y축 위로 오프셋)
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!isActive || infernoAnim == null) return;

        TextureRegion frame = infernoAnim.getKeyFrame(animationTime, false);

        // 히트박스 60x60 기준, scale 1.5 적용 = 90x90
        float baseSize = Constants.INFERNO_HITBOX_SIZE;
        float scaledSize = baseSize * effectScale;

        // Y축 위로 오프셋 적용 (플레이어 중심 위로)
        float renderY = position.y + Constants.INFERNO_Y_OFFSET;

        batch.draw(frame,
            position.x - scaledSize / 2,
            renderY - scaledSize / 2,
            scaledSize / 2,
            scaledSize / 2,
            scaledSize,
            scaledSize,
            1f, 1f,
            0f);  // 각도 고정
    }

    /**
     * 종료 처리
     */
    @Override
    protected void onEnd() {
        // 특별한 처리 없음
    }
}
