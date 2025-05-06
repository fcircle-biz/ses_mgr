package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通知更新リクエストDTOクラス
 * Notification update request DTO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationUpdateRequestDto {

    @JsonProperty("read")
    private Boolean read;

    public NotificationUpdateRequestDto() {
    }

    public NotificationUpdateRequestDto(Boolean read) {
        this.read = read;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}