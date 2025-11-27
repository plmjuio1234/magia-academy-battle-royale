package com.example.yugeup.network;

import com.example.yugeup.network.messages.GetRoomListMsg;
import com.example.yugeup.utils.Constants;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkManager 테스트
 *
 * 네트워크 연결 및 메시지 송수신 기능을 테스트합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestNetworkManager {

    private NetworkManager networkManager;

    @BeforeEach
    public void setUp() {
        networkManager = NetworkManager.getInstance();
    }

    @AfterEach
    public void tearDown() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.disconnect();
        }
    }

    @Test
    @Order(1)
    @DisplayName("NetworkManager 싱글톤 인스턴스 생성 테스트")
    public void 싱글톤_인스턴스가_정상적으로_생성된다() {
        // Given & When
        NetworkManager instance1 = NetworkManager.getInstance();
        NetworkManager instance2 = NetworkManager.getInstance();

        // Then
        assertNotNull(instance1, "NetworkManager 인스턴스는 null이 아니어야 합니다");
        assertSame(instance1, instance2, "동일한 싱글톤 인스턴스를 반환해야 합니다");
    }

    @Test
    @Order(2)
    @DisplayName("초기 연결 상태 테스트")
    public void 초기_연결_상태는_false이다() {
        // Given & When
        boolean isConnected = networkManager.isConnected();

        // Then
        assertFalse(isConnected, "초기 연결 상태는 false여야 합니다");
    }

    @Test
    @Order(3)
    @DisplayName("서버 연결 시도 테스트")
    public void 서버에_연결을_시도할_수_있다() {
        // Given
        String host = Constants.SERVER_HOST;
        int port = Constants.SERVER_PORT;

        // When & Then
        // 서버가 실행 중이지 않을 수 있으므로 예외가 발생하지 않는지만 확인
        assertDoesNotThrow(() -> {
            networkManager.connect(host, port);
        }, "서버 연결 시도는 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @Order(4)
    @DisplayName("메시지 전송 시도 테스트")
    public void 메시지를_전송할_수_있다() {
        // Given
        GetRoomListMsg message = new GetRoomListMsg();

        // When & Then
        // 연결 여부와 관계없이 sendTCP는 예외를 발생시키지 않아야 함
        assertDoesNotThrow(() -> {
            networkManager.sendTCP(message);
        }, "메시지 전송 메서드는 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @Order(5)
    @DisplayName("서버 연결 해제 테스트")
    public void 서버_연결을_해제할_수_있다() {
        // Given & When
        networkManager.disconnect();

        // Then
        assertFalse(networkManager.isConnected(), "연결 해제 후 연결 상태는 false여야 합니다");
    }

    @Test
    @Order(6)
    @DisplayName("연결되지 않은 상태에서 메시지 전송 시 에러 처리")
    public void 연결되지_않은_상태에서_메시지_전송_시_에러가_발생하지_않는다() {
        // Given
        assertFalse(networkManager.isConnected(), "연결되지 않은 상태여야 합니다");

        // When & Then
        GetRoomListMsg message = new GetRoomListMsg();
        assertDoesNotThrow(() -> {
            networkManager.sendTCP(message);
        }, "연결되지 않은 상태에서도 sendTCP는 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @Order(7)
    @DisplayName("잘못된 호스트 연결 시도 테스트")
    public void 잘못된_호스트로_연결_시도가_가능하다() {
        // Given
        String invalidHost = "invalid.host.example.com";
        int port = Constants.SERVER_PORT;

        // When & Then
        // 잘못된 호스트라도 연결 시도 자체는 예외를 발생시키지 않아야 함
        assertDoesNotThrow(() -> {
            networkManager.connect(invalidHost, port);
        }, "잘못된 호스트 연결 시도도 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @Order(8)
    @DisplayName("업데이트 메서드 호출 테스트")
    public void 업데이트_메서드가_정상적으로_호출된다() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            networkManager.update(0.016f); // 60 FPS 기준 delta
        }, "update 메서드는 예외를 발생시키지 않아야 합니다");
    }
}
