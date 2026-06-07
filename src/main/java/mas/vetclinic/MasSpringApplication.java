package mas.vetclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MasSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasSpringApplication.class, args);
    }

}
