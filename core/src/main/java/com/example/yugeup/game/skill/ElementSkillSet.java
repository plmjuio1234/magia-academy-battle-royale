package com.example.yugeup.game.skill;

import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.fire.Fireball;
import com.example.yugeup.game.skill.fire.FlameWave;
import com.example.yugeup.game.skill.fire.Inferno;
import com.example.yugeup.game.skill.water.WaterShot;
import com.example.yugeup.game.skill.water.IceSpike;
import com.example.yugeup.game.skill.water.Flood;
import com.example.yugeup.game.skill.wind.AirSlash;
import com.example.yugeup.game.skill.wind.Tornado;
import com.example.yugeup.game.skill.wind.Storm;
import com.example.yugeup.game.skill.lightning.LightningBolt;
import com.example.yugeup.game.skill.lightning.ChainLightning;
import com.example.yugeup.game.skill.lightning.ThunderStorm;
import com.example.yugeup.game.skill.earth.RockSmash;
import com.example.yugeup.game.skill.earth.EarthSpike;
import com.example.yugeup.game.skill.earth.StoneShield;

import java.util.ArrayList;
import java.util.List;

/**
 * 원소 스킬 세트
 *
 * 하나의 원소에 속한 3개의 스킬을 관리합니다.
 * 각 원소는 고유한 스킬 A, B, C를 가집니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ElementSkillSet {
    private ElementType element;
    private ElementalSkill skillA;  // 첫 번째 스킬
    private ElementalSkill skillB;  // 두 번째 스킬
    private ElementalSkill skillC;  // 세 번째 스킬
    private Player owner;

    /**
     * 원소 스킬 세트 생성자
     *
     * @param element 원소 타입
     * @param owner 스킬 소유자
     */
    public ElementSkillSet(ElementType element, Player owner) {
        this.element = element;
        this.owner = owner;
        initializeSkills();
    }

    /**
     * 원소에 맞는 스킬 초기화
     *
     * 선택한 원소에 맞게 실제 스킬 클래스를 생성합니다.
     */
    private void initializeSkills() {
        String[] skillNames = element.getSkillNames();

        // 원소별 실제 스킬 생성
        switch (element) {
            case FIRE:
                this.skillA = new Fireball(owner);
                this.skillB = new FlameWave(owner);
                this.skillC = new Inferno(owner);
                break;

            case WATER:
                this.skillA = new WaterShot(owner);
                this.skillB = new IceSpike(owner);
                this.skillC = new Flood(owner);
                break;

            case WIND:
                this.skillA = new AirSlash(owner);
                this.skillB = new Tornado(owner);
                this.skillC = new Storm(owner);
                break;

            case LIGHTNING:
                this.skillA = new LightningBolt(owner);
                this.skillB = new ChainLightning(owner);
                this.skillC = new ThunderStorm(owner);
                break;

            case EARTH:
                this.skillA = new RockSmash(owner);
                this.skillB = new EarthSpike(owner);
                this.skillC = new StoneShield(owner);
                break;

            default:
                // 기본값: 더미 스킬
                this.skillA = createDummySkill(0, skillNames[0], owner);
                this.skillB = createDummySkill(1, skillNames[1], owner);
                this.skillC = createDummySkill(2, skillNames[2], owner);
                break;
        }

        System.out.println("[ElementSkillSet] " + element.getDisplayName() + " 원소 스킬 세트 초기화 완료");
        System.out.println("  - 스킬 A: " + skillNames[0]);
        System.out.println("  - 스킬 B: " + skillNames[1]);
        System.out.println("  - 스킬 C: " + skillNames[2]);
    }

    /**
     * 더미 스킬 생성 (기본값용)
     *
     * @param skillIndex 스킬 인덱스 (0=A, 1=B, 2=C)
     * @param skillName 스킬 이름
     * @param owner 소유자
     * @return 더미 ElementalSkill
     */
    private ElementalSkill createDummySkill(int skillIndex, String skillName, Player owner) {
        return new ElementalSkill(5000 + skillIndex, skillName, 10, 2.0f, 30, element, owner) {
            @Override
            public void cast(Player caster, Vector2 targetPosition) {
                System.out.println("[DummySkill] " + skillName + " 시전 (기본값)");
            }
        };
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
            default:
                System.err.println("[ElementSkillSet] 잘못된 슬롯 번호: " + slot);
                return null;
        }
    }

    /**
     * 모든 스킬 리스트 반환
     *
     * @return 스킬 A, B, C 리스트
     */
    public List<ElementalSkill> getAllSkills() {
        List<ElementalSkill> skills = new ArrayList<>();
        skills.add(skillA);
        skills.add(skillB);
        skills.add(skillC);
        return skills;
    }

    /**
     * 스킬 업데이트 (매 프레임)
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (skillA != null) skillA.update(delta);
        if (skillB != null) skillB.update(delta);
        if (skillC != null) skillC.update(delta);
    }

    // ===== Getters =====

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
