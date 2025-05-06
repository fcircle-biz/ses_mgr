package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionUpdateRequestDto {

    @NotEmpty(message = "権限リストは空にできません")
    private List<String> permissions;
}