package com.bnm.recouvrement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Cette classe active la planification des tâches dans Spring Boot
    // Elle est nécessaire pour que les rappels de commentaires fonctionnent correctement
}
