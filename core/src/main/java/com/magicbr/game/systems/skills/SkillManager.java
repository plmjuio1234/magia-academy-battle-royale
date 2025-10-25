package com.magicbr.game.systems.skills;

import com.magicbr.game.entities.Player;
import com.magicbr.game.systems.skills.fire.Fireball;
import com.magicbr.game.systems.skills.fire.Explosion;
import com.magicbr.game.systems.skills.fire.LavaBurst;
import com.magicbr.game.systems.skills.water.Icicle;
import com.magicbr.game.systems.skills.water.WaterStream;
import com.magicbr.game.systems.skills.water.HealingWater;
import com.magicbr.game.systems.skills.wind.Tornado;
import com.magicbr.game.systems.skills.wind.Blink;
import com.magicbr.game.systems.skills.wind.WindBlade;
import com.magicbr.game.systems.skills.earth.RockThrow;
import com.magicbr.game.systems.skills.earth.StoneShield;
import com.magicbr.game.systems.skills.earth.Earthquake;
import com.magicbr.game.systems.skills.electric.LightningBolt;
import com.magicbr.game.systems.skills.electric.Shock;
import com.magicbr.game.systems.skills.electric.ChainLightning;

import java.util.HashMap;
import java.util.Map;

/**
 * 플레이어의 스킬을 관리하는 클래스
 * - 선택한 원소에 따라 3개의 스킬 제공
 * - 플레이어 레벨에 따라 스킬 활성화
 * - 스킬 쿨다운 관리
 * - 스킬 시전 처리
 */
public class SkillManager {
    private Player player;
    private Skill[] currentSkills;  // 현재 선택한 원소의 스킬 3개
    private ProjectilePool projectilePool;

    // 모든 원소의 스킬 풀
    private Map<String, Skill[]> skillsByElement = new HashMap<>();

    public SkillManager(Player player, ProjectilePool projectilePool) {
        this.player = player;
        this.projectilePool = projectilePool;
        this.currentSkills = new Skill[3];

        // 각 원소별 스킬 등록
        registerSkills();

        // 초기 원소는 "불"
        setElementSkills("불");
    }

    /**
     * 모든 원소의 스킬을 등록
     */
    private void registerSkills() {
        // 불 원소
        Skill[] fireSkills = new Skill[]{
            new Fireball(),
            new Explosion(),
            new LavaBurst()
        };
        for (Skill skill : fireSkills) {
            skill.setProjectilePool(projectilePool);
        }
        skillsByElement.put("불", fireSkills);

        // 물 원소
        Skill[] waterSkills = new Skill[]{
            new Icicle(),
            new WaterStream(),
            new HealingWater()
        };
        for (Skill skill : waterSkills) {
            skill.setProjectilePool(projectilePool);
        }
        skillsByElement.put("물", waterSkills);

        // 바람 원소
        Skill[] windSkills = new Skill[]{
            new Tornado(),
            new Blink(),
            new WindBlade()
        };
        for (Skill skill : windSkills) {
            skill.setProjectilePool(projectilePool);
        }
        skillsByElement.put("바람", windSkills);

        // 땅 원소
        Skill[] earthSkills = new Skill[]{
            new RockThrow(),
            new StoneShield(),
            new Earthquake()
        };
        for (Skill skill : earthSkills) {
            skill.setProjectilePool(projectilePool);
        }
        skillsByElement.put("땅", earthSkills);

        // 전기 원소
        Skill[] electricSkills = new Skill[]{
            new LightningBolt(),
            new Shock(),
            new ChainLightning()
        };
        for (Skill skill : electricSkills) {
            skill.setProjectilePool(projectilePool);
        }
        skillsByElement.put("전기", electricSkills);
    }

    /**
     * 원소 변경 시 현재 스킬 세트 변경
     */
    public void setElementSkills(String element) {
        Skill[] skills = skillsByElement.get(element);
        if (skills != null) {
            currentSkills = skills;
            System.out.println("[스킬] 원소 변경: " + element);
        }
    }

    /**
     * 매 프레임마다 호출되어 모든 스킬의 쿨다운 업데이트
     */
    public void update(float delta) {
        for (Skill skill : currentSkills) {
            if (skill != null) {
                skill.update(delta);
            }
        }
    }

    /**
     * 지정된 인덱스(0-2)의 스킬 시전
     */
    public boolean castSkill(int skillIndex, float targetX, float targetY) {
        if (skillIndex < 0 || skillIndex >= 3) {
            System.err.println("[스킬] 잘못된 스킬 인덱스: " + skillIndex);
            return false;
        }

        Skill skill = currentSkills[skillIndex];
        
        // 스킬이 레벨로 인해 아직 활성화되지 않은 경우
        if (skill == null || !isSkillAvailable(skillIndex)) {
            System.out.println("[스킬] 스킬 " + (skillIndex + 1) + "은(는) 아직 활성화되지 않았습니다.");
            return false;
        }

        // 스킬 시전 가능 여부 확인
        if (!skill.canCast(player)) {
            String reason = skill.isOnCooldown() ? "쿨다운 중" : "MP 부족";
            System.out.println("[스킬] " + skill.getSkillName() + " 시전 불가 (" + reason + ")");
            return false;
        }

        // MP 소비
        player.setMp(player.getMp() - skill.getManaCost());

        // 스킬 실행 (각 스킬의 execute() 메서드)
        skill.execute(player, targetX, targetY);

        // 쿨다운 시작
        skill.resetCooldown();

        System.out.println("[스킬] " + skill.getSkillName() + " 시전! (남은 MP: " + player.getMp() + ")");
        return true;
    }

    /**
     * 플레이어 레벨에 따라 활성화된 스킬만 반환
     * 레벨 1 = 스킬 1개 (인덱스 0)
     * 레벨 2 = 스킬 2개 (인덱스 0, 1)
     * 레벨 3+ = 스킬 3개 (인덱스 0, 1, 2)
     */
    public Skill[] getSkillsForLevel(int level) {
        int skillCount = Math.min(level, 3);
        Skill[] availableSkills = new Skill[skillCount];
        
        for (int i = 0; i < skillCount; i++) {
            availableSkills[i] = currentSkills[i];
        }
        
        return availableSkills;
    }

    /**
     * 특정 스킬 인덱스가 현재 레벨에서 활성화되었는지 확인
     */
    public boolean isSkillAvailable(int skillIndex) {
        int playerLevel = player.getLevel();
        return skillIndex < Math.min(playerLevel, 3);
    }

    /**
     * 현재 스킬 배열 반환
     */
    public Skill[] getCurrentSkills() {
        return currentSkills;
    }

    /**
     * 특정 인덱스의 스킬 반환
     */
    public Skill getSkill(int index) {
        if (index >= 0 && index < 3 && isSkillAvailable(index)) {
            return currentSkills[index];
        }
        return null;
    }

    /**
     * 투사체 풀 반환 (GameScreen에서 렌더링 등에 사용)
     */
    public ProjectilePool getProjectilePool() {
        return projectilePool;
    }
}
