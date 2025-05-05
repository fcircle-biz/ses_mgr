package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequestDto {

    @NotBlank(message = "ステータスは必須です")
    @Pattern(regexp = "^(active|inactive|locked)$", message = "ステータスは active, inactive, locked のいずれかを指定してください")
    private String status;
}