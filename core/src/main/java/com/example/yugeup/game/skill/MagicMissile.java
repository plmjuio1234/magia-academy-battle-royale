package com.example.yugeup.game.skill;

import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 매직 미사일 스킬
 *
 * 자동으로 가장 가까운 몬스터를 타게팅하여 공격합니다.
 * ON/OFF 토글이 가능하며, 마나 소모가 없습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MagicMissile extends Skill {
    // 타게팅 시스템
    private TargetingSystem targetingSystem;

    // 스킬 설정
    // 화면 범위 내 몬스터만 타게팅 (줌 0.3배 고려)
    // 실제 화면 너비: 2856 / 0.3 = 9520 픽셀의 절반 = 약 800 픽셀
    private static final float TARGETING_RANGE = 800f;  // 타게팅 범위
    private static final int BASE_DAMAGE = 15;          // 기본 데미지
    private static final float FIRE_RATE = 1.0f;        // 발사 주기 (1초)

    // 발사체 목록 (외부에서 주입받을 수도 있음)
    private List<Projectile> projectiles;

    /**
     * MagicMissile 생성자
     *
     * @param owner 스킬 소유자
     * @param targetingSystem 타게팅 시스템
     */
    public MagicMissile(Player owner, TargetingSystem targetingSystem) {
        super("Magic Missile", 0, FIRE_RATE, owner);
        this.targetingSystem = targetingSystem;
        this.projectiles = new ArrayList<>();
        setDescription("가장 가까운 적에게 자동으로 마법 미사일을 발사합니다.");
    }

    /**
     * 업데이트 (자동 발동 포함)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        // 쿨타임 감소
        super.update(delta);

        // 활성화된 경우 자동 발동
        if (isEnabled && isReady()) {
            tryUse();
        }
    }

    @Override
    protected void use() {
        // 타겟 찾기
        Monster target = targetingSystem.findNearestMonster(
            owner.getPosition(),
            TARGETING_RANGE
        );

        if (target == null) {
            return;  // 타겟 없음
        }

        // 발사체 생성
        createProjectile(target);
    }

    /**
     * 발사체를 생성합니다.
     *
     * @param target 타겟 몬스터
     */
    private void createProjectile(Monster target) {
        // 데미지 계산 (공격력 기반)
        int damage = BASE_DAMAGE + owner.getStats().getAttackPower();

        // 발사체 생성 (플레이어 위치도 함께 전달)
        Projectile projectile = new Projectile(
            owner.getPosition(),
            target,
            damage,
            Constants.MAGIC_MISSILE_SPEED,  // 400 픽셀/초
            owner.getPosition()  // 플레이어 위치 (서버 검증용)
        );

        // 발사체 목록에 추가
        projectiles.add(projectile);

        // 네트워크로 발사 메시지 전송 (다른 플레이어에게도 보이도록)
        sendProjectileFiredMessage(target);
    }

    /**
     * 발사체 발사 메시지를 서버로 전송합니다.
     *
     * @param target 타겟 몬스터
     */
    private void sendProjectileFiredMessage(Monster target) {
        com.example.yugeup.network.messages.ProjectileFiredMsg msg =
            new com.example.yugeup.network.messages.ProjectileFiredMsg();
        msg.playerId = 0;  // 서버가 자동으로 설정
        msg.startX = owner.getPosition().x;
        msg.startY = owner.getPosition().y;
        msg.targetMonsterId = target.getMonsterId();
        msg.skillType = "MagicMissile";

        // 서버로 전송
        com.example.yugeup.network.NetworkManager.getInstance().sendTCP(msg);
    }

    /**
     * 발사체 목록을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void updateProjectiles(float delta) {
        // 발사체 업데이트 및 사망한 발사체 제거
        List<Projectile> toRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            projectile.update(delta);

            if (!projectile.isAlive()) {
                toRemove.add(projectile);
            }
        }

        // 사망한 발사체 제거
        for (Projectile projectile : toRemove) {
            projectile.dispose();
            projectiles.remove(projectile);
        }
    }

    /**
     * 발사체 목록을 반환합니다.
     *
     * @return 발사체 목록
     */
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        for (Projectile projectile : projectiles) {
            projectile.dispose();
        }
        projectiles.clear();
    }
}
