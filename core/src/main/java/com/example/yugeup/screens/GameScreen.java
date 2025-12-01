package com.example.yugeup.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.game.level.LevelUpListener;
import com.example.yugeup.game.map.GameMap;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.ui.ElementSelectOverlay;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.player.PlayerController;
import com.example.yugeup.network.MessageHandler;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.ui.hud.HUDRenderer;
import com.example.yugeup.ui.hud.LevelUpUpgradePanel;
import com.example.yugeup.utils.AssetManager;
import com.example.yugeup.utils.Constants;
import org.example.Main.*;
import org.example.MonsterSpawnMsg;
import org.example.MonsterUpdateMsg;
import org.example.MonsterDeathMsg;
import org.example.MonsterDamageMsg;
import com.example.yugeup.network.messages.SkillCastMsg;
import com.example.yugeup.network.messages.FogZoneMsg;
import com.example.yugeup.network.messages.FogDamageMsg;
import com.example.yugeup.network.messages.MonsterAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerDeathMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 게임 화면
 *
 * 실제 게임 플레이가 진행되는 메인 화면입니다.
 * 플레이어, 몬스터, 스킬, UI 등을 렌더링하고 업데이트합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class GameScreen implements Screen {

  private Game game;
  private RoomInfo roomInfo;
  private List<PlayerInfo> players;

  // 렌더링
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;
  private Viewport viewport;
  private OrthographicCamera camera;

  // 게임 맵 (PHASE_09)
  private GameMap gameMap;

  // 폰트
  private BitmapFont font;

  // 캐릭터 아틀라스 및 애니메이션
  private TextureAtlas characterAtlas;
  private TextureRegion[] characterFrontFrames;
  private TextureRegion[] characterBackFrames;
  private TextureRegion[] characterLeftFrames;
  private TextureRegion[] characterRightFrames;

  // 애니메이션 타이머
  private float animationTimer;

  // 현재 플레이어
  private Player myPlayer;
  private PlayerController playerController;

  // 원격 플레이어 목록 (playerId -> Player) - PHASE_23
  private Map<Integer, Player> remotePlayers;

  // 다른 플레이어의 발사체 목록 (네트워크 동기화용)
  private java.util.List<com.example.yugeup.game.skill.Projectile> otherPlayerProjectiles;
  // 다른 플레이어의 PVP 발사체 목록
  private java.util.List<com.example.yugeup.game.skill.PvpProjectile> otherPlayerPvpProjectiles;

  // 몬스터 관리자 (PHASE_20)
  private com.example.yugeup.game.monster.MonsterManager monsterManager;

  // 몬스터 HP 동기화 문제 해결용 타임스탬프 맵
  private Map<Integer, Long> lastDamageTimestamp;

  // HUD 렌더러 (PHASE_11)
  private HUDRenderer hudRenderer;

  // 원소 선택 오버레이 (PHASE_13)
  private ElementSelectOverlay elementSelectOverlay;
  private boolean elementSelected = false; // 원소 선택 완료 여부

  // UI 시스템 (PHASE_19 - 레벨업 업그레이드)
  private Stage stage;
  private Skin skin;
  private LevelUpUpgradePanel levelUpUpgradePanel;

    private Map<Integer, String> playerNames;  // playerId -> playerName


    // 몬스터 스폰 비활성화 플래그
  private boolean monsterSpawnEnabled = true; // 몬스터 스폰 활성화

  // MonsterData 클래스 제거 - MonsterManager 사용

  // 원격 스킬 이펙트 목록
  private java.util.List<com.example.yugeup.game.effect.RemoteSkillEffect> remoteSkillEffects;

  // Fog 시스템 (PHASE_24)
  private float gameTime = 0f;  // 게임 경과 시간

  // 사망 결과 시스템 (PHASE_26)
  private boolean showDeathResult = false;  // 사망 결과 표시 여부
  private int deathRank = 0;                // 사망 순위
  private String deathKillerName = "";      // 킬러 이름

  /**
   * GameScreen 생성자
   *
   * @param game     Game 인스턴스
   * @param roomInfo 방 정보
   * @param players  플레이어 목록
   */
  public GameScreen(Game game, RoomInfo roomInfo, List<PlayerInfo> players) {
    this.game = game;
    this.roomInfo = roomInfo;
    this.players = players;
    this.monsterManager = new com.example.yugeup.game.monster.MonsterManager();
    this.remotePlayers = new HashMap<>();
    this.otherPlayerProjectiles = new java.util.ArrayList<>();
    this.otherPlayerPvpProjectiles = new java.util.ArrayList<>();  // PVP 발사체 초기화
    this.lastDamageTimestamp = new HashMap<>();  // HP 동기화용 타임스탬프 초기화
    this.remoteSkillEffects = new java.util.ArrayList<>();  // 원격 스킬 이펙트 초기화
  }

  @Override
  public void show() {
      // 렌더링 초기화
    batch = new SpriteBatch();
    playerNames = new HashMap<>();
    shapeRenderer = new ShapeRenderer();
    viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    camera = (OrthographicCamera) viewport.getCamera();
    camera.zoom = 0.05f; // 카메라 줌 인 (0.15 → 0.05, 극도로 확대하여 매우 좁은 시야)

    // players 정보로 초기화
    if (players != null) {
        for (PlayerInfo playerInfo : players) {
            playerNames.put(playerInfo.playerId, playerInfo.playerName);
        }
    }
    // 게임 맵 초기화 (PHASE_09)
    gameMap = new GameMap();

    // 에셋 로드
    AssetManager assetManager = AssetManager.getInstance();
    font = assetManager.getFont("font_small");
    characterAtlas = assetManager.getAtlas("character");

    // 캐릭터 애니메이션 프레임 로드 (4방향 x 4프레임)
    characterFrontFrames = new TextureRegion[4];
    characterBackFrames = new TextureRegion[4];
    characterLeftFrames = new TextureRegion[4];
    characterRightFrames = new TextureRegion[4];

    for (int i = 0; i < 4; i++) {
      characterFrontFrames[i] = characterAtlas.findRegion("character-front-" + i);
      characterBackFrames[i] = characterAtlas.findRegion("character-back-" + i);
      characterLeftFrames[i] = characterAtlas.findRegion("character-left-" + i);
      characterRightFrames[i] = characterAtlas.findRegion("character-right-" + i);
    }

    // 애니메이션 타이머 초기화
    animationTimer = 0f;

    // 플레이어 초기화 - 서버에서 할당받은 스폰 위치 사용
    int myPlayerId = NetworkManager.getInstance().getCurrentPlayerId();
    myPlayer = new Player(myPlayerId);

    // 서버에서 받은 스폰 위치 찾기
    float spawnX = gameMap.getWidth() / 2f;  // 기본값
    float spawnY = gameMap.getHeight() / 2f;

    if (players != null) {
      for (PlayerInfo playerInfo : players) {
        if (playerInfo.playerId == myPlayerId) {
          // 서버에서 받은 스폰 위치 사용
          if (playerInfo.spawnX != 0 || playerInfo.spawnY != 0) {
            spawnX = playerInfo.spawnX;
            spawnY = playerInfo.spawnY;
          }
          break;
        }
      }
    }

    myPlayer.setPosition(spawnX, spawnY);
    System.out.println("[GameScreen] 플레이어 스폰 위치 (서버): (" + spawnX + ", " + spawnY + ")");
    System.out.println("[GameScreen] 플레이어 초기 HP: " + myPlayer.getStats().getCurrentHealth() +
        "/" + myPlayer.getStats().getMaxHealth());

    // 방에 이미 있던 다른 플레이어들 초기화 - 각자의 스폰 위치 사용 (PHASE_23)
    if (players != null) {
      for (PlayerInfo playerInfo : players) {
        if (playerInfo.playerId != myPlayerId) {
          Player remotePlayer = new Player(playerInfo.playerId);
          remotePlayer.setRemote(true); // 원격 플레이어로 설정 (보간 이동 활성화)

          // 각 플레이어의 스폰 위치 사용
          float remoteSpawnX = playerInfo.spawnX != 0 ? playerInfo.spawnX : spawnX;
          float remoteSpawnY = playerInfo.spawnY != 0 ? playerInfo.spawnY : spawnY;
          remotePlayer.setPosition(remoteSpawnX, remoteSpawnY);

          remotePlayers.put(playerInfo.playerId, remotePlayer);
          System.out.println("[GameScreen] 초기 원격 플레이어 추가: ID=" + playerInfo.playerId +
              " 위치: (" + remoteSpawnX + ", " + remoteSpawnY + ")");
        }
      }
    }

    // PlayerController 초기화 (viewport 전달)
    playerController = new PlayerController(myPlayer, viewport);
    playerController.setGameMap(gameMap); // 벽 충돌 체크를 위한 GameMap 설정

    // 아틀라스 로드
    TextureAtlas elementalsAtlas = assetManager.getAtlas("elemental");
    TextureAtlas skillsAtlas = assetManager.getAtlas("skills");

    // 스킬 이펙트 애니메이션 시스템 초기화 (PHASE_24)
    try {
      TextureAtlas skillEffectsAtlas = new TextureAtlas(
          com.badlogic.gdx.Gdx.files.internal("skills/skills.atlas"));
      com.example.yugeup.game.skill.SkillEffectManager.getInstance().loadAtlas(skillEffectsAtlas);
      System.out.println("[GameScreen] 스킬 이펙트 애니메이션 시스템 초기화 완료");
    } catch (Exception e) {
      System.out.println("[GameScreen] 스킬 이펙트 atlas 로드 실패: " + e.getMessage());
    }

    // HUD 초기화 (PHASE_11, 스킬 아이콘 아틀라스 포함)
    hudRenderer = new HUDRenderer(myPlayer, font, elementalsAtlas, skillsAtlas);

    // 원소 선택 오버레이 초기화 (PHASE_13)
    elementSelectOverlay = new ElementSelectOverlay(myPlayer, font, elementalsAtlas, viewport);
    elementSelectOverlay.setListener(new ElementSelectOverlay.ElementSelectListener() {
      @Override
      public void onElementConfirmed(ElementType element) {
        elementSelected = true;
        // 원소 선택 완료 후 HUD 재초기화 (스킬 버튼 생성)
        TextureAtlas skillsAtlas = assetManager.getAtlas("skills");
        hudRenderer = new HUDRenderer(myPlayer, font, elementalsAtlas, skillsAtlas);

        // 스킬 버튼 위치 재조정 (viewport 크기 기준)
        hudRenderer.repositionSkillButtons();

        // HUDRenderer를 PlayerController에 설정
        playerController.setHUDRenderer(hudRenderer);
        // PlayerController 활성화
        Gdx.input.setInputProcessor(playerController);

        // MagicMissile 타게팅 시스템을 MonsterManager와 연결
        setupMagicMissileTargeting();

        System.out.println("[GameScreen] 원소 선택 완료: " + element.getDisplayName());
      }
    });

    // 원소 선택 전에는 입력 프로세서 설정 안 함 (ElementSelectOverlay가 직접 입력 처리)

    // UI 시스템 초기화 (PHASE_19 - 레벨업 업그레이드)
    stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));

    // UI 스킨 로드 시도 (파일이 없으면 스킵)
    try {
      skin = new Skin(Gdx.files.internal(Constants.UI_SKIN_PATH));
      System.out.println("[GameScreen] UI 스킨 로드 완료");
    } catch (Exception e) {
      System.out.println("[GameScreen] UI 스킨 파일을 찾을 수 없습니다: " + e.getMessage());
      skin = null;  // 나중에 프로그래매틱 스킨 생성 가능
    }

    // 레벨업 리스너 등록 - 레벨업 시 UI 표시
    myPlayer.getLevelSystem().addListener(new LevelUpListener() {
      @Override
      public void onExpGained(int amount, int currentExp, int maxExp) {
        // 경험치 획득 이벤트 (현재 미사용)
      }

      @Override
      public void onLevelUp(int newLevel) {
        // 레벨업 시 업그레이드 패널 표시
        System.out.println("[GameScreen] 레벨업! 현재 레벨: " + newLevel);
        showLevelUpUpgradePanel(newLevel);
      }
    });

    // 몬스터 사망 리스너 설정 (경험치 획득)
    monsterManager.setDeathListener(new com.example.yugeup.game.monster.MonsterManager.MonsterDeathListener() {
      @Override
      public void onMonsterDeath(com.example.yugeup.game.monster.Monster monster) {
        // 경험치 계산 및 획득
        int exp = com.example.yugeup.game.level.ExperienceManager.getExpForMonster(monster.getType());
        myPlayer.getLevelSystem().gainExperience(exp);
        System.out.println("[GameScreen] 몬스터 처치! +" + exp + " EXP (타입: " + monster.getType() + ")");
      }
    });

    // 몬스터에게 GameMap 설정 (벽 충돌 감지)
    monsterManager.setGameMap(gameMap);

    System.out.println("[GameScreen] 게임 화면 초기화 완료");
    System.out.println("[GameScreen] 플레이어 ID: " + myPlayerId);
    System.out.println("[GameScreen] 초기 위치: (" + myPlayer.getX() + ", " + myPlayer.getY() + ")");
  }

  /**
   * 레벨업 업그레이드 패널을 표시합니다. (PHASE_19)
   *
   * @param newLevel 새 레벨
   */
  private void showLevelUpUpgradePanel(int newLevel) {
    try {
      // 패널이 없으면 생성
      if (levelUpUpgradePanel == null) {
        levelUpUpgradePanel = new LevelUpUpgradePanel(myPlayer, newLevel);
      }

      // 레벨업 이벤트 호출 (내부에서 show() 호출됨)
      levelUpUpgradePanel.onLevelUp(newLevel);
      System.out.println("[GameScreen] 레벨업 업그레이드 패널 표시: 레벨 " + newLevel);
    } catch (Exception e) {
      System.out.println("[GameScreen] 레벨업 패널 생성 실패: " + e.getMessage());
      e.printStackTrace();
    }

    // ===== 테스트용 몬스터 스폰 (서버에서 관리하므로 비활성화) =====
    // spawnTestMonsters();
  }

  /**
   * MagicMissile 타게팅 시스템을 MonsterManager와 연결합니다.
   */
  private void setupMagicMissileTargeting() {
    // Player의 TargetingSystem에 MonsterManager의 몬스터 리스트 연결
    com.example.yugeup.game.skill.TargetingSystem targetingSystem = myPlayer.getTargetingSystem();
    if (targetingSystem == null) {
      System.out.println("[GameScreen] 경고: TargetingSystem이 null입니다!");
      return;
    }

    // MonsterManager의 몬스터 리스트를 TargetingSystem에 설정
    // 이제 MagicMissile이 가장 가까운 몬스터를 찾을 수 있습니다!
    targetingSystem.setMonsters(monsterManager.getMonsters());

    // PVP 시스템: 플레이어 목록도 주입
    targetingSystem.setPlayers(remotePlayers.values());
    targetingSystem.setMyPlayerId(myPlayer.getPlayerId());

    System.out.println("[GameScreen] MagicMissile 타게팅 시스템 연결 완료");
    System.out.println("[GameScreen] 현재 몬스터 수: " + monsterManager.getMonsterCount());
    System.out.println("[GameScreen] PVP 타게팅 활성화 - 원격 플레이어 수: " + remotePlayers.size());
  }

  /**
   * 테스트용 몬스터 스폰
   */
  private void spawnTestMonsters() {
    System.out.println("[GameScreen] 테스트 몬스터 스폰 시작");

    // 플레이어 주변에 각 타입의 몬스터 스폰
    float playerX = myPlayer.getX();
    float playerY = myPlayer.getY();

    // 고스트 (왼쪽)
    com.example.yugeup.game.monster.Ghost ghost = new com.example.yugeup.game.monster.Ghost();
    ghost.setMonsterId(1001);
    ghost.setPosition(playerX - 150, playerY);
    monsterManager.addMonster(ghost);

    // 박쥐 (위쪽)
    com.example.yugeup.game.monster.Bat bat = new com.example.yugeup.game.monster.Bat();
    bat.setMonsterId(1002);
    bat.setPosition(playerX, playerY + 150);
    monsterManager.addMonster(bat);

    // 골렘 (오른쪽)
    com.example.yugeup.game.monster.Golem golem = new com.example.yugeup.game.monster.Golem();
    golem.setMonsterId(1003);
    golem.setPosition(playerX + 150, playerY);
    monsterManager.addMonster(golem);

    System.out.println("[GameScreen] 테스트 몬스터 " + monsterManager.getMonsterCount() + "마리 스폰 완료");
  }

  @Override
  public void render(float delta) {
    // 배경 클리어 (어두운 회색)
    Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // 원소 선택 전에는 게임 업데이트 안 함
    if (!elementSelected) {
      // 원소 선택 중에도 네트워크 메시지는 처리 (몬스터 동기화)
      if (monsterSpawnEnabled) {
        handleMonsterMessages();
      }
      handlePlayerMoveMessages();

      // UI용 카메라 설정 (화면 고정, 줌 없음)
      camera.position.set(Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2, 0);
      camera.zoom = 1.0f;
      camera.update();
      batch.setProjectionMatrix(camera.combined);
      shapeRenderer.setProjectionMatrix(camera.combined);

      // 오버레이 입력 처리
      elementSelectOverlay.handleInput();

      // 오버레이 렌더링
      elementSelectOverlay.render(batch, shapeRenderer);

      // 카메라 줌 복원
      camera.zoom = 0.3f; // 게임 줌과 동일하게 설정
      return;
    }

    // 플레이어 컨트롤러 업데이트
    playerController.update(delta);

    // 원격 플레이어 업데이트 (보간 이동) - PHASE_23
    for (Player remotePlayer : remotePlayers.values()) {
      remotePlayer.update(delta);
    }

    // 몬스터 AI는 서버에서 관리하므로 클라이언트에서는 비활성화
    // 로컬 AI 대신 서버로부터 받은 위치로 보간 이동만 수행
    // for (com.example.yugeup.game.monster.Monster monster : monsterManager.getMonsters()) {
    //   monster.setAITarget(myPlayer.getX(), myPlayer.getY());
    // }

    // 몬스터 업데이트 (서버 위치로 보간 이동)
    monsterManager.update(delta);

    // 원격 스킬 이펙트 업데이트
    java.util.Iterator<com.example.yugeup.game.effect.RemoteSkillEffect> effectIterator = remoteSkillEffects.iterator();
    while (effectIterator.hasNext()) {
      com.example.yugeup.game.effect.RemoteSkillEffect effect = effectIterator.next();
      effect.update(delta);
      if (!effect.isAlive()) {
        effectIterator.remove();
      }
    }

    // 매직미사일 업데이트 (자동 타게팅 & 발사)
    if (myPlayer.getMagicMissile() != null) {
      // 매 프레임마다 플레이어 목록 갱신 (PVP 타겟팅용)
      com.example.yugeup.game.skill.TargetingSystem ts = myPlayer.getTargetingSystem();
      if (ts != null) {
        ts.setPlayers(remotePlayers.values());
      }
      myPlayer.getMagicMissile().update(delta);
      myPlayer.getMagicMissile().updateProjectiles(delta);
    }

    // 원소 스킬 업데이트 (존 및 발사체)
    if (myPlayer.getElementSkillSet() != null) {
      java.util.List<com.example.yugeup.game.monster.Monster> monsters = monsterManager.getMonsters();

      for (int i = 0; i < 3; i++) {
        com.example.yugeup.game.skill.ElementalSkill skill = myPlayer.getElementSkillSet().getSkill(i);
        if (skill != null) {
          // 발사체에 몬스터 리스트 주입 (update 전에 먼저!)
          if (skill instanceof com.example.yugeup.game.skill.fire.Fireball) {
            for (com.example.yugeup.game.skill.fire.FireballProjectile proj : ((com.example.yugeup.game.skill.fire.Fireball) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
              proj.setPlayerList(new java.util.ArrayList<>(remotePlayers.values()));
              proj.setOwnerPlayerId(myPlayer.getPlayerId());
            }
          } else if (skill instanceof com.example.yugeup.game.skill.water.WaterShot) {
            for (com.example.yugeup.game.skill.water.WaterShotProjectile proj : ((com.example.yugeup.game.skill.water.WaterShot) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
              proj.setPlayerList(new java.util.ArrayList<>(remotePlayers.values()));
              proj.setOwnerPlayerId(myPlayer.getPlayerId());
            }
          } else if (skill instanceof com.example.yugeup.game.skill.water.IceSpike) {
            for (com.example.yugeup.game.skill.water.IceSpikeProjectile proj : ((com.example.yugeup.game.skill.water.IceSpike) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
              proj.setPlayerList(new java.util.ArrayList<>(remotePlayers.values()));
              proj.setOwnerPlayerId(myPlayer.getPlayerId());
            }
          } else if (skill instanceof com.example.yugeup.game.skill.wind.AirSlash) {
            // 스킬 자체에 몬스터 리스트 주입 (근접 공격용)
            ((com.example.yugeup.game.skill.wind.AirSlash) skill).setMonsterList(monsters);
            for (com.example.yugeup.game.skill.wind.AirSlashProjectile proj : ((com.example.yugeup.game.skill.wind.AirSlash) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
              proj.setPlayerList(new java.util.ArrayList<>(remotePlayers.values()));
              proj.setOwnerPlayerId(myPlayer.getPlayerId());
            }
          } else if (skill instanceof com.example.yugeup.game.skill.earth.RockSmash) {
            // 스킬 자체에 몬스터 리스트 주입 (타겟팅용)
            ((com.example.yugeup.game.skill.earth.RockSmash) skill).setMonsterList(monsters);
            for (com.example.yugeup.game.skill.earth.RockSmashZone zone : ((com.example.yugeup.game.skill.earth.RockSmash) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.lightning.LightningBolt) {
            // 스킬 자체에 몬스터 리스트 주입 (타겟팅용)
            ((com.example.yugeup.game.skill.lightning.LightningBolt) skill).setMonsterList(monsters);
            for (com.example.yugeup.game.skill.lightning.LightningBoltZone zone : ((com.example.yugeup.game.skill.lightning.LightningBolt) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.lightning.ChainLightning) {
            // 투사체에 몬스터 리스트 주입
            for (com.example.yugeup.game.skill.lightning.ChainLightningProjectile proj : ((com.example.yugeup.game.skill.lightning.ChainLightning) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
              proj.setPlayerList(new java.util.ArrayList<>(remotePlayers.values()));
              proj.setOwnerPlayerId(myPlayer.getPlayerId());
            }
          }

          // 존/투사체에 몬스터 리스트 주입
          if (skill instanceof com.example.yugeup.game.skill.fire.FlameWave) {
            for (com.example.yugeup.game.skill.fire.FlameWaveProjectile proj : ((com.example.yugeup.game.skill.fire.FlameWave) skill).getActiveProjectiles()) {
              proj.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.fire.Inferno) {
            for (com.example.yugeup.game.skill.fire.InfernoZone zone : ((com.example.yugeup.game.skill.fire.Inferno) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.water.Flood) {
            for (com.example.yugeup.game.skill.water.FloodProjectile projectile : ((com.example.yugeup.game.skill.water.Flood) skill).getActiveProjectiles()) {
              projectile.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.wind.Tornado) {
            for (com.example.yugeup.game.skill.wind.TornadoProjectile projectile : ((com.example.yugeup.game.skill.wind.Tornado) skill).getActiveProjectiles()) {
              projectile.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.wind.Storm) {
            for (com.example.yugeup.game.skill.wind.StormZone zone : ((com.example.yugeup.game.skill.wind.Storm) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.earth.EarthSpike) {
            // 스킬 자체에 몬스터 리스트 주입
            ((com.example.yugeup.game.skill.earth.EarthSpike) skill).setMonsterList(monsters);
            for (com.example.yugeup.game.skill.earth.EarthSpikeZone zone : ((com.example.yugeup.game.skill.earth.EarthSpike) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          } else if (skill instanceof com.example.yugeup.game.skill.lightning.ThunderStorm) {
            // 스킬 자체에 몬스터 리스트 주입
            ((com.example.yugeup.game.skill.lightning.ThunderStorm) skill).setMonsterList(monsters);
            for (com.example.yugeup.game.skill.lightning.ThunderStormZone zone : ((com.example.yugeup.game.skill.lightning.ThunderStorm) skill).getActiveZones()) {
              zone.setMonsterList(monsters);
            }
          }

          // 몬스터 리스트 주입 후 스킬 업데이트
          skill.update(delta);
        }
      }
    }

    // 게임 맵 업데이트 (PHASE_09)
    gameMap.update(delta);

    // 애니메이션 타이머 업데이트
    animationTimer += delta;

    // 몬스터 메시지 처리 (스폰 비활성화 시 무시)
    if (monsterSpawnEnabled) {
      handleMonsterMessages();
    }

    // 발사체 메시지 처리
    handleProjectileMessages();

    // 다른 플레이어의 발사체 업데이트
    updateOtherPlayerProjectiles(delta);

    // 플레이어 동기화 메시지 처리 (PHASE_23)
    handlePlayerMoveMessages();

    // Fog 시스템 메시지 처리 (PHASE_24)
    // 디버그: fog 메시지 처리 전 HP 확인
    int hpBeforeFog = myPlayer.getStats().getCurrentHealth();
    handleFogMessages();
    int hpAfterFog = myPlayer.getStats().getCurrentHealth();
    if (hpBeforeFog != hpAfterFog) {
      System.out.println("[GameScreen] ★★★ fog 처리 후 HP 변화 감지: " + hpBeforeFog + " → " + hpAfterFog);
    }

    // 전투 시스템 메시지 처리 (PHASE_25)
    handleCombatMessages();

    // 게임 시간 업데이트 (PHASE_24)
    gameTime += delta;

    // ===== 렌더링 시작 =====
    // 1. 뷰포트 적용
    viewport.apply();

    // 2. 카메라를 플레이어 위치로 이동 (모든 렌더링의 기준점)
    camera.position.set(myPlayer.getX(), myPlayer.getY(), 0);
    camera.update();

    // 3. projection matrix 설정
    batch.setProjectionMatrix(camera.combined);
    shapeRenderer.setProjectionMatrix(camera.combined);

    // 맵 렌더링 (PHASE_09 - 타일 기반)
    batch.begin();
    gameMap.render(batch, camera);
    batch.end();

    // 몬스터 렌더링 (스폰 비활성화 시에도 기존 몬스터는 표시)
    renderMonsters();

    // 스킬 발사체 렌더링 (몬스터 위에, 플레이어 아래)
    renderOtherPlayerProjectiles();

    // 원격 플레이어 렌더링 (PHASE_23)
    renderRemotePlayers();

    // 로컬 플레이어 렌더링 (맨 위에 그려짐)
    renderPlayer();

    // 스킬 방향 표시기 렌더링 (월드 좌표계)
    if (hudRenderer != null) {
      hudRenderer.renderDirectionIndicator(camera);
    }

    // UI 렌더링 (HUD, 남은 인원, 플레이어 닉네임)
    renderUI();

    // 조이스틱 렌더링 (맨 마지막, 화면 고정)
    renderJoystick();

    // 레벨업 업그레이드 패널 처리 - PHASE_19 (이미지 기반)
    if (levelUpUpgradePanel != null && levelUpUpgradePanel.isVisible()) {
      // 입력 처리
      levelUpUpgradePanel.handleInput();

      // 현재 게임 카메라 상태 저장
      com.badlogic.gdx.math.Vector3 savedPos = new com.badlogic.gdx.math.Vector3(camera.position);
      float savedZoom = camera.zoom;

      // UI 카메라 설정 (화면 고정 좌표계) - 게임 화면 위에 투명 오버레이로 표시
      camera.position.set(Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2, 0);
      camera.zoom = 1.0f;
      camera.update();
      batch.setProjectionMatrix(camera.combined);
      shapeRenderer.setProjectionMatrix(camera.combined);

      // 패널 렌더링
      levelUpUpgradePanel.render(batch, shapeRenderer);

      // 게임 카메라 상태 복원
      camera.position.set(savedPos);
      camera.zoom = savedZoom;
      camera.update();
      batch.setProjectionMatrix(camera.combined);
      shapeRenderer.setProjectionMatrix(camera.combined);
    }

    // UI Stage 렌더링 (레벨업 업그레이드 패널 등) - PHASE_19
    if (stage != null) {
      stage.act(delta);
      stage.draw();
    }

    // 사망 결과 UI 렌더링 (PHASE_26)
    if (showDeathResult) {
      renderDeathResultUI();
    }
  }

  /**
   * 사망 결과 UI를 렌더링합니다. (PHASE_26)
   */
  private void renderDeathResultUI() {
    // UI 카메라 설정 (화면 고정 좌표계)
    com.badlogic.gdx.math.Vector3 savedPos = new com.badlogic.gdx.math.Vector3(camera.position);
    float savedZoom = camera.zoom;

    camera.position.set(Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2, 0);
    camera.zoom = 1.0f;
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    shapeRenderer.setProjectionMatrix(camera.combined);

    // 반투명 검은 배경 렌더링
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(0, 0, 0, 0.7f);  // 투명도 70%
    shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);

    batch.begin();

    if (font != null) {
      batch.setColor(1, 1, 1, 1);

      // 1등 우승 vs 패배 구분
      boolean isWinner = (deathRank == 1 && "우승!".equals(deathKillerName));

      // 결과 텍스트 표시 (1등: 졸업 성공, 2~4등: 유급)
      String resultTitle = isWinner ? "졸업!" : "유급...";
      String rankText = deathRank + "등";
      String messageText = isWinner ? "졸업에 성공하셨어요!" : "이번에는 유급하셨어요 ㅠㅠ";
      String killerText = isWinner ? "" : "처치자: " + deathKillerName;
      String exitText = "화면을 터치하면 메인 메뉴로";

      // 화면 중앙 기준
      float centerX = Constants.SCREEN_WIDTH / 2f;
      float centerY = Constants.SCREEN_HEIGHT / 2f;

      // GlyphLayout으로 중앙 정렬
      com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

      // 타이틀 (졸업! / 유급...)
      font.getData().setScale(2.5f);
      font.setColor(isWinner ? Color.GOLD : Color.RED);
      layout.setText(font, resultTitle);
      font.draw(batch, resultTitle, centerX - layout.width / 2, centerY + 200);

      // 순위
      font.getData().setScale(2.0f);
      font.setColor(Color.WHITE);
      layout.setText(font, rankText);
      font.draw(batch, rankText, centerX - layout.width / 2, centerY + 100);

      // 메시지 (졸업 성공 / 유급 메시지)
      font.getData().setScale(1.5f);
      font.setColor(isWinner ? Color.GREEN : Color.LIGHT_GRAY);
      layout.setText(font, messageText);
      font.draw(batch, messageText, centerX - layout.width / 2, centerY + 20);

      // 킬러 (패배 시에만)
      if (!isWinner && killerText.length() > 0) {
        font.getData().setScale(1.2f);
        font.setColor(Color.GRAY);
        layout.setText(font, killerText);
        font.draw(batch, killerText, centerX - layout.width / 2, centerY - 50);
      }

      // 안내 텍스트
      font.getData().setScale(1.0f);
      font.setColor(Color.GRAY);
      layout.setText(font, exitText);
      font.draw(batch, exitText, centerX - layout.width / 2, centerY - 150);

      // 폰트 상태 복원
      font.getData().setScale(1.0f);
      font.setColor(Color.WHITE);
    }
    batch.end();

    // 게임 카메라 상태 복원
    camera.position.set(savedPos);
    camera.zoom = savedZoom;
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    shapeRenderer.setProjectionMatrix(camera.combined);

    // 터치하면 메인 메뉴로 (방 나가기 메시지 전송)
    if (com.badlogic.gdx.Gdx.input.justTouched()) {
      leaveRoomAndReturnToMenu();
    }
  }

  /**
   * 방 나가기 메시지를 보내고 메인 메뉴로 돌아갑니다.
   */
  private void leaveRoomAndReturnToMenu() {
    // 서버에 방 나가기 메시지 전송
    try {
      org.example.Main.LeaveRoomMsg leaveMsg = new org.example.Main.LeaveRoomMsg();
      NetworkManager.getInstance().sendTCP(leaveMsg);
      System.out.println("[GameScreen] LeaveRoomMsg 전송 완료");
    } catch (Exception e) {
      System.out.println("[GameScreen] LeaveRoomMsg 전송 실패: " + e.getMessage());
    }

    // 메인 메뉴로 전환
    game.setScreen(new MainMenuScreen(game));
  }

  /**
   * 플레이어 이동 메시지를 처리합니다. (PHASE_23)
   */
  private void handlePlayerMoveMessages() {
    MessageHandler handler = MessageHandler.getInstance();
    int myPlayerId = NetworkManager.getInstance().getCurrentPlayerId();

    // 서버로부터 받은 모든 PlayerMoveMsg 처리
    PlayerMoveMsg moveMsg;
    while ((moveMsg = handler.pollPlayerMoveMsg()) != null) {
      // 자기 자신의 메시지는 무시
      if (moveMsg.playerId == myPlayerId) {
        continue;
      }

      // 원격 플레이어 가져오기 또는 생성
      Player remotePlayer = remotePlayers.get(moveMsg.playerId);
      if (remotePlayer == null) {
        remotePlayer = new Player(moveMsg.playerId);
        remotePlayer.setRemote(true); // 원격 플레이어로 설정
        remotePlayer.setPosition(moveMsg.x, moveMsg.y); // 초기 위치 설정
        remotePlayers.put(moveMsg.playerId, remotePlayer);
        // System.out.println("[GameScreen] 원격 플레이어 추가: ID=" + moveMsg.playerId);
      }

      // 목표 위치 업데이트 (보간으로 부드럽게 이동)
      remotePlayer.setTargetPosition(moveMsg.x, moveMsg.y);
    }
  }

  /**
   * 몬스터 관련 메시지를 처리합니다.
   */
  private void handleMonsterMessages() {
    MessageHandler handler = MessageHandler.getInstance();

    // 몬스터 스폰 메시지 처리
    MonsterSpawnMsg spawnMsg;
    int spawnCount = 0;
    while ((spawnMsg = handler.pollMonsterSpawnMsg()) != null) {
      spawnCount++;
      System.out.println("[GameScreen] ★★★ MonsterSpawnMsg 수신! ID=" + spawnMsg.monsterId + ", Type=" + spawnMsg.monsterType + ", Pos=(" + spawnMsg.x + "," + spawnMsg.y + ")");

      // 서버에서 받은 문자열을 MonsterType으로 변환
      com.example.yugeup.game.monster.MonsterType monsterType =
          com.example.yugeup.game.monster.MonsterType.fromString(spawnMsg.monsterType);

      if (monsterType != null) {
        com.example.yugeup.game.monster.Monster monster =
            com.example.yugeup.game.monster.MonsterFactory.createMonster(monsterType);
        monster.setMonsterId(spawnMsg.monsterId);
        monster.setPosition(spawnMsg.x, spawnMsg.y);
        monsterManager.addMonster(monster);
        System.out.println("[GameScreen] 몬스터 스폰 완료: ID=" + spawnMsg.monsterId);
      } else {
        System.out.println("[GameScreen] ❌ MonsterType 변환 실패: " + spawnMsg.monsterType);
      }
    }
    if (spawnCount > 0) {
      System.out.println("[GameScreen] 이번 프레임 총 " + spawnCount + "마리 스폰 처리");
    }

    // 몬스터 업데이트 메시지 처리 (보간 이동)
    MonsterUpdateMsg updateMsg;
    int updateCount = 0;
    while ((updateMsg = handler.pollMonsterUpdateMsg()) != null) {
      updateCount++;
      com.example.yugeup.game.monster.Monster monster = monsterManager.getMonster(updateMsg.monsterId);
      if (monster != null) {
        // 서버 위치를 목표 위치로 설정 (부드러운 보간 이동)
        monster.setTargetPosition(updateMsg.x, updateMsg.y);
        monster.setMaxHealth(updateMsg.maxHp);

        // HP는 최근 데미지 메시지 후 200ms 경과 시에만 업데이트
        // (데미지 메시지와 업데이트 메시지의 순서가 뒤바뀌는 문제 방지)
        Long lastDamage = lastDamageTimestamp.get(updateMsg.monsterId);
        if (lastDamage == null || System.currentTimeMillis() - lastDamage > 200) {
          monster.setCurrentHealth(updateMsg.hp);
        }
        // 200ms 이내면 DamageMsg의 HP를 우선 신뢰
      }
    }
    // 업데이트 로그는 너무 많아서 주석 처리
    // if (updateCount > 0) {
    //   System.out.println("[GameScreen] 몬스터 업데이트 " + updateCount + "개 처리");
    // }

    // 스킬 시전 메시지 처리 (다른 플레이어의 스킬 이펙트)
    SkillCastMsg skillMsg;
    while ((skillMsg = handler.pollSkillCastMsg()) != null) {
      // 자기 자신의 메시지는 무시
      if (skillMsg.playerId == NetworkManager.getInstance().getCurrentPlayerId()) {
        continue;
      }

      // 원격 플레이어 찾기
      Player remotePlayer = remotePlayers.get(skillMsg.playerId);
      if (remotePlayer != null) {
        // 원격 스킬 이펙트 생성 (SkillCastMsg의 상세 정보 포함)
        com.example.yugeup.game.effect.RemoteSkillEffect effect =
          new com.example.yugeup.game.effect.RemoteSkillEffect(
            skillMsg,
            remotePlayer.getX(), remotePlayer.getY()
          );
        remoteSkillEffects.add(effect);
      }
    }

    // 몬스터 사망 메시지 처리
    MonsterDeathMsg deathMsg;
    while ((deathMsg = handler.pollMonsterDeathMsg()) != null) {
      // 경험치 지급: 막타친 플레이어만 경험치 획득
      int myPlayerId = NetworkManager.getInstance().getCurrentPlayerId();
      if (deathMsg.killerId == myPlayerId) {
        com.example.yugeup.game.monster.Monster deadMonster = monsterManager.getMonster(deathMsg.monsterId);
        if (deadMonster != null) {
          int exp = com.example.yugeup.game.level.ExperienceManager.getExpForMonster(deadMonster.getType());
          myPlayer.getLevelSystem().gainExperience(exp);
          System.out.println("[GameScreen] 몬스터 처치! +" + exp + " EXP (ID=" + deathMsg.monsterId + ", 타입: " + deadMonster.getType() + ")");
        }
      }

      monsterManager.removeMonster(deathMsg.monsterId);
      System.out.println("[GameScreen] 몬스터 사망: ID=" + deathMsg.monsterId);
    }

    // 몬스터 데미지 메시지 처리
    MonsterDamageMsg damageMsg;
    while ((damageMsg = handler.pollMonsterDamageMsg()) != null) {
      com.example.yugeup.game.monster.Monster monster = monsterManager.getMonster(damageMsg.monsterId);
      if (monster != null) {
        monster.setCurrentHealth(damageMsg.newHp);
        // 데미지 발생 시각 기록 (HP 동기화 우선순위 처리용)
        lastDamageTimestamp.put(damageMsg.monsterId, System.currentTimeMillis());
      }
    }
  }

  /**
   * Fog 시스템 메시지를 처리합니다. (PHASE_24)
   * 서버에서 받은 fog 활성화 및 데미지 메시지를 처리합니다.
   */
  private void handleFogMessages() {
    MessageHandler handler = MessageHandler.getInstance();

    // Fog 구역 활성화 메시지 처리
    FogZoneMsg fogMsg;
    while ((fogMsg = handler.pollFogZoneMsg()) != null) {
      if (fogMsg.active) {
        // fog 레이어 활성화
        gameMap.activateFogLayer(fogMsg.zoneName);
        System.out.println("[GameScreen] ★ fog 활성화: " + fogMsg.zoneName + " (게임시간: " + (int)fogMsg.gameTime + "초)");
      } else {
        // fog 레이어 비활성화
        gameMap.deactivateFogLayer(fogMsg.zoneName);
      }

      // 서버 게임 시간과 동기화
      this.gameTime = fogMsg.gameTime;
    }

    // Fog 데미지 메시지 처리
    FogDamageMsg damageMsg;
    while ((damageMsg = handler.pollFogDamageMsg()) != null) {
      System.out.println("[GameScreen] ★★★ FogDamageMsg 수신: playerId=" + damageMsg.playerId +
          ", damage=" + damageMsg.damage + ", newHp=" + damageMsg.newHp + ", zone=" + damageMsg.zoneName);

      // 내 플레이어인 경우 HP 업데이트
      if (damageMsg.playerId == myPlayer.getPlayerId()) {
        int oldHp = myPlayer.getStats().getCurrentHealth();
        myPlayer.getStats().setCurrentHealth(damageMsg.newHp);
        int currentHp = myPlayer.getStats().getCurrentHealth();
        System.out.println("[GameScreen] ★ fog 데미지 적용! HP: " + oldHp + " → " + currentHp +
            " (서버값: " + damageMsg.newHp + ", 구역: " + damageMsg.zoneName + ")");
      }
    }
  }

  /**
   * 전투 시스템 메시지를 처리합니다. (PHASE_25)
   * 몬스터 공격, PVP 공격, 플레이어 사망 메시지를 처리합니다.
   */
  private void handleCombatMessages() {
    MessageHandler handler = MessageHandler.getInstance();

    // 몬스터 → 플레이어 공격 메시지 처리
    MonsterAttackPlayerMsg monsterAttackMsg;
    while ((monsterAttackMsg = handler.pollMonsterAttackPlayerMsg()) != null) {
      // 내 플레이어인 경우 HP 업데이트
      if (monsterAttackMsg.playerId == myPlayer.getPlayerId()) {
        myPlayer.getStats().setCurrentHealth(monsterAttackMsg.newHp);
        System.out.println("[GameScreen] 몬스터 공격! 데미지: " + monsterAttackMsg.damage +
            ", HP: " + monsterAttackMsg.newHp + "/" + monsterAttackMsg.maxHp);
      }
    }

    // PVP 공격 메시지 처리
    PlayerAttackPlayerMsg pvpMsg;
    while ((pvpMsg = handler.pollPlayerAttackPlayerMsg()) != null) {
      // 내가 피격당한 경우 HP 업데이트
      if (pvpMsg.targetId == myPlayer.getPlayerId()) {
        myPlayer.getStats().setCurrentHealth(pvpMsg.newHp);
        System.out.println("[GameScreen] PVP 피격! 공격자: " + pvpMsg.attackerId +
            ", 데미지: " + pvpMsg.damage + ", HP: " + pvpMsg.newHp + "/" + pvpMsg.maxHp);
      }
      // 다른 플레이어가 피격당한 경우 그 플레이어의 HP 업데이트
      else {
        Player targetPlayer = remotePlayers.get(pvpMsg.targetId);
        if (targetPlayer != null) {
          targetPlayer.getStats().setCurrentHealth(pvpMsg.newHp);
        }
      }
    }

    // 플레이어 사망 메시지 처리
    PlayerDeathMsg deathMsg;
    while ((deathMsg = handler.pollPlayerDeathMsg()) != null) {
      System.out.println("[GameScreen] 플레이어 사망/우승: " + deathMsg.playerName +
          " (킬러: " + deathMsg.killerName + ", 순위: " + deathMsg.rank + ")");

      // 내가 사망/우승한 경우
      if (deathMsg.playerId == myPlayer.getPlayerId()) {
        // 1등 우승 (killerId == 0)
        if (deathMsg.killerId == 0) {
          showDeathResult = true;
          deathRank = 1;
          deathKillerName = "우승!";
          System.out.println("[GameScreen] ★ 1등 우승!");
        }
        // 일반 사망
        else {
          myPlayer.setDead(true);
          showDeathResult = true;
          deathRank = deathMsg.rank;
          deathKillerName = deathMsg.killerName;
        }
      }
      // 다른 플레이어가 사망한 경우
      else {
        Player deadPlayer = remotePlayers.get(deathMsg.playerId);
        if (deadPlayer != null) {
          deadPlayer.setDead(true);
          // 사망한 플레이어는 목록에서 제거 (남은 인원 카운트용)
          remotePlayers.remove(deathMsg.playerId);
        }
      }
    }
  }

  /**
   * 발사체 메시지를 처리합니다.
   * 다른 플레이어가 발사한 발사체를 화면에 표시합니다.
   */
  private void handleProjectileMessages() {
    MessageHandler handler = MessageHandler.getInstance();

    com.example.yugeup.network.messages.ProjectileFiredMsg msg;
    while ((msg = handler.pollProjectileFiredMsg()) != null) {
      // 자기 자신이 발사한 발사체는 무시 (이미 로컬에서 생성함)
      if (msg.playerId == myPlayer.getPlayerId()) {
        continue;
      }

      com.badlogic.gdx.math.Vector2 startPos = new com.badlogic.gdx.math.Vector2(msg.startX, msg.startY);

      // PVP 발사체 처리 (targetPlayerId >= 0)
      if (msg.targetPlayerId >= 0) {
        // 타겟 플레이어 찾기 (자신 또는 다른 원격 플레이어)
        com.example.yugeup.game.player.Player targetPlayer = null;
        if (msg.targetPlayerId == myPlayer.getPlayerId()) {
          targetPlayer = myPlayer;
        } else {
          targetPlayer = remotePlayers.get(msg.targetPlayerId);
        }

        if (targetPlayer != null) {
          // PVP 발사체 생성 (데미지 0 - 시각 효과용)
          com.example.yugeup.game.skill.PvpProjectile pvpProjectile = new com.example.yugeup.game.skill.PvpProjectile(
              startPos,
              targetPlayer,
              0,  // 데미지 0 (시각 효과용, 실제 데미지는 서버에서 처리)
              Constants.MAGIC_MISSILE_SPEED,
              msg.playerId
          );

          otherPlayerPvpProjectiles.add(pvpProjectile);
          System.out.println("[GameScreen] 다른 플레이어 PVP 발사체 생성: Player=" + msg.playerId + ", TargetPlayer=" + msg.targetPlayerId);
        }
      }
      // 몬스터 타겟 발사체 처리
      else if (msg.targetMonsterId >= 0) {
        com.example.yugeup.game.monster.Monster target = monsterManager.getMonster(msg.targetMonsterId);
        if (target != null) {
          com.example.yugeup.game.skill.Projectile projectile = new com.example.yugeup.game.skill.Projectile(
              startPos,
              target,
              0,  // 데미지 0 (시각 효과용)
              Constants.MAGIC_MISSILE_SPEED,
              startPos
          );

          otherPlayerProjectiles.add(projectile);
          System.out.println("[GameScreen] 다른 플레이어 발사체 생성: Player=" + msg.playerId + ", Target=" + msg.targetMonsterId);
        }
      }
    }
  }

  /**
   * 다른 플레이어의 발사체를 업데이트합니다.
   *
   * @param delta 프레임 시간 (초)
   */
  private void updateOtherPlayerProjectiles(float delta) {
    // 몬스터 타겟 발사체 업데이트
    java.util.Iterator<com.example.yugeup.game.skill.Projectile> iterator = otherPlayerProjectiles.iterator();
    while (iterator.hasNext()) {
      com.example.yugeup.game.skill.Projectile projectile = iterator.next();
      projectile.update(delta);

      if (!projectile.isAlive()) {
        projectile.dispose();
        iterator.remove();
      }
    }

    // PVP 발사체 업데이트
    java.util.Iterator<com.example.yugeup.game.skill.PvpProjectile> pvpIterator = otherPlayerPvpProjectiles.iterator();
    while (pvpIterator.hasNext()) {
      com.example.yugeup.game.skill.PvpProjectile pvpProjectile = pvpIterator.next();
      pvpProjectile.update(delta);

      if (!pvpProjectile.isAlive()) {
        pvpProjectile.dispose();
        pvpIterator.remove();
      }
    }
  }

  /**
   * 플레이어를 렌더링합니다.
   */
  private void renderPlayer() {
    // 카메라는 이미 render()에서 설정됨
    batch.setProjectionMatrix(camera.combined);
    batch.begin();

    // 플레이어 프레임 선택
    TextureRegion currentFrame = getCurrentFrame(myPlayer, animationTimer);

    // 플레이어 렌더링 (크기 축소: 64 → 48)
    float playerWidth = 48;
    float playerHeight = 48;
    batch.draw(currentFrame,
        myPlayer.getX() - playerWidth / 2,
        myPlayer.getY() - playerHeight / 2,
        playerWidth, playerHeight);

    batch.end();
  }

  /**
   * 원격 플레이어를 렌더링합니다. (PHASE_23)
   */
  private void renderRemotePlayers() {
    batch.begin();

    for (Player remotePlayer : remotePlayers.values()) {
      TextureRegion frame = getCurrentFrame(remotePlayer, animationTimer);

      float playerWidth = 48;
      float playerHeight = 48;
      batch.draw(frame,
          remotePlayer.getX() - playerWidth / 2,
          remotePlayer.getY() - playerHeight / 2,
          playerWidth, playerHeight);
    }

    batch.end();
  }

  /**
   * 현재 애니메이션 프레임을 반환합니다.
   */
  private TextureRegion getCurrentFrame(Player player, float stateTime) {
    // 애니메이션 속도 (0.1초마다 프레임 변경)
    int frameIndex = (int) ((stateTime % 0.4f) / 0.1f);

    // 정지 상태면 첫 프레임
    if (player.getVelocity().len() < 0.01f) {
      frameIndex = 0;
    }

    // 방향에 따라 프레임 선택
    switch (player.getDirection()) {
      case FRONT:
        return characterFrontFrames[frameIndex];
      case BACK:
        return characterBackFrames[frameIndex];
      case LEFT:
        return characterLeftFrames[frameIndex];
      case RIGHT:
        return characterRightFrames[frameIndex];
      default:
        return characterFrontFrames[frameIndex];
    }
  }

  /**
   * 몬스터를 렌더링합니다.
   */
  private void renderMonsters() {
    // MonsterManager의 render 메서드 사용 (Monster 클래스가 자체 렌더링 처리)
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    monsterManager.render(batch);
    batch.end();
  }

  /**
   * 몬스터 공격 범위를 와이어프레임으로 표시합니다. (디버그용)
   */
  private void renderMonsterAttackRanges() {
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

    for (com.example.yugeup.game.monster.Monster monster : monsterManager.getMonsters()) {
      if (monster == null || monster.isDead()) continue;

      // 스프라이트 중앙 좌표 계산 (x, y는 좌하단 기준)
      float monsterCenterX = monster.getX() + monster.getWidth() / 2;
      float monsterCenterY = monster.getY() + monster.getHeight() / 2;

      // 몬스터 타입에 따른 공격 범위 (서버와 동일하게)
      float attackRange;
      com.example.yugeup.game.monster.MonsterType type = monster.getType();
      if (type == null) {
        attackRange = 20f;
        shapeRenderer.setColor(Color.WHITE);
      } else {
        switch (type) {
          case GHOST:
            attackRange = 20f;  // 서버 Ghost attackRange
            shapeRenderer.setColor(Color.CYAN);
            break;
          case BAT:
            attackRange = 16f;  // 서버 Bat attackRange
            shapeRenderer.setColor(Color.YELLOW);
            break;
          case GOLEM:
            attackRange = 24f;  // 서버 Golem attackRange
            shapeRenderer.setColor(Color.ORANGE);
            break;
          default:
            attackRange = 20f;
            shapeRenderer.setColor(Color.WHITE);
        }
      }

      // 공격 범위 원 그리기 (스프라이트 중앙 기준)
      shapeRenderer.circle(monsterCenterX, monsterCenterY, attackRange, 32);
    }

    shapeRenderer.end();
  }

  /**
   * 스킬 발사체 타격 범위를 와이어프레임으로 표시합니다. (디버그용)
   */
  private void renderProjectileHitRanges() {
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.GREEN);

    // 충돌 반경 (Projectile.COLLISION_RADIUS = 40f, BaseProjectile.COLLISION_RADIUS = 20f)
    float magicMissileRadius = 40f;
    float elementalSkillRadius = 20f;

    // 매직 미사일 발사체 타격 범위
    if (myPlayer.getMagicMissile() != null) {
      for (com.example.yugeup.game.skill.Projectile projectile : myPlayer.getMagicMissile().getProjectiles()) {
        if (projectile != null && projectile.isAlive()) {
          shapeRenderer.setColor(Color.MAGENTA);
          shapeRenderer.circle(projectile.getPosition().x, projectile.getPosition().y, magicMissileRadius, 16);
        }
      }

      // PVP 발사체 타격 범위 (빨간색으로 표시)
      for (com.example.yugeup.game.skill.PvpProjectile pvpProjectile : myPlayer.getMagicMissile().getPvpProjectiles()) {
        if (pvpProjectile != null && pvpProjectile.isAlive()) {
          shapeRenderer.setColor(Color.RED);
          shapeRenderer.circle(pvpProjectile.getPosition().x, pvpProjectile.getPosition().y, magicMissileRadius, 16);
        }
      }
    }

    // 원소 스킬 발사체 타격 범위
    if (myPlayer.getElementSkillSet() != null) {
      for (int i = 0; i < 3; i++) {
        com.example.yugeup.game.skill.ElementalSkill skill = myPlayer.getElementSkillSet().getSkill(i);
        if (skill == null) continue;

        // 각 스킬 타입별 발사체 처리
        java.util.List<? extends com.example.yugeup.game.skill.BaseProjectile> projectiles = null;

        if (skill instanceof com.example.yugeup.game.skill.fire.Fireball) {
          projectiles = ((com.example.yugeup.game.skill.fire.Fireball) skill).getActiveProjectiles();
          shapeRenderer.setColor(Color.RED);
        } else if (skill instanceof com.example.yugeup.game.skill.water.WaterShot) {
          projectiles = ((com.example.yugeup.game.skill.water.WaterShot) skill).getActiveProjectiles();
          shapeRenderer.setColor(Color.BLUE);
        } else if (skill instanceof com.example.yugeup.game.skill.water.IceSpike) {
          projectiles = ((com.example.yugeup.game.skill.water.IceSpike) skill).getActiveProjectiles();
          shapeRenderer.setColor(Color.CYAN);
        } else if (skill instanceof com.example.yugeup.game.skill.wind.AirSlash) {
          projectiles = ((com.example.yugeup.game.skill.wind.AirSlash) skill).getActiveProjectiles();
          shapeRenderer.setColor(Color.WHITE);
        }
        // LightningBolt, ChainLightning은 Zone 방식이므로 Projectile 디버그 렌더링에서 제외

        if (projectiles != null) {
          for (com.example.yugeup.game.skill.BaseProjectile proj : projectiles) {
            if (proj != null && proj.isAlive()) {
              shapeRenderer.circle(proj.getPosition().x, proj.getPosition().y, elementalSkillRadius, 16);
            }
          }
        }
      }
    }

    shapeRenderer.end();
  }

  /**
   * 다른 플레이어의 발사체를 렌더링합니다.
   */
  private void renderOtherPlayerProjectiles() {
    batch.setProjectionMatrix(camera.combined);
    batch.begin();

    // 다른 플레이어의 발사체 렌더링 (몬스터 타겟)
    for (com.example.yugeup.game.skill.Projectile projectile : otherPlayerProjectiles) {
      if (projectile != null && projectile.isAlive()) {
        projectile.render(batch);
      }
    }

    // 다른 플레이어의 PVP 발사체 렌더링
    for (com.example.yugeup.game.skill.PvpProjectile pvpProjectile : otherPlayerPvpProjectiles) {
      if (pvpProjectile != null && pvpProjectile.isAlive()) {
        pvpProjectile.render(batch);
      }
    }

    // 내 매직 미사일 발사체 렌더링
    if (myPlayer.getMagicMissile() != null) {
      for (com.example.yugeup.game.skill.Projectile projectile : myPlayer.getMagicMissile().getProjectiles()) {
        if (projectile != null && projectile.isAlive()) {
          projectile.render(batch);
        }
      }

      // PVP 발사체 렌더링 (플레이어 타겟팅용)
      for (com.example.yugeup.game.skill.PvpProjectile pvpProjectile : myPlayer.getMagicMissile().getPvpProjectiles()) {
        if (pvpProjectile != null && pvpProjectile.isAlive()) {
          pvpProjectile.render(batch);
        }
      }
    }

    // 원소 스킬 발사체 렌더링 (원소 선택된 경우만)
    if (myPlayer.getElementSkillSet() != null) {
      // 스킬 A, B, C 순회
      for (int i = 0; i < 3; i++) {
        com.example.yugeup.game.skill.ElementalSkill skill = myPlayer.getElementSkillSet().getSkill(i);
        if (skill == null)
          continue;

        // 발사체를 가진 스킬인 경우 렌더링
        if (skill instanceof com.example.yugeup.game.skill.fire.Fireball) {
          com.example.yugeup.game.skill.fire.Fireball fireball = (com.example.yugeup.game.skill.fire.Fireball) skill;
          for (com.example.yugeup.game.skill.fire.FireballProjectile proj : fireball.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.water.WaterShot) {
          com.example.yugeup.game.skill.water.WaterShot waterShot = (com.example.yugeup.game.skill.water.WaterShot) skill;
          for (com.example.yugeup.game.skill.water.WaterShotProjectile proj : waterShot.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.water.IceSpike) {
          com.example.yugeup.game.skill.water.IceSpike iceSpike = (com.example.yugeup.game.skill.water.IceSpike) skill;
          for (com.example.yugeup.game.skill.water.IceSpikeProjectile proj : iceSpike.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.wind.AirSlash) {
          com.example.yugeup.game.skill.wind.AirSlash airSlash = (com.example.yugeup.game.skill.wind.AirSlash) skill;
          for (com.example.yugeup.game.skill.wind.AirSlashProjectile proj : airSlash.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.earth.RockSmash) {
          com.example.yugeup.game.skill.earth.RockSmash rockSmash = (com.example.yugeup.game.skill.earth.RockSmash) skill;
          for (com.example.yugeup.game.skill.earth.RockSmashZone zone : rockSmash.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.lightning.LightningBolt) {
          com.example.yugeup.game.skill.lightning.LightningBolt lightningBolt = (com.example.yugeup.game.skill.lightning.LightningBolt) skill;
          for (com.example.yugeup.game.skill.lightning.LightningBoltZone zone : lightningBolt.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.lightning.ChainLightning) {
          com.example.yugeup.game.skill.lightning.ChainLightning chainLightning = (com.example.yugeup.game.skill.lightning.ChainLightning) skill;
          for (com.example.yugeup.game.skill.lightning.ChainLightningProjectile proj : chainLightning.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        }

        // 존/투사체 스킬 렌더링
        else if (skill instanceof com.example.yugeup.game.skill.fire.FlameWave) {
          com.example.yugeup.game.skill.fire.FlameWave flameWave = (com.example.yugeup.game.skill.fire.FlameWave) skill;
          for (com.example.yugeup.game.skill.fire.FlameWaveProjectile proj : flameWave.getActiveProjectiles()) {
            if (proj != null && proj.isAlive()) {
              proj.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.fire.Inferno) {
          com.example.yugeup.game.skill.fire.Inferno inferno = (com.example.yugeup.game.skill.fire.Inferno) skill;
          for (com.example.yugeup.game.skill.fire.InfernoZone zone : inferno.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.water.Flood) {
          com.example.yugeup.game.skill.water.Flood flood = (com.example.yugeup.game.skill.water.Flood) skill;
          for (com.example.yugeup.game.skill.water.FloodProjectile projectile : flood.getActiveProjectiles()) {
            if (projectile != null && projectile.isAlive()) {
              projectile.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.wind.Tornado) {
          com.example.yugeup.game.skill.wind.Tornado tornado = (com.example.yugeup.game.skill.wind.Tornado) skill;
          for (com.example.yugeup.game.skill.wind.TornadoProjectile projectile : tornado.getActiveProjectiles()) {
            if (projectile != null && projectile.isAlive()) {
              projectile.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.wind.Storm) {
          com.example.yugeup.game.skill.wind.Storm storm = (com.example.yugeup.game.skill.wind.Storm) skill;
          for (com.example.yugeup.game.skill.wind.StormZone zone : storm.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.earth.EarthSpike) {
          com.example.yugeup.game.skill.earth.EarthSpike earthSpike = (com.example.yugeup.game.skill.earth.EarthSpike) skill;
          for (com.example.yugeup.game.skill.earth.EarthSpikeZone zone : earthSpike.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.lightning.ThunderStorm) {
          com.example.yugeup.game.skill.lightning.ThunderStorm thunderStorm = (com.example.yugeup.game.skill.lightning.ThunderStorm) skill;
          for (com.example.yugeup.game.skill.lightning.ThunderStormZone zone : thunderStorm.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        } else if (skill instanceof com.example.yugeup.game.skill.earth.StoneShield) {
          com.example.yugeup.game.skill.earth.StoneShield stoneShield = (com.example.yugeup.game.skill.earth.StoneShield) skill;
          for (com.example.yugeup.game.skill.earth.StoneShieldZone zone : stoneShield.getActiveZones()) {
            if (zone != null && zone.isActive()) {
              zone.render(batch);
            }
          }
        }
      }
    }

    // 원격 스킬 이펙트 렌더링
    for (com.example.yugeup.game.effect.RemoteSkillEffect effect : remoteSkillEffects) {
      effect.render(batch);
    }

    batch.end();
  }

  /**
   * 조이스틱을 렌더링합니다.
   */
  private void renderJoystick() {
    // 조이스틱 위치 업데이트 (플레이어 따라감, 카메라 줌 고려)
    playerController.getJoystickController().updatePosition(myPlayer.getX(), myPlayer.getY(), camera);

    // 조이스틱은 월드 좌표 사용
    batch.setProjectionMatrix(camera.combined);
    shapeRenderer.setProjectionMatrix(camera.combined);

    // 그라데이션 투명도 효과를 위해 blend mode 활성화
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    playerController.getJoystickController().render(shapeRenderer);
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  /**
   * UI를 렌더링합니다.
   */
  private void renderUI() {
    // HUD 렌더링 (HP/MP/EXP 바) - PHASE_11
    // HUD는 자체 카메라를 사용하여 화면에 고정됨
    hudRenderer.render(batch, camera);

    // 화면 고정 UI (남은 인원, 플레이어 닉네임) - HUD 카메라 사용
    OrthographicCamera hudCam = new OrthographicCamera();
    hudCam.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    hudCam.update();

    batch.setProjectionMatrix(hudCam.combined);

    // 1. ShapeRenderer로 반투명 검은색 배경을 먼저 그립니다.
    Gdx.gl.glEnable(GL20.GL_BLEND); // 반투명 효과를 위해 블렌딩 활성화
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(hudCam.combined); // HUD 카메라 사용
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(0, 0, 0, 0.4f); // 반투명 검은색 (RGBA)

    // 배경 사각형 그리기 (텍스트보다 약간 크게)
    float bgWidth = 320f;
    float bgHeight = 60f;
    float bgX = Constants.SCREEN_WIDTH - bgWidth - 50f;
    float bgY = Constants.SCREEN_HEIGHT - bgHeight - 70f;
    shapeRenderer.rect(bgX, bgY, bgWidth, bgHeight);

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND); // 블렌딩 비활성화

    batch.begin();

    // 우상단: 남은 인원
    int alivePlayers = 1 + remotePlayers.size(); // 자신 + 원격 플레이어
    font.setColor(Color.WHITE);
    font.getData().setScale(1.0f);
    String survivorText = "남은 인원: " + alivePlayers + "명";

    // GlyphLayout으로 텍스트 중앙 정렬 (배경 기준)
    com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, survivorText);
    float textX = bgX + (bgWidth - layout.width) / 2f;
    float textY = bgY + (bgHeight + layout.height) / 2f;

    font.draw(batch, survivorText, textX, textY);

    batch.end();

    // 플레이어 닉네임 렌더링 (월드 좌표)
    renderPlayerNames();
  }

  /**
   * 플레이어 닉네임을 렌더링합니다.
   */
  private void renderPlayerNames() {
    batch.setProjectionMatrix(camera.combined);
    batch.begin();

    // 로컬 플레이어 닉네임
    font.setColor(Color.BLACK);
    font.getData().setScale(0.25f);
    String myName = playerNames.getOrDefault(myPlayer.getPlayerId(), "Player" + myPlayer.getPlayerId());

      // 텍스트 중앙 정렬
    com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, myName);
    float textX = myPlayer.getX() - layout.width / 2;
    float textY = myPlayer.getY() + 40; // 캐릭터 머리 위 (60 → 40, 더 가까이)
    font.draw(batch, myName, textX, textY);

    // 원격 플레이어 닉네임
    font.setColor(Color.BLACK);  // 검은색으로 통일
    for (Player remotePlayer : remotePlayers.values()) {
      String remoteName = playerNames.getOrDefault(remotePlayer.getPlayerId(), "Player" + remotePlayer.getPlayerId());
      layout.setText(font, remoteName);
      textX = remotePlayer.getX() - layout.width / 2;
      textY = remotePlayer.getY() + 40;
      font.draw(batch, remoteName, textX, textY);
    }

    batch.end();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void hide() {
  }

  @Override
  public void dispose() {
    if (batch != null)
      batch.dispose();
    if (shapeRenderer != null)
      shapeRenderer.dispose();
    if (gameMap != null)
      gameMap.dispose();
    if (hudRenderer != null)
      hudRenderer.dispose();
    // UI 시스템 정리 (PHASE_19)
    if (levelUpUpgradePanel != null)
      levelUpUpgradePanel.dispose();
    if (stage != null)
      stage.dispose();
    if (skin != null)
      skin.dispose();
  }
}
