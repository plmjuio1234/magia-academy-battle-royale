package com.magicbr.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.utils.Constants;

public class Player {
    private Vector2 position;
    private Vector2 velocity;
    private float size;
    private int hp;
    private int mp;
    private int maxHp;
    private int maxMp;
    private int level;
    private int experience;
    private String selectedElement;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        size = Constants.PLAYER_SIZE;
        maxHp = Constants.PLAYER_MAX_HP;
        maxMp = Constants.PLAYER_MAX_MP;
        hp = maxHp;
        mp = maxMp;
        level = 1;
        experience = 0;
        selectedElement = "없음";
    }

    public void update(float delta) {
        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 맵 경계 제한
        float halfSize = size / 2f;
        if (position.x < halfSize) position.x = halfSize;
        if (position.x > Constants.MAP_WIDTH - halfSize) position.x = Constants.MAP_WIDTH - halfSize;
        if (position.y < halfSize) position.y = halfSize;
        if (position.y > Constants.MAP_HEIGHT - halfSize) position.y = Constants.MAP_HEIGHT - halfSize;

        // 속도 감소 (마찰)
        velocity.scl(0.9f);
    }

    public void render(ShapeRenderer shapeRenderer) {
        // 플레이어를 원으로 그리기
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(position.x, position.y, size / 2f);

        // 선택된 원소에 따른 테두리 색상
        Color elementColor = getElementColor();
        if (elementColor != null) {
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(elementColor);
            shapeRenderer.circle(position.x, position.y, size / 2f + 3f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        }
    }

    private Color getElementColor() {
        switch (selectedElement) {
            case "불": return Color.RED;
            case "물": return Color.CYAN;
            case "땅": return Color.BROWN;
            case "전기": return Color.YELLOW;
            case "바람": return Color.GREEN;
            default: return null;
        }
    }

    public void setVelocity(float vx, float vy) {
        velocity.set(vx, vy);
    }

    public void move(float deltaX, float deltaY) {
        velocity.add(deltaX * Constants.PLAYER_SPEED, deltaY * Constants.PLAYER_SPEED);
    }

    public void addExperience(int exp) {
        experience += exp;
        // 레벨업 체크 (간단한 공식)
        int requiredExp = level * 100;
        if (experience >= requiredExp) {
            experience -= requiredExp;
            level++;
            maxHp += 20;
            maxMp += 15;
            hp = maxHp;
            mp = maxMp;
        }
    }

    public void selectElement(String element) {
        this.selectedElement = element;
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public float getSize() { return size; }
    public int getHp() { return hp; }
    public int getMp() { return mp; }
    public int getMaxHp() { return maxHp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public String getSelectedElement() { return selectedElement; }

    // Setters
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    public void setMp(int mp) { this.mp = Math.max(0, Math.min(mp, maxMp)); }
}