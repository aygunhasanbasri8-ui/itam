package tr.com.nkolay.itam;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Tüm integration test'lerin ortak temeli.
 * <p>
 * Konteyner static tanımlandığı için tüm test sınıfları aynı PostgreSQL örneğini
 * paylaşır; her sınıf için yeniden ayağa kaldırılmaz.
 * <p>
 * {@code @ServiceConnection} datasource ayarlarını çalışma anında enjekte eder;
 * test yaml dosyasında url/user/password yazmaya gerek kalmaz.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("itam")
                    .withUsername("itam")
                    .withPassword("itam");

    static {
        POSTGRES.start();
    }
}