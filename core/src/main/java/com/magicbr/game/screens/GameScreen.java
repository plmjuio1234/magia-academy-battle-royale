package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.entities.Player;
import com.magicbr.game.ui.VirtualJoystick;
import com.magicbr.game.utils.Client;
import com.magicbr.game.utils.Constants;
import com.magicbr.game.utils.FontManager;
import com.magicbr.game.systems.skills.SkillManager;
import com.magicbr.game.systems.skills.ProjectilePool;
import com.magicbr.game.systems.skills.Projectile;
import com.magicbr.game.systems.skills.Skill;

import java.util.HashMap;
import java.util.Map;

public class GameScreen extends BaseScreen {
    private Constants.CharacterClass playerClass;
    private Table hudTable;
    private Label levelLabel;
    private Label elementLabel;
    private Label hpLabel;
    private Label mpLabel;
    private Label timerLabel;
    private float gameTimer = 0f;

    private Player player;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapeRenderer;
    private VirtualJoystick virtualJoystick;

    // 스킬 시스템
    private ProjectilePool projectilePool;
    private SkillManager skillManager;
    private Table skillButtonTable;  // 스킬 버튼 테이블
    private TextButton.TextButtonStyle buttonStyle;  // 스킬 버튼 스타일 (원소 변경 시 재사용)

    private Client client;
    private float syncTimer = 0;
    private Map<Integer, OtherPlayer> otherPlayers = new HashMap<>();

    static class OtherPlayer {
        int id;
        float x, y;                          // 현재 보간된 위치
        float targetX, targetY;              // 목표 위치 (서버에서 받은 위치)
        private static final float LERP_SPEED = 15f;  // 보간 속도

        OtherPlayer(int id, float x, float y) {
            this.id = id;
            this.x = this.targetX = x;
            this.y = this.targetY = y;
        }

        // 매 프레임마다 호출되어 현재 위치를 목표 위치로 부드럽게 이동
        void update(float delta) {
            // 선형 보간: 현재 위치 + (목표 - 현재) * 속도
            float lerpFactor = delta * LERP_SPEED;
            x = lerp(x, targetX, lerpFactor);
            y = lerp(y, targetY, lerpFactor);
        }

        // 서버로부터 받은 새로운 위치를 목표로 설정
        void setTargetPosition(float newX, float newY) {
            this.targetX = newX;
            this.targetY = newY;
        }

        private float lerp(float start, float end, float alpha) {
            return start + (end - start) * Math.min(alpha, 1f);
        }

        void render(ShapeRenderer shapeRenderer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.circle(x, y, 30);

            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.circle(x, y, 20);
        }
    }

    public GameScreen(MagicBattleRoyale game, Constants.CharacterClass playerClass) {
        super(game);
        this.playerClass = playerClass;
        this.client = game.getClient();

        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);

        player = new Player(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();

        Label.LabelStyle labelStyle = new Label.LabelStyle(koreanFont, Color.WHITE);
        this.buttonStyle = new TextButton.TextButtonStyle();
        this.buttonStyle.font = koreanFont;
        this.buttonStyle.fontColor = Color.WHITE;
        this.buttonStyle.overFontColor = Color.YELLOW;
        this.buttonStyle.downFontColor = Color.GRAY;

        levelLabel = new Label("레벨: " + player.getLevel(), labelStyle);
        elementLabel = new Label("원소: " + player.getSelectedElement(), labelStyle);
        hpLabel = new Label("HP: " + player.getHp() + "/" + player.getMaxHp(), labelStyle);
        mpLabel = new Label("MP: " + player.getMp() + "/" + player.getMaxMp(), labelStyle);
        timerLabel = new Label("시간: 0:00", labelStyle);

        TextButton exitButton = new TextButton("메뉴로 돌아가기", buttonStyle);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        hudTable.add(levelLabel).pad(10).row();
        hudTable.add(elementLabel).pad(10).row();
        hudTable.add(hpLabel).pad(10).row();
        hudTable.add(mpLabel).pad(10).row();
        hudTable.add(timerLabel).pad(10).row();
        hudTable.add(exitButton).pad(20);

        game.getUiStage().addActor(hudTable);

        // 스킬 시스템 초기화
        projectilePool = new ProjectilePool();
        skillManager = new SkillManager(player, projectilePool);

        // 스킬 버튼 UI 생성
        createSkillButtons(buttonStyle);

        virtualJoystick = new VirtualJoystick(Constants.JOYSTICK_SIZE);
        virtualJoystick.setPosition(Constants.HUD_PADDING, Constants.HUD_PADDING);
        game.getUiStage().addActor(virtualJoystick);
    }

    /**
     * 스킬 버튼 4개 생성 - 오른쪽 위 4분할 중앙에 배치
     */
    private void createSkillButtons(TextButton.TextButtonStyle buttonStyle) {
        skillButtonTable = new Table();
        skillButtonTable.setFillParent(false);  // 전체 화면 채우기 해제

        // 오른쪽 아래 4분할의 중앙 위치
        // 화면 4분할: 좌우 0.5, 상하 0.5
        // 오른쪽 아래 = x: 0.75 중앙, y: 0.25 중앙
        // Scene2D는 좌측 하단이 원점이므로 y = 0.25 * VIRTUAL_HEIGHT
        float skillButtonX = Constants.VIRTUAL_WIDTH * 0.75f;      // 1440 (우측)
        float skillButtonY = Constants.VIRTUAL_HEIGHT * 0.25f;     // 270 (하단)

        // 플레이어 레벨에 따른 스킬만 표시
        Skill[] skills = skillManager.getSkillsForLevel(player.getLevel());

        // 2x2 그리드 레이아웃
        int buttonCount = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (buttonCount >= 4 || buttonCount >= skills.length) break;

                final int skillIndex = buttonCount;
                Skill skill = skills[skillIndex];

                // 스킬 버튼 생성
                TextButton skillButton = new TextButton(
                    (skillIndex + 1) + ": " + skill.getSkillName() + "\n" + skill.getManaCost() + "MP",
                    buttonStyle
                );

                skillButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // 마우스 위치를 게임 월드 좌표로 변환
                        com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(
                            Gdx.input.getX(), Gdx.input.getY(), 0
                        );
                        gameCamera.unproject(mousePos);

                        // 로컬 스킬 시전
                        boolean success = skillManager.castSkill(skillIndex, mousePos.x, mousePos.y);

                        // 성공 시 서버에 알림 (다른 플레이어들에게 동기화)
                        if (success) {
                            Skill skill = skillManager.getSkill(skillIndex);
                            if (skill != null) {
                                client.sendSkillCast(player.getId(), skill, mousePos.x, mousePos.y);
                            }
                        }
                    }
                });

                skillButtonTable.add(skillButton).width(100).height(60).pad(5);
                buttonCount++;
            }
            skillButtonTable.row();  // 다음 줄로
        }

        // 스킬 버튼 테이블 위치 설정
        skillButtonTable.setPosition(skillButtonX - 110, skillButtonY);  // 중앙 정렬
        game.getUiStage().addActor(skillButtonTable);
    }

    @Override
    public void render(float delta) {
        gameTimer += delta;

        handleInput(delta);

        player.update(delta);

        // 스킬 시스템 업데이트
        skillManager.update(delta);
        projectilePool.updateAll(delta);

        syncPlayerPosition(delta);

        updateOtherPlayers();

        gameCamera.position.x = player.getPosition().x;
        gameCamera.position.y = player.getPosition().y;
        gameCamera.update();

        ScreenUtils.clear(0.1f, 0.3f, 0.1f, 1f);

        renderGameWorld();

        renderVirtualJoystick();

        updateHUD();

        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }

    private void syncPlayerPosition(float delta) {
        syncTimer += delta;
        if (syncTimer >= 0.05f) {
            Vector2 pos = player.getPosition();
            client.sendPlayerMove(pos.x, pos.y);
            syncTimer = 0;
        }
    }

    private void updateOtherPlayers() {
        // 큐에 있는 모든 PlayerMoveMsg 처리
        java.util.Queue<Client.PlayerMoveMsg> messages = client.getPlayerMoveMessages();
        while (!messages.isEmpty()) {
            Client.PlayerMoveMsg moveMsg = messages.poll();
            OtherPlayer other = otherPlayers.get(moveMsg.playerId);
            if (other == null) {
                other = new OtherPlayer(moveMsg.playerId, moveMsg.x, moveMsg.y);
                otherPlayers.put(moveMsg.playerId, other);
                Gdx.app.log("GameScreen", "새 플레이어 추가: " + moveMsg.playerId);
            } else {
                // 직접 할당하지 말고 목표 위치만 설정 (보간이 점진적으로 처리함)
                other.setTargetPosition(moveMsg.x, moveMsg.y);
            }
        }

        // 모든 다른 플레이어의 보간 업데이트
        for (OtherPlayer other : otherPlayers.values()) {
            other.update(Gdx.graphics.getDeltaTime());
        }

        // 다른 플레이어 스킬 처리
        updateOtherPlayerSkills();
    }

    /**
     * 다른 플레이어들의 스킬 투사체 처리
     */
    private void updateOtherPlayerSkills() {
        // Client에서 SkillCastMsg 수신
        java.util.Queue<Client.SkillCastMsg> skillMessages = client.getSkillCastMessages();
        while (!skillMessages.isEmpty()) {
            Client.SkillCastMsg skillMsg = skillMessages.poll();

            // 자신의 스킬은 무시 (이미 로컬에서 처리됨)
            if (skillMsg.playerId == player.getId()) {
                continue;
            }

            // 다른 플레이어 위치에서 투사체 생성
            OtherPlayer caster = otherPlayers.get(skillMsg.playerId);
            if (caster != null) {
                // AoE 스킬 판별: projectileSpeed가 0이면 AoE 스킬 (화염 폭발 등)
                boolean isAoESkill = skillMsg.projectileSpeed == 0;

                // 투사체 생성 위치 결정
                float startX, startY;
                float vx = 0, vy = 0;

                if (isAoESkill) {
                    // AoE 스킬: 목표 위치에서 생성 (정지 상태)
                    startX = skillMsg.targetX;
                    startY = skillMsg.targetY;
                    // 속도는 0 (이미 설정됨)
                } else {
                    // 투사체 스킬: 플레이어 위치에서 목표로 이동
                    startX = caster.x;
                    startY = caster.y;

                    // 목표 방향 계산
                    float dirX = skillMsg.targetX - startX;
                    float dirY = skillMsg.targetY - startY;
                    float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);

                    // 방향 정규화 및 속도 설정
                    if (distance > 0) {
                        dirX /= distance;
                        dirY /= distance;
                        vx = dirX * skillMsg.projectileSpeed;
                        vy = dirY * skillMsg.projectileSpeed;
                    }
                }

                // 투사체 생성 - 네트워크에서 받은 모든 정보 사용
                Projectile projectile = projectilePool.obtain(
                    startX, startY,
                    vx, vy,
                    skillMsg.baseDamage,            // 네트워크 값 사용 ✅
                    skillMsg.playerId,
                    skillMsg.skillName,             // 네트워크 값 사용 ✅
                    skillMsg.elementColor,          // 네트워크 값 사용 ✅
                    skillMsg.projectileLifetime     // 네트워크 값 사용 ✅
                );
                projectile.setRadius(skillMsg.projectileRadius);  // 네트워크 값 사용 ✅

                Gdx.app.log("GameScreen", "플레이어 스킬 수신: " + skillMsg.skillName +
                    " (색상: " + skillMsg.elementColor + ", 속도: " + skillMsg.projectileSpeed +
                    ", 위치: " + (isAoESkill ? "목표" : "플레이어") + ")");
            }
        }
    }

    private void handleInput(float delta) {
        float moveX = 0, moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = 1;
        }

        if (virtualJoystick.isTouched()) {
            moveX = virtualJoystick.getKnobPercentX();
            moveY = virtualJoystick.getKnobPercentY();
        }

        if (!virtualJoystick.isTouched() && moveX != 0 && moveY != 0) {
            moveX *= 0.707f;
            moveY *= 0.707f;
        }

        player.move(moveX, moveY);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            changeElement("불");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            changeElement("물");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            changeElement("땅");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            changeElement("전기");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            changeElement("바람");
        }

        // 디버그: K 키 = 레벨 업
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            int currentLevel = player.getLevel();
            if (currentLevel < Constants.PLAYER_MAX_LEVEL) {
                player.addExperience(currentLevel * 100);  // 현재 레벨의 필요 경험치
                System.out.println("[디버그] 레벨업: " + (currentLevel) + " → " + player.getLevel());

                // 스킬 버튼 재생성 (새로운 스킬 활성화)
                skillButtonTable.clearChildren();
                createSkillButtons(buttonStyle);
            }
        }

        // 디버그: L 키 = 레벨 다운
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            int currentLevel = player.getLevel();
            if (currentLevel > 1) {
                // Player에 setLevel() 메서드 추가 필요
                try {
                    java.lang.reflect.Field levelField = Player.class.getDeclaredField("level");
                    levelField.setAccessible(true);
                    levelField.setInt(player, currentLevel - 1);
                    System.out.println("[디버그] 레벨다운: " + currentLevel + " → " + (currentLevel - 1));

                    // 스킬 버튼 재생성
                    skillButtonTable.clearChildren();
                    createSkillButtons(buttonStyle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 원소 변경 및 스킬 업데이트
     */
    private void changeElement(String element) {
        player.selectElement(element);
        skillManager.setElementSkills(element);

        // 스킬 버튼 UI 재생성
        skillButtonTable.clearChildren();
        createSkillButtons(buttonStyle);

        System.out.println("[게임] 원소 변경: " + element);
    }

    private void renderGameWorld() {
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawMapBounds();

        player.render(shapeRenderer);

        for (OtherPlayer other : otherPlayers.values()) {
            other.render(shapeRenderer);
        }

        // 투사체 렌더링
        projectilePool.renderAll(shapeRenderer);

        shapeRenderer.end();
    }

    private void drawMapBounds() {
        shapeRenderer.setColor(Color.DARK_GRAY);

        float borderThickness = 50f;

        shapeRenderer.rect(0, Constants.MAP_HEIGHT - borderThickness, Constants.MAP_WIDTH, borderThickness);
        shapeRenderer.rect(0, 0, Constants.MAP_WIDTH, borderThickness);
        shapeRenderer.rect(0, 0, borderThickness, Constants.MAP_HEIGHT);
        shapeRenderer.rect(Constants.MAP_WIDTH - borderThickness, 0, borderThickness, Constants.MAP_HEIGHT);

        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(borderThickness, borderThickness,
            Constants.MAP_WIDTH - 2 * borderThickness,
            Constants.MAP_HEIGHT - 2 * borderThickness);
    }

    private void renderVirtualJoystick() {
        shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        virtualJoystick.render(shapeRenderer);
        shapeRenderer.end();
    }

    private void updateHUD() {
        levelLabel.setText("레벨: " + player.getLevel());
        elementLabel.setText("원소: " + player.getSelectedElement() + " (1-5로 변경)");
        hpLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        mpLabel.setText("MP: " + player.getMp() + "/" + player.getMaxMp());

        // 게임 타이머 (MM:SS 형식)
        int minutes = (int) (gameTimer / 60);
        int seconds = (int) (gameTimer % 60);
        timerLabel.setText(String.format("시간: %d:%02d", minutes, seconds));
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (virtualJoystick != null) {
            virtualJoystick.dispose();
        }
        super.dispose();
    }
}
