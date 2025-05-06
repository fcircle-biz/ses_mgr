package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ses_mgr.common.entity.Notification;
import com.ses_mgr.common.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通知DTOクラス
 * Notification DTO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("read")
    private boolean read;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("sender")
    private UserInfoDto sender;

    @JsonProperty("recipient")
    private UserInfoDto recipient;

    @JsonProperty("importance")
    private String importance;

    @JsonProperty("action_url")
    private String actionUrl;

    /**
     * ユーザー情報DTO内部クラス
     * User info DTO inner class
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfoDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        public UserInfoDto() {
        }

        public UserInfoDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * デフォルトコンストラクタ
     * Default constructor
     */
    public NotificationDto() {
    }

    /**
     * エンティティからDTOを作成するコンストラクタ
     * Constructor to create DTO from entity
     *
     * @param notification 通知エンティティ
     */
    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType() != null ? notification.getType().toLowerCase() : null;
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.read = notification.isRead();
        this.metadata = notification.getMetadata();
        this.createdAt = notification.getCreatedAt();
        this.updatedAt = notification.getUpdatedAt();
        this.importance = notification.getImportance();
        this.actionUrl = notification.getActionUrl();

        if (notification.getSenderId() != null) {
            this.sender = new UserInfoDto(notification.getSenderId(), notification.getSenderName());
        }

        if (notification.getRecipientId() != null) {
            this.recipient = new UserInfoDto(notification.getRecipientId(), notification.getRecipientName());
        }
    }

    /**
     * DTOからエンティティを作成するメソッド
     * Method to create entity from DTO
     *
     * @return 通知エンティティ
     */
    public Notification toEntity() {
        Notification notification = new Notification();
        notification.setId(this.id);
        notification.setType(type != null ? NotificationType.fromString(this.type) : null);
        notification.setTitle(this.title);
        notification.setBody(this.body);
        notification.setRead(this.read);
        notification.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
        notification.setImportance(this.importance);
        notification.setActionUrl(this.actionUrl);

        if (this.sender != null) {
            notification.setSenderId(this.sender.getId());
            notification.setSenderName(this.sender.getName());
        }

        if (this.recipient != null) {
            notification.setRecipientId(this.recipient.getId());
            notification.setRecipientName(this.recipient.getName());
        }

        return notification;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserInfoDto getSender() {
        return sender;
    }

    public void setSender(UserInfoDto sender) {
        this.sender = sender;
    }

    public UserInfoDto getRecipient() {
        return recipient;
    }

    public void setRecipient(UserInfoDto recipient) {
        this.recipient = recipient;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}