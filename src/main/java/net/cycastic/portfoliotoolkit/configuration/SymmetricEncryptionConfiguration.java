package net.cycastic.portfoliotoolkit.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.encryption")
public class SymmetricEncryptionConfiguration extends ExtendibleKeyConfiguration{
}
