package org.kalki.doppelganger;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DoppelgangerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoppelgangerApplication.class, args);
	}
	
	@Bean(initMethod = "migrate")
	Flyway flyway() {
	    Flyway flyway = new Flyway();
	    flyway.setBaselineOnMigrate(true);
	    flyway.setDataSource("jdbc:mysql://localhost:3306/doppelganger?createDatabaseIfNotExist=true", "root", "Welcome1@#");
	    return flyway;
	}

}
