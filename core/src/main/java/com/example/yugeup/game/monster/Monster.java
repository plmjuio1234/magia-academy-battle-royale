package com.example.yugeup.game.monster;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.yugeup.game.buff.Buff;
import com.example.yugeup.game.animation.MonsterAnimation;
import com.example.yugeup.ui.hud.HPBar;
import java.util.ArrayList;
import java.util.List;

/**
 * 몬스터 기본 클래스
 *
 * 모든 몬스터 타입의 기본이 되는 클래스입니다.
 * 고스트, 슬라임, 골렘이 이 클래스를 상속받습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Monster {

    // 몬스터 고유 ID
    protected int monsterId;

    // 몬스터 타입
    protected MonsterType monsterType;

    // 레벨 (PHASE_11에서 추가)
    protected int level;

    // 위치 정보
    protected float x;
    protected float y;

    // 능력치
    protected int currentHealth;
    protected int maxHealth;
    protected int attack;
    protected float speed;

    // 상태
    protected boolean isDead;
    protected MonsterState state = MonsterState.IDLE;

    // 방향 (0=left, 1=right)
    protected int direction = 1;  // 기본: 오른쪽

    // 렌더링 관련
    protected Sprite sprite;
    protected MonsterAnimation animation;
    protected HPBar hpBar;

    // 크기 정보
    protected float width;
    protected float height;

    // 적용된 버프 목록
    protected List<Buff> activeBuffs;

    // 추가 방어력 보너스 (버프에서 적용)
    protected int defenseBonus;

    // 마지막 공격 시간 (밀리초)
    protected long lastAttackTime = 0;

    /**
     * 몬스터 생성자 (버프 목록 초기화)
     */
    public Monster() {
        this.activeBuffs = new ArrayList<>();
        this.defenseBonus = 0;
        this.hpBar = new HPBar(this);

        // 초기 위치와 목표 위치 동기화
        this.targetX = this.x;
        this.targetY = this.y;
    }

    /**
     * 몬스터 초기화 (서브클래스에서 호출)
     *
     * @param type 몬스터 타입
     */
    protected void initialize(MonsterType type) {
        this.monsterType = type;
        if (type != null) {
            this.animation = new MonsterAnimation(type);
        }
    }

    // AI 타겟 (플레이어 위치) - 추적할 목표
    private float aiTargetX = 0;
    private float aiTargetY = 0;
    private boolean hasTarget = false;

    // 실제 이동 목표 위치 (보간 이동용)
    private float targetX = 0;
    private float targetY = 0;

    // GameMap 참조 (벽 충돌 감지용)
    private com.example.yugeup.game.map.GameMap gameMap;

    /**
     * GameMap 설정 (벽 충돌 감지용)
     *
     * @param gameMap 게임 맵
     */
    public void setGameMap(com.example.yugeup.game.map.GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * AI 타겟 설정 (플레이어 위치)
     *
     * @param targetX 타겟 X 좌표
     * @param targetY 타겟 Y 좌표
     */
    public void setAITarget(float targetX, float targetY) {
        this.aiTargetX = targetX;
        this.aiTargetY = targetY;
        this.hasTarget = true;
    }

    /**
     * 몬스터를 업데이트합니다.
     *
     * 서버 위치로의 보간 이동과 버프 업데이트를 처리합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 버프 업데이트
        updateBuffs(delta);

        // AI 이동 (로컬 테스트용 - 서버에서 위치를 받으면 이 부분은 무시됨)
        if (hasTarget && !isDead) {
            updateAIMovement(delta);
        } else if (!isDead) {
            // 서버로부터 받은 목표 위치로 보간 이동
            updateInterpolation(delta);
        }

        // 애니메이션 업데이트
        if (animation != null) {
            animation.update(delta);
        }

        // HP 바 위치 업데이트
        if (hpBar != null) {
            hpBar.setPosition(x, y + height + 5);
        }
    }

    /**
     * 서버 위치로의 보간 이동 (부드러운 동기화)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateInterpolation(float delta) {
        // 이동 전 위치 저장
        float oldX = x;
        float oldY = y;

        // 목표 위치까지의 거리
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 거리가 매우 가까우면 즉시 목표 위치로 이동 (멈춤 감지)
        if (distance < 2f) {
            x = targetX;
            y = targetY;
            setState(MonsterState.IDLE);
            return;
        }

        // 보간 속도: 서버 최대 속도의 1.2배 (Ghost: 120 픽셀/초)
        // 서버가 0.1초마다 위치 전송하므로 빠르게 따라가야 함
        float interpolationSpeed = 50f;  // 초당 50배 보간 (순간이동 방지)
        float lerpAmount = Math.min(1f, interpolationSpeed * delta);

        // 선형 보간으로 부드럽게 이동
        x += dx * lerpAmount;
        y += dy * lerpAmount;

        // 벽 충돌 체크
        if (gameMap != null) {
            float radius = 12f;  // 몬스터 충돌 반경
            if (gameMap.isWallInArea(x, y, radius)) {
                // 벽에 부딪히면 원위치
                x = oldX;
                y = oldY;
                // 목표 위치도 현재 위치로 리셋 (벽 너머로 이동하지 않도록)
                targetX = x;
                targetY = y;
            }
        }

        // 방향 업데이트 (dx가 음수면 왼쪽, 양수면 오른쪽)
        if (Math.abs(dx) > 1f) {  // 이동량이 충분히 클 때만 방향 변경
            direction = (dx < 0) ? 0 : 1;  // 0=left, 1=right
        }

        // 이동 중이면 MOVING 상태로 변경
        if (distance > 5f) {
            setState(MonsterState.MOVING);
        } else {
            setState(MonsterState.IDLE);
        }
    }

    /**
     * AI 이동 업데이트 (로컬 테스트용)
     *
     * 플레이어 방향으로 천천히 이동합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateAIMovement(float delta) {
        // AI 타겟(플레이어)까지의 거리 계산
        float dx = aiTargetX - x;
        float dy = aiTargetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 공격 범위 안에 있으면 멈춤
        float attackRange = 30f;
        if (distance <= attackRange) {
            setState(MonsterState.IDLE);
            return;
        }

        // 이동 속도: 매우 느리게 (픽셀/초)
        float moveSpeed = 50f;  // 모든 몬스터 동일 속도

        // 플레이어 방향으로 직접 이동
        if (distance > 0) {
            float moveAmount = moveSpeed * delta;
            float moveX = (dx / distance) * moveAmount;
            float moveY = (dy / distance) * moveAmount;

            x += moveX;
            y += moveY;

            // 방향 업데이트
            if (Math.abs(dx) > 1f) {
                direction = (dx < 0) ? 0 : 1;
            }

            setState(MonsterState.MOVING);
        }
    }

    /**
     * 활성 버프를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    protected void updateBuffs(float delta) {
        // 활성화되지 않은 버프 제거
        activeBuffs.removeIf(buff -> !buff.isActive());

        // 모든 활성 버프 업데이트
        for (Buff buff : activeBuffs) {
            buff.update(delta);
        }
    }

    /**
     * 몬스터를 렌더링합니다.
     *
     * @param batch 스프라이트 배치
     */
    public void render(SpriteBatch batch) {
        if (animation == null) {
            return;  // 애니메이션이 없으면 렌더링 안 함
        }

        // 현재 상태와 방향의 프레임 가져오기
        TextureRegion currentFrame = animation.getCurrentFrame(state, direction);

        // 스프라이트 렌더링
        if (currentFrame != null && currentFrame.getTexture() != null) {
            batch.draw(currentFrame, x, y, width, height);
        }

        // HP 바 렌더링
        if (hpBar != null && !isDead) {
            hpBar.render(batch);
        }

        // 버프 이펙트 렌더링
        renderBuffEffects(batch);
    }

    /**
     * 버프 이펙트 렌더링
     *
     * @param batch 스프라이트 배치
     */
    private void renderBuffEffects(SpriteBatch batch) {
        if (hasBuff(com.example.yugeup.game.buff.BuffType.STUN)) {
            // 스턴 이펙트 (별 표시 등)
            renderStunEffect(batch);
        }

        // 추가 버프 이펙트는 여기에 구현
    }

    /**
     * 스턴 이펙트 렌더링
     *
     * @param batch 스프라이트 배치
     */
    private void renderStunEffect(SpriteBatch batch) {
        // 임시: 노란색 오버레이
        batch.setColor(1f, 1f, 0f, 0.3f);
        // TODO: 실제로는 스턴 아이콘 렌더링
        batch.setColor(1, 1, 1, 1);
    }

    /**
     * 몬스터가 데미지를 받습니다.
     *
     * 버프 효과 (무적, 방어력 증가)를 고려하여 데미지를 적용합니다.
     *
     * @param damage 받을 데미지
     * @return 실제로 적용된 데미지
     */
    public int takeDamage(int damage) {
        if (isDead) {
            return 0;  // 이미 사망한 몬스터는 데미지를 받지 않음
        }

        // 무적 상태 확인
        if (hasInvincibleBuff()) {
            return 0;
        }

        // 방어력 보너스 적용
        int actualDamage = Math.max(1, damage - defenseBonus);

        // 체력 감소
        currentHealth -= actualDamage;

        // 사망 처리
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
        }

        return actualDamage;
    }

    /**
     * 버프를 추가합니다.
     *
     * @param buff 추가할 버프
     */
    public void addBuff(Buff buff) {
        if (buff == null) return;
        activeBuffs.add(buff);
    }

    /**
     * 특정 타입의 버프를 제거합니다.
     *
     * @param buffType 제거할 버프 타입
     */
    public void removeBuff(com.example.yugeup.game.buff.BuffType buffType) {
        activeBuffs.removeIf(buff -> buff.getBuffType() == buffType);
    }

    /**
     * 특정 타입의 버프가 적용되어 있는지 확인합니다.
     *
     * @param buffType 확인할 버프 타입
     * @return 버프 적용 여부
     */
    public boolean hasBuff(com.example.yugeup.game.buff.BuffType buffType) {
        return activeBuffs.stream()
            .anyMatch(buff -> buff.getBuffType() == buffType && buff.isActive());
    }

    /**
     * 무적 상태인지 확인합니다.
     *
     * @return 무적 상태 여부
     */
    public boolean hasInvincibleBuff() {
        return hasBuff(com.example.yugeup.game.buff.BuffType.INVINCIBLE);
    }

    /**
     * 기절 상태인지 확인합니다.
     *
     * @return 기절 상태 여부
     */
    public boolean isStunned() {
        return hasBuff(com.example.yugeup.game.buff.BuffType.STUN);
    }

    /**
     * 현재 적용된 이동 속도 배수를 계산합니다.
     *
     * 속도 버프와 둔화 버프를 모두 고려합니다.
     *
     * @return 이동 속도 배수 (1.0 = 정상 속도)
     */
    public float getSpeedMultiplier() {
        float multiplier = 1.0f;

        // 둔화 버프 적용
        for (Buff buff : activeBuffs) {
            if (buff.getBuffType() == com.example.yugeup.game.buff.BuffType.SLOW && buff.isActive()) {
                com.example.yugeup.game.buff.SlowBuff slowBuff = (com.example.yugeup.game.buff.SlowBuff) buff;
                multiplier *= slowBuff.getSpeedMultiplier();
            }
        }

        // 가속 버프 적용
        for (Buff buff : activeBuffs) {
            if (buff.getBuffType() == com.example.yugeup.game.buff.BuffType.SPEED && buff.isActive()) {
                com.example.yugeup.game.buff.SpeedBuff speedBuff = (com.example.yugeup.game.buff.SpeedBuff) buff;
                multiplier *= speedBuff.getSpeedMultiplier();
            }
        }

        return multiplier;
    }

    /**
     * 모든 활성 버프를 제거합니다.
     */
    public void clearBuffs() {
        activeBuffs.clear();
        defenseBonus = 0;
    }

    // Getter & Setter
    public int getMonsterId() { return monsterId; }
    public void setMonsterId(int monsterId) { this.monsterId = monsterId; }

    public MonsterType getType() { return monsterType; }
    public void setType(MonsterType monsterType) { this.monsterType = monsterType; }

    // 레거시 호환용 (int 타입 getter/setter)
    public int getMonsterType() { return monsterType != null ? monsterType.getTypeId() : 0; }
    public void setMonsterType(int typeId) { this.monsterType = MonsterType.fromTypeId(typeId); }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public int getDirection() { return direction; }

    /**
     * 위치를 즉시 설정합니다 (스폰 시 사용)
     *
     * @param x X 좌표
     * @param y Y 좌표
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        // 목표 위치도 동기화 (초기 스폰 시)
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * 서버로부터 받은 목표 위치를 설정합니다 (보간 이동)
     *
     * @param x 목표 X 좌표
     * @param y 목표 Y 좌표
     */
    public void setTargetPosition(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public boolean isDead() { return isDead; }
    public void setDead(boolean dead) { isDead = dead; }

    public MonsterState getState() { return state; }
    public void setState(MonsterState state) {
        if (this.state != state) {
            this.state = state;
            if (animation != null) {
                animation.resetStateTime();
            }
        }
    }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long time) { this.lastAttackTime = time; }
}
