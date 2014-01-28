package org.springframework.yarn.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class ActivatorAppmasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActivatorAppmasterApplication.class, args);
	}

}
