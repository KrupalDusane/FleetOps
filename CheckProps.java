import java.util.Properties;
import java.io.FileInputStream;
public class CheckProps {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("src/main/resources/application.properties"));
        System.out.println("Keys: " + props.keySet());
        System.out.println("URL: " + props.getProperty("spring.datasource.url"));
    }
}
