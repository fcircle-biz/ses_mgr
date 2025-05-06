package com.ses_mgr.common.repository.search;

import com.ses_mgr.common.entity.search.SearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 検索インデックスリポジトリ
 * 検索インデックスの永続化と検索機能を担当します
 */
@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {

    /**
     * リソースIDとタイプからインデックスを取得
     *
     * @param resourceId リソースID
     * @param resourceType リソースタイプ
     * @return 検索インデックス
     */
    Optional<SearchIndex> findByResourceIdAndResourceType(String resourceId, String resourceType);

    /**
     * 全文検索を実行
     *
     * @param searchQuery 検索クエリ
     * @param resourceTypes リソースタイプリスト
     * @param pageable ページネーション情報
     * @return 検索結果のページ
     */
    @Query(value = "SELECT si.* FROM search_index si " +
            "WHERE MATCH(si.title, si.subtitle, si.description) AGAINST(:searchQuery IN BOOLEAN MODE) " +
            "AND (:resourceTypes IS NULL OR si.resource_type IN (:resourceTypes)) " +
            "ORDER BY MATCH(si.title, si.subtitle, si.description) AGAINST(:searchQuery IN BOOLEAN MODE) DESC",
            nativeQuery = true)
    Page<SearchIndex> searchByQuery(
            @Param("searchQuery") String searchQuery,
            @Param("resourceTypes") List<String> resourceTypes,
            Pageable pageable);

    /**
     * 特定のリソースタイプの全文検索を実行
     *
     * @param searchQuery 検索クエリ
     * @param resourceType リソースタイプ
     * @param pageable ページネーション情報
     * @return 検索結果のページ
     */
    @Query(value = "SELECT si.* FROM search_index si " +
            "WHERE MATCH(si.title, si.subtitle, si.description) AGAINST(:searchQuery IN BOOLEAN MODE) " +
            "AND si.resource_type = :resourceType " +
            "ORDER BY MATCH(si.title, si.subtitle, si.description) AGAINST(:searchQuery IN BOOLEAN MODE) DESC",
            nativeQuery = true)
    Page<SearchIndex> searchByQueryAndResourceType(
            @Param("searchQuery") String searchQuery,
            @Param("resourceType") String resourceType,
            Pageable pageable);

    /**
     * 特定のリソースタイプに対する検索クエリの結果件数を取得
     *
     * @param searchQuery 検索クエリ
     * @param resourceType リソースタイプ
     * @return 検索結果件数
     */
    @Query(value = "SELECT COUNT(*) FROM search_index si " +
            "WHERE MATCH(si.title, si.subtitle, si.description) AGAINST(:searchQuery IN BOOLEAN MODE) " +
            "AND si.resource_type = :resourceType",
            nativeQuery = true)
    long countByQueryAndResourceType(
            @Param("searchQuery") String searchQuery,
            @Param("resourceType") String resourceType);

    /**
     * 特定のリソースタイプのインデックスを取得
     *
     * @param resourceType リソースタイプ
     * @param pageable ページネーション情報
     * @return インデックスのページ
     */
    Page<SearchIndex> findByResourceType(String resourceType, Pageable pageable);
}