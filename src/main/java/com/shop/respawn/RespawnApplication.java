package com.shop.respawn;

import jakarta.persistence.PostPersist;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RespawnApplication {

	public static void main(String[] args) {
		SpringApplication.run(RespawnApplication.class, args);
	}

}
