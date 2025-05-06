package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 通知一覧レスポンスDTOクラス
 * Notification list response DTO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationListResponseDto {

    @JsonProperty("data")
    private List<NotificationDto> notifications;

    @JsonProperty("pagination")
    private PaginationDto pagination;

    @JsonProperty("summary")
    private SummaryDto summary;

    /**
     * ページング情報DTO内部クラス
     * Pagination DTO inner class
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationDto {
        @JsonProperty("current_page")
        private int currentPage;

        @JsonProperty("page_size")
        private int pageSize;

        @JsonProperty("total_pages")
        private int totalPages;

        @JsonProperty("total_items")
        private long totalItems;

        public PaginationDto() {
        }

        public PaginationDto(int currentPage, int pageSize, int totalPages, long totalItems) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }
    }

    /**
     * 概要情報DTO内部クラス
     * Summary DTO inner class
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SummaryDto {
        @JsonProperty("unread_count")
        private long unreadCount;

        @JsonProperty("total_count")
        private long totalCount;

        public SummaryDto() {
        }

        public SummaryDto(long unreadCount, long totalCount) {
            this.unreadCount = unreadCount;
            this.totalCount = totalCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }
    }

    /**
     * デフォルトコンストラクタ
     * Default constructor
     */
    public NotificationListResponseDto() {
    }

    /**
     * コンストラクタ
     * Constructor
     *
     * @param notifications 通知リスト
     * @param pagination    ページング情報
     * @param summary       概要情報
     */
    public NotificationListResponseDto(List<NotificationDto> notifications, PaginationDto pagination, SummaryDto summary) {
        this.notifications = notifications;
        this.pagination = pagination;
        this.summary = summary;
    }

    /**
     * ビルダークラス
     * Builder class
     */
    public static class Builder {
        private List<NotificationDto> notifications;
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalItems;
        private long unreadCount;
        private long totalCount;

        public Builder notifications(List<NotificationDto> notifications) {
            this.notifications = notifications;
            return this;
        }

        public Builder currentPage(int currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder totalItems(long totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public Builder unreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public Builder totalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public NotificationListResponseDto build() {
            PaginationDto pagination = new PaginationDto(currentPage, pageSize, totalPages, totalItems);
            SummaryDto summary = new SummaryDto(unreadCount, totalCount);
            return new NotificationListResponseDto(notifications, pagination, summary);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationDto> notifications) {
        this.notifications = notifications;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }

    public SummaryDto getSummary() {
        return summary;
    }

    public void setSummary(SummaryDto summary) {
        this.summary = summary;
    }
}