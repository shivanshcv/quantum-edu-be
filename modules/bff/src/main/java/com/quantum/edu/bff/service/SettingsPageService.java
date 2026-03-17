package com.quantum.edu.bff.service;

import com.quantum.edu.auth.api.AuthApi;
import com.quantum.edu.bff.dto.*;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SettingsPageService {

    private static final List<SettingsDetails.SettingsField> PROFILE_FIELDS = List.of(
            SettingsDetails.SettingsField.builder()
                    .id("name").label("Full name").type("text").placeholder("Jane Doe").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("email").label("Email").type("email").placeholder("jane@example.com").required(true).readOnly(true).build(),
            SettingsDetails.SettingsField.builder()
                    .id("phone").label("Phone").type("text").placeholder("+919876543210").required(false).readOnly(false).build()
    );

    private static final List<SettingsDetails.SettingsField> SECURITY_FIELDS = List.of(
            SettingsDetails.SettingsField.builder()
                    .id("currentPassword").label("Current password").type("password").placeholder("••••••••").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("newPassword").label("New password").type("password").placeholder("••••••••").required(true).readOnly(false).build()
    );

    private static final List<SettingsDetails.SettingsField> BILLING_FIELDS = List.of(
            SettingsDetails.SettingsField.builder()
                    .id("billingName").label("Full name").type("text").placeholder("Jane Doe").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingAddressLine1").label("Address line 1").type("text").placeholder("123 Main Street").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingAddressLine2").label("Address line 2 (optional)").type("text").placeholder("Apt 4B").required(false).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingCity").label("City").type("text").placeholder("Mumbai").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingState").label("State").type("text").placeholder("Maharashtra").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingCountry").label("Country").type("text").placeholder("India").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingPostalCode").label("Postal code").type("text").placeholder("400001").required(true).readOnly(false).build(),
            SettingsDetails.SettingsField.builder()
                    .id("billingGstNumber").label("GST number (optional)").type("text").placeholder("22AAAAA0000A1Z5").required(false).readOnly(false).build()
    );

    private final UserProfileApi userProfileApi;
    private final AuthApi authApi;

    public SettingsPageService(UserProfileApi userProfileApi, AuthApi authApi) {
        this.userProfileApi = userProfileApi;
        this.authApi = authApi;
    }

    public PageResponse getSettingsPage(Long userId) {
        Optional<UserProfile> profileOpt = userProfileApi.getProfile(userId);
        UserProfile profile = profileOpt.orElse(null);
        String email = authApi.getEmailByUserId(userId).orElse(null);

        String name = profile != null ? buildProfileName(profile) : "";
        String phone = profile != null && profile.getPhone() != null ? profile.getPhone() : "";
        String billingName = profile != null ? profile.getBillingName() : "";

        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        values.put("email", email != null ? email : "");
        values.put("phone", phone);
        values.put("billingName", billingName);
        values.put("billingAddressLine1", profile != null && profile.getAddressLine1() != null ? profile.getAddressLine1() : "");
        values.put("billingAddressLine2", profile != null && profile.getAddressLine2() != null ? profile.getAddressLine2() : "");
        values.put("billingCity", profile != null && profile.getCity() != null ? profile.getCity() : "");
        values.put("billingState", profile != null && profile.getState() != null ? profile.getState() : "");
        values.put("billingCountry", profile != null && profile.getCountry() != null ? profile.getCountry() : "");
        values.put("billingPostalCode", profile != null && profile.getPostalCode() != null ? profile.getPostalCode() : "");
        values.put("billingGstNumber", profile != null && profile.getGstNumber() != null ? profile.getGstNumber() : "");

        SettingsDetails details = SettingsDetails.builder()
                .profileSection(SettingsDetails.SettingsSection.builder()
                        .title("Profile")
                        .fields(PROFILE_FIELDS)
                        .submitLabel("Save changes")
                        .build())
                .securitySection(SettingsDetails.SettingsSection.builder()
                        .title("Security")
                        .fields(SECURITY_FIELDS)
                        .submitLabel("Change password")
                        .build())
                .billingSection(SettingsDetails.SettingsSection.builder()
                        .title("Billing Details")
                        .fields(BILLING_FIELDS)
                        .submitLabel("Save changes")
                        .build())
                .values(values)
                .build();

        ComponentResponse component = ComponentResponse.builder()
                .id("settings-form")
                .type("SETTINGS_PAGE")
                .config(Map.of("theme", "light"))
                .details(details)
                .build();

        MainSection main = MainSection.builder()
                .type("SETTINGS")
                .components(List.of(component))
                .build();

        return PageResponse.builder()
                .main(main)
                .build();
    }

    private static String buildProfileName(UserProfile profile) {
        if (profile.getLastName() != null && !profile.getLastName().isBlank()) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return profile.getFirstName();
    }
}
