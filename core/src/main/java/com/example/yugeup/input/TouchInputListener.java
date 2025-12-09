package com.example.yugeup.input;

import com.badlogic.gdx.InputAdapter;

/**
 * 터치 입력 리스너 클래스
 *
 * libGDX의 InputAdapter를 상속받아 터치 이벤트를 처리합니다.
 * InputHandler와 연동하여 동작합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class TouchInputListener extends InputAdapter {

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // TODO: PHASE_08에서 구현
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO: PHASE_08에서 구현
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO: PHASE_08에서 구현
        return false;
    }
}
