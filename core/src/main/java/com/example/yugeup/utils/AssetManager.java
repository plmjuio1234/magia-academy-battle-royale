package com.example.yugeup.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * 에셋 관리 클래스
 *
 * 게임에서 사용하는 모든 리소스(텍스처, 폰트, 사운드 등)를 로드하고 관리합니다.
 * 싱글톤 패턴을 사용합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class AssetManager {

    // 싱글톤 인스턴스
    private static AssetManager instance;

    // 텍스처 저장소
    private Map<String, Texture> textures;

    // 폰트 저장소
    private Map<String, BitmapFont> fonts;

    // 아틀라스 저장소
    private Map<String, TextureAtlas> atlases;

    // 로딩 진행률 (목표)
    private float loadingProgress;

    // 로딩 진행률 (현재 - 부드러운 애니메이션용)
    private float currentProgress;

    // 로딩 완료 여부
    private boolean loaded;

    // 로딩 단계
    private int loadingStep;

    // 로딩 지연 시간 (초)
    private float loadingDelay;
    private static final float STEP_DELAY = 0.1f;  // 각 단계마다 0.1초 대기 (부드러운 진행률 애니메이션 고려)

    /**
     * Private 생성자 - 싱글톤 패턴
     */
    private AssetManager() {
        this.textures = new HashMap<>();
        this.fonts = new HashMap<>();
        this.atlases = new HashMap<>();
        this.loadingProgress = 0f;
        this.currentProgress = 0f;
        this.loaded = false;
        this.loadingStep = 0;
        this.loadingDelay = 0f;
    }

    /**
     * AssetManager 싱글톤 인스턴스를 반환합니다.
     *
     * @return AssetManager 인스턴스
     */
    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /**
     * 에셋을 단계별로 로드합니다. (메인 스레드에서 호출)
     * 매 프레임마다 조금씩 로드하여 화면이 멈추지 않도록 합니다.
     *
     * @param delta 이전 프레임으로부터의 시간
     */
    public void loadAssetsStep(float delta) {
        // 현재 진행률을 일정한 속도로 증가 (선형 방식)
        if (currentProgress < loadingProgress) {
            // 초당 20%씩 증가 (약 5초에 100% 완료)
            float incrementSpeed = 0.20f;
            currentProgress = Math.min(loadingProgress, currentProgress + incrementSpeed * delta);
        }

        // 로딩이 완료되었으면 단계 진행은 중단 (하지만 위의 진행률 증가는 계속 실행됨)
        if (loaded) return;

        // 지연 시간 누적
        loadingDelay += delta;

        // 각 단계마다 일정 시간 대기
        if (loadingDelay < STEP_DELAY) {
            return;  // 대기 중에는 단계만 진행하지 않고, 위에서 진행률은 계속 증가
        }

        // 지연 시간 리셋
        loadingDelay = 0f;

        try {
            switch (loadingStep) {
                case 0:
                    // 1. 로딩 화면 배경 로드 (0 ~ 10%)
                    Logger.info("에셋 로딩 시작...");
                    loadingProgress = 0.1f;
                    loadTexture("loading_bg", Constants.LOADING_BACKGROUND_PATH);
                    Logger.info("로딩 화면 배경 로드 완료");
                    loadingStep++;
                    break;

                case 1:
                    // 2. 폰트 로드 (10 ~ 30%)
                    loadingProgress = 0.3f;
                    loadFonts();
                    Logger.info("폰트 로드 완료");
                    loadingStep++;
                    break;

                case 2:
                    // 3. UI 텍스처 및 아틀라스 로드 (30 ~ 50%)
                    loadingProgress = 0.5f;
                    loadTexture("main_bg", Constants.MAIN_BACKGROUND_PATH);
                    loadTexture("lobby_bg", Constants.LOBBY_BACKGROUND_PATH);
                    loadTexture("waitroom_bg", Constants.WAITROOM_BACKGROUND_PATH);
                    loadTexture("logo", Constants.LOGO_PATH);
                    loadAtlas("button", Constants.BUTTON_ATLAS_PATH);
                    Logger.info("UI 텍스처 및 버튼 아틀라스 로드 완료");
                    loadingStep++;
                    break;

                case 3:
                    // 4. 캐릭터 아틀라스 로드 (50 ~ 70%)
                    loadingProgress = 0.7f;
                    loadAtlas("character", Constants.CHARACTER_ATLAS_PATH);
                    Logger.info("캐릭터 아틀라스 로드 완료");
                    loadingStep++;
                    break;

                case 4:
                    // 5. 원소 속성 아틀라스 로드 (70 ~ 85%)
                    loadingProgress = 0.85f;
                    loadAtlas("elemental", Constants.ELEMENTAL_ATLAS_PATH);
                    Logger.info("원소 속성 아틀라스 로드 완료");
                    loadingStep++;
                    break;

                case 5:
                    // 6. 스킬 아이콘 아틀라스 로드 (85 ~ 90%)
                    loadingProgress = 0.9f;
                    loadAtlas("skills", Constants.SKILLS_ATLAS_PATH);
                    Logger.info("스킬 아이콘 아틀라스 로드 완료");
                    loadingStep++;
                    break;

                case 6:
                    // 7. 몬스터 아틀라스 로드 (90 ~ 95%)
                    loadingProgress = 0.95f;
                    loadAtlas("ghost", "monsters/ghost.atlas");
                    loadAtlas("bat", "monsters/bat.atlas");
                    loadAtlas("golem", "monsters/golem.atlas");
                    Logger.info("몬스터 아틀라스 로드 완료");
                    loadingStep++;
                    break;

                case 7:
                    // 8. 로딩 완료 (95 ~ 100%)
                    loadingProgress = 1.0f;  // 목표를 100%로 설정 (currentProgress는 Lerp로 따라감)
                    loaded = true;
                    Logger.info("모든 에셋 로드 완료!");
                    break;
            }

        } catch (Exception e) {
            Logger.error("에셋 로드 중 에러 발생: " + e.getMessage(), e);
            loadingProgress = 1.0f;
            loaded = true;
        }
    }

    /**
     * 텍스처를 로드합니다.
     * 배경 이미지는 MipMap 없이, UI 요소는 Linear 필터링을 적용합니다.
     *
     * @param key 텍스처 키
     * @param path 텍스처 경로
     */
    private void loadTexture(String key, String path) {
        try {
            // MipMap 없이 로드 (배경 이미지는 대부분 크기가 크고 2의 거듭제곱이 아닐 수 있음)
            Texture texture = new Texture(Gdx.files.internal(path), false);
            // Linear 필터링만 적용 (부드러운 확대/축소)
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.put(key, texture);
        } catch (Exception e) {
            Logger.error("텍스처 로드 실패: " + path, e);
        }
    }

    /**
     * 아틀라스를 로드합니다.
     * 작은 이미지의 경우 Nearest 필터로 선명도를 유지합니다.
     *
     * @param key 아틀라스 키
     * @param path 아틀라스 경로
     */
    private void loadAtlas(String key, String path) {
        try {
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path));

            // 아틀라스 내 모든 텍스처에 Nearest 필터링 적용 (선명한 픽셀 유지)
            for (Texture texture : atlas.getTextures()) {
                // Nearest 필터링 적용 (작은 이미지를 확대할 때 선명하게 유지)
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }

            atlases.put(key, atlas);
        } catch (Exception e) {
            Logger.error("아틀라스 로드 실패: " + path, e);
        }
    }

    /**
     * 폰트를 로드합니다.
     */
    private void loadFonts() {
        try {
            // Regular 폰트 생성
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal(Constants.FONT_REGULAR_PATH)
            );

            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            // 한글 문자셋 포함 (실제 유니코드 문자로 생성)
            StringBuilder koreanChars = new StringBuilder();

            // 한글 완성형: 가(U+AC00) ~ 힣(U+D7A3)
            for (char c = '\uAC00'; c <= '\uD7A3'; c++) {
                koreanChars.append(c);
            }

            // 한글 자음: ㄱ(U+3131) ~ ㅎ(U+314E)
            for (char c = '\u3131'; c <= '\u314E'; c++) {
                koreanChars.append(c);
            }

            // 한글 모음: ㅏ(U+314F) ~ ㅣ(U+3163)
            for (char c = '\u314F'; c <= '\u3163'; c++) {
                koreanChars.append(c);
            }

            String allChars = FreeTypeFontGenerator.DEFAULT_CHARS + koreanChars.toString();

            // 작은 폰트 (36px)
            parameter.size = 36;
            parameter.characters = allChars;
            BitmapFont fontSmall = generator.generateFont(parameter);
            fonts.put("font_small", fontSmall);

            // 중간 폰트 (48px)
            parameter.size = 48;
            parameter.characters = allChars;
            BitmapFont fontMedium = generator.generateFont(parameter);
            fonts.put("font_medium", fontMedium);

            // 큰 폰트 (72px)
            parameter.size = 72;
            parameter.characters = allChars;
            BitmapFont fontLarge = generator.generateFont(parameter);
            fonts.put("font_large", fontLarge);

            generator.dispose();

            // Bold 폰트 생성
            FreeTypeFontGenerator generatorBold = new FreeTypeFontGenerator(
                Gdx.files.internal(Constants.FONT_BOLD_PATH)
            );

            // 타이틀 폰트 (96px, Bold)
            parameter.size = 96;
            parameter.characters = allChars;
            BitmapFont fontTitle = generatorBold.generateFont(parameter);
            fonts.put("font_title", fontTitle);

            generatorBold.dispose();

        } catch (Exception e) {
            Logger.error("폰트 로드 실패", e);
        }
    }

    /**
     * 텍스처를 반환합니다.
     *
     * @param key 텍스처 키
     * @return 텍스처 객체
     */
    public Texture getTexture(String key) {
        return textures.get(key);
    }

    /**
     * 폰트를 반환합니다.
     *
     * @param key 폰트 키
     * @return 폰트 객체
     */
    public BitmapFont getFont(String key) {
        return fonts.get(key);
    }

    /**
     * 아틀라스를 반환합니다.
     *
     * @param key 아틀라스 키
     * @return 아틀라스 객체
     */
    public TextureAtlas getAtlas(String key) {
        return atlases.get(key);
    }

    /**
     * 에셋 로딩이 완료되었는지 확인합니다.
     *
     * @return 로딩 완료 여부
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 에셋 로딩 진행률을 반환합니다. (부드러운 애니메이션용)
     *
     * @return 로딩 진행률 (0.0 ~ 1.0)
     */
    public float getProgress() {
        return currentProgress;
    }

    /**
     * 실제 에셋 로딩 진행률을 반환합니다. (목표값)
     *
     * @return 실제 로딩 진행률 (0.0 ~ 1.0)
     */
    public float getActualProgress() {
        return loadingProgress;
    }

    /**
     * 모든 에셋을 해제합니다.
     */
    public void dispose() {
        Logger.info("에셋 해제 시작...");

        // 텍스처 해제
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();

        // 폰트 해제
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        fonts.clear();

        // 아틀라스 해제
        for (TextureAtlas atlas : atlases.values()) {
            atlas.dispose();
        }
        atlases.clear();

        Logger.info("에셋 해제 완료");
    }
}
