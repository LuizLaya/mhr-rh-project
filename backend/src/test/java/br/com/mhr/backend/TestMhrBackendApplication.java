package br.com.mhr.backend;

import org.springframework.boot.SpringApplication;

public class TestMhrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(MhrBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
