package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ログインリクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "ログインIDは必須です")
    private String loginId;

    @NotBlank(message = "パスワードは必須です")
    private String password;
    
    /**
     * 長期間ログイン状態を維持するかどうか
     * trueの場合、リフレッシュトークンの有効期間が延長される
     */
    @Builder.Default
    private boolean rememberMe = false;
}