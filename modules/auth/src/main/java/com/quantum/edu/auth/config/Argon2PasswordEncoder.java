package com.quantum.edu.auth.config;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordEncoder {

    private static final int ITERATIONS = 2;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16, 32);

    public String encode(CharSequence rawPassword) {
        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, rawPassword.toString().toCharArray());
        } finally {
            argon2.wipeArray(new char[0]);
        }
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return argon2.verify(encodedPassword, rawPassword.toString().toCharArray());
    }
}
