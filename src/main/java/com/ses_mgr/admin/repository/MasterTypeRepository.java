package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.MasterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * マスタデータタイプのリポジトリインターフェース
 * Repository interface for master data types
 */
@Repository
public interface MasterTypeRepository extends JpaRepository<MasterType, Long> {

    /**
     * タイプコードでマスタデータタイプを検索
     * Find master data type by type code
     *
     * @param typeCode タイプコード
     * @return マスタデータタイプ（オプショナル）
     */
    Optional<MasterType> findByTypeCode(String typeCode);
    
    /**
     * コードでマスタデータタイプを検索（getCode()互換用）
     * Find master data type by code (for getCode() compatibility)
     *
     * @param code コード（typeCodeと同じ）
     * @return マスタデータタイプ（オプショナル）
     */
    default Optional<MasterType> findByCode(String code) {
        return findByTypeCode(code);
    }

    /**
     * アクティブなマスタデータタイプのみを検索
     * Find only active master data types
     *
     * @param pageable ページング情報
     * @return マスタデータタイプのページ
     */
    Page<MasterType> findByIsActiveTrue(Pageable pageable);

    /**
     * タイプコードの存在チェック
     * Check if type code exists
     *
     * @param typeCode タイプコード
     * @return 存在する場合はtrue
     */
    boolean existsByTypeCode(String typeCode);

    /**
     * 指定したIDとタイプコードが別レコードで存在するかチェック（更新時の重複チェック用）
     * Check if type code exists in a different record (used for duplicate check during update)
     *
     * @param id       マスタデータタイプID
     * @param typeCode タイプコード
     * @return 存在する場合はtrue
     */
    boolean existsByTypeCodeAndIdNot(String typeCode, Long id);

    /**
     * タイプ名で検索（部分一致）
     * Search by type name (partial match)
     *
     * @param keyword キーワード
     * @param pageable ページング情報
     * @return マスタデータタイプのページ
     */
    @Query("SELECT mt FROM MasterType mt WHERE " +
           "(LOWER(mt.typeNameJa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mt.typeNameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mt.typeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND mt.isActive = true")
    Page<MasterType> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 表示順で並べたアクティブなマスタデータタイプのリストを取得
     * Get a list of active master data types sorted by display order
     *
     * @return マスタデータタイプのリスト
     */
    @Query("SELECT mt FROM MasterType mt WHERE mt.isActive = true ORDER BY mt.displayOrder ASC NULLS LAST, mt.typeNameJa ASC")
    List<MasterType> findAllActiveOrderByDisplayOrder();
}