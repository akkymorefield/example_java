package example.long_polling_example;

import java.io.IOException;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App
{
    public static void main( String[] args ) throws IOException
    {
    	SpringApplication app = new SpringApplication(App.class);
    	app.setDefaultProperties(Collections
    	          .singletonMap("server.port", "8081"));
    	app.run(args);
    }
}
