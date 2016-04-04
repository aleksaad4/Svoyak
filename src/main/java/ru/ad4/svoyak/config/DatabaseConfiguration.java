package ru.ad4.svoyak.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("ru.ad4.svoyak.data.repositories")
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class DatabaseConfiguration {
}
