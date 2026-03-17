package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsDetails {

    private SettingsSection profileSection;
    private SettingsSection securitySection;
    private SettingsSection billingSection;
    private Map<String, String> values;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingsSection {
        private String title;
        private List<SettingsField> fields;
        private String submitLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingsField {
        private String id;
        private String label;
        private String type;
        private String placeholder;
        private boolean required;
        private boolean readOnly;
    }
}
