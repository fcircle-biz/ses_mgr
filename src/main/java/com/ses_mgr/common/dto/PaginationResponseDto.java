package com.ses_mgr.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponseDto {

    private long total;
    private int page;
    private int limit;
    private int pages;
}