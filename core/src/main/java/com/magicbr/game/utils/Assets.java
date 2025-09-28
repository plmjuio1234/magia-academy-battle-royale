package com.magicbr.game.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Assets {
    public static final String TEXTURE_ATLAS_GAME = "images/atlas/game.atlas";
    public static final String TEXTURE_ATLAS_UI = "images/atlas/ui.atlas";

    // 캐릭터 텍스처
    public static final String PLAYER_ELEMENTALIST = "images/characters/elementalist.png";
    public static final String PLAYER_SUMMONER = "images/characters/summoner.png";
    public static final String PLAYER_ENCHANTER = "images/characters/enchanter.png";
    public static final String PLAYER_NECROMANCER = "images/characters/necromancer.png";

    // UI 텍스처
    public static final String UI_BACKGROUND = "images/ui/background.png";
    public static final String UI_BUTTON = "images/ui/button.png";
    public static final String UI_PANEL = "images/ui/panel.png";
    public static final String UI_HEALTH_BAR = "images/ui/health_bar.png";
    public static final String UI_MANA_BAR = "images/ui/mana_bar.png";

    // 이펙트
    public static final String EFFECT_FIREBALL = "images/effects/fireball.png";
    public static final String EFFECT_ICEBLAST = "images/effects/iceblast.png";
    public static final String EFFECT_LIGHTNING = "images/effects/lightning.png";

    // 맵
    public static final String MAP_DUNGEON = "maps/dungeon.tmx";

    // 사운드
    public static final String SOUND_CLICK = "sounds/click.wav";
    public static final String SOUND_SPELL_CAST = "sounds/spell_cast.wav";
    public static final String SOUND_DAMAGE = "sounds/damage.wav";
    public static final String SOUND_VICTORY = "sounds/victory.wav";

    // 폰트
    public static final String FONT_DEFAULT = "fonts/default.fnt";
    public static final String FONT_UI = "fonts/ui.fnt";

    private AssetManager assetManager;

    // 실제 로드된 에셋들에 대한 참조
    public TextureAtlas gameAtlas;
    public TextureAtlas uiAtlas;
    public BitmapFont defaultFont;
    public BitmapFont uiFont;

    public Assets(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void loadGameAssets() {
        // 현재 존재하는 기본 에셋만 로드 (나머지는 디자이너가 추가할 예정)
        assetManager.load("libgdx.png", Texture.class);

        // TODO: 디자이너가 리소스를 추가하면 아래 주석을 해제하고 사용

        // Texture Atlas
        // assetManager.load(TEXTURE_ATLAS_GAME, TextureAtlas.class);
        // assetManager.load(TEXTURE_ATLAS_UI, TextureAtlas.class);

        // 개별 텍스처 (Atlas가 없을 때 백업용)
        // assetManager.load(PLAYER_ELEMENTALIST, Texture.class);
        // assetManager.load(PLAYER_SUMMONER, Texture.class);
        // assetManager.load(PLAYER_ENCHANTER, Texture.class);
        // assetManager.load(PLAYER_NECROMANCER, Texture.class);

        // assetManager.load(UI_BACKGROUND, Texture.class);
        // assetManager.load(UI_BUTTON, Texture.class);
        // assetManager.load(UI_PANEL, Texture.class);
        // assetManager.load(UI_HEALTH_BAR, Texture.class);
        // assetManager.load(UI_MANA_BAR, Texture.class);

        // assetManager.load(EFFECT_FIREBALL, Texture.class);
        // assetManager.load(EFFECT_ICEBLAST, Texture.class);
        // assetManager.load(EFFECT_LIGHTNING, Texture.class);

        // 맵
        // assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        // assetManager.load(MAP_DUNGEON, TiledMap.class);

        // 사운드
        // assetManager.load(SOUND_CLICK, Sound.class);
        // assetManager.load(SOUND_SPELL_CAST, Sound.class);
        // assetManager.load(SOUND_DAMAGE, Sound.class);
        // assetManager.load(SOUND_VICTORY, Sound.class);

        // 폰트
        // assetManager.load(FONT_DEFAULT, BitmapFont.class);
        // assetManager.load(FONT_UI, BitmapFont.class);
    }

    public void finishLoading() {
        assetManager.finishLoading();

        // 자주 사용되는 에셋들을 미리 캐시
        if (assetManager.isLoaded(TEXTURE_ATLAS_GAME)) {
            gameAtlas = assetManager.get(TEXTURE_ATLAS_GAME, TextureAtlas.class);
        }
        if (assetManager.isLoaded(TEXTURE_ATLAS_UI)) {
            uiAtlas = assetManager.get(TEXTURE_ATLAS_UI, TextureAtlas.class);
        }
        if (assetManager.isLoaded(FONT_DEFAULT)) {
            defaultFont = assetManager.get(FONT_DEFAULT, BitmapFont.class);
        }
        if (assetManager.isLoaded(FONT_UI)) {
            uiFont = assetManager.get(FONT_UI, BitmapFont.class);
        }
    }

    public <T> T get(String fileName, Class<T> type) {
        return assetManager.get(fileName, type);
    }

    public boolean isLoaded(String fileName) {
        return assetManager.isLoaded(fileName);
    }

    public float getProgress() {
        return assetManager.getProgress();
    }

    public boolean update() {
        return assetManager.update();
    }

    public void dispose() {
        assetManager.dispose();
    }
}