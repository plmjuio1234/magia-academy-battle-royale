package com.magicbr.game.utils;

import java.util.Random;

public class GameTips {
    private static final String[] TIPS = {
        "📚 학원 지침: 서로 다른 원소를 융합하면 상급 마법을 시전할 수 있습니다!",
        "⚠️ 위험 경고: 마도 결계 밖의 마독 구름을 피하고 안전지대로 이동하세요!",
        "⚔️ 실전 조언: 마물을 토벌하면 마력 경험치와 마법 크리스탈을 획득합니다!",
        "🔥 원소 비법: 화염, 빙결, 대지, 뇌전, 질풍 각각의 특성을 파악하여 전투 스타일을 구축하세요!",
        "🎯 전술 지침: 다른 마도사들과 적절한 거리를 유지하며 기습과 견제를 활용하세요!",
        "💎 수집 요령: 필드의 마력 수정을 모아 강력한 원소 스킬을 해제하세요!",
        "🏆 승리 비결: 최후의 1인이 되어 마도학원 최강자의 타이틀을 획득하세요!"
    };

    private static final Random random = new Random();

    public static String getRandomTip() {
        return TIPS[random.nextInt(TIPS.length)];
    }
}