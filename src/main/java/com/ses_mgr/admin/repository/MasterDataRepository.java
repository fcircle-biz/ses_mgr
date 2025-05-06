package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.MasterData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * マスタデータのリポジトリインターフェース
 * Repository interface for master data
 */
@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {

    /**
     * マスタデータタイプIDとコードでマスタデータを検索
     * Find master data by master type ID and code
     *
     * @param masterTypeId マスタデータタイプID
     * @param code         コード
     * @return マスタデータ（オプショナル）
     */
    Optional<MasterData> findByMasterTypeIdAndCode(Long masterTypeId, String code);

    /**
     * マスタデータタイプIDとコードでマスタデータを検索（アクティブなもののみ）
     * Find active master data by master type ID and code
     *
     * @param masterTypeId マスタデータタイプID
     * @param code         コード
     * @return マスタデータ（オプショナル）
     */
    Optional<MasterData> findByMasterTypeIdAndCodeAndIsActiveTrue(Long masterTypeId, String code);

    /**
     * マスタデータタイプIDでマスタデータのリストを検索
     * Find list of master data by master type ID
     *
     * @param masterTypeId マスタデータタイプID
     * @param pageable     ページング情報
     * @return マスタデータのページ
     */
    Page<MasterData> findByMasterTypeId(Long masterTypeId, Pageable pageable);

    /**
     * マスタデータタイプIDでアクティブなマスタデータのリストを検索
     * Find list of active master data by master type ID
     *
     * @param masterTypeId マスタデータタイプID
     * @param pageable     ページング情報
     * @return マスタデータのページ
     */
    Page<MasterData> findByMasterTypeIdAndIsActiveTrue(Long masterTypeId, Pageable pageable);

    /**
     * マスタデータタイプのコードでアクティブなマスタデータのリストを検索
     * Find list of active master data by master type code
     *
     * @param typeCode     マスタデータタイプコード
     * @param pageable     ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND md.isActive = true")
    Page<MasterData> findByMasterTypeCodeAndIsActiveTrue(@Param("typeCode") String typeCode, Pageable pageable);
    
    /**
     * マスタデータタイプのコードでマスタデータのリストを検索
     * Find list of master data by master type code
     *
     * @param typeCode     マスタデータタイプコード
     * @param pageable     ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode")
    Page<MasterData> findByTypeCode(@Param("typeCode") String typeCode, Pageable pageable);
    
    /**
     * タイプコードとコードでマスタデータを検索
     * Find master data by type code and code
     *
     * @param typeCode  マスタデータタイプコード
     * @param code      コード
     * @return マスタデータ（オプショナル）
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND md.code = :code")
    Optional<MasterData> findByTypeCodeAndCode(@Param("typeCode") String typeCode, @Param("code") String code);
    
    /**
     * タイプコードに基づくマスタデータの数をカウント
     * Count master data based on type code
     *
     * @param typeCode  マスタデータタイプコード
     * @return カウント
     */
    @Query("SELECT COUNT(md) FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode")
    long countByTypeCode(@Param("typeCode") String typeCode);
    
    /**
     * タイプコードとステータスでマスタデータを検索
     * Find master data by type code and status
     *
     * @param typeCode  マスタデータタイプコード
     * @param status    ステータス（"active"または"inactive"）
     * @param pageable  ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND (:status = 'active' AND md.isActive = true OR :status = 'inactive' AND md.isActive = false)")
    Page<MasterData> findByTypeCodeAndStatus(@Param("typeCode") String typeCode, @Param("status") String status, Pageable pageable);
    
    /**
     * タイプコードと名前の部分一致でマスタデータを検索
     * Find master data by type code and partial name match
     *
     * @param typeCode  マスタデータタイプコード
     * @param name      検索名
     * @param pageable  ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND LOWER(md.nameJa) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<MasterData> findByTypeCodeAndNameContainingIgnoreCase(@Param("typeCode") String typeCode, @Param("name") String name, Pageable pageable);
    
    /**
     * タイプコード、ステータス、名前の部分一致でマスタデータを検索
     * Find master data by type code, status, and partial name match
     *
     * @param typeCode  マスタデータタイプコード
     * @param status    ステータス（"active"または"inactive"）
     * @param name      検索名
     * @param pageable  ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND (:status = 'active' AND md.isActive = true OR :status = 'inactive' AND md.isActive = false) " +
           "AND LOWER(md.nameJa) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<MasterData> findByTypeCodeAndStatusAndNameContainingIgnoreCase(@Param("typeCode") String typeCode, @Param("status") String status, @Param("name") String name, Pageable pageable);
    
    /**
     * タイプコードとステータスでマスタデータのリストを取得し、表示順でソート
     * Get a list of master data by type code and status, sorted by display order
     * 
     * @param typeCode タイプコード
     * @param status ステータス（"active"または"inactive"）
     * @return マスタデータのリスト
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND (:status = 'active' AND md.isActive = true OR :status = 'inactive' AND md.isActive = false) " +
           "ORDER BY md.displayOrder ASC NULLS LAST, md.nameJa ASC")
    List<MasterData> findByTypeCodeAndStatusOrderByDisplayOrderAsc(@Param("typeCode") String typeCode, @Param("status") String status);
    
    /**
     * タイプコードでマスタデータのリストを取得し、表示順でソート
     * Get a list of master data by type code, sorted by display order
     * 
     * @param typeCode タイプコード
     * @return マスタデータのリスト
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode " +
           "ORDER BY md.displayOrder ASC NULLS LAST, md.nameJa ASC")
    List<MasterData> findByTypeCodeOrderByDisplayOrderAsc(@Param("typeCode") String typeCode);

    /**
     * キーワードで検索（部分一致）
     * Search by keyword (partial match)
     *
     * @param typeCode マスタデータタイプコード
     * @param keyword  キーワード
     * @param pageable ページング情報
     * @return マスタデータのページ
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode " +
           "AND (LOWER(md.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(md.nameJa) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(md.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND md.isActive = true")
    Page<MasterData> searchByKeyword(@Param("typeCode") String typeCode, 
                                     @Param("keyword") String keyword, 
                                     Pageable pageable);

    /**
     * マスタデータタイプIDとコードの存在チェック
     * Check if master data exists by master type ID and code
     *
     * @param masterTypeId マスタデータタイプID
     * @param code         コード
     * @return 存在する場合はtrue
     */
    boolean existsByMasterTypeIdAndCode(Long masterTypeId, String code);

    /**
     * 指定したIDとは別のレコードで同じタイプIDとコードが存在するかチェック（更新時の重複チェック用）
     * Check if master data exists with the same type ID and code in a different record
     * (used for duplicate check during update)
     *
     * @param masterTypeId マスタデータタイプID
     * @param code         コード
     * @param id           マスタデータID
     * @return 存在する場合はtrue
     */
    boolean existsByMasterTypeIdAndCodeAndIdNot(Long masterTypeId, String code, Long id);

    /**
     * 表示順で並べたアクティブなマスタデータのリストを取得
     * Get a list of active master data sorted by display order
     *
     * @param masterTypeId マスタデータタイプID
     * @return マスタデータのリスト
     */
    @Query("SELECT md FROM MasterData md WHERE md.masterType.id = :masterTypeId AND md.isActive = true " +
           "ORDER BY md.displayOrder ASC NULLS LAST, md.nameJa ASC")
    List<MasterData> findAllActiveByMasterTypeIdOrderByDisplayOrder(@Param("masterTypeId") Long masterTypeId);

    /**
     * 表示順で並べたアクティブなマスタデータのリストを取得（タイプコード指定）
     * Get a list of active master data sorted by display order (specified by type code)
     *
     * @param typeCode マスタデータタイプコード
     * @return マスタデータのリスト
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND md.isActive = true " +
           "ORDER BY md.displayOrder ASC NULLS LAST, md.nameJa ASC")
    List<MasterData> findAllActiveByMasterTypeCodeOrderByDisplayOrder(@Param("typeCode") String typeCode);

    /**
     * 親ID指定でアクティブなマスタデータのリストを取得
     * Get a list of active master data by parent ID
     *
     * @param parentId 親マスタデータID
     * @return マスタデータのリスト
     */
    List<MasterData> findByParentIdAndIsActiveTrue(Long parentId);

    /**
     * 現在有効なマスタデータのリストを取得（期間指定）
     * Get a list of currently valid master data (specified by validity period)
     *
     * @param typeCode 　　　マスタデータタイプコード
     * @param currentDateTime 現在日時
     * @return マスタデータのリスト
     */
    @Query("SELECT md FROM MasterData md JOIN md.masterType mt " +
           "WHERE mt.typeCode = :typeCode AND md.isActive = true " +
           "AND (md.validFrom IS NULL OR md.validFrom <= :currentDateTime) " +
           "AND (md.validTo IS NULL OR md.validTo >= :currentDateTime) " +
           "ORDER BY md.displayOrder ASC NULLS LAST, md.nameJa ASC")
    List<MasterData> findAllCurrentlyValid(@Param("typeCode") String typeCode, 
                                          @Param("currentDateTime") LocalDateTime currentDateTime);
}