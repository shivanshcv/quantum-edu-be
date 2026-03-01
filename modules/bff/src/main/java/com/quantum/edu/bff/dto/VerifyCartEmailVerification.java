package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCartEmailVerification {
    private boolean isVerified;
    private String verificationMessage;
    private String verificationButtonText;
    private String verificationButtonLink;
}
