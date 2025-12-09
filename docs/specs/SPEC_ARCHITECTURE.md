# SPEC_ARCHITECTURE.md - 클라이언트 아키텍처 (빠른 참조)

> 각 모듈의 책임과 클래스 구조를 빠르게 파악하기 위한 문서

---

## 📦 패키지 맵

### com.example.yugeup.screens
**역할**: 게임 화면 전환 관리

```
LoadingScreen
  ├─ loadResources()
  ├─ updateProgress()
  └─ onLoadComplete() → MainMenuScreen로

MainMenuScreen
  ├─ showMenu()
  └─ onStartClick() → LobbyScreen으로

LobbyScreen
  ├─ fetchRoomList()
  ├─ createRoom()
  ├─ joinRoom(roomId) → WaitingRoomScreen으로
  └─ showCharacterPreview()

WaitingRoomScreen
  ├─ showPlayers()
  ├─ sendChat()
  ├─ onGameStart() → GameScreen으로
  └─ onLeaveRoom() → LobbyScreen으로

GameScreen
  ├─ update(delta)
  ├─ render(batch)
  ├─ handleInput()
  └─ onGameEnd() → ResultScreen으로

ResultScreen
  ├─ showRank()
  └─ returnToMenu() → MainMenuScreen으로
```

### com.example.yugeup.game.player
**역할**: 플레이어 엔티티 및 제어

```
Player (엔티티)
  ├─ position: Vector2
  ├─ stats: PlayerStats
  ├─ skills: SkillManager
  ├─ update(delta)
  └─ render(batch)

PlayerStats (능력치 저장소)
  ├─ health, maxHealth
  ├─ mana, maxMana
  ├─ attack, defense, speed
  ├─ level, experience
  ├─ takeDamage(damage): int
  └─ gainExperience(exp)

PlayerController (입력 처리)
  ├─ handleJoystickInput(x, y)
  ├─ handleSkillButton(slotId)
  └─ sendPlayerMove()
```

### com.example.yugeup.game.monster
**역할**: 몬스터 엔티티 및 관리

```
Monster (추상 클래스)
  ├─ position, velocity
  ├─ stats: MonsterStats
  ├─ aiState: AIState
  ├─ target: Player
  ├─ update(delta, players)
  ├─ takeDamage(damage)
  └─ render(batch)

Ghost, Slime, Golem (구체 클래스)
  └─ updateAI() 구현

MonsterManager
  ├─ monsters: List<Monster>
  ├─ update(delta)
  ├─ addMonster(monster)
  ├─ removeMonster(monsterId)
  └─ getMonster(monsterId)

MonsterSpawner
  ├─ spawnRate: float
  ├─ getCurrentSpawnType(): MonsterType
  └─ spawn(): Monster
```

### com.example.yugeup.game.skill
**역할**: 스킬 시스템

```
Skill (기본 클래스)
  ├─ skillId: int
  ├─ name: String
  ├─ manaCost: int
  ├─ baseDamage: int
  ├─ cooldown, currentCooldown: float
  ├─ cast(caster)
  └─ update(delta)

MagicMissile (기본 공격)
  ├─ autoTarget: boolean
  ├─ projectileSpeed: float
  └─ findNearestMonster(): Monster

ElementalSkill (원소 스킬)
  ├─ element: ElementType
  ├─ level: int
  ├─ upgrades: UpgradeType[3]
  └─ applyUpgrade(type)

SkillManager
  ├─ activeSkills[4]: Skill
  ├─ castSkill(slotId)
  ├─ upgradeSkill(slotId, type)
  └─ update(delta)

Projectile
  ├─ position, velocity
  ├─ damage: int
  ├─ lifetime: float
  └─ update(delta)
```

### com.example.yugeup.game.combat
**역할**: 전투 시스템

```
CombatSystem
  ├─ dealDamage(attacker, target, baseDamage): int
  ├─ calculateDamage(...): int
  ├─ applyStatusEffect(target, effect)
  ├─ onMonsterDeath(monster, killer)
  └─ onPlayerDeath(player, killer)

DamageCalculator
  ├─ attacker: Entity
  ├─ defender: Entity
  ├─ baseDamage: int
  ├─ calculate(): int
  └─ getTypeModifier(type): float

CollisionDetector
  ├─ checkProjectile(projectile): List<Target>
  ├─ checkMonsterAttack(monster): Player
  └─ checkPlayerCollision(player): List<Player>
```

### com.example.yugeup.game.level
**역할**: 레벨 및 경험치

```
LevelSystem
  ├─ currentLevel: int = 1
  ├─ currentExp: int = 0
  ├─ gainExperience(exp)
  ├─ levelUp()
  ├─ getRequiredExp(level): int
  └─ isMaxLevel(): boolean

ExperienceTable
  └─ (static) 레벨별 필요 경험치 상수
```

### com.example.yugeup.game.map
**역할**: 맵 및 자기장

```
GameMap
  ├─ WIDTH = 1920, HEIGHT = 1920
  ├─ zones: List<Zone>
  ├─ getZoneAtPosition(x, y): Zone
  └─ isInBounds(x, y): boolean

Zone
  ├─ zoneId: int
  ├─ bounds: Rectangle
  ├─ status: ZoneStatus
  ├─ closureTimer: float
  ├─ damagePerSecond: int
  └─ isPlayerInZone(player): boolean

ZoneManager
  ├─ currentTime: float
  ├─ gameEndTime: float = 600f
  ├─ update(delta)
  ├─ closeZone(zoneId)
  └─ getZoneDamage(player): int
```

### com.example.yugeup.network
**역할**: 네트워킹

```
NetworkManager (Singleton)
  ├─ isConnected: boolean
  ├─ playerId: int
  ├─ connect(host, port)
  ├─ sendMessage(msg)
  ├─ onMessageReceived(msg)
  └─ disconnect()

MessageHandler
  ├─ handlers: Map<Class, Handler>
  ├─ handle(msg)
  ├─ onPlayerMove(msg)
  ├─ onMonsterSpawn(msg)
  ├─ onMonsterUpdate(msg)
  ├─ onMonsterDeath(msg)
  ├─ onSkillCast(msg)
  └─ onGameEnd(msg)

RoomManager
  ├─ roomId: int
  ├─ players: List<Player>
  ├─ isHost: boolean
  ├─ createRoom(name, maxPlayers)
  ├─ joinRoom(roomId)
  ├─ startGame()
  └─ leaveRoom()

Messages/
  ├─ PlayerMoveMsg
  ├─ SkillCastMsg
  ├─ MonsterSpawnMsg
  ├─ MonsterUpdateMsg
  ├─ MonsterDeathMsg
  ├─ ChatMsg
  └─ ... (기타)
```

### com.example.yugeup.ui
**역할**: UI 컴포넌트

```
hud/
  ├─ HPBarComponent (플레이어 체력)
  ├─ MPBarComponent (플레이어 마나)
  ├─ LevelDisplayComponent
  ├─ SkillButtonComponent[4] (스킬 버튼)
  ├─ KillLogComponent (킬 로그)
  └─ ZoneTimerComponent

lobby/
  ├─ RoomListPanel
  ├─ CharacterPreviewPanel
  └─ CharacterCustomizePanel

dialog/
  ├─ ResultDialog
  ├─ SettingsDialog
  └─ ConfirmDialog
```

### com.example.yugeup.input
**역할**: 입력 처리

```
InputHandler
  ├─ onJoystickMove(x, y)
  ├─ onSkillButtonPressed(slotId)
  └─ onUIButtonPressed(button)

JoystickController
  ├─ position: Vector2
  ├─ radius: float
  ├─ getDirection(): Vector2
  └─ isDragging: boolean

TouchInputListener
  └─ (InputAdapter 상속)
```

### com.example.yugeup.animation
**역할**: 애니메이션

```
AnimationManager
  ├─ animations: Map<String, Animation>
  ├─ getAnimation(name): Animation
  └─ playAnimation(entity, animName)

SpriteAnimation
  ├─ frames: Sprite[]
  ├─ frameTime: float
  ├─ update(delta)
  └─ getCurrentFrame(): Sprite
```

### com.example.yugeup.utils
**역할**: 유틸리티

```
Constants
  ├─ SCREEN_WIDTH, SCREEN_HEIGHT
  ├─ GAME_SCALE
  ├─ PLAYER_SPEED, PLAYER_MAX_HP
  ├─ MONSTER_* (각 몬스터 상수)
  ├─ SKILL_* (각 스킬 상수)
  └─ NETWORK_* (네트워크 설정)

MathUtils
  ├─ distance(x1, y1, x2, y2): float
  ├─ clamp(value, min, max): float
  └─ randomRange(min, max): float

AssetManager
  ├─ loadTexture(path): Texture
  ├─ loadFont(path): BitmapFont
  └─ getAsset(key): T

Logger
  ├─ info(msg)
  ├─ warn(msg)
  └─ error(msg, exception)
```

---

## 🔄 주요 데이터 흐름

### 플레이어 입력 → 공격 → 동기화

```
PlayerController.onSkillButtonPressed()
  ↓
SkillManager.castSkill()
  ↓
Skill.cast()
  ↓
CombatSystem.dealDamage()
  ↓
MonsterManager.takeDamage()
  ↓
NetworkManager.sendPlayerAttackMsg()
  ↓
(서버에서 처리)
```

### 서버 → 클라이언트 수신

```
NetworkManager.onMessageReceived()
  ↓
MessageHandler.handle()
  ↓
(메시지 타입별 핸들러 실행)
  ├─ onMonsterSpawn()
  ├─ onMonsterUpdate()
  ├─ onMonsterDeath()
  └─ onPlayerMove()
  ↓
GameScreen.update() 반영
```

---

## 📋 클래스 책임 (Single Responsibility)

| 클래스 | 책임 | 의존성 |
|--------|------|--------|
| Player | 플레이어 엔티티 | Stats, Skills |
| Monster | 몬스터 엔티티 | MonsterStats, AI |
| CombatSystem | 데미지 계산 | DamageCalculator |
| SkillManager | 스킬 관리 | Skill들 |
| NetworkManager | 네트워킹 | MessageHandler |
| GameScreen | 렌더링 | 모든 게임 객체 |

---

**SPEC 참조**: 구체적인 클래스 설계는 SDD.md 참조
**Phase 참조**: 구현 순서는 PHASE_*.md 참조

