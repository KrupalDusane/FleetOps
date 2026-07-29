import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class PrintDbContext {
    public static void main(String[] args) throws Exception {
        System.out.println("Connecting to Spring Context...");
        // Since we can't easily bootstrap the exact same context without port conflicts,
        // let's just parse the application.properties manually and simulate what Spring does,
        // OR better yet, we can't start a web server on 9090 since it's already running.
        // Wait, if we want to query the running process, we should use a java agent.
    }
}
