package ru.croc.finalproject.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
@Data
public class BotConfig {
    @Value("${bot.username}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

}
