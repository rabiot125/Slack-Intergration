package com.example.slackpoc.config;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {
    @Value("${slack.bot.token}")
    private String botToken;
    @Value("${slack.signing.secret}")
    private String signingSecret;
    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }
    @Bean
    public MethodsClient slackMethodsClient(Slack slack) {
        return slack.methods(botToken);
    }

    @Bean
    public App slackApp() {
        AppConfig appConfig = AppConfig.builder()
                .singleTeamBotToken(botToken)
                .signingSecret(signingSecret)
                .build();
        return new App(appConfig);
    }
}