package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBulkStatusRequestDto {

    @NotEmpty(message = "ユーザーIDリストは必須です")
    private List<UUID> userIds;

    @NotBlank(message = "ステータスは必須です")
    @Pattern(regexp = "^(active|inactive|locked)$", message = "ステータスは active, inactive, locked のいずれかを指定してください")
    private String status;
}