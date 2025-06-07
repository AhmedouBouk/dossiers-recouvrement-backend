package com.bnm.recouvrement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.bnm.recouvrement.repository", "com.bnm.recouvrement.dao"})
public class RecouvrementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecouvrementApplication.class, args);
	}

}
