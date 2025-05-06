package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequestDto {

    @NotBlank(message = "ロール名は必須です")
    @Size(min = 2, max = 50, message = "ロール名は2〜50文字で入力してください")
    private String name;
    
    @Size(max = 200, message = "説明は200文字以内で入力してください")
    private String description;
    
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "ロールコードは半角英数字、ハイフン、アンダースコアのみ使用できます")
    private String roleCode;
    
    private List<String> permissions;
}