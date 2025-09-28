package com.magicbr.game.utils;

public class Constants {
    // 화면 크기
    public static final int VIRTUAL_WIDTH = 1920;
    public static final int VIRTUAL_HEIGHT = 1080;
    public static final int MIN_WIDTH = 480;
    public static final int MIN_HEIGHT = 270;

    // 게임 설정
    public static final int MAX_PLAYERS = 4;
    public static final int GAME_DURATION_MINUTES = 10;
    public static final float TICK_RATE = 20f;
    public static final float NETWORK_UPDATE_RATE = 0.05f; // 50ms

    // 맵 설정
    public static final int MAP_WIDTH = 1920;
    public static final int MAP_HEIGHT = 1920;
    public static final int TILE_SIZE = 32;

    // 플레이어 설정
    public static final float PLAYER_SPEED = 200f;
    public static final float PLAYER_SIZE = 64f;
    public static final int PLAYER_MAX_HP = 100;
    public static final int PLAYER_MAX_MP = 100;

    // 던전 축소 설정
    public static final float PHASE_1_TIME = 3f * 60f; // 3분
    public static final float PHASE_2_TIME = 6f * 60f; // 6분
    public static final float PHASE_3_TIME = 8f * 60f; // 8분
    public static final int POISON_DAMAGE_1 = 5;
    public static final int POISON_DAMAGE_2 = 10;
    public static final int POISON_DAMAGE_3 = 20;

    // UI 설정
    public static final float JOYSTICK_SIZE = 250f; // 더 큰 조이스틱
    public static final float SKILL_BUTTON_SIZE = 80f;
    public static final float HUD_PADDING = 60f; // 화면 가장자리에서 더 안쪽으로

    // 애니메이션 설정
    public static final float ANIMATION_FRAME_DURATION = 0.1f;
    public static final float DAMAGE_TEXT_DURATION = 1.5f;
    public static final float PARTICLE_DURATION = 2f;

    // 네트워크 설정
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int DEFAULT_SERVER_PORT = 54555;
    public static final int NETWORK_TIMEOUT = 5000;

    // 캐릭터 클래스
    public enum CharacterClass {
        ELEMENTALIST,
        SUMMONER,
        ENCHANTER,
        NECROMANCER
    }

    // 스킬 타입
    public enum SkillType {
        DAMAGE,
        HEAL,
        BUFF,
        DEBUFF,
        SUMMON
    }
}