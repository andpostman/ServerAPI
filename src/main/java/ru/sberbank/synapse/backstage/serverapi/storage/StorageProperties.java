package ru.sberbank.synapse.backstage.serverapi.storage;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    String location;
}
