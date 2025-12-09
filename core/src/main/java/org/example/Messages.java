package org.example;

/**
 * 기본 텍스트 메시지 클래스
 *
 * 서버와의 초기 연결 테스트 및 간단한 텍스트 통신에 사용됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Messages {
    public String text;

    public Messages() {}

    public Messages(String text) {
        this.text = text;
    }
}
