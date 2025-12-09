# PHASE_13_ELEMENT_SELECT.md - 원소 선택 시스템

---

## 🎯 목표
게임 시작 시 5가지 원소(불, 물, 바람, 번개, 흙) 중 하나를 선택하는 시스템 구현

---

## 📋 구현 범위

### 원소 시스템
- ✅ 5가지 원소 타입 정의 (ElementType)
- ✅ 각 원소별 3개 스킬 세트
- ✅ 원소 선택 UI (대기실 또는 게임 시작 전)
- ✅ 선택된 원소에 따라 스킬 활성화

### UI 컴포넌트
- ✅ 원소 선택 다이얼로그
- ✅ 각 원소별 아이콘 및 설명
- ✅ 스킬 미리보기 (3개 스킬 정보)
- ✅ 선택 확정 버튼

---

## 📁 필요 파일

### 생성할 파일
```
game/skill/
  ├─ ElementType.java              (새로 생성)
  ├─ ElementSkillSet.java          (새로 생성)
  └─ ElementalSkill.java           (새로 생성)

ui/dialog/
  ├─ ElementSelectDialog.java      (새로 생성)
  └─ ElementButton.java            (새로 생성)

game/player/
  └─ PlayerElement.java            (새로 생성)
```

### 기존 파일 수정
```
Constants.java                    (수정 - 원소 관련 상수 추가)
Player.java                       (수정 - 원소 정보 추가)
SkillManager.java                 (수정 - 원소별 스킬 로드)
```

---

## 🔧 구현 가이드

### 1. ElementType 열거형

```java
/**
 * 원소 타입
 *
 * 게임 내 5가지 원소를 정의합니다.
 * 각 원소는 고유한 3가지 스킬을 가집니다.
 */
public enum ElementType {
    FIRE("불", "공격적인 화염 마법", 0xFF4500),
    WATER("물", "방어와 회복의 물 마법", 0x1E90FF),
    WIND("바람", "속도와 기동성의 바람 마법", 0x7FFF00),
    LIGHTNING("번개", "빠르고 강력한 번개 마법", 0xFFFF00),
    EARTH("흙", "방어와 지속 데미지의 대지 마법", 0x8B4513);

    // 원소 이름 (한글)
    private final String displayName;

    // 원소 설명
    private final String description;

    // 원소 대표 색상 (RGB)
    private final int color;

    ElementType(String displayName, String description, int color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    /**
     * 해당 원소의 스킬 ID 배열 반환
     *
     * @return 3개의 스킬 ID (스킬 A, B, C)
     */
    public int[] getSkillIds() {
        switch (this) {
            case FIRE:
                return new int[] {101, 102, 103};  // 파이어볼, 불 기둥, 운석
            case WATER:
                return new int[] {201, 202, 203};  // 아이스 샤드, 물 방어막, 파도
            case WIND:
                return new int[] {301, 302, 303};  // 회오리, 바람 베기, 질주
            case LIGHTNING:
                return new int[] {401, 402, 403};  // 번개, 체인 라이트닝, 전자기장
            case EARTH:
                return new int[] {501, 502, 503};  // 바위 던지기, 지진, 흙 갑옷
            default:
                return new int[] {0, 0, 0};
        }
    }

    /**
     * 해당 원소의 스킬 이름 배열 반환
     */
    public String[] getSkillNames() {
        switch (this) {
            case FIRE:
                return new String[] {"파이어볼", "불 기둥", "운석"};
            case WATER:
                return new String[] {"아이스 샤드", "물 방어막", "파도"};
            case WIND:
                return new String[] {"회오리", "바람 베기", "질주"};
            case LIGHTNING:
                return new String[] {"번개", "체인 라이트닝", "전자기장"};
            case EARTH:
                return new String[] {"바위 던지기", "지진", "흙 갑옷"};
            default:
                return new String[] {"", "", ""};
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }
}
```

### 2. ElementSkillSet 클래스

```java
/**
 * 원소 스킬 세트
 *
 * 하나의 원소에 속한 3개의 스킬을 관리합니다.
 */
public class ElementSkillSet {
    private ElementType element;
    private ElementalSkill skillA;  // 첫 번째 스킬
    private ElementalSkill skillB;  // 두 번째 스킬
    private ElementalSkill skillC;  // 세 번째 스킬

    /**
     * 원소 스킬 세트 생성자
     *
     * @param element 원소 타입
     */
    public ElementSkillSet(ElementType element) {
        this.element = element;
        initializeSkills();
    }

    /**
     * 원소에 맞는 스킬 초기화
     */
    private void initializeSkills() {
        int[] skillIds = element.getSkillIds();
        String[] skillNames = element.getSkillNames();

        // 스킬 팩토리를 통해 생성 (PHASE_14~18에서 구현)
        this.skillA = SkillFactory.createElementalSkill(skillIds[0], skillNames[0], element);
        this.skillB = SkillFactory.createElementalSkill(skillIds[1], skillNames[1], element);
        this.skillC = SkillFactory.createElementalSkill(skillIds[2], skillNames[2], element);
    }

    /**
     * 슬롯 번호로 스킬 가져오기
     *
     * @param slot 슬롯 번호 (0=A, 1=B, 2=C)
     * @return 해당 슬롯의 스킬
     */
    public ElementalSkill getSkill(int slot) {
        switch (slot) {
            case 0: return skillA;
            case 1: return skillB;
            case 2: return skillC;
            default: return null;
        }
    }

    /**
     * 모든 스킬 리스트 반환
     */
    public List<ElementalSkill> getAllSkills() {
        List<ElementalSkill> skills = new ArrayList<>();
        skills.add(skillA);
        skills.add(skillB);
        skills.add(skillC);
        return skills;
    }

    public ElementType getElement() {
        return element;
    }

    public ElementalSkill getSkillA() {
        return skillA;
    }

    public ElementalSkill getSkillB() {
        return skillB;
    }

    public ElementalSkill getSkillC() {
        return skillC;
    }
}
```

### 3. ElementalSkill 추상 클래스

```java
/**
 * 원소 스킬 기본 클래스
 *
 * 모든 원소 스킬은 이 클래스를 상속합니다.
 * (PHASE_14~18에서 각 원소별 스킬 구현)
 */
public abstract class ElementalSkill extends Skill {
    protected ElementType element;      // 원소 타입
    protected int skillLevel;           // 스킬 레벨 (1~3)

    // 업그레이드 가능한 속성
    protected float damageMultiplier;   // 데미지 배율
    protected float rangeMultiplier;    // 범위 배율
    protected float cooldownReduction;  // 쿨타임 감소

    /**
     * 원소 스킬 생성자
     *
     * @param skillId 스킬 ID
     * @param name 스킬 이름
     * @param element 원소 타입
     */
    public ElementalSkill(int skillId, String name, ElementType element) {
        super(skillId, name);
        this.element = element;
        this.skillLevel = 1;

        // 초기 배율 설정
        this.damageMultiplier = 1.0f;
        this.rangeMultiplier = 1.0f;
        this.cooldownReduction = 0f;
    }

    /**
     * 스킬 업그레이드 (PHASE_19에서 구현)
     *
     * @param upgradeType 업그레이드 타입 (DAMAGE/RANGE/COOLDOWN)
     */
    public void upgrade(UpgradeType upgradeType) {
        skillLevel++;

        switch (upgradeType) {
            case DAMAGE:
                damageMultiplier += 0.3f;  // 30% 증가
                break;
            case RANGE:
                rangeMultiplier += 0.25f;  // 25% 증가
                break;
            case COOLDOWN:
                cooldownReduction += 0.2f;  // 20% 감소
                break;
        }
    }

    /**
     * 최종 데미지 계산 (업그레이드 적용)
     */
    @Override
    public int getDamage() {
        return (int) (baseDamage * damageMultiplier);
    }

    /**
     * 최종 쿨타임 계산 (업그레이드 적용)
     */
    @Override
    public float getCooldown() {
        return baseCooldown * (1.0f - cooldownReduction);
    }

    /**
     * 스킬 시전 (각 원소별로 오버라이드)
     */
    @Override
    public abstract void cast(Player caster, Vector2 targetPosition);

    public ElementType getElement() {
        return element;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    /**
     * 업그레이드 타입
     */
    public enum UpgradeType {
        DAMAGE,      // 데미지 증가
        RANGE,       // 범위 증가
        COOLDOWN     // 쿨타임 감소
    }
}
```

### 4. ElementSelectDialog UI

```java
/**
 * 원소 선택 다이얼로그
 *
 * 게임 시작 전 플레이어가 원소를 선택하는 UI입니다.
 */
public class ElementSelectDialog extends Dialog {
    private Stage stage;
    private Skin skin;

    private ElementType selectedElement = null;
    private ElementButton[] elementButtons;

    // 스킬 미리보기 영역
    private Label skillPreviewLabel;
    private Table skillPreviewTable;

    /**
     * 다이얼로그 생성자
     */
    public ElementSelectDialog(Stage stage, Skin skin) {
        super("원소 선택", skin);
        this.stage = stage;
        this.skin = skin;

        setupUI();
    }

    /**
     * UI 구성
     */
    private void setupUI() {
        // 다이얼로그 크기 설정
        this.setWidth(800);
        this.setHeight(600);
        this.setModal(true);

        // 상단 안내 문구
        Label titleLabel = new Label("당신의 원소를 선택하세요", skin);
        titleLabel.setFontScale(1.5f);
        this.getContentTable().add(titleLabel).pad(20).row();

        // 원소 버튼 영역
        Table elementTable = new Table();
        elementButtons = new ElementButton[5];

        int index = 0;
        for (ElementType element : ElementType.values()) {
            ElementButton button = new ElementButton(element, skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onElementSelected(element);
                }
            });

            elementButtons[index++] = button;
            elementTable.add(button).size(140, 140).pad(10);
        }

        this.getContentTable().add(elementTable).pad(20).row();

        // 스킬 미리보기 영역
        skillPreviewLabel = new Label("스킬을 확인하세요", skin);
        this.getContentTable().add(skillPreviewLabel).pad(10).row();

        skillPreviewTable = new Table();
        skillPreviewTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f)));
        this.getContentTable().add(skillPreviewTable).size(700, 150).pad(10).row();

        // 확정 버튼
        TextButton confirmButton = new TextButton("선택 확정", skin);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onConfirmSelection();
            }
        });
        this.getButtonTable().add(confirmButton).size(200, 60).pad(20);
    }

    /**
     * 원소 선택 시 호출
     */
    private void onElementSelected(ElementType element) {
        this.selectedElement = element;

        // 모든 버튼 선택 해제
        for (ElementButton btn : elementButtons) {
            btn.setSelected(false);
        }

        // 선택한 버튼 강조
        for (ElementButton btn : elementButtons) {
            if (btn.getElement() == element) {
                btn.setSelected(true);
                break;
            }
        }

        // 스킬 미리보기 업데이트
        updateSkillPreview(element);
    }

    /**
     * 스킬 미리보기 업데이트
     */
    private void updateSkillPreview(ElementType element) {
        skillPreviewTable.clear();

        String[] skillNames = element.getSkillNames();

        for (int i = 0; i < 3; i++) {
            Label skillLabel = new Label("스킬 " + (char)('A' + i) + ": " + skillNames[i], skin);
            skillPreviewTable.add(skillLabel).pad(10).expandX().left().row();
        }
    }

    /**
     * 선택 확정
     */
    private void onConfirmSelection() {
        if (selectedElement == null) {
            // 경고 메시지
            Dialog warningDialog = new Dialog("경고", skin);
            warningDialog.text("원소를 선택해주세요!");
            warningDialog.button("확인");
            warningDialog.show(stage);
            return;
        }

        // 플레이어에게 원소 적용
        Player localPlayer = GameManager.getInstance().getLocalPlayer();
        localPlayer.setElement(selectedElement);

        // 스킬 매니저에 스킬 로드
        SkillManager skillManager = localPlayer.getSkillManager();
        skillManager.loadElementalSkills(selectedElement);

        // 다이얼로그 닫기
        this.hide();
    }
}
```

### 5. ElementButton 컴포넌트

```java
/**
 * 원소 버튼
 *
 * 원소 선택 UI의 각 원소 버튼입니다.
 */
public class ElementButton extends Table {
    private ElementType element;
    private boolean isSelected = false;

    private Image iconImage;
    private Label nameLabel;
    private Skin skin;

    public ElementButton(ElementType element, Skin skin) {
        this.element = element;
        this.skin = skin;

        setupUI();
    }

    /**
     * 버튼 UI 구성
     */
    private void setupUI() {
        // 배경 설정
        Color elementColor = new Color(element.getColor());
        this.setBackground(skin.newDrawable("white", elementColor));

        // 아이콘 (추후 텍스처 추가)
        iconImage = new Image(skin.getDrawable("element_" + element.name().toLowerCase()));
        this.add(iconImage).size(80, 80).pad(10).row();

        // 이름 라벨
        nameLabel = new Label(element.getDisplayName(), skin);
        nameLabel.setFontScale(1.2f);
        this.add(nameLabel).pad(5);
    }

    /**
     * 선택 상태 설정
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;

        if (selected) {
            // 선택 시 테두리 강조
            this.setBackground(skin.newDrawable("white", Color.YELLOW));
        } else {
            // 기본 색상으로 복원
            Color elementColor = new Color(element.getColor());
            this.setBackground(skin.newDrawable("white", elementColor));
        }
    }

    public ElementType getElement() {
        return element;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
```

### 6. Player 클래스 수정

```java
/**
 * Player 클래스에 추가할 코드
 */
public class Player extends Entity {
    // 기존 필드들...

    // 선택한 원소
    private ElementType selectedElement = null;
    private ElementSkillSet elementSkillSet = null;

    /**
     * 원소 설정
     *
     * @param element 선택한 원소
     */
    public void setElement(ElementType element) {
        this.selectedElement = element;
        this.elementSkillSet = new ElementSkillSet(element);
    }

    /**
     * 원소 스킬 세트 가져오기
     */
    public ElementSkillSet getElementSkillSet() {
        return elementSkillSet;
    }

    /**
     * 선택한 원소 가져오기
     */
    public ElementType getSelectedElement() {
        return selectedElement;
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * ElementType 테스트
 */
public class TestElementType {
    @Test
    public void 모든_원소는_3개의_스킬을_가진다() {
        for (ElementType element : ElementType.values()) {
            int[] skillIds = element.getSkillIds();
            assertEquals(3, skillIds.length);
        }
    }

    @Test
    public void 각_원소는_고유한_스킬_ID를_가진다() {
        Set<Integer> allSkillIds = new HashSet<>();

        for (ElementType element : ElementType.values()) {
            int[] skillIds = element.getSkillIds();
            for (int id : skillIds) {
                assertTrue(allSkillIds.add(id), "중복된 스킬 ID: " + id);
            }
        }
    }

    @Test
    public void 원소_이름이_정상적으로_반환된다() {
        assertEquals("불", ElementType.FIRE.getDisplayName());
        assertEquals("물", ElementType.WATER.getDisplayName());
    }
}

/**
 * ElementSkillSet 테스트
 */
public class TestElementSkillSet {
    private ElementSkillSet fireSkillSet;

    @BeforeEach
    public void setUp() {
        fireSkillSet = new ElementSkillSet(ElementType.FIRE);
    }

    @Test
    public void 스킬_세트가_초기화된다() {
        assertNotNull(fireSkillSet.getSkillA());
        assertNotNull(fireSkillSet.getSkillB());
        assertNotNull(fireSkillSet.getSkillC());
    }

    @Test
    public void 슬롯_번호로_스킬을_가져온다() {
        ElementalSkill skillA = fireSkillSet.getSkill(0);
        assertEquals(fireSkillSet.getSkillA(), skillA);
    }

    @Test
    public void 모든_스킬_리스트_반환() {
        List<ElementalSkill> skills = fireSkillSet.getAllSkills();
        assertEquals(3, skills.size());
    }
}

/**
 * ElementalSkill 테스트
 */
public class TestElementalSkill {
    private ElementalSkill testSkill;

    @BeforeEach
    public void setUp() {
        // 테스트용 스킬 생성 (구체적인 구현은 PHASE_14에서)
        testSkill = new ElementalSkill(101, "테스트 스킬", ElementType.FIRE) {
            @Override
            public void cast(Player caster, Vector2 targetPosition) {
                // 테스트 구현
            }
        };
        testSkill.baseDamage = 100;
        testSkill.baseCooldown = 5.0f;
    }

    @Test
    public void 초기_스킬_레벨은_1이다() {
        assertEquals(1, testSkill.getSkillLevel());
    }

    @Test
    public void 데미지_업그레이드_적용() {
        int originalDamage = testSkill.getDamage();

        testSkill.upgrade(ElementalSkill.UpgradeType.DAMAGE);

        assertTrue(testSkill.getDamage() > originalDamage);
        assertEquals(2, testSkill.getSkillLevel());
    }

    @Test
    public void 쿨타임_업그레이드_적용() {
        float originalCooldown = testSkill.getCooldown();

        testSkill.upgrade(ElementalSkill.UpgradeType.COOLDOWN);

        assertTrue(testSkill.getCooldown() < originalCooldown);
    }

    @Test
    public void 범위_업그레이드_적용() {
        testSkill.upgrade(ElementalSkill.UpgradeType.RANGE);

        assertEquals(1.25f, testSkill.rangeMultiplier, 0.01f);
    }
}

/**
 * ElementSelectDialog 테스트
 */
public class TestElementSelectDialog {
    private ElementSelectDialog dialog;
    private Stage mockStage;

    @BeforeEach
    public void setUp() {
        mockStage = new Stage();
        Skin mockSkin = new Skin();
        dialog = new ElementSelectDialog(mockStage, mockSkin);
    }

    @Test
    public void 다이얼로그가_생성된다() {
        assertNotNull(dialog);
    }

    @Test
    public void 5개의_원소_버튼이_생성된다() {
        assertEquals(5, dialog.elementButtons.length);
    }

    @Test
    public void 원소_선택_시_플레이어에_적용() {
        dialog.onElementSelected(ElementType.FIRE);
        dialog.onConfirmSelection();

        Player localPlayer = GameManager.getInstance().getLocalPlayer();
        assertEquals(ElementType.FIRE, localPlayer.getSelectedElement());
    }
}
```

---

## ✅ 완료 조건

- [ ] ElementType 열거형 구현
- [ ] ElementSkillSet 클래스 구현
- [ ] ElementalSkill 추상 클래스 구현
- [ ] ElementSelectDialog UI 구현
- [ ] ElementButton 컴포넌트 구현
- [ ] Player 클래스에 원소 정보 추가
- [ ] 원소 선택 시 스킬 로드 동작 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_14: 불 속성 스킬**
- 파이어볼 (Fireball)
- 불 기둥 (Flame Pillar)
- 운석 (Meteor)
