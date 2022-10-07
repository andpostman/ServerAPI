package ru.sberbank.synapse.backstage.serverapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.sberbank.synapse.backstage.serverapi.storage.StorageProperties;
import ru.sberbank.synapse.backstage.serverapi.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ServerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApiApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService){
        return args -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
