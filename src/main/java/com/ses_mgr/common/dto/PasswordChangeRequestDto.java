package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * パスワード変更リクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequestDto {

    @NotBlank(message = "現在のパスワードは必須です")
    private String currentPassword;

    @NotBlank(message = "新しいパスワードは必須です")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", 
             message = "パスワードは8文字以上で、少なくとも1つの英字と1つの数字を含む必要があります")
    private String newPassword;

    @NotBlank(message = "新しいパスワード（確認用）は必須です")
    private String newPasswordConfirmation;
}