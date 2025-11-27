package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.game.skill.ElementSkillSet;
import com.example.yugeup.game.skill.SkillUpgradeManager;

import java.util.List;

/**
 * 스킬 업그레이드 패널
 *
 * 게임 중 스킬을 업그레이드하는 UI 패널입니다.
 * (레벨업 시 또는 수동으로 열림)
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillUpgradePanel extends Table {
    private Player player;
    private SkillUpgradeManager upgradeManager;
    private Stage stage;
    private Skin skin;

    // UI 컴포넌트
    private Label expLabel;                 // 보유 경험치 표시
    private Table skillListTable;           // 스킬 목록 테이블
    private Table upgradeOptionsTable;      // 업그레이드 옵션 테이블

    /**
     * 스킬 업그레이드 패널 생성자
     *
     * @param player 플레이어 엔티티
     * @param stage 게임 스테이지
     * @param skin UI 스킨
     */
    public SkillUpgradePanel(Player player, Stage stage, Skin skin) {
        this.player = player;
        this.upgradeManager = new SkillUpgradeManager(player);
        this.stage = stage;
        this.skin = skin;

        setupUI();
    }

    /**
     * UI 구성
     */
    private void setupUI() {
        // 배경 설정
        this.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.9f)));
        this.setSize(700, 500);

        // 상단: 경험치 표시
        expLabel = new Label("보유 경험치: " + player.getLevelSystem().getCurrentExp(), skin);
        expLabel.setFontScale(1.2f);
        this.add(expLabel).pad(10).colspan(2).row();

        // 좌측: 스킬 목록 라벨
        Label skillListLabel = new Label("스킬 목록", skin);
        skillListLabel.setFontScale(1.1f);
        this.add(skillListLabel).pad(5).top().left();

        // 우측: 업그레이드 옵션 라벨
        Label upgradeLabel = new Label("업그레이드 옵션", skin);
        upgradeLabel.setFontScale(1.1f);
        this.add(upgradeLabel).pad(5).top().left().row();

        // 스킬 목록 테이블 생성
        skillListTable = new Table();
        skillListTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 1f)));

        // 업그레이드 옵션 테이블 생성
        upgradeOptionsTable = new Table();
        upgradeOptionsTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 1f)));

        // 양쪽 테이블 추가
        this.add(skillListTable).size(300, 350).pad(10);
        this.add(upgradeOptionsTable).size(300, 350).pad(10).row();

        // 하단: 닫기 버튼
        TextButton closeButton = new TextButton("닫기", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        this.add(closeButton).size(150, 50).pad(10).colspan(2);

        // 스킬 목록 로드
        loadSkillList();
    }

    /**
     * 스킬 목록 로드
     */
    private void loadSkillList() {
        skillListTable.clear();

        ElementSkillSet skillSet = player.getElementSkillSet();
        if (skillSet == null) {
            Label noSkillLabel = new Label("선택된 스킬이 없습니다", skin);
            skillListTable.add(noSkillLabel).pad(20);
            return;
        }

        List<ElementalSkill> skills = skillSet.getAllSkills();

        for (ElementalSkill skill : skills) {
            SkillButton skillButton = new SkillButton(skill, skin);
            skillButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onSkillSelected(skill);
                }
            });

            skillListTable.add(skillButton).size(280, 80).pad(5).row();
        }
    }

    /**
     * 스킬 선택 시 호출
     *
     * @param skill 선택된 스킬
     */
    private void onSkillSelected(ElementalSkill skill) {
        loadUpgradeOptions(skill);
    }

    /**
     * 업그레이드 옵션 로드
     *
     * @param skill 선택된 스킬
     */
    private void loadUpgradeOptions(ElementalSkill skill) {
        upgradeOptionsTable.clear();

        // 최대 레벨 도달 시 표시
        if (skill.getSkillLevel() >= 3) {
            Label maxLevelLabel = new Label("최대 레벨 도달!", skin);
            maxLevelLabel.setColor(Color.GOLD);
            maxLevelLabel.setFontScale(1.2f);
            upgradeOptionsTable.add(maxLevelLabel).pad(20).row();
            return;
        }

        // 3가지 업그레이드 옵션
        for (ElementalSkill.UpgradeType upgradeType : ElementalSkill.UpgradeType.values()) {
            UpgradeOptionButton optionButton = new UpgradeOptionButton(
                skill, upgradeType, upgradeManager, skin
            );

            optionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onUpgradeSelected(skill, upgradeType);
                }
            });

            upgradeOptionsTable.add(optionButton).size(280, 100).pad(5).row();
        }
    }

    /**
     * 업그레이드 선택 시 호출
     *
     * @param skill 업그레이드할 스킬
     * @param upgradeType 업그레이드 타입
     */
    private void onUpgradeSelected(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType) {
        boolean success = upgradeManager.upgradeSkill(skill, upgradeType);

        if (success) {
            // 성공 메시지 표시
            showUpgradeSuccessDialog(skill, upgradeType);

            // UI 갱신
            updateExpLabel();
            loadSkillList();
            loadUpgradeOptions(skill);
        } else {
            // 실패 메시지 표시
            showUpgradeFailDialog();
        }
    }

    /**
     * 경험치 라벨 업데이트
     */
    private void updateExpLabel() {
        expLabel.setText("보유 경험치: " + player.getLevelSystem().getCurrentExp());
    }

    /**
     * 업그레이드 성공 다이얼로그 표시
     *
     * @param skill 업그레이드된 스킬
     * @param upgradeType 업그레이드 타입
     */
    private void showUpgradeSuccessDialog(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType) {
        Dialog dialog = new Dialog("업그레이드 성공!", skin);
        dialog.text(skill.getName() + " 스킬이 강화되었습니다!\n"
            + "(" + upgradeType.name() + " 레벨 " + skill.getSkillLevel() + ")");
        dialog.button("확인");
        dialog.show(stage);
    }

    /**
     * 업그레이드 실패 다이얼로그 표시
     */
    private void showUpgradeFailDialog() {
        Dialog dialog = new Dialog("업그레이드 불가", skin);
        dialog.text("경험치가 부족하거나 최대 레벨입니다.");
        dialog.button("확인");
        dialog.show(stage);
    }

    /**
     * 패널 표시
     */
    public void show() {
        this.setVisible(true);
        this.toFront();
    }

    /**
     * 패널 숨김
     */
    public void hide() {
        this.setVisible(false);
    }
}

/**
 * 스킬 버튼
 *
 * 스킬 정보를 표시하는 버튼 컴포넌트입니다.
 */
class SkillButton extends Table {
    /**
     * 스킬 버튼 생성자
     *
     * @param skill 표시할 스킬
     * @param skin UI 스킨
     */
    public SkillButton(ElementalSkill skill, Skin skin) {
        // 배경 색상 (원소별)
        Color bgColor = new Color(skill.getElement().getColor());
        this.setBackground(skin.newDrawable("white", bgColor));

        // 스킬 이름
        Label nameLabel = new Label(skill.getName(), skin);
        nameLabel.setFontScale(1.1f);
        this.add(nameLabel).pad(5).left().row();

        // 스킬 레벨
        Label levelLabel = new Label("레벨: " + skill.getSkillLevel() + " / 3", skin);
        this.add(levelLabel).pad(5).left();
    }
}

/**
 * 업그레이드 옵션 버튼
 *
 * 업그레이드 옵션을 선택하는 버튼 컴포넌트입니다.
 */
class UpgradeOptionButton extends Table {
    private ElementalSkill skill;
    private ElementalSkill.UpgradeType upgradeType;
    private SkillUpgradeManager upgradeManager;
    private Skin skin;

    /**
     * 업그레이드 옵션 버튼 생성자
     *
     * @param skill 업그레이드할 스킬
     * @param upgradeType 업그레이드 타입
     * @param upgradeManager 업그레이드 매니저
     * @param skin UI 스킨
     */
    public UpgradeOptionButton(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType,
                               SkillUpgradeManager upgradeManager, Skin skin) {
        this.skill = skill;
        this.upgradeType = upgradeType;
        this.upgradeManager = upgradeManager;
        this.skin = skin;

        setupUI();
    }

    /**
     * UI 구성
     */
    private void setupUI() {
        // 배경
        this.setBackground(skin.newDrawable("white", new Color(0.3f, 0.3f, 0.3f, 1f)));

        // 업그레이드 타입 이름
        String typeName = "";
        switch (upgradeType) {
            case DAMAGE:
                typeName = "데미지 증가";
                break;
            case COOLDOWN:
                typeName = "쿨타임 감소";
                break;
        }

        Label typeLabel = new Label(typeName, skin);
        typeLabel.setFontScale(1.05f);
        this.add(typeLabel).pad(5).left().row();

        // 미리보기
        SkillUpgradeManager.UpgradePreview preview = upgradeManager.getUpgradePreview(skill, upgradeType);
        Label previewLabel = new Label(preview.displayText, skin);
        previewLabel.setColor(Color.YELLOW);
        previewLabel.setFontScale(0.95f);
        this.add(previewLabel).pad(5).left().row();

        // 비용
        int cost = upgradeManager.getUpgradeCost(skill.getSkillLevel());
        Label costLabel = new Label("비용: " + cost + " EXP", skin);

        // 업그레이드 가능 여부에 따라 색상 변경
        if (upgradeManager.canUpgrade(skill, upgradeType)) {
            costLabel.setColor(Color.GREEN);
        } else {
            costLabel.setColor(Color.RED);
        }

        this.add(costLabel).pad(5).left();
    }
}
