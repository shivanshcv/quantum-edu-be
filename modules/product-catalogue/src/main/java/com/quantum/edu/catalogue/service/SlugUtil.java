package com.quantum.edu.catalogue.service;

import java.text.Normalizer;
import java.util.function.Predicate;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String generate(String input, Predicate<String> existsCheck) {
        String base = toSlug(input);
        if (!existsCheck.test(base)) {
            return base;
        }
        for (int i = 2; i < 1000; i++) {
            String candidate = base + "-" + i;
            if (!existsCheck.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique slug for: " + input);
    }

    private static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
