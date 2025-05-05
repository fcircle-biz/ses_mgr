package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotEmpty;
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
public class UserBulkUnlockRequestDto {

    @NotEmpty(message = "ユーザーIDリストは必須です")
    private List<UUID> userIds;
}