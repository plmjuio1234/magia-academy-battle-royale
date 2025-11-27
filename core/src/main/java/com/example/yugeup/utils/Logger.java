package com.example.yugeup.utils;

/**
 * 로깅 유틸리티 클래스
 *
 * 게임 전반의 로그를 관리합니다.
 * 디버그, 정보, 경고, 에러 등의 로그 레벨을 지원합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Logger {

    /** 로그 태그 */
    private static final String TAG = "YuGeup";

    /**
     * 디버그 로그를 출력합니다.
     *
     * @param message 로그 메시지
     */
    public static void debug(String message) {
        if (Constants.DEBUG_MODE) {
            System.out.println("[DEBUG] [" + TAG + "] " + message);
        }
    }

    /**
     * 정보 로그를 출력합니다.
     *
     * @param message 로그 메시지
     */
    public static void info(String message) {
        System.out.println("[INFO] [" + TAG + "] " + message);
    }

    /**
     * 경고 로그를 출력합니다.
     *
     * @param message 로그 메시지
     */
    public static void warn(String message) {
        System.out.println("[WARN] [" + TAG + "] " + message);
    }

    /**
     * 에러 로그를 출력합니다.
     *
     * @param message 로그 메시지
     * @param throwable 예외 객체
     */
    public static void error(String message, Throwable throwable) {
        System.err.println("[ERROR] [" + TAG + "] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    /**
     * Private 생성자 - 인스턴스 생성 방지
     */
    private Logger() {
        throw new AssertionError("Logger 클래스는 인스턴스화할 수 없습니다.");
    }
}
