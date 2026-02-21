package com.quantum.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.quantum.edu")
public class QuantumEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantumEduApplication.class, args);
    }
}
