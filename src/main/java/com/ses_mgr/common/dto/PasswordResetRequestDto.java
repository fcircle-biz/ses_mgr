package com.ses_mgr.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * パスワードリセットリクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;
}