package com.example.yugeup.game.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * 레벨업 이펙트
 *
 * 레벨업 시 재생되는 시각적 효과입니다.
 * 금색 파티클이 사방으로 퍼지며 중력에 의해 떨어집니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LevelUpEffect {
    private Vector2 position;
    private float lifetime;
    private float maxLifetime = 2.0f;  // 2초
    private boolean isAlive;

    // 파티클 효과
    private List<Particle> particles = new ArrayList<>();
    private Color color = new Color(1f, 1f, 0f, 1f);  // 금색

    // 1x1 흰색 픽셀 텍스처
    private static Texture whitePixel;

    static {
        // 1x1 흰색 텍스처 생성
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * 생성자
     *
     * @param position 이펙트 시작 위치
     */
    public LevelUpEffect(Vector2 position) {
        this.position = new Vector2(position);
        this.lifetime = 0f;
        this.isAlive = true;

        // 파티클 생성
        createParticles();
    }

    /**
     * 파티클 생성
     */
    private void createParticles() {
        for (int i = 0; i < 20; i++) {
            float angle = (float)Math.random() * 360f;
            float speed = 100f + (float)Math.random() * 100f;
            particles.add(new Particle(position, angle, speed));
        }
    }

    /**
     * 업데이트
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        lifetime += delta;

        // 파티클 업데이트
        for (Particle particle : particles) {
            particle.update(delta);
        }

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        // 알파값 감소 (페이드 아웃)
        float alpha = 1.0f - (lifetime / maxLifetime);
        color.a = alpha;

        batch.setColor(color);

        // 파티클 렌더링
        for (Particle particle : particles) {
            particle.render(batch);
        }

        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() {
        return isAlive;
    }

    /**
     * 파티클 클래스
     */
    private static class Particle {
        Vector2 position;
        Vector2 velocity;
        float lifetime;

        public Particle(Vector2 origin, float angle, float speed) {
            this.position = new Vector2(origin);
            this.velocity = new Vector2(
                (float)Math.cos(Math.toRadians(angle)) * speed,
                (float)Math.sin(Math.toRadians(angle)) * speed
            );
            this.lifetime = 0f;
        }

        public void update(float delta) {
            position.add(velocity.x * delta, velocity.y * delta);
            lifetime += delta;

            // 중력 효과
            velocity.y -= 500f * delta;
        }

        public void render(SpriteBatch batch) {
            // 작은 사각형으로 렌더링
            batch.draw(whitePixel, position.x, position.y, 4, 4);
        }
    }
}
