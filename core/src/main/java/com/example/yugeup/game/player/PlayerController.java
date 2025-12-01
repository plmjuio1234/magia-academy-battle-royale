package com.example.yugeup.game.player;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.game.map.GameMap;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.input.JoystickController;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.ui.hud.HUDRenderer;
import com.example.yugeup.utils.Constants;
import org.example.Main.PlayerMoveMsg;

/**
 * 플레이어 입력 처리 클래스
 *
 * 조이스틱, 터치 입력 등을 받아 플레이어를 제어합니다.
 * 입력 데이터를 서버로 전송하여 동기화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PlayerController implements InputProcessor {

    // 제어할 플레이어
    private Player player;

    // 조이스틱 컨트롤러
    private JoystickController joystickController;

    // 뷰포트 (좌표 변환용)
    private Viewport viewport;

    // 네트워크 매니저
    private NetworkManager networkManager;

    // HUD 렌더러 (스킬 버튼 입력 처리용)
    private HUDRenderer hudRenderer;

    // 게임 맵 (벽 충돌 체크용)
    private GameMap gameMap;

    // 동기화 타이머
    private float syncTimer;

    // 스킬 방향 선택 모드
    private boolean isSelectingSkillDirection;
    private ElementalSkill pendingSkill;  // 방향 선택 대기 중인 스킬

    /**
     * PlayerController 생성자
     *
     * @param player 제어할 플레이어
     * @param viewport 게임 뷰포트
     */
    public PlayerController(Player player, Viewport viewport) {
        this.player = player;
        this.viewport = viewport;
        this.joystickController = new JoystickController(viewport);
        this.networkManager = NetworkManager.getInstance();
        this.hudRenderer = null;  // GameScreen에서 설정됨
        this.gameMap = null;  // GameScreen에서 설정됨
        this.syncTimer = 0f;
        this.isSelectingSkillDirection = false;
        this.pendingSkill = null;
    }

    /**
     * 입력을 처리하고 플레이어를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // 조이스틱 입력으로 플레이어 방향 결정
        Vector2 direction = joystickController.getDirection();

        // 플레이어 이동
        if (direction.len() > 0) {
            // 이동 속도 = 기본 속도 * 버프 배율 (폭풍 스킬 등)
            float speed = Constants.PLAYER_MOVE_SPEED * player.getSpeedMultiplier();
            player.setVelocity(direction.x * speed, direction.y * speed);
        } else {
            // 입력 없음
            player.setVelocity(0, 0);
        }

        // 벽 충돌 체크 후 플레이어 업데이트
        updatePlayerWithCollision(delta);

        // 위치 동기화 (주기적)
        syncTimer += delta;
        if (syncTimer >= Constants.PLAYER_SYNC_INTERVAL) {
            sendPlayerMove();
            syncTimer = 0f;
        }
    }

    /**
     * 벽 충돌을 체크하면서 플레이어 위치를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    private void updatePlayerWithCollision(float delta) {
        // 이동 전 위치 저장
        float oldX = player.getX();
        float oldY = player.getY();

        // 플레이어 업데이트 (위치 이동)
        player.update(delta);

        // 벽 충돌 체크 (GameMap이 설정된 경우만)
        if (gameMap == null) {
            System.out.println("[PlayerController] 경고: gameMap이 null입니다! 벽 충돌 체크 불가능");
            return;
        }

        float newX = player.getX();
        float newY = player.getY();
        float radius = 12f;  // 플레이어 충돌 반경 (캐릭터 48px의 절반보다 작게 - 좁은 문 통과 가능)

        // 새 위치에 벽이 있는지 확인
        if (gameMap.isWallInArea(newX, newY, radius)) {
            // X축만 이동 시도
            player.setPosition(newX, oldY);
            if (gameMap.isWallInArea(player.getX(), player.getY(), radius)) {
                // Y축만 이동 시도
                player.setPosition(oldX, newY);
                if (gameMap.isWallInArea(player.getX(), player.getY(), radius)) {
                    // 둘 다 안되면 원위치
                    player.setPosition(oldX, oldY);
                }
            }
        }
    }

    /**
     * 서버로 플레이어 위치 전송
     * 정지 상태여도 전송하여 다른 플레이어가 정확한 위치를 알 수 있도록 함
     */
    private void sendPlayerMove() {
        PlayerMoveMsg msg = new PlayerMoveMsg();
        msg.playerId = player.getPlayerId();
        msg.x = player.getX();
        msg.y = player.getY();

        networkManager.sendUDP(msg);
    }

    // ===== InputProcessor 구현 =====

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println("[PlayerController] touchDown: (" + screenX + ", " + screenY + ")");

        // 스킬 방향 선택 모드가 아닐 때만 스킬 버튼 처리
        if (!isSelectingSkillDirection) {
            // 스킬 버튼 입력 처리 (HUD가 설정되어 있으면)
            if (hudRenderer != null) {
                System.out.println("[PlayerController] HUD 렌더러 존재");
                ElementalSkill skill = hudRenderer.handleSkillButtonTouchDown(screenX, screenY);
                System.out.println("[PlayerController] 반환된 스킬: " + (skill != null ? skill.getName() : "null"));

                if (skill != null) {
                    System.out.println("[PlayerController] 스킬 준비상태: " + skill.isReady());
                    // 스킬이 준비 상태라면
                    if (skill.isReady()) {
                        // 방향이 필요한 스킬인지 확인
                        if (needsDirection(skill)) {
                            // 방향 선택 모드로 진입
                            isSelectingSkillDirection = true;
                            pendingSkill = skill;
                            if (hudRenderer.getDirectionIndicator() != null) {
                                hudRenderer.getDirectionIndicator().activate(player.getX(), player.getY());
                            }
                            System.out.println("[PlayerController] 방향 선택 모드 시작: " + skill.getName());
                        } else if (needsRangeConfirm(skill)) {
                            // 범위 확인 모드로 진입 (인페르노 등 광역 스킬)
                            isSelectingSkillDirection = true;
                            pendingSkill = skill;
                            if (hudRenderer.getDirectionIndicator() != null) {
                                float range = getSkillRange(skill);
                                hudRenderer.getDirectionIndicator().activateRangeMode(player.getX(), player.getY(), range);
                            }
                            System.out.println("[PlayerController] 범위 확인 모드 시작: " + skill.getName());
                        } else {
                            // 즉시 시전 스킬
                            Vector2 targetPosition = new Vector2(player.getX(), player.getY());
                            skill.cast(player, targetPosition);
                            System.out.println("[PlayerController] ✅ 즉시 스킬 발동: " + skill.getName());
                        }
                    } else {
                        System.out.println("[PlayerController] 스킬 쿨타임 중");
                    }
                    return true;  // 스킬 버튼 클릭 처리됨
                }
            }

            // 조이스틱 입력 처리
            return joystickController.onTouchDown(screenX, screenY, pointer);
        }

        return false;
    }

    /**
     * 스킬이 방향 선택이 필요한지 확인
     *
     * @param skill 확인할 스킬
     * @return 방향 선택 필요 여부
     */
    private boolean needsDirection(ElementalSkill skill) {
        // 발사체 스킬은 방향 필요
        String skillName = skill.getName();
        return skillName.contains("파이어볼") || skillName.contains("플레임 웨이브") ||
               skillName.contains("워터 샷") || skillName.contains("아이스 스파이크") ||
               skillName.contains("플러드") || skillName.contains("에어 슬래시") ||
               skillName.contains("토네이도") || skillName.contains("라이트닝 볼트") ||
               skillName.contains("체인 라이트닝") || skillName.contains("썬더 스톰") ||
               skillName.contains("록 스매시") || skillName.contains("어스 스파이크");
    }

    /**
     * 스킬이 범위 확인이 필요한지 확인 (광역 스킬)
     *
     * @param skill 확인할 스킬
     * @return 범위 확인 필요 여부
     */
    private boolean needsRangeConfirm(ElementalSkill skill) {
        String skillName = skill.getName();
        // 썬더 스톰은 방향 지정식으로 변경됨 (needsDirection에서 처리)
        return skillName.contains("인페르노") || skillName.contains("스톤 실드");
    }

    /**
     * 광역 스킬의 범위 반경 반환
     *
     * @param skill 스킬
     * @return 범위 반경 (픽셀)
     */
    private float getSkillRange(ElementalSkill skill) {
        String skillName = skill.getName();
        if (skillName.contains("인페르노")) {
            return com.example.yugeup.utils.Constants.INFERNO_RANGE;
        }
        return 100f;  // 기본값
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // 방향 선택 모드일 때는 방향 업데이트
        if (isSelectingSkillDirection && hudRenderer != null) {
            // 스크린 좌표를 월드 좌표로 변환
            Vector2 worldPos = screenToWorld(screenX, screenY);
            hudRenderer.getDirectionIndicator().updateTarget(worldPos.x, worldPos.y);
            return true;
        }

        joystickController.onTouchDragged(screenX, screenY, pointer);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // 방향 선택 모드일 때는 스킬 발동
        if (isSelectingSkillDirection && pendingSkill != null && hudRenderer != null) {
            // 최종 방향으로 스킬 시전
            Vector2 targetPos = hudRenderer.getDirectionIndicator().getTargetPosition();
            pendingSkill.cast(player, targetPos);
            System.out.println("[PlayerController] ✅ 방향 선택 완료 - 스킬 발동: " + pendingSkill.getName());

            // 방향 선택 모드 종료
            hudRenderer.getDirectionIndicator().deactivate();
            isSelectingSkillDirection = false;
            pendingSkill = null;
            return true;
        }

        joystickController.onTouchUp(pointer);
        return true;
    }

    /**
     * 스크린 좌표를 월드 좌표로 변환
     *
     * @param screenX 스크린 X
     * @param screenY 스크린 Y
     * @return 월드 좌표
     */
    private Vector2 screenToWorld(int screenX, int screenY) {
        // Viewport를 사용하여 스크린 좌표를 월드 좌표로 변환
        Vector2 worldPos = new Vector2(screenX, screenY);
        viewport.unproject(worldPos);
        return worldPos;
    }

    @Override
    public boolean keyDown(int keycode) {
        // 디버그 키: K = 경험치 증가, L = 경험치 감소 (PHASE_11)
        if (keycode == Input.Keys.K) {
            player.getLevelSystem().gainExperience(50);
            System.out.println("[DEBUG] 경험치 +50");
            return true;
        } else if (keycode == Input.Keys.L) {
            player.getLevelSystem().gainExperience(-50);
            System.out.println("[DEBUG] 경험치 -50");
            return true;
        } else if (keycode == Input.Keys.R) {
            // 디버그 키: R = 스킬 버튼 위치 재조정
            if (hudRenderer != null) {
                hudRenderer.repositionSkillButtons();
                System.out.println("[DEBUG] 스킬 버튼 위치 재조정 완료");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        joystickController.onTouchUp(pointer);
        return true;
    }

    // ===== Getter & Setter =====

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public JoystickController getJoystickController() {
        return joystickController;
    }

    public void setHUDRenderer(HUDRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public HUDRenderer getHUDRenderer() {
        return hudRenderer;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public GameMap getGameMap() {
        return gameMap;
    }
}
