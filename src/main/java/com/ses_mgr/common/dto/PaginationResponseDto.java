package com.ses_mgr.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponseDto<T> {

    private List<T> items;
    private long total;
    private int page;
    private int limit;
    private int pages;
}