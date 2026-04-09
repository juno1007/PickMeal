package com.hankki.pickmeal.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PasswordResetToken {
    private Long tokenId;
    private Long userId;
    private String token;
    private LocalDateTime expiryDate;
}
