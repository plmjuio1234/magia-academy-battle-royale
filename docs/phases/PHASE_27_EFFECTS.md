# PHASE_27_EFFECTS.md - 스킬 이펙트 및 파티클 시스템

---

## 🎯 목표
스킬 이펙트 및 파티클 시스템 구현

---

## 📋 구현 범위

- ✅ 파티클 시스템
- ✅ 스킬별 이펙트
- ✅ 사운드 효과 (향후)
- ✅ 카메라 쉐이크

---

## 🔧 구현 가이드

### 1. ParticleEffect 관리

```java
public class EffectManager {
    private static EffectManager instance;
    private List<ParticleEffect> activeEffects;

    public void addEffect(ParticleEffect effect, Vector2 position) {
        effect.setPosition(position.x, position.y);
        effect.start();
        activeEffects.add(effect);
    }

    public void update(float delta) {
        Iterator<ParticleEffect> iter = activeEffects.iterator();
        while (iter.hasNext()) {
            ParticleEffect effect = iter.next();
            effect.update(delta);

            if (effect.isComplete()) {
                effect.dispose();
                iter.remove();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (ParticleEffect effect : activeEffects) {
            effect.draw(batch);
        }
    }
}
```

### 2. 카메라 쉐이크

```java
public class CameraShake {
    private float shakeTime;
    private float shakePower;

    public void shake(float power, float duration) {
        this.shakePower = power;
        this.shakeTime = duration;
    }

    public void update(float delta, Camera camera) {
        if (shakeTime > 0) {
            float offsetX = (float) (Math.random() - 0.5f) * shakePower;
            float offsetY = (float) (Math.random() - 0.5f) * shakePower;

            camera.translate(offsetX, offsetY, 0);
            shakeTime -= delta;
        }
    }
}
```

---

## ✅ 완료 조건

- [ ] ParticleEffect 시스템
- [ ] 스킬 이펙트 추가
- [ ] 카메라 쉐이크 구현

---

## 🔗 다음 Phase

**PHASE_28: 최적화**
