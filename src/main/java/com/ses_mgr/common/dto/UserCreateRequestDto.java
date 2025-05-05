package com.ses_mgr.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "名前は必須です")
    @Size(min = 1, max = 100, message = "名前は1〜100文字で入力してください")
    private String name;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$", message = "パスワードは英字と数字を含む必要があります")
    private String password;

    @NotBlank(message = "ログインIDは必須です")
    @Size(min = 3, max = 50, message = "ログインIDは3〜50文字で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "ログインIDは英数字、ピリオド、アンダースコア、ハイフンのみ使用できます")
    private String loginId;

    private String department;
    private String position;
    private String phone;
    private String role;

    @Pattern(regexp = "^(active|inactive|locked)$", message = "ステータスは active, inactive, locked のいずれかを指定してください")
    @Builder.Default
    private String status = "active";
}