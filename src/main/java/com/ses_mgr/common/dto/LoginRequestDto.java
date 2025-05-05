package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "ログインIDは必須です")
    private String loginId;

    @NotBlank(message = "パスワードは必須です")
    private String password;
}