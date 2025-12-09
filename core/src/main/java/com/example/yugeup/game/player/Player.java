package com.example.yugeup.game.player;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.buff.Buff;
import com.example.yugeup.game.buff.BuffType;
import com.example.yugeup.game.level.ExperienceManager;
import com.example.yugeup.game.level.LevelSystem;
import com.example.yugeup.game.level.LevelUpListener;
import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.ElementSkillSet;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.MagicMissile;
import com.example.yugeup.game.skill.TargetingSystem;
import com.example.yugeup.utils.Constants;
import java.util.List;
import java.util.ArrayList;

/**
 * 플레이어 엔티티 클래스
 *
 * 플레이어의 위치, 능력치, 상태를 관리하는 핵심 클래스입니다.
 * 네트워크를 통해 다른 클라이언트와 동기화됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Player {

    // 플레이어 고유 ID
    private int playerId;

    // 위치 정보
    private Vector2 position;

    // 이동 속도 벡터 (픽셀/초)
    private Vector2 velocity;

    // 원격 플레이어 보간용 목표 위치 (PHASE_23)
    private Vector2 targetPosition;
    private boolean isRemote;

    // 능력치
    private PlayerStats stats;

    // 레벨 시스템 (PHASE_11)
    private LevelSystem levelSystem;

    // 스킬 시스템 (PHASE_12)
    private MagicMissile magicMissile;
    private TargetingSystem targetingSystem;

    // 원소 시스템 (PHASE_13)
    private ElementType selectedElement = null;
    private ElementSkillSet elementSkillSet = null;

    // 상태
    private boolean isDead;

    // 현재 방향
    private PlayerDirection direction;

    // 적용된 버프 목록
    private List<Buff> activeBuffs;

    // 추가 방어력 보너스 (버프에서 적용)
    private int defenseBonus;

    // 배운 스킬 ID 목록 (레벨업으로 배운 스킬)
    private List<Integer> learnedSkillIds;

    // 업그레이드 관리자 (PHASE_19 - 마나 재생 등)
    private com.example.yugeup.game.upgrade.UpgradeManager upgradeManager;

    // 마나 재생 타이머
    private float manaRegenTimer = 0f;

    // 체력 재생 타이머
    private float hpRegenTimer = 0f;

    /**
     * Player 생성자
     */
    public Player() {
        this.position = new Vector2(0, 0);
        this.velocity = new Vector2(0, 0);
        this.targetPosition = new Vector2(0, 0);
        this.isDead = false;
        this.direction = PlayerDirection.FRONT;  // 기본 방향: 아래
        this.isRemote = false;

        // 버프 시스템 초기화
        this.activeBuffs = new ArrayList<>();
        this.defenseBonus = 0;

        // 배운 스킬 목록 초기화
        this.learnedSkillIds = new ArrayList<>();

        // 능력치 초기화 (PHASE_10)
        this.stats = new PlayerStats(1);  // 레벨 1로 시작
        initializeStatsListener();

        // 레벨 시스템 초기화 (PHASE_11)
        this.levelSystem = new LevelSystem(stats);
        initializeLevelSystemListener();

        // 스킬 시스템 초기화 (PHASE_12)
        this.targetingSystem = new TargetingSystem();
        this.magicMissile = new MagicMissile(this, targetingSystem);

        // 업그레이드 관리자 초기화 (PHASE_19)
        this.upgradeManager = new com.example.yugeup.game.upgrade.UpgradeManager(this);
    }

    /**
     * Player 생성자 (ID 지정)
     *
     * @param playerId 플레이어 ID
     */
    public Player(int playerId) {
        this();
        this.playerId = playerId;
    }

    /**
     * 능력치 변화 리스너 초기화 (PHASE_10)
     */
    private void initializeStatsListener() {
        stats.addListener(new StatsChangeListener() {
            @Override
            public void onHealthChanged(int oldValue, int newValue, int maxValue) {
                // HP 변화 처리
                if (newValue <= 0 && !isDead) {
                    handleDeath();
                }
            }

            @Override
            public void onManaChanged(int oldValue, int newValue, int maxValue) {
                // MP 변화 처리 (향후 UI 업데이트 등)
            }

            @Override
            public void onLevelUp(int newLevel) {
                // 레벨업 처리 (향후 이펙트, 알림 등)
                System.out.println("[Player] 능력치 레벨업! 새 레벨: " + newLevel);
            }
        });
    }

    /**
     * 레벨 시스템 리스너 초기화 (PHASE_11)
     */
    private void initializeLevelSystemListener() {
        final Player self = this;  // this 참조 명시
        levelSystem.addListener(new LevelUpListener() {
            @Override
            public void onExpGained(int amount, int currentExp, int maxExp) {
                System.out.println("[Player] 경험치 획득: +" + amount + " (" + currentExp + "/" + maxExp + ")");
            }

            @Override
            public void onLevelUp(int newLevel) {
                System.out.println("[Player] 레벨업! Lv." + newLevel);
                // 레벨업 시 스킬 배우기
                self.handleLevelUpSkillLearning(newLevel);

                // 레벨업 시 서버에 HP 동기화 (풀회복됨)
                com.example.yugeup.network.NetworkManager networkManager =
                    com.example.yugeup.network.NetworkManager.getInstance();
                if (networkManager != null && networkManager.isConnected()) {
                    networkManager.sendLevelUp(
                        self.playerId,
                        newLevel,
                        self.stats.getMaxHealth(),
                        self.stats.getCurrentHealth()
                    );
                }
            }
        });
    }

    /**
     * 몬스터 처치 시 경험치 획득 (PHASE_11)
     *
     * @param monster 처치한 몬스터
     */
    public void onMonsterKilled(Monster monster) {
        int baseExp = ExperienceManager.getExpForMonster(monster.getType());
        int adjustedExp = ExperienceManager.adjustExpByLevelDifference(
            baseExp,
            levelSystem.getCurrentLevel(),
            monster.getLevel()
        );

        levelSystem.gainExperience(adjustedExp);
        System.out.println("[Player] 몬스터 처치! 경험치: " + adjustedExp);
    }

    /**
     * 데미지 받기 (PHASE_10)
     *
     * 피해 감소 버프(스톤 실드 등)가 적용됩니다.
     *
     * @param rawDamage 기본 데미지
     */
    public void takeDamage(int rawDamage) {
        // 피해 감소 버프 적용
        float damageMultiplier = getDamageMultiplier();
        int reducedDamage = (int) (rawDamage * damageMultiplier);

        int actualDamage = stats.calculateDamageReceived(reducedDamage);
        stats.decreaseHealth(actualDamage);
        System.out.println("[Player] 데미지 받음: " + actualDamage + " (현재 HP: " + stats.getCurrentHealth() + "/" + stats.getMaxHealth() + ")");
    }

    /**
     * 체력 회복 (PHASE_10)
     *
     * @param amount 회복량
     */
    public void heal(int amount) {
        int actualHeal = stats.increaseHealth(amount);
        System.out.println("[Player] 체력 회복: " + actualHeal + " (현재 HP: " + stats.getCurrentHealth() + "/" + stats.getMaxHealth() + ")");
    }

    /**
     * 마나 소비 (PHASE_10)
     *
     * @param amount 소비량
     * @return 소비 성공 여부
     */
    public boolean useMana(int amount) {
        return stats.consumeMana(amount);
    }

    /**
     * 사망 처리 (PHASE_10)
     */
    private void handleDeath() {
        this.isDead = true;
        System.out.println("[Player] 사망!");
        // 향후: 사망 애니메이션, 서버 알림, 리스폰 대기 등
    }

    /**
     * 플레이어를 업데이트합니다.
     *
     * 버프 업데이트를 포함합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 사망 상태면 업데이트 안 함
        if (isDead) {
            return;
        }

        // 버프 업데이트
        updateBuffs(delta);

        // 원격 플레이어인 경우 보간 이동 (PHASE_23)
        if (isRemote) {
            updateRemotePlayerInterpolation(delta);
        } else {
            // 로컬 플레이어: 위치 업데이트: position += velocity * delta
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
        }

        // 이동 중이면 방향 업데이트
        updateDirection();

        // 맵 경계 제한은 PlayerController의 벽 충돌 체크에서 처리함
        // clampToMapBounds(); // REMOVED: Tiled 맵 사용으로 인해 PlayerController에서 처리

        // 스킬 업데이트 (PHASE_12) - 로컬 플레이어만
        if (!isRemote && magicMissile != null) {
            magicMissile.update(delta);
            magicMissile.updateProjectiles(delta);
        }

        // 원소 스킬 업데이트 (PHASE_14~18) - 로컬 플레이어만
        if (!isRemote && elementSkillSet != null) {
            elementSkillSet.update(delta);
        }

        // 마나 재생 (PHASE_19) - 로컬 플레이어만
        if (!isRemote) {
            updateManaRegeneration(delta);
            // HP 재생은 서버가 관리하므로 클라이언트에서 비활성화
            // updateHpRegeneration(delta);
        }
    }

    /**
     * 마나 자동 재생 (PHASE_19)
     *
     * 기본 재생: 5 MP/초 (대폭 증가)
     * 업그레이드 보너스: +1 MP/초씩 추가
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateManaRegeneration(float delta) {
        // 기본 마나 재생: 5 MP/초 (기존 1 → 5)
        float baseManaRegen = 5.0f;

        // 업그레이드 보너스 추가
        float totalManaRegen = baseManaRegen;
        if (upgradeManager != null) {
            totalManaRegen += upgradeManager.getManaRegenBonus();
        }

        // 1초마다 마나 회복
        manaRegenTimer += delta;
        if (manaRegenTimer >= 1.0f) {
            manaRegenTimer -= 1.0f;

            // 마나 회복
            int regenAmount = (int) totalManaRegen;
            if (regenAmount > 0) {
                stats.increaseMana(regenAmount);
            }
        }
    }

    /**
     * 체력 재생을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateHpRegeneration(float delta) {
        // 기본 체력 재생: 2 HP/초
        float baseHpRegen = 2.0f;

        // 2초마다 체력 회복
        hpRegenTimer += delta;
        if (hpRegenTimer >= 2.0f) {
            hpRegenTimer -= 2.0f;

            // 체력 회복 (최대 체력 미만일 때만)
            int regenAmount = (int) (baseHpRegen * 2);  // 2초치
            if (regenAmount > 0 && stats.getCurrentHealth() < stats.getMaxHealth()) {
                stats.increaseHealth(regenAmount);
            }
        }
    }

    /**
     * 활성 버프를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateBuffs(float delta) {
        // 활성화되지 않은 버프 제거
        activeBuffs.removeIf(buff -> !buff.isActive());

        // 모든 활성 버프 업데이트
        for (Buff buff : activeBuffs) {
            buff.update(delta);
        }
    }

    /**
     * 원격 플레이어의 보간 이동을 처리합니다. (PHASE_23)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updateRemotePlayerInterpolation(float delta) {
        // 목표 위치까지의 거리 계산
        float distance = position.dst(targetPosition);

        // 거리가 매우 가까우면 즉시 목표 위치로 이동 (멈춤 감지)
        if (distance < 2f) {
            position.set(targetPosition);
            velocity.set(0, 0);
            return;
        }

        // 보간 속도: 10 unit/s (부드럽게 따라감)
        float interpolationSpeed = 10f;

        // 현재 위치에서 목표 위치로 선형 보간
        position.lerp(targetPosition, Math.min(1f, interpolationSpeed * delta));

        // 이동 속도 계산 (방향 업데이트를 위해)
        float dx = targetPosition.x - position.x;
        float dy = targetPosition.y - position.y;
        velocity.set(dx, dy);
    }

    /**
     * 이동 방향에 따라 플레이어 방향을 업데이트합니다.
     */
    private void updateDirection() {
        // 정지 상태면 방향 변경 안 함
        if (velocity.len() < 0.01f) {
            return;
        }

        // 수평/수직 방향 중 더 큰 쪽으로 방향 결정
        float absX = Math.abs(velocity.x);
        float absY = Math.abs(velocity.y);

        if (absX > absY) {
            // 좌우 이동이 더 큼
            direction = velocity.x > 0 ? PlayerDirection.RIGHT : PlayerDirection.LEFT;
        } else {
            // 상하 이동이 더 큼
            direction = velocity.y > 0 ? PlayerDirection.BACK : PlayerDirection.FRONT;
        }
    }

    // REMOVED: clampToMapBounds() - Tiled 맵 벽 충돌은 PlayerController에서 처리

    /**
     * 플레이어 위치를 설정합니다.
     *
     * @param x X 좌표
     * @param y Y 좌표
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        // 원격 플레이어인 경우 목표 위치도 설정 (PHASE_23)
        if (isRemote) {
            this.targetPosition.set(x, y);
        }
    }

    /**
     * 원격 플레이어의 목표 위치를 설정합니다. (PHASE_23)
     *
     * @param x 목표 X 좌표
     * @param y 목표 Y 좌표
     */
    public void setTargetPosition(float x, float y) {
        this.targetPosition.set(x, y);
    }

    /**
     * 플레이어 속도를 설정합니다.
     *
     * @param vx X 방향 속도
     * @param vy Y 방향 속도
     */
    public void setVelocity(float vx, float vy) {
        this.velocity.set(vx, vy);
    }

    // ===== Getter & Setter =====

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public boolean isDead() {
        return isDead || (stats != null && stats.isDead());
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    /**
     * 플레이어가 살아있는가? (PHASE_10)
     *
     * @return 생존 여부
     */
    public boolean isAlive() {
        return !isDead();
    }

    public PlayerDirection getDirection() {
        return direction;
    }

    public void setDirection(PlayerDirection direction) {
        this.direction = direction;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public LevelSystem getLevelSystem() {
        return levelSystem;
    }

    /**
     * 매직 미사일 스킬을 반환합니다. (PHASE_12)
     *
     * @return 매직 미사일 스킬
     */
    public MagicMissile getMagicMissile() {
        return magicMissile;
    }

    /**
     * 타게팅 시스템을 반환합니다. (PHASE_12)
     *
     * @return 타게팅 시스템
     */
    public TargetingSystem getTargetingSystem() {
        return targetingSystem;
    }

    /**
     * 원소 설정 (PHASE_13)
     *
     * 원소 선택 시 현재 레벨에 맞는 스킬을 모두 배웁니다.
     *
     * @param element 선택한 원소
     */
    public void setElement(ElementType element) {
        this.selectedElement = element;
        this.elementSkillSet = new ElementSkillSet(element, this);
        System.out.println("[Player] 원소 선택: " + element.getDisplayName());

        // 현재 레벨까지의 모든 스킬 배우기
        int currentLevel = levelSystem.getCurrentLevel();
        for (int level = 1; level <= currentLevel; level++) {
            handleLevelUpSkillLearning(level);
        }
    }

    /**
     * 원소 스킬 세트 가져오기 (PHASE_13)
     *
     * @return 원소 스킬 세트
     */
    public ElementSkillSet getElementSkillSet() {
        return elementSkillSet;
    }

    /**
     * 선택한 원소 가져오기 (PHASE_13)
     *
     * @return 선택한 원소 타입
     */
    public ElementType getSelectedElement() {
        return selectedElement;
    }

    // ===== 버프 관리 메서드 (PHASE_14+) =====

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
    public void removeBuff(BuffType buffType) {
        activeBuffs.removeIf(buff -> buff.getBuffType() == buffType);
    }

    /**
     * 특정 타입의 버프가 적용되어 있는지 확인합니다.
     *
     * @param buffType 확인할 버프 타입
     * @return 버프 적용 여부
     */
    public boolean hasBuff(BuffType buffType) {
        return activeBuffs.stream()
            .anyMatch(buff -> buff.getBuffType() == buffType && buff.isActive());
    }

    /**
     * 무적 상태인지 확인합니다.
     *
     * @return 무적 상태 여부
     */
    public boolean hasInvincibleBuff() {
        return hasBuff(BuffType.INVINCIBLE);
    }

    /**
     * 기절 상태인지 확인합니다.
     *
     * @return 기절 상태 여부
     */
    public boolean isStunned() {
        return hasBuff(BuffType.STUN);
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
            if (buff.getBuffType() == BuffType.SLOW && buff.isActive()) {
                com.example.yugeup.game.buff.SlowBuff slowBuff = (com.example.yugeup.game.buff.SlowBuff) buff;
                multiplier *= slowBuff.getSpeedMultiplier();
            }
        }

        // 가속 버프 적용
        for (Buff buff : activeBuffs) {
            if (buff.getBuffType() == BuffType.SPEED && buff.isActive()) {
                com.example.yugeup.game.buff.SpeedBuff speedBuff = (com.example.yugeup.game.buff.SpeedBuff) buff;
                multiplier *= speedBuff.getSpeedMultiplier();
            }
        }

        return multiplier;
    }

    /**
     * 현재 적용된 피해 감소 배수를 계산합니다.
     *
     * 스톤 실드 등의 피해 감소 버프를 고려합니다.
     *
     * @return 피해 배수 (1.0 = 정상, 0.5 = 50% 감소)
     */
    public float getDamageMultiplier() {
        float multiplier = 1.0f;

        // 피해 감소 버프 적용
        for (Buff buff : activeBuffs) {
            if (buff.getBuffType() == BuffType.DAMAGE_REDUCTION && buff.isActive()) {
                com.example.yugeup.game.buff.DamageReductionBuff drBuff =
                    (com.example.yugeup.game.buff.DamageReductionBuff) buff;
                multiplier *= drBuff.getDamageMultiplier();
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

    // ===== 스킬 학습 시스템 =====

    /**
     * 레벨업 시 스킬을 배웁니다.
     *
     * 레벨별로 각 원소의 스킬을 순차적으로 배웁니다:
     * - 레벨 1: 기본 공격 (MagicMissile)
     * - 레벨 2: 선택한 원소의 스킬 A
     * - 레벨 3: 선택한 원소의 스킬 B
     * - 레벨 4: 선택한 원소의 스킬 C
     * - 레벨 5+: 모든 스킬 보유
     *
     * @param newLevel 새로운 레벨
     */
    public void handleLevelUpSkillLearning(int newLevel) {
        // 원소를 선택하지 않았으면 반환
        if (selectedElement == null || elementSkillSet == null) {
            System.out.println("[Player] 원소 미선택 상태로 스킬 배우기 건너뜀");
            return;
        }

        int[] skillIds = selectedElement.getSkillIds();
        String[] skillNames = selectedElement.getSkillNames();

        // 레벨별 스킬 학습
        switch (newLevel) {
            case 1:
                // 레벨 1: 기본 공격은 처음부터 보유
                learnSkill(0, "기본 공격");
                break;

            case 2:
                // 레벨 2: 선택한 원소의 첫 번째 스킬 배우기
                learnSkill(skillIds[0], skillNames[0]);
                break;

            case 3:
                // 레벨 3: 선택한 원소의 두 번째 스킬 배우기
                learnSkill(skillIds[1], skillNames[1]);
                break;

            case 4:
                // 레벨 4: 선택한 원소의 세 번째 스킬 배우기
                learnSkill(skillIds[2], skillNames[2]);
                break;

            default:
                // 레벨 5 이상: 모든 스킬 보유 (이미 배운 스킬이므로 추가 처리 없음)
                break;
        }
    }

    /**
     * 스킬을 배웁니다.
     *
     * @param skillId 배울 스킬 ID
     * @param skillName 스킬 이름
     */
    private void learnSkill(int skillId, String skillName) {
        // 이미 배운 스킬이면 중복 학습 방지
        if (learnedSkillIds.contains(skillId)) {
            System.out.println("[Player] 이미 배운 스킬: " + skillName);
            return;
        }

        // 새로운 스킬 배우기
        learnedSkillIds.add(skillId);
        System.out.println("[Player] 새로운 스킬 배움: " + skillName + " (ID: " + skillId + ")");
    }

    /**
     * 특정 스킬을 배웠는지 확인합니다.
     *
     * @param skillId 확인할 스킬 ID
     * @return 스킬 학습 여부
     */
    public boolean hasLearnedSkill(int skillId) {
        return learnedSkillIds.contains(skillId);
    }

    /**
     * 배운 모든 스킬의 ID를 반환합니다.
     *
     * @return 배운 스킬 ID 목록
     */
    public List<Integer> getLearnedSkillIds() {
        return new ArrayList<>(learnedSkillIds);
    }

    /**
     * 현재 레벨에서 사용 가능한 스킬 개수를 반환합니다.
     *
     * @return 사용 가능한 스킬 개수
     */
    public int getAvailableSkillCount() {
        int level = levelSystem.getCurrentLevel();
        if (level >= 4) return 3;  // 레벨 4 이상: 원소 스킬 3개 모두
        if (level >= 3) return 2;  // 레벨 3: 원소 스킬 2개
        if (level >= 2) return 1;  // 레벨 2: 원소 스킬 1개
        return 0;  // 레벨 1: 기본 공격만
    }

    /**
     * 업그레이드 관리자를 반환합니다. (PHASE_19)
     *
     * @return 업그레이드 관리자
     */
    public com.example.yugeup.game.upgrade.UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
}
