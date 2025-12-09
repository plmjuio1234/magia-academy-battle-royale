package com.example.yugeup.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.utils.Constants;

/**
 * 원소 선택 다이얼로그
 *
 * 게임 시작 전 플레이어가 원소를 선택하는 UI입니다.
 * Scene2D를 사용하여 구현되었습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ElementSelectDialog extends Dialog {
    private Stage stage;
    private Player player;

    private ElementType selectedElement = null;
    private Table[] elementButtons;

    // 스킬 미리보기 영역
    private Label skillPreviewLabel;
    private Table skillPreviewTable;

    // 확정 버튼
    private TextButton confirmButton;

    // 선택 완료 리스너
    private ElementSelectListener selectListener;

    /**
     * 다이얼로그 생성자
     *
     * @param stage Scene2D Stage
     * @param skin UI Skin
     * @param player 플레이어
     */
    public ElementSelectDialog(Stage stage, Skin skin, Player player) {
        super("", skin);
        this.stage = stage;
        this.player = player;

        setupUI(skin);
    }

    /**
     * UI 구성
     *
     * @param skin UI Skin
     */
    private void setupUI(Skin skin) {
        // 다이얼로그 크기 설정
        this.setWidth(Constants.ELEMENT_DIALOG_WIDTH);
        this.setHeight(Constants.ELEMENT_DIALOG_HEIGHT);
        this.setModal(true);
        this.setMovable(false);

        // 배경 설정
        this.getContentTable().setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.95f)));

        // 상단 안내 문구
        Label titleLabel = new Label("당신의 원소를 선택하세요", skin);
        titleLabel.setFontScale(2.0f);
        titleLabel.setColor(Color.YELLOW);
        this.getContentTable().add(titleLabel).pad(30).row();

        // 원소 버튼 영역
        Table elementTable = new Table();
        elementButtons = new Table[5];

        int index = 0;
        for (ElementType element : ElementType.values()) {
            Table elementButton = createElementButton(element, skin);
            elementButtons[index++] = elementButton;
            elementTable.add(elementButton).size(Constants.ELEMENT_BUTTON_SIZE, Constants.ELEMENT_BUTTON_SIZE)
                        .pad(Constants.ELEMENT_BUTTON_SPACING);
        }

        this.getContentTable().add(elementTable).pad(20).row();

        // 스킬 미리보기 라벨
        skillPreviewLabel = new Label("원소를 선택하면 3가지 스킬을 확인할 수 있습니다", skin);
        skillPreviewLabel.setFontScale(1.2f);
        skillPreviewLabel.setColor(Color.LIGHT_GRAY);
        this.getContentTable().add(skillPreviewLabel).pad(15).row();

        // 스킬 미리보기 영역
        skillPreviewTable = new Table();
        skillPreviewTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.9f)));
        this.getContentTable().add(skillPreviewTable)
                              .size(Constants.ELEMENT_DIALOG_WIDTH - 100, 200)
                              .pad(10).row();

        // 확정 버튼
        confirmButton = new TextButton("선택 확정", skin);
        confirmButton.getLabel().setFontScale(1.5f);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onConfirmSelection();
            }
        });
        this.getButtonTable().add(confirmButton).size(300, 80).pad(20);

        // 중앙 정렬
        this.setPosition(
            (Constants.SCREEN_WIDTH - this.getWidth()) / 2,
            (Constants.SCREEN_HEIGHT - this.getHeight()) / 2
        );
    }

    /**
     * 원소 버튼 생성
     *
     * @param element 원소 타입
     * @param skin UI Skin
     * @return 원소 버튼 테이블
     */
    private Table createElementButton(ElementType element, Skin skin) {
        Table button = new Table();

        // 원소 색상 설정
        Color elementColor = intToColor(element.getColor());
        button.setBackground(skin.newDrawable("white", elementColor));

        // 원소 이름 라벨
        Label nameLabel = new Label(element.getDisplayName(), skin);
        nameLabel.setFontScale(1.5f);
        nameLabel.setColor(Color.WHITE);
        button.add(nameLabel).center().expand();

        // 클릭 리스너
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onElementSelected(element);
            }
        });

        return button;
    }

    /**
     * int 색상을 Color로 변환
     *
     * @param colorInt RGB 정수값
     * @return libGDX Color 객체
     */
    private Color intToColor(int colorInt) {
        float r = ((colorInt >> 16) & 0xFF) / 255f;
        float g = ((colorInt >> 8) & 0xFF) / 255f;
        float b = (colorInt & 0xFF) / 255f;
        return new Color(r, g, b, 1f);
    }

    /**
     * 원소 선택 시 호출
     *
     * @param element 선택한 원소
     */
    private void onElementSelected(ElementType element) {
        this.selectedElement = element;

        // 모든 버튼 원래 색상으로 복원
        int index = 0;
        for (ElementType e : ElementType.values()) {
            Color elementColor = intToColor(e.getColor());
            elementButtons[index++].setBackground(getSkin().newDrawable("white", elementColor));
        }

        // 선택한 버튼 강조 (노란색 테두리 효과)
        index = 0;
        for (ElementType e : ElementType.values()) {
            if (e == element) {
                elementButtons[index].setBackground(getSkin().newDrawable("white", Color.YELLOW));
                break;
            }
            index++;
        }

        // 스킬 미리보기 업데이트
        updateSkillPreview(element);

        System.out.println("[ElementSelectDialog] 원소 선택: " + element.getDisplayName());
    }

    /**
     * 스킬 미리보기 업데이트
     *
     * @param element 선택한 원소
     */
    private void updateSkillPreview(ElementType element) {
        skillPreviewTable.clear();

        // 원소 설명
        Label descLabel = new Label(element.getDescription(), getSkin());
        descLabel.setFontScale(1.2f);
        descLabel.setColor(Color.CYAN);
        skillPreviewTable.add(descLabel).pad(10).colspan(2).row();

        // 스킬 이름 표시
        String[] skillNames = element.getSkillNames();

        for (int i = 0; i < 3; i++) {
            Label slotLabel = new Label("스킬 " + (char)('A' + i) + ":", getSkin());
            slotLabel.setFontScale(1.1f);
            slotLabel.setColor(Color.LIGHT_GRAY);

            Label skillLabel = new Label(skillNames[i], getSkin());
            skillLabel.setFontScale(1.1f);
            skillLabel.setColor(Color.WHITE);

            skillPreviewTable.add(slotLabel).pad(8).left();
            skillPreviewTable.add(skillLabel).pad(8).left().expandX().row();
        }
    }

    /**
     * 선택 확정
     */
    private void onConfirmSelection() {
        if (selectedElement == null) {
            // 경고 메시지
            Dialog warningDialog = new Dialog("경고", getSkin());

            Label warningLabel = new Label("원소를 선택해주세요!", getSkin());
            warningLabel.setFontScale(1.5f);
            warningLabel.setColor(Color.RED);
            warningDialog.getContentTable().add(warningLabel).pad(30);

            TextButton okButton = new TextButton("확인", getSkin());
            okButton.getLabel().setFontScale(1.2f);
            warningDialog.button(okButton);

            warningDialog.show(stage);
            return;
        }

        // 플레이어에게 원소 적용
        player.setElement(selectedElement);

        System.out.println("[ElementSelectDialog] 원소 확정: " + selectedElement.getDisplayName());

        // 리스너 호출
        if (selectListener != null) {
            selectListener.onElementSelected(selectedElement);
        }

        // 다이얼로그 닫기
        this.hide();
    }

    /**
     * 선택 완료 리스너 설정
     *
     * @param listener 리스너
     */
    public void setSelectListener(ElementSelectListener listener) {
        this.selectListener = listener;
    }

    /**
     * 원소 선택 완료 리스너 인터페이스
     */
    public interface ElementSelectListener {
        void onElementSelected(ElementType element);
    }
}
