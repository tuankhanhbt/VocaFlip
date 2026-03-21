package com.example.vocaflip;

import org.springframework.boot.SpringApplication;

public class TestVocaflipApplication {

	public static void main(String[] args) {
		SpringApplication.from(VocaflipApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
