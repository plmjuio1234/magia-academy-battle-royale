package com.magicbr.game.systems.skills;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.magicbr.game.entities.Player;
import com.magicbr.game.utils.Constants;

/**
 * 스킬이 발사하는 투사체
 * 화염구, 얼음창, 번개 등이 이에 해당
 */
public class Projectile {
    // 위치와 속도
    public Vector2 position;
    public Vector2 velocity;

    // 투사체 정보
    public int damage;
    public int ownerId;          // 누가 발사했는가
    public String skillName;
    public String elementColor;

    // 생명 주기
    public float lifetime;       // 남은 생명 시간 (초)
    public float maxLifetime;    // 최대 생명 시간
    public float radius;         // 충돌 반지름

    public boolean active;       // 활성 상태

    // DoT (지속 피해) 정보
    public int dotDamage;        // 초당 피해
    public float dotDuration;    // DoT 지속 시간

    /**
     * 투사체 생성자
     */
    public Projectile(float x, float y, float vx, float vy, int damage,
                      int ownerId, String skillName, String elementColor, float lifetime) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(vx, vy);
        this.damage = damage;
        this.ownerId = ownerId;
        this.skillName = skillName;
        this.elementColor = elementColor;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.radius = 10f;  // 기본 충돌 반지름
        this.active = true;
        this.dotDamage = 0;
        this.dotDuration = 0;
    }

    /**
     * 매 프레임마다 호출되어 위치 업데이트
     */
    public void update(float delta) {
        if (!active) return;

        // 위치 업데이트
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // 생명 시간 감소
        lifetime -= delta;
        if (lifetime <= 0) {
            active = false;
            return;
        }

        // 맵 경계 체크 (맵 밖으로 나가면 제거)
        if (position.x < 0 || position.x > Constants.MAP_WIDTH ||
            position.y < 0 || position.y > Constants.MAP_HEIGHT) {
            active = false;
        }
    }

    /**
     * 투사체 렌더링 (ParticleEffect 스타일의 시각적 효과)
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;

        // 생명 주기에 따른 밝기 조절 (끝나갈수록 어두워짐)
        float alphaFactor = lifetime / maxLifetime;

        // 원소 색상 가져오기
        Color baseColor = getColorFromElement();
        Color mainColor = new Color(baseColor.r, baseColor.g, baseColor.b, alphaFactor);
        Color brightColor = new Color(
            Math.min(baseColor.r + 0.3f, 1f),
            Math.min(baseColor.g + 0.3f, 1f),
            Math.min(baseColor.b + 0.3f, 1f),
            alphaFactor * 0.7f
        );

        // ============ 파티클 효과 (주변 작은 원들) ============
        // 시간에 따라 회전하는 작은 원 3개
        float rotation = (maxLifetime - lifetime) * 360f;
        for (int i = 0; i < 3; i++) {
            float angle = (rotation + i * 120f) * (float)Math.PI / 180f;
            float particleX = position.x + (float)Math.cos(angle) * (radius + 8f);
            float particleY = position.y + (float)Math.sin(angle) * (radius + 8f);

            Color particleColor = new Color(
                baseColor.r, baseColor.g, baseColor.b,
                alphaFactor * 0.5f
            );
            shapeRenderer.setColor(particleColor);
            shapeRenderer.circle(particleX, particleY, 3f);
        }

        // ============ 외부 빛 원 (Aura) ============
        shapeRenderer.setColor(brightColor);
        shapeRenderer.circle(position.x, position.y, radius + 8f);

        // ============ 중간 원 (Glow) ============
        Color glowColor = new Color(baseColor.r, baseColor.g, baseColor.b, alphaFactor * 0.5f);
        shapeRenderer.setColor(glowColor);
        shapeRenderer.circle(position.x, position.y, radius + 4f);

        // ============ 중앙 핵심 원 (Core) ============
        shapeRenderer.setColor(mainColor);
        shapeRenderer.circle(position.x, position.y, radius);

        // ============ 이동 방향 표시 (속도가 있을 때) ============
        float speed = velocity.len();
        if (speed > 0) {
            float arrowLength = radius * 2f;
            float arrowX = velocity.x / speed * arrowLength;
            float arrowY = velocity.y / speed * arrowLength;

            // 화살표 줄기
            Color arrowColor = new Color(mainColor.r, mainColor.g, mainColor.b, alphaFactor * 0.6f);
            shapeRenderer.setColor(arrowColor);
            shapeRenderer.line(
                position.x - arrowX * 0.3f, position.y - arrowY * 0.3f,
                position.x + arrowX * 0.7f, position.y + arrowY * 0.7f
            );

            // 화살표 끝부분 (작은 삼각형 표현 - 두 줄로)
            float arrowHeadSize = radius * 0.8f;
            float angle = (float)Math.atan2(arrowY, arrowX);
            float tipX = position.x + arrowX * 0.7f;
            float tipY = position.y + arrowY * 0.7f;

            shapeRenderer.line(
                tipX, tipY,
                tipX - (float)Math.cos(angle - 0.5f) * arrowHeadSize,
                tipY - (float)Math.sin(angle - 0.5f) * arrowHeadSize
            );
            shapeRenderer.line(
                tipX, tipY,
                tipX - (float)Math.cos(angle + 0.5f) * arrowHeadSize,
                tipY - (float)Math.sin(angle + 0.5f) * arrowHeadSize
            );
        }

        // ============ 사라지는 궤적 (Fade Trail) ============
        if (speed > 50) {  // 충분히 빠르게 움직일 때만
            float trailX = position.x - velocity.x * 0.016f;  // 1 프레임 이전 위치
            float trailY = position.y - velocity.y * 0.016f;

            Color trailColor = new Color(baseColor.r, baseColor.g, baseColor.b, alphaFactor * 0.2f);
            shapeRenderer.setColor(trailColor);
            shapeRenderer.circle(trailX, trailY, radius * 0.7f);
        }
    }

    /**
     * 플레이어와의 충돌 검사
     */
    public boolean checkCollision(Player player) {
        if (!active) return false;

        // 같은 사람의 투사체는 충돌하지 않음 (자기 자신)
        if (player.getId() == ownerId) return false;

        // 거리 계산
        float distance = Vector2.dst(position.x, position.y, player.getPosition().x, player.getPosition().y);

        // 반지름의 합이 거리보다 크면 충돌
        return distance <= (radius + 20f);  // 플레이어 반지름 20px
    }

    /**
     * 광역 충돌 검사 (AoE: Area of Effect)
     */
    public boolean checkCollisionAoE(float targetX, float targetY, float aoeRadius) {
        if (!active) return false;

        float distance = Vector2.dst(position.x, position.y, targetX, targetY);
        return distance <= aoeRadius;
    }

    /**
     * 원소 이름에 맞는 색상 반환
     */
    private Color getColorFromElement() {
        switch (elementColor) {
            case "불":
                return Color.RED;
            case "물":
                return Color.CYAN;
            case "바람":
                return Color.WHITE;
            case "땅":
                return new Color(0.6f, 0.4f, 0.2f, 1f);  // 갈색
            case "전기":
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }

    /**
     * DoT (지속 피해) 설정
     */
    public void setDot(int damage, float duration) {
        this.dotDamage = damage;
        this.dotDuration = duration;
    }

    /**
     * 투사체 반지름 설정 (기본: 10f)
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * 투사체 재사용 (오브젝트 풀용)
     */
    public void reset(float x, float y, float vx, float vy, int damage,
                      int ownerId, String skillName, String elementColor, float lifetime) {
        this.position.set(x, y);
        this.velocity.set(vx, vy);
        this.damage = damage;
        this.ownerId = ownerId;
        this.skillName = skillName;
        this.elementColor = elementColor;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.radius = 10f;
        this.active = true;
        this.dotDamage = 0;
        this.dotDuration = 0;
    }
}
