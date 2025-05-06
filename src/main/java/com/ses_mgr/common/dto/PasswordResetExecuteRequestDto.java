package com.ses_mgr.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * パスワードリセット実行リクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetExecuteRequestDto {

    @NotBlank(message = "トークンは必須です")
    private String token;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", 
             message = "パスワードは8文字以上で、少なくとも1つの英字と1つの数字を含む必要があります")
    private String password;

    @NotBlank(message = "パスワード確認は必須です")
    private String passwordConfirmation;
}