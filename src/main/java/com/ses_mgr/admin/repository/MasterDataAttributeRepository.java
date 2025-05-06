package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.MasterDataAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * マスタデータ属性定義のリポジトリインターフェース
 * Repository interface for master data attribute definitions
 */
@Repository
public interface MasterDataAttributeRepository extends JpaRepository<MasterDataAttribute, Long> {

    /**
     * マスタデータタイプIDで属性定義のリストを検索
     * Find list of attribute definitions by master type ID
     *
     * @param masterTypeId マスタデータタイプID
     * @return 属性定義のリスト
     */
    List<MasterDataAttribute> findByMasterTypeId(Long masterTypeId);

    /**
     * マスタデータタイプIDと属性名で属性定義を検索
     * Find attribute definition by master type ID and attribute name
     *
     * @param masterTypeId   マスタデータタイプID
     * @param attributeName 属性名
     * @return 属性定義（オプショナル）
     */
    Optional<MasterDataAttribute> findByMasterTypeIdAndAttributeName(Long masterTypeId, String attributeName);

    /**
     * マスタデータタイプIDと属性名の存在チェック
     * Check if attribute definition exists by master type ID and attribute name
     *
     * @param masterTypeId   マスタデータタイプID
     * @param attributeName 属性名
     * @return 存在する場合はtrue
     */
    boolean existsByMasterTypeIdAndAttributeName(Long masterTypeId, String attributeName);

    /**
     * 指定したIDとは別のレコードで同じタイプIDと属性名が存在するかチェック（更新時の重複チェック用）
     * Check if attribute definition exists with the same type ID and attribute name in a different record
     * (used for duplicate check during update)
     *
     * @param masterTypeId   マスタデータタイプID
     * @param attributeName 属性名
     * @param id             属性定義ID
     * @return 存在する場合はtrue
     */
    boolean existsByMasterTypeIdAndAttributeNameAndIdNot(Long masterTypeId, String attributeName, Long id);

    /**
     * 表示順で並べた属性定義のリストを取得
     * Get a list of attribute definitions sorted by display order
     *
     * @param masterTypeId マスタデータタイプID
     * @return 属性定義のリスト
     */
    List<MasterDataAttribute> findByMasterTypeIdOrderByDisplayOrderAscDisplayNameJaAsc(Long masterTypeId);
    
    /**
     * マスタデータIDで属性を削除
     * Delete attributes by master data ID
     *
     * @param masterDataId マスタデータID
     */
    void deleteByMasterDataId(Long masterDataId);
    
}