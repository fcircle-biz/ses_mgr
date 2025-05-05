package com.ses_mgr.common.dto;

import jakarta.validation.constraints.Email;
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
public class UserUpdateRequestDto {

    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @Size(min = 1, max = 100, message = "名前は1〜100文字で入力してください")
    private String name;

    private String department;
    private String position;
    private String phone;
    private String role;

    @Pattern(regexp = "^(active|inactive|locked)$", message = "ステータスは active, inactive, locked のいずれかを指定してください")
    private String status;
}