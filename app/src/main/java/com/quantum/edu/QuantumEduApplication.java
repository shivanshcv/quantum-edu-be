package com.quantum.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(scanBasePackages = "com.quantum.edu")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class QuantumEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantumEduApplication.class, args);
    }
}
