package se.curvysoft.eve.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.curvysoft.eve.tools.model.esi.SessionData;
import se.curvysoft.eve.tools.model.esi.SharedData;

@SpringBootApplication
public class EveToolsApplication {
    private final Environment environment;

    public EveToolsApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(EveToolsApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    @Bean
    @SessionScope
    public SessionData sessionData() {
        return new SessionData();
    }

    @Bean
    @RequestScope
    public SharedData sharedData() {
        SharedData sharedData = new SharedData();
        sharedData.setCurrentPath(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI());
        return sharedData;
    }

}
