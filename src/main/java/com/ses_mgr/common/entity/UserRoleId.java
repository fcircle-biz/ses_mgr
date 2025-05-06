package com.ses_mgr.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserRoleId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_id")
    private UUID roleId;
    
    /**
     * NullセーフなコンストラクタでUserRoleIdを生成します。
     * roleIdがnullの場合は新しいUUIDを生成します。
     *
     * @param userId ユーザーID
     * @param roleId ロールID（nullの場合は新しいUUIDが生成される）
     */
    public UserRoleId(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = (roleId != null) ? roleId : UUID.randomUUID();
    }
}