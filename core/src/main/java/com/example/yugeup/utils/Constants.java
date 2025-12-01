package com.example.yugeup.utils;

/**
 * 게임 전역 상수 정의
 *
 * 모든 게임 설정값, 밸런스 수치, 에셋 경로 등을 관리합니다.
 * 하드코딩을 방지하고 유지보수성을 높이기 위해 사용됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Constants {

    // ==================== 화면 설정 ====================

    /** 게임 화면 너비 (픽셀) - 모바일 가로 모드 */
    public static final int SCREEN_WIDTH = 2856;

    /** 게임 화면 높이 (픽셀) - 모바일 가로 모드 */
    public static final int SCREEN_HEIGHT = 1280;

    /** 게임 월드 스케일 */
    public static final float GAME_SCALE = 1.0f;

    /** 타겟 FPS */
    public static final int TARGET_FPS = 60;


    // ==================== 서버 설정 ====================

    /** 서버 호스트 주소 */
    public static final String SERVER_HOST = "219.254.146.234"; // 실제 로컬 IP로 변경 (에뮬레이터 + 실제 기기 모두 접속 가능)
    //public static final String SERVER_HOST = "10.0.2.2"; // 에뮬레이터 전용
    //public static final String SERVER_HOST = "127.0.0.1"; // 데스크톱 전용

    /** 서버 TCP 포트 */
    public static final int SERVER_PORT = 5000;

    /** 서버 연결 타임아웃 (밀리초) */
    public static final int CONNECTION_TIMEOUT = 5000;

    /** 네트워크 동기화 주기 (밀리초) - 서버 20Hz */
    public static final int NETWORK_TICK_RATE = 50;


    // ==================== 플레이어 설정 ====================

    /** 플레이어 이동 속도 (픽셀/초) */
    public static final float PLAYER_SPEED = 300f;

    /** 플레이어 최대 체력 */
    public static final int PLAYER_MAX_HP = 100;

    /** 플레이어 최대 마나 */
    public static final int PLAYER_MAX_MP = 50;

    /** 플레이어 기본 공격력 */
    public static final int PLAYER_BASE_ATK = 10;

    /** 플레이어 기본 방어력 */
    public static final int PLAYER_BASE_DEF = 5;

    /** 플레이어 충돌 반경 (픽셀) */
    public static final float PLAYER_COLLISION_RADIUS = 32f;

    /** 레벨당 능력치 증가량 - HP */
    public static final int STAT_HP_PER_LEVEL = 10;

    /** 레벨당 능력치 증가량 - MP */
    public static final int STAT_MP_PER_LEVEL = 5;

    /** 레벨당 능력치 증가량 - ATK */
    public static final int STAT_ATK_PER_LEVEL = 2;

    /** 레벨당 능력치 증가량 - DEF */
    public static final int STAT_DEF_PER_LEVEL = 1;


    // ==================== 레벨 & 경험치 설정 ====================

    /** 최대 레벨 */
    public static final int MAX_LEVEL = 20;

    /** 레벨 1에서 2로 가는데 필요한 경험치 */
    public static final int BASE_EXP_REQUIRED = 100;

    /** 레벨당 필요 경험치 증가 계수 */
    public static final float EXP_SCALE_FACTOR = 1.5f;


    // ==================== 몬스터 설정 ====================

    /** 고스트 체력 */
    public static final int GHOST_HP = 60;

    /** 고스트 공격력 */
    public static final int GHOST_ATK = 8;

    /** 고스트 이동 속도 (픽셀/초) */
    public static final float GHOST_SPEED = 75f;  // 150 → 75로 감소 (50%)

    /** 고스트 공격 범위 (픽셀) */
    public static final float GHOST_ATTACK_RANGE = 50f;

    /** 고스트 경험치 드랍 */
    public static final int GHOST_EXP_DROP = 50;


    /** 박쥐 체력 */
    public static final int BAT_HP = 30;

    /** 박쥐 공격력 */
    public static final int BAT_ATK = 6;

    /** 박쥐 이동 속도 (픽셀/초) */
    public static final float BAT_SPEED = 90f;  // 180 → 90으로 감소 (50%)

    /** 박쥐 공격 범위 (픽셀) */
    public static final float BAT_ATTACK_RANGE = 45f;

    /** 박쥐 경험치 드랍 */
    public static final int BAT_EXP_DROP = 25;


    /** 골렘 체력 */
    public static final int GOLEM_HP = 150;

    /** 골렘 공격력 */
    public static final int GOLEM_ATK = 15;

    /** 골렘 이동 속도 (픽셀/초) */
    public static final float GOLEM_SPEED = 40f;  // 80 → 40으로 감소 (50%)

    /** 골렘 공격 범위 (픽셀) */
    public static final float GOLEM_ATTACK_RANGE = 60f;

    /** 골렘 경험치 드랍 */
    public static final int GOLEM_EXP_DROP = 100;

    /** 골렘 크기 (픽셀) */
    public static final float GOLEM_SIZE = 64f;  // 기본 크기의 2배 (32 → 64)


    /** 몬스터 인식 범위 (픽셀) */
    public static final float MONSTER_DETECTION_RANGE = 300f;

    /** 몬스터 공격 쿨다운 (초) */
    public static final float MONSTER_ATTACK_COOLDOWN = 1.5f;


    // ==================== 스킬 설정 ====================

    // 매직 미사일 (기본 공격)
    /** 매직 미사일 데미지 */
    public static final int MAGIC_MISSILE_DAMAGE = 40;

    /** 매직 미사일 발사 속도 (픽셀/초) */
    public static final float MAGIC_MISSILE_SPEED = 400f;

    /** 매직 미사일 사거리 (픽셀) */
    public static final float MAGIC_MISSILE_RANGE = 500f;

    /** 매직 미사일 쿨다운 (초) */
    public static final float MAGIC_MISSILE_COOLDOWN = 0.5f;


    // ==================== 불 원소 스킬 (새 스펙) ====================

    // 파이어볼 (단일 직선 공격)
    /** 파이어볼 데미지 */
    public static final int FIREBALL_DAMAGE = 30;
    /** 파이어볼 마나 소모 */
    public static final int FIREBALL_MANA_COST = 10;
    /** 파이어볼 쿨다운 (초) */
    public static final float FIREBALL_COOLDOWN = 2.0f;
    /** 파이어볼 사거리 (픽셀) */
    public static final float FIREBALL_RANGE = 300f;
    /** 파이어볼 속도 (픽셀/초) */
    public static final float FIREBALL_SPEED = 200f;
    /** 파이어볼 히트박스 크기 (픽셀) */
    public static final float FIREBALL_HITBOX_SIZE = 12f;
    /** 파이어볼 이펙트 스케일 (렌더링용) */
    public static final float FIREBALL_SCALE = 4f;

    // 플레임 웨이브 (직선 투사체, 도트딜)
    /** 플레임 웨이브 데미지 (틱당) */
    public static final int FLAME_WAVE_DAMAGE = 50;
    /** 플레임 웨이브 마나 소모 */
    public static final int FLAME_WAVE_MANA_COST = 20;
    /** 플레임 웨이브 쿨다운 (초) */
    public static final float FLAME_WAVE_COOLDOWN = 5.0f;
    /** 플레임 웨이브 사거리 (픽셀) */
    public static final float FLAME_WAVE_RANGE = 400f;
    /** 플레임 웨이브 이동 속도 (픽셀/초) */
    public static final float FLAME_WAVE_SPEED = 200f;
    /** 플레임 웨이브 히트박스 크기 (픽셀) */
    public static final float FLAME_WAVE_HITBOX_SIZE = 16f;
    /** 플레임 웨이브 도트딜 간격 (초) */
    public static final float FLAME_WAVE_TICK_RATE = 0.3f;
    /** 플레임 웨이브 이펙트 스케일 (렌더링용) */
    public static final float FLAME_WAVE_SCALE = 4f;

    // 인페르노 (주변 범위 폭발) - Zone
    /** 인페르노 데미지 */
    public static final int INFERNO_DAMAGE = 100;
    /** 인페르노 마나 소모 */
    public static final int INFERNO_MANA_COST = 30;
    /** 인페르노 쿨다운 (초) */
    public static final float INFERNO_COOLDOWN = 10.0f;
    /** 인페르노 범위 (픽셀) - 렌더링 크기의 절반 */
    public static final float INFERNO_RANGE = 90f;
    /** 인페르노 히트박스 크기 (픽셀) */
    public static final float INFERNO_HITBOX_SIZE = 60f;
    /** 인페르노 이펙트 스케일 */
    public static final float INFERNO_SCALE = 3.0f;
    /** 인페르노 Y 오프셋 (플레이어 위로, 양수) */
    public static final float INFERNO_Y_OFFSET = 40f;


    // ==================== 원소 스킬 시스템 (PHASE_14-18) ====================

    // 물 원소 스킬
    // 워터샷 (직선 투사체, 파이어볼보다 느리지만 큰 히트박스)
    /** 워터 샷 데미지 */
    public static final int WATER_SHOT_DAMAGE = 25;
    /** 워터 샷 마나 소모 */
    public static final int WATER_SHOT_MANA_COST = 10;
    /** 워터 샷 쿨다운 (초) */
    public static final float WATER_SHOT_COOLDOWN = 1.5f;
    /** 워터 샷 발사 속도 (픽셀/초) - 파이어볼(200)보다 느림 */
    public static final float WATER_SHOT_SPEED = 150f;
    /** 워터 샷 사거리 (픽셀) */
    public static final float WATER_SHOT_RANGE = 500f;
    /** 워터 샷 히트박스 크기 (픽셀) */
    public static final float WATER_SHOT_HITBOX_SIZE = 24f;
    /** 워터 샷 이펙트 스케일 */
    public static final float WATER_SHOT_SCALE = 2.5f;

    // 아이스 스파이크 (3방향 발사, 빠른 속도)
    /** 아이스 스파이크 데미지 */
    public static final int ICE_SPIKE_DAMAGE = 35;
    /** 아이스 스파이크 마나 소모 */
    public static final int ICE_SPIKE_MANA_COST = 20;
    /** 아이스 스파이크 쿨다운 (초) */
    public static final float ICE_SPIKE_COOLDOWN = 3.0f;
    /** 아이스 스파이크 발사 속도 (픽셀/초) - 파이어볼(200)보다 빠름 */
    public static final float ICE_SPIKE_SPEED = 300f;
    /** 아이스 스파이크 사거리 (픽셀) */
    public static final float ICE_SPIKE_RANGE = 450f;
    /** 아이스 스파이크 히트박스 크기 (픽셀) */
    public static final float ICE_SPIKE_HITBOX_SIZE = 10f;
    /** 아이스 스파이크 이펙트 스케일 */
    public static final float ICE_SPIKE_SCALE = 3f;
    /** 아이스 스파이크 발사 각도 간격 (도) */
    public static final float ICE_SPIKE_ANGLE_SPREAD = 20f;

    // 플러드 (관통 소용돌이 투사체)
    /** 플러드 데미지 */
    public static final int FLOOD_DAMAGE = 45;
    /** 플러드 마나 소모 */
    public static final int FLOOD_MANA_COST = 30;
    /** 플러드 쿨다운 (초) */
    public static final float FLOOD_COOLDOWN = 5.0f;
    /** 플러드 발사 속도 (픽셀/초) - 느리게 이동하는 소용돌이 */
    public static final float FLOOD_SPEED = 80f;
    /** 플러드 사거리 (픽셀) */
    public static final float FLOOD_RANGE = 600f;
    /** 플러드 히트박스 너비 (픽셀) */
    public static final float FLOOD_HITBOX_WIDTH = 60f;
    /** 플러드 히트박스 높이 (픽셀) */
    public static final float FLOOD_HITBOX_HEIGHT = 90f;
    /** 플러드 이펙트 스케일 */
    public static final float FLOOD_SCALE = 3f;
    /** 플러드 도트딜 간격 (초) */
    public static final float FLOOD_TICK_RATE = 0.3f;

    // 바람 원소 스킬
    /** 에어 슬래시 데미지 */
    public static final int AIR_SLASH_DAMAGE = 30;
    /** 에어 슬래시 마나 소모 */
    public static final int AIR_SLASH_MANA_COST = 15;
    /** 에어 슬래시 쿨다운 (초) */
    public static final float AIR_SLASH_COOLDOWN = 2.0f;
    /** 에어 슬래시 근접 범위 (픽셀) - 24칸 */
    public static final float AIR_SLASH_MELEE_RANGE = 24f;
    /** 에어 슬래시 검기 사거리 (픽셀) */
    public static final float AIR_SLASH_RANGE = 350f;
    /** 에어 슬래시 검기 속도 (픽셀/초) */
    public static final float AIR_SLASH_SPEED = 200f;
    /** 에어 슬래시 검기 히트박스 너비 (픽셀) */
    public static final float AIR_SLASH_HITBOX_WIDTH = 24f;
    /** 에어 슬래시 검기 히트박스 높이 (픽셀) */
    public static final float AIR_SLASH_HITBOX_HEIGHT = 10f;
    /** 에어 슬래시 검기 스케일 */
    public static final float AIR_SLASH_SCALE = 3f;

    /** 토네이도 데미지 */
    public static final int TORNADO_DAMAGE = 40;
    /** 토네이도 마나 소모 */
    public static final int TORNADO_MANA_COST = 20;
    /** 토네이도 쿨다운 (초) */
    public static final float TORNADO_COOLDOWN = 3.5f;
    /** 토네이도 사거리 (픽셀) */
    public static final float TORNADO_RANGE = 500f;
    /** 토네이도 속도 (픽셀/초) */
    public static final float TORNADO_SPEED = 200f;
    /** 토네이도 히트박스 크기 (픽셀) */
    public static final float TORNADO_HITBOX_SIZE = 18f;
    /** 토네이도 스케일 */
    public static final float TORNADO_SCALE = 3f;

    /** 폭풍 데미지 (틱당) */
    public static final int STORM_DAMAGE = 50;
    /** 폭풍 마나 소모 */
    public static final int STORM_MANA_COST = 25;
    /** 폭풍 쿨다운 (초) */
    public static final float STORM_COOLDOWN = 4.0f;
    /** 폭풍 히트박스 크기 (픽셀) - 64x64 */
    public static final float STORM_HITBOX_SIZE = 64f;
    /** 폭풍 스케일 */
    public static final float STORM_SCALE = 2f;
    /** 폭풍 지속시간 (초) */
    public static final float STORM_DURATION = 8.0f;
    /** 폭풍 속도 증가 배수 (50% 증가) */
    public static final float STORM_SPEED_MULTIPLIER = 1.5f;
    /** 폭풍 도트딜 간격 (초) */
    public static final float STORM_TICK_RATE = 0.3f;

    // 번개 원소 스킬

    // 라이트닝 볼트 (단일 낙뢰 타겟팅) - Zone
    /** 라이트닝 볼트 데미지 (파이어볼의 70%) */
    public static final int LIGHTNING_BOLT_DAMAGE = 21;
    /** 라이트닝 볼트 마나 소모 */
    public static final int LIGHTNING_BOLT_MANA_COST = 10;
    /** 라이트닝 볼트 쿨다운 (초) */
    public static final float LIGHTNING_BOLT_COOLDOWN = 1.5f;
    /** 라이트닝 볼트 시전 거리 (픽셀) - 방향 지정기 끝 부분 */
    public static final float LIGHTNING_BOLT_TARGETING_RANGE = 100f;
    /** 라이트닝 볼트 히트박스 크기 (픽셀) - 렌더링과 동일 (64*1.5=96) */
    public static final float LIGHTNING_BOLT_HITBOX_SIZE = 96f;
    /** 라이트닝 볼트 스케일 (크기 축소) */
    public static final float LIGHTNING_BOLT_SCALE = 1.5f;

    // 체인 라이트닝 (단순 투사체 - 연쇄 제거)
    /** 체인 라이트닝 데미지 */
    public static final int CHAIN_LIGHTNING_DAMAGE = 25;
    /** 체인 라이트닝 마나 소모 */
    public static final int CHAIN_LIGHTNING_MANA_COST = 20;
    /** 체인 라이트닝 쿨다운 (초) */
    public static final float CHAIN_LIGHTNING_COOLDOWN = 3.0f;
    /** 체인 라이트닝 사거리 (픽셀) */
    public static final float CHAIN_LIGHTNING_RANGE = 300f;
    /** 체인 라이트닝 속도 (픽셀/초) */
    public static final float CHAIN_LIGHTNING_SPEED = 180f;
    /** 체인 라이트닝 히트박스 크기 (픽셀) - 렌더링과 동일 (16*2.5=40) */
    public static final float CHAIN_LIGHTNING_HITBOX_SIZE = 40f;
    /** 체인 라이트닝 스케일 */
    public static final float CHAIN_LIGHTNING_SCALE = 2.5f;

    // 썬더 스톰 (이동하는 구름 + 번개) - Zone
    /** 썬더 스톰 데미지 */
    public static final int THUNDER_STORM_DAMAGE = 50;
    /** 썬더 스톰 마나 소모 */
    public static final int THUNDER_STORM_MANA_COST = 30;
    /** 썬더 스톰 쿨다운 (초) */
    public static final float THUNDER_STORM_COOLDOWN = 5.0f;
    /** 썬더 스톰 구름 속도 (픽셀/초) */
    public static final float THUNDER_STORM_CLOUD_SPEED = 20f;
    /** 썬더 스톰 사거리 (픽셀) */
    public static final float THUNDER_STORM_RANGE = 200f;
    /** 썬더 스톰 구름-번개 오프셋 Y (픽셀) */
    public static final float THUNDER_STORM_CLOUD_OFFSET_Y = 60f;
    /** 썬더 스톰 번개 히트박스 가로 (픽셀) - 렌더링과 동일 (64*3.5=224) */
    public static final float THUNDER_STORM_LIGHTNING_HITBOX_WIDTH = 224f;
    /** 썬더 스톰 번개 히트박스 세로 (픽셀) - 렌더링과 동일 (64*1.8=115) */
    public static final float THUNDER_STORM_LIGHTNING_HITBOX_HEIGHT = 115f;
    /** 썬더 스톰 구름 스케일 Y (세로) - 살짝 축소 */
    public static final float THUNDER_STORM_CLOUD_SCALE_Y = 1.8f;
    /** 썬더 스톰 구름 스케일 X (가로) - 살짝 축소 */
    public static final float THUNDER_STORM_CLOUD_SCALE_X = 3.5f;
    /** 썬더 스톰 번개 스케일 Y (세로) - 살짝 축소 */
    public static final float THUNDER_STORM_LIGHTNING_SCALE_Y = 1.8f;
    /** 썬더 스톰 번개 스케일 X (가로) - 살짝 축소 */
    public static final float THUNDER_STORM_LIGHTNING_SCALE_X = 3.5f;

    // 땅 원소 스킬

    // 록 스매시 (가장 가까운 적 위에 돌 떨어뜨리기) - Zone
    /** 록 스매시 데미지 */
    public static final int ROCK_SMASH_DAMAGE = 60;
    /** 록 스매시 마나 소모 */
    public static final int ROCK_SMASH_MANA_COST = 15;
    /** 록 스매시 쿨다운 (초) */
    public static final float ROCK_SMASH_COOLDOWN = 2.5f;
    /** 록 스매시 히트박스 크기 (픽셀) - 48x48 */
    public static final float ROCK_SMASH_HITBOX_SIZE = 48f;
    /** 록 스매시 스케일 */
    public static final float ROCK_SMASH_SCALE = 2f;
    /** 록 스매시 타겟팅 범위 (픽셀) */
    public static final float ROCK_SMASH_TARGETING_RANGE = 300f;
    /** 록 스매시 체공 시간 (초) - 돌이 떨어지는 시간 */
    public static final float ROCK_SMASH_FALL_DURATION = 0.5f;
    /** 록 스매시 잔존 시간 (초) - 바닥에 떨어진 후 */
    public static final float ROCK_SMASH_LINGER_DURATION = 1.0f;

    // 어스 스파이크 (바닥에서 가시 솟아오르기) - 이동형 Zone
    /** 어스 스파이크 데미지 */
    public static final int EARTH_SPIKE_DAMAGE = 45;
    /** 어스 스파이크 마나 소모 */
    public static final int EARTH_SPIKE_MANA_COST = 18;
    /** 어스 스파이크 쿨다운 (초) */
    public static final float EARTH_SPIKE_COOLDOWN = 2.0f;
    /** 어스 스파이크 사거리 (픽셀) */
    public static final float EARTH_SPIKE_RANGE = 400f;
    /** 어스 스파이크 속도 (픽셀/초) */
    public static final float EARTH_SPIKE_SPEED = 250f;
    /** 어스 스파이크 히트박스 너비 (픽셀) */
    public static final float EARTH_SPIKE_HITBOX_WIDTH = 36f;
    /** 어스 스파이크 히트박스 높이 (픽셀) */
    public static final float EARTH_SPIKE_HITBOX_HEIGHT = 24f;
    /** 어스 스파이크 스케일 */
    public static final float EARTH_SPIKE_SCALE = 3f;

    // 스톤 실드 (피해 50% 감소) - Zone
    /** 스톤 실드 마나 소모 */
    public static final int STONE_SHIELD_MANA_COST = 20;
    /** 스톤 실드 쿨다운 (초) */
    public static final float STONE_SHIELD_COOLDOWN = 10.0f;
    /** 스톤 실드 데미지 감소율 (50% 감소) */
    public static final float STONE_SHIELD_DAMAGE_REDUCTION = 0.5f;
    /** 스톤 실드 지속시간 (초) */
    public static final float STONE_SHIELD_DURATION = 8.0f;
    /** 스톤 실드 스케일 */
    public static final float STONE_SHIELD_SCALE = 1.2f;


    // ==================== 원소 시스템 설정 (PHASE_13) ====================

    /** 원소 선택 다이얼로그 너비 */
    public static final float ELEMENT_DIALOG_WIDTH = 1200f;

    /** 원소 선택 다이얼로그 높이 */
    public static final float ELEMENT_DIALOG_HEIGHT = 800f;

    /** 원소 버튼 크기 */
    public static final float ELEMENT_BUTTON_SIZE = 200f;

    /** 원소 버튼 간격 */
    public static final float ELEMENT_BUTTON_SPACING = 30f;

    // ==================== 업그레이드 시스템 (PHASE_19 - 재설계) ====================

    /** 업그레이드 최대 레벨 (모든 업그레이드 공통) */
    public static final int MAX_UPGRADE_LEVEL = 5;

    // 스킬 강화
    /** 스킬 데미지 업그레이드 보너스 (고정값) */
    public static final int SKILL_DAMAGE_UPGRADE_BONUS = 5;  // 스킬 A: +5, B: +8, C: +12

    /** 스킬 쿨타임 업그레이드 감소율 */
    public static final float SKILL_COOLDOWN_UPGRADE_REDUCTION = 0.1f;  // 10% 감소

    // 스탯 강화
    /** 최대 체력 업그레이드 보너스 */
    public static final int STAT_HP_UPGRADE_BONUS = 30;

    /** 최대 마나 업그레이드 보너스 */
    public static final int STAT_MP_UPGRADE_BONUS = 20;

    /** 마나 재생 업그레이드 보너스 (초당) */
    public static final float STAT_MP_REGEN_UPGRADE_BONUS = 1.0f;  // +1 MP/초

    /** 공격력 업그레이드 보너스 */
    public static final int STAT_ATTACK_UPGRADE_BONUS = 5;

    /** 이동 속도 업그레이드 배율 */
    public static final float STAT_SPEED_UPGRADE_MULTIPLIER = 0.05f;  // 5% 증가

    // 기존 상수 (하위 호환성 유지)
    /** 스킬 최대 레벨 (구버전) */
    @Deprecated
    public static final int MAX_SKILL_LEVEL = 3;

    /** 스킬 업그레이드 비용 - 레벨 1→2 (구버전) */
    @Deprecated
    public static final int SKILL_UPGRADE_COST_LEVEL1 = 50;

    /** 스킬 업그레이드 비용 - 레벨 2→3 (구버전) */
    @Deprecated
    public static final int SKILL_UPGRADE_COST_LEVEL2 = 100;

    /** 스킬 업그레이드 비용 - 레벨 3→MAX (구버전) */
    @Deprecated
    public static final int SKILL_UPGRADE_COST_LEVEL3 = 200;

    /** 스킬 업그레이드 - 데미지 증가율 (구버전) */
    @Deprecated
    public static final float SKILL_UPGRADE_DAMAGE_INCREASE = 0.3f;  // 30%

    /** 스킬 업그레이드 - 범위 증가율 (구버전) */
    @Deprecated
    public static final float SKILL_UPGRADE_RANGE_INCREASE = 0.25f;  // 25%

    /** 스킬 업그레이드 - 쿨타임 감소율 (구버전) */
    @Deprecated
    public static final float SKILL_UPGRADE_COOLDOWN_REDUCTION = 0.2f;  // 20%


    // ==================== 맵 & 자기장 설정 ====================

    /** 게임 맵 너비 (픽셀) */
    public static final int MAP_WIDTH = 4000;

    /** 게임 맵 높이 (픽셀) */
    public static final int MAP_HEIGHT = 4000;

    /** 자기장 시작 대기 시간 (초) */
    public static final float ZONE_START_DELAY = 60f;

    /** 자기장 축소 속도 (픽셀/초) */
    public static final float ZONE_SHRINK_SPEED = 10f;

    /** 자기장 데미지 (초당) */
    public static final int ZONE_DAMAGE_PER_SECOND = 5;


    // ==================== Fog 시스템 설정 (PHASE_24) ====================

    /** Fog 활성화 간격 (초) - 2분 */
    public static final float FOG_ACTIVATION_INTERVAL = 120f;

    /** Fog 구역 개수 */
    public static final int FOG_ZONE_COUNT = 5;

    /** Fog 데미지 (초당) */
    public static final int FOG_DAMAGE_PER_SECOND = 5;

    /** Fog 구역 이름들 */
    public static final String[] FOG_ZONE_NAMES = {
        "town-square",    // 중앙 광장 (마지막 활성화)
        "dormitory",      // 기숙사
        "library",        // 도서관
        "classroom",      // 교실
        "alchemy-room"    // 연금술실
    };


    // ==================== 전투 설정 ====================

    /** 데미지 계산: 방어력 감소 계수 (DEF * 2 = 감소 데미지) */
    public static final int DEF_DAMAGE_REDUCTION = 2;

    /** 크리티컬 확률 (%) */
    public static final float CRITICAL_CHANCE = 10f;

    /** 크리티컬 데미지 배수 */
    public static final float CRITICAL_MULTIPLIER = 1.5f;


    // ==================== PVP 설정 ====================

    /** PVP 데미지 배율 (플레이어에게 가하는 데미지는 30% - 70% 감소) */
    public static final float PVP_DAMAGE_MULTIPLIER = 0.3f;

    /** 카메라 줌 레벨 (실제 화면 크기 계산용) */
    public static final float CAMERA_ZOOM = 0.3f;


    // ==================== 자동공격 (매직 미사일) 설정 ====================

    /** 자동공격 타겟팅 범위 (픽셀) - 세로 기준 + 여유 */
    public static final float AUTO_ATTACK_TARGETING_RANGE = 250f;

    /** 발사체 충돌 반경 (픽셀) - 발사체 크기(24px)와 동일 */
    public static final float PROJECTILE_COLLISION_RADIUS = 24f;

    /** 서버 공격 검증 거리 (픽셀) - 타겟팅 범위와 동일하게 */
    public static final float SERVER_ATTACK_VALIDATION_RANGE = 250f;

    /**
     * 자동공격 타겟팅 범위 반환
     *
     * @return 타겟팅 범위 (픽셀)
     */
    public static float getTargetingRange() {
        return AUTO_ATTACK_TARGETING_RANGE;
    }

    /**
     * 충돌 반경 (플레이어/몬스터 히트박스)
     * 플레이어 크기(128px)의 절반 정도가 적절함
     *
     * @return 충돌 반경 (픽셀)
     */
    public static float getCollisionRadius() {
        // 플레이어 크기 기준으로 적절한 충돌 반경 (64픽셀)
        return PLAYER_SIZE * 0.5f;
    }


    // ==================== UI 설정 ====================

    /** UI 패딩 (픽셀) */
    public static final int UI_PADDING = 20;

    /** 버튼 너비 (픽셀) */
    public static final int BUTTON_WIDTH = 200;

    /** 버튼 높이 (픽셀) */
    public static final int BUTTON_HEIGHT = 60;

    /** 스킬 버튼 크기 (픽셀) */
    public static final float SKILL_BUTTON_SIZE = 70f;


    // ==================== 에셋 경로 ====================

    /** 기본 폰트 경로 (Regular) */
    public static final String FONT_REGULAR_PATH = "fonts/HeirofLightRegular.ttf";

    /** 굵은 폰트 경로 (Bold) */
    public static final String FONT_BOLD_PATH = "fonts/HeirofLightBold.ttf";

    /** UI 스킨 경로 */
    public static final String UI_SKIN_PATH = "ui/uiskin.json";

    /** 캐릭터 스프라이트 경로 */
    public static final String CHARACTER_SPRITE_PATH = "character/character.png";

    /** 캐릭터 애니메이션 아틀라스 경로 */
    public static final String CHARACTER_ATLAS_PATH = "character/character.atlas";

    /** 버튼 아틀라스 경로 */
    public static final String BUTTON_ATLAS_PATH = "ui/button.atlas";

    /** 로딩 화면 배경 경로 */
    public static final String LOADING_BACKGROUND_PATH = "images/backgrounds/Loading.png";

    /** 메인 배경 경로 (500x226) */
    public static final String MAIN_BACKGROUND_PATH = "images/backgrounds/main_background500X226.png";

    /** 로비 배경 경로 (500x226) */
    public static final String LOBBY_BACKGROUND_PATH = "images/backgrounds/lobby_background500x226.png";

    /** 원소 속성 아틀라스 경로 (fire, water, rack, thunder, wind) */
    public static final String ELEMENTAL_ATLAS_PATH = "ui/elementals.atlas";

    /** 스킬 아이콘 아틀라스 경로 */
    public static final String SKILLS_ATLAS_PATH = "ui/skills.atlas";

    /** 게임 로고 경로 */
    public static final String LOGO_PATH = "images/backgrounds/Logo.png";


    // ==================== 게임 룰 ====================

    /** 최대 플레이어 수 */
    public static final int MAX_PLAYERS = 8;

    /** 최소 게임 시작 인원 */
    public static final int MIN_PLAYERS_TO_START = 2;

    /** 게임 최대 시간 (초) */
    public static final int MAX_GAME_TIME = 600;


    // ==================== 개발/디버그 설정 ====================

    /** 디버그 모드 활성화 */
    public static final boolean DEBUG_MODE = true;

    /** 충돌 박스 표시 */
    public static final boolean SHOW_COLLISION_BOXES = true;

    /** FPS 표시 */
    public static final boolean SHOW_FPS = true;

    /** 네트워크 로그 출력 */
    public static final boolean LOG_NETWORK = true;


    // ==================== 로딩 화면 설정 ====================

    /** 로딩 팁 목록 */
    public static final String[] LOADING_TIPS = {
        "팁: 몬스터를 처치하면 경험치를 획득할 수 있습니다!",
        "팁: 자기장 밖에 있으면 지속 데미지를 받습니다!",
        "팁: 레벨업하면 HP, MP, 공격력이 증가합니다!",
        "팁: 스킬은 원소별로 다른 특성을 가지고 있습니다!",
        "팁: 마지막 1명이 살아남으면 승리합니다!"
    };

    /** 로딩 최소 시간 (초) - 충분한 로딩 시간 확보 */
    public static final float LOADING_MIN_TIME = 5.0f;

    /**
     * 랜덤 로딩 팁을 반환합니다.
     *
     * @return 무작위 선택된 팁 텍스트
     */
    public static String getRandomTip() {
        int index = (int)(Math.random() * LOADING_TIPS.length);
        return LOADING_TIPS[index];
    }


    // ==================== 메인 메뉴 설정 ====================

    /** 게임 타이틀 */
    public static final String GAME_TITLE = "유급은 싫어";

    /** 메인 메뉴 버튼 너비 (원본 92px * 6.5배) */
    public static final float MENU_BUTTON_WIDTH = 598f;

    /** 메인 메뉴 버튼 높이 (원본 32px * 6.5배) */
    public static final float MENU_BUTTON_HEIGHT = 208f;

    /** 메인 메뉴 버튼 간격 */
    public static final float MENU_BUTTON_SPACING = 25f;

    /** 로고 Y 위치 */
    public static final float MENU_LOGO_Y = 880f;

    /** 로고 너비 (2:1 비율) */
    public static final float MENU_LOGO_WIDTH = 800f;

    /** 로고 높이 (2:1 비율) */
    public static final float MENU_LOGO_HEIGHT = 400f;

    /** 첫 번째 버튼 Y 위치 */
    public static final float MENU_FIRST_BUTTON_Y = 645f;

    /** 화면 전환 애니메이션 시간 (초) */
    public static final float SCREEN_TRANSITION_DURATION = 1.0f;


    // ==================== 로비 화면 설정 ====================

    /** 로비 배경 스케일 (원본 500x226을 화면에 맞춤) */
    public static final float LOBBY_BACKGROUND_SCALE = SCREEN_WIDTH / 500f;

    /** 로비 패널 여백 (좌우) */
    public static final float LOBBY_PANEL_MARGIN = 80f;

    /** 로비 패널 상단 여백 */
    public static final float LOBBY_PANEL_TOP_MARGIN = 150f;

    /** 로비 패널 하단 여백 */
    public static final float LOBBY_PANEL_BOTTOM_MARGIN = 150f;

//    /** 방 목록 패널 X 위치 (UI 디버거로 조정됨) */
//    public static final float LOBBY_ROOM_LIST_X = 586.0f;
//
//    /** 방 목록 패널 Y 위치 (UI 디버거로 조정됨) */
//    public static final float LOBBY_ROOM_LIST_Y = 419.0f;
//
//    /** 방 목록 패널 너비 (UI 디버거로 조정됨) */
//    public static final float LOBBY_ROOM_LIST_WIDTH = 751.0f;
//
//    /** 방 목록 패널 높이 (UI 디버거로 조정됨) */
//    public static final float LOBBY_ROOM_LIST_HEIGHT = 640.0f;

    /** 방 목록 패널 X 위치 */
    public static final float LOBBY_ROOM_LIST_X = 586f;

    /** 방 목록 패널 Y 위치 */
    public static final float LOBBY_ROOM_LIST_Y = 419f;

    /** 방 목록 패널 너비 */
    public static final float LOBBY_ROOM_LIST_WIDTH = 900f; // 너비 1200

    /** 방 목록 패널 높이 */
    public static final float LOBBY_ROOM_LIST_HEIGHT = 800f; // 높이 800

    /** 캐릭터 프리뷰 패널 X 위치 (UI 디버거로 조정됨) */
    public static final float LOBBY_CHARACTER_PREVIEW_X = 1693.0f;

    /** 캐릭터 프리뷰 패널 Y 위치 (UI 디버거로 조정됨) */
    public static final float LOBBY_CHARACTER_PREVIEW_Y = 178.0f;

    /** 캐릭터 프리뷰 패널 너비 (UI 디버거로 조정됨) */
    public static final float LOBBY_CHARACTER_PREVIEW_WIDTH = 995.0f;

    /** 캐릭터 프리뷰 패널 높이 (UI 디버거로 조정됨) */
    public static final float LOBBY_CHARACTER_PREVIEW_HEIGHT = 778.0f;

    /** 방 목록 테이블 행 높이 */
    public static final float LOBBY_TABLE_ROW_HEIGHT = 60f;

    /** 방 목록 최대 표시 개수 */
    public static final int LOBBY_MAX_VISIBLE_ROOMS = 12;

    /** 기본 닉네임 접두사 */
    public static final String DEFAULT_NICKNAME_PREFIX = "Player";

    /** 로비 버튼 너비 */
    public static final float LOBBY_BUTTON_WIDTH = 200f;

    /** 로비 버튼 높이 */
    public static final float LOBBY_BUTTON_HEIGHT = 60f;

    /** 방 목록 새로고침 주기 (초) */
    public static final float LOBBY_REFRESH_INTERVAL = 5.0f;


    // ==================== 대기실 화면 설정 ====================

    /** 대기실 배경 경로 */
    public static final String WAITROOM_BACKGROUND_PATH = "images/backgrounds/waitroom_background500x226.png";

    /** 대기실 배경 스케일 (원본 500x226을 화면에 맞춤) */
    public static final float WAITROOM_BACKGROUND_SCALE = SCREEN_WIDTH / 500f;

    // 플레이어 슬롯 (상단 4칸) - 수동 조절 가능
    // 배경 이미지 500x226 기준, 실제 측정값
    /** 배경 스케일 비율 */
    private static final float BG_SCALE = SCREEN_WIDTH / 500f;  // 5.712

    // ========== 여기서 값을 조절하세요! ==========
    /** 플레이어 슬롯 X 오프셋 (배경 이미지 기준 픽셀, 조절 가능) */
    public static final float WAITROOM_SLOT_OFFSET_X = 63.0f;

    /** 플레이어 슬롯 Y 오프셋 (배경 이미지 기준 픽셀, 조절 가능) */
    public static final float WAITROOM_SLOT_OFFSET_Y = 9.0f;

    /** 플레이어 슬롯 너비 (배경 이미지 기준 픽셀) */
    public static final float WAITROOM_SLOT_WIDTH_PX = 79.0f;

    /** 플레이어 슬롯 높이 (배경 이미지 기준 픽셀) */
    public static final float WAITROOM_SLOT_HEIGHT_PX = 89.0f;

    /** 플레이어 슬롯 간격 (배경 이미지 기준 픽셀) */
    public static final float WAITROOM_SLOT_SPACING_PX = 21.0f;
    // =========================================

    /** 플레이어 슬롯 너비 (화면 좌표) */
    public static final float WAITROOM_SLOT_WIDTH = WAITROOM_SLOT_WIDTH_PX * BG_SCALE;

    /** 플레이어 슬롯 높이 (화면 좌표) */
    public static final float WAITROOM_SLOT_HEIGHT = WAITROOM_SLOT_HEIGHT_PX * BG_SCALE;

    /** 플레이어 슬롯 간격 (화면 좌표) */
    public static final float WAITROOM_SLOT_SPACING = WAITROOM_SLOT_SPACING_PX * BG_SCALE;

    /** 플레이어 슬롯 시작 X (화면 좌표) */
    public static final float WAITROOM_SLOT_START_X = WAITROOM_SLOT_OFFSET_X * BG_SCALE;

    /** 플레이어 슬롯 Y 위치 (화면 좌표) */
    public static final float WAITROOM_SLOT_Y = SCREEN_HEIGHT - (WAITROOM_SLOT_OFFSET_Y * BG_SCALE) - WAITROOM_SLOT_HEIGHT;

    /** 플레이어 캐릭터 스프라이트 크기 */
    public static final float WAITROOM_CHARACTER_SIZE = 180f;

    // 채팅 영역 (좌측 하단) - 이미지 3번 픽셀 측정값 기준
    // ========== 여기서 값을 조절하세요! ==========
    /** 채팅 영역 X 오프셋 (배경 이미지 기준 픽셀) */
    public static final float WAITROOM_CHAT_OFFSET_X = 64.0f;

    /** 채팅 영역 Y 오프셋 (배경 이미지 기준, 하단에서 픽셀) */
    public static final float WAITROOM_CHAT_OFFSET_Y = 12.0f;

    /** 채팅 영역 너비 (배경 이미지 기준 픽셀) - 약 285px */
    public static final float WAITROOM_CHAT_WIDTH_PX = 288.0f;

    /** 채팅 영역 높이 (배경 이미지 기준 픽셀) - 약 120px */
    public static final float WAITROOM_CHAT_HEIGHT_PX = 100.0f;
    // =========================================

    /** 채팅 영역 X 위치 (화면 좌표) */
    public static final float WAITROOM_CHAT_X = WAITROOM_CHAT_OFFSET_X * BG_SCALE;

    /** 채팅 영역 Y 위치 (화면 좌표) */
    public static final float WAITROOM_CHAT_Y = WAITROOM_CHAT_OFFSET_Y * BG_SCALE;

    /** 채팅 영역 너비 (화면 좌표) */
    public static final float WAITROOM_CHAT_WIDTH = WAITROOM_CHAT_WIDTH_PX * BG_SCALE;

    /** 채팅 영역 높이 (화면 좌표) */
    public static final float WAITROOM_CHAT_HEIGHT = WAITROOM_CHAT_HEIGHT_PX * BG_SCALE;

    /** 채팅 입력창 높이 */
    public static final float WAITROOM_CHAT_INPUT_HEIGHT = 80f;

    /** 채팅 메시지 표시 높이 */
    public static final float WAITROOM_CHAT_MESSAGE_HEIGHT = WAITROOM_CHAT_HEIGHT - WAITROOM_CHAT_INPUT_HEIGHT;

    // 버튼 영역 (우측 하단) - 수동 조절 가능
    // ========== 여기서 값을 조절하세요! ==========
    /** 버튼 영역 X 오프셋 (배경 이미지 기준 픽셀) */
    public static final float WAITROOM_BUTTON_OFFSET_X = 370.0f;  // 360 → 370으로 오른쪽 이동

    /** 버튼 영역 Y 오프셋 (배경 이미지 기준, 하단에서 픽셀) */
    public static final float WAITROOM_BUTTON_OFFSET_Y = 35.0f;  // 50 → 35로 살짝 아래로 이동

    /** 버튼 스케일 배율 */
    public static final float WAITROOM_BUTTON_SCALE = 5f;

    /** 방 나가기 버튼 스케일 배율 */
    public static final float WAITROOM_EXIT_BUTTON_SCALE = 4f;

    /** 버튼 간격 (픽셀) */
    public static final float WAITROOM_BUTTON_SPACING_PX = 20f;
    // =========================================

    /** 버튼 영역 X 위치 (화면 좌표) */
    public static final float WAITROOM_BUTTON_X = WAITROOM_BUTTON_OFFSET_X * BG_SCALE;

    /** 버튼 영역 Y 위치 (화면 좌표) */
    public static final float WAITROOM_BUTTON_Y = WAITROOM_BUTTON_OFFSET_Y * BG_SCALE;

    /** 버튼 너비 (아틀라스 원본 92px * 스케일) */
    public static final float WAITROOM_BUTTON_WIDTH = 92f * WAITROOM_BUTTON_SCALE;

    /** 버튼 높이 (아틀라스 원본 32px * 스케일) */
    public static final float WAITROOM_BUTTON_HEIGHT = 32f * WAITROOM_BUTTON_SCALE;

    /** 방 나가기 버튼 크기 (64x64 * 스케일) */
    public static final float WAITROOM_EXIT_BUTTON_SIZE = 64f * WAITROOM_EXIT_BUTTON_SCALE;

    /** 버튼 간격 */
    public static final float WAITROOM_BUTTON_SPACING = WAITROOM_BUTTON_SPACING_PX;

    // ===== 게임 맵 설정 (PHASE_07) =====

    /** 게임 맵 너비 (화면과 동일) */
    public static final float GAME_MAP_WIDTH = SCREEN_WIDTH;

    /** 게임 맵 높이 (화면과 동일) */
    public static final float GAME_MAP_HEIGHT = SCREEN_HEIGHT;

    /** 중앙 구역 너비 (맵의 약 40%) */
    public static final float CENTER_ZONE_WIDTH = GAME_MAP_WIDTH * 0.4f;

    /** 중앙 구역 높이 (맵의 약 40%) */
    public static final float CENTER_ZONE_HEIGHT = GAME_MAP_HEIGHT * 0.4f;

    /** 중앙 구역 X 좌표 (중앙 정렬) */
    public static final float CENTER_ZONE_X = (GAME_MAP_WIDTH - CENTER_ZONE_WIDTH) / 2f;

    /** 중앙 구역 Y 좌표 (중앙 정렬) */
    public static final float CENTER_ZONE_Y = (GAME_MAP_HEIGHT - CENTER_ZONE_HEIGHT) / 2f;

    /** 플레이어 초기 스폰 위치 X (중앙 구역 중심) */
    public static final float PLAYER_SPAWN_X = GAME_MAP_WIDTH / 2f;

    /** 플레이어 초기 스폰 위치 Y (중앙 구역 중심) */
    public static final float PLAYER_SPAWN_Y = GAME_MAP_HEIGHT / 2f;

    /** 플레이어 이동 속도 (픽셀/초) */
    public static final float PLAYER_MOVE_SPEED = 150f;  // 300 → 150 감소 (이동속도 더 느리게)

    /** 플레이어 스프라이트 크기 (화면에 표시되는 크기) */
    public static final float PLAYER_SIZE = 128f;

    /** 플레이어 애니메이션 프레임 시간 (초) */
    public static final float PLAYER_ANIMATION_FRAME_TIME = 0.15f;

    // ===== 조이스틱 설정 (PHASE_08) =====

    /** 조이스틱 반경 (화면 너비의 6%) */
    public static final float JOYSTICK_RADIUS = SCREEN_WIDTH * 0.06f;

    /** 조이스틱 배경 반경 (실제 반경의 1.2배) */
    public static final float JOYSTICK_BG_RADIUS = JOYSTICK_RADIUS * 1.2f;

    /** 조이스틱 스틱 반경 (실제 반경의 0.5배) */
    public static final float JOYSTICK_STICK_RADIUS = JOYSTICK_RADIUS * 0.5f;

    /** 조이스틱 X 위치 (왼쪽 여백 증가, 20px → 100px) */
    public static final float JOYSTICK_X = JOYSTICK_BG_RADIUS + 100f;

    /** 조이스틱 Y 위치 (아래쪽 여백 증가, 20px → 100px) */
    public static final float JOYSTICK_Y = JOYSTICK_BG_RADIUS + 100f;

    /** 조이스틱 데드존 (입력 인식 최소값, 0~1 범위) */
    public static final float JOYSTICK_DEADZONE = 0.2f;

    /** 조이스틱 터치 범위 여유 (반경의 1.5배) */
    public static final float JOYSTICK_TOUCH_RANGE = 1.5f;

    /** 플레이어 위치 동기화 주기 (초) */
    public static final float PLAYER_SYNC_INTERVAL = 0.1f;


    /**
     * Private 생성자 - 인스턴스 생성 방지
     */
    private Constants() {
        throw new AssertionError("Constants 클래스는 인스턴스화할 수 없습니다.");
    }
}
