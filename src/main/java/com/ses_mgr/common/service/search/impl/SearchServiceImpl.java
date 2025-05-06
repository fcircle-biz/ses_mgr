package com.ses_mgr.common.service.search.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.search.SearchHistoryResponseDto;
import com.ses_mgr.common.dto.search.SearchRequestDto;
import com.ses_mgr.common.dto.search.SearchResultDto;
import com.ses_mgr.common.entity.search.SearchHistory;
import com.ses_mgr.common.entity.search.SearchIndex;
import com.ses_mgr.common.repository.search.SearchHistoryRepository;
import com.ses_mgr.common.repository.search.SearchIndexRepository;
import com.ses_mgr.common.service.search.SearchService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 検索サービス実装クラス
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final ObjectMapper objectMapper;

    @Value("${search.max.page.size:50}")
    private int maxPageSize;

    @Value("${search.default.page.size:20}")
    private int defaultPageSize;

    @Autowired
    public SearchServiceImpl(SearchHistoryRepository searchHistoryRepository,
                             SearchIndexRepository searchIndexRepository,
                             ObjectMapper objectMapper) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.searchIndexRepository = searchIndexRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SearchResultDto search(SearchRequestDto searchRequestDto, UUID userId) {
        long startTime = System.currentTimeMillis();

        // リクエストの検証
        validateSearchRequest(searchRequestDto);

        // 検索対象のリソースタイプを取得
        List<String> resourceTypes = searchRequestDto.getResourceTypes();
        
        // ページネーション設定
        int page = 0;
        int pageSize = defaultPageSize;
        if (searchRequestDto.getPagination() != null) {
            page = searchRequestDto.getPagination().getPage() != null ? 
                   Math.max(0, searchRequestDto.getPagination().getPage() - 1) : 0;
            pageSize = searchRequestDto.getPagination().getPageSize() != null ? 
                       Math.min(searchRequestDto.getPagination().getPageSize(), maxPageSize) : defaultPageSize;
        }
        Pageable pageable = PageRequest.of(page, pageSize);

        // 検索クエリを作成（MySQLの全文検索用に整形）
        String processedQuery = processSearchQuery(searchRequestDto.getQuery());

        SearchResultDto result;
        if (Boolean.TRUE.equals(searchRequestDto.getGroupByResourceType())) {
            // リソースタイプ別にグループ化した検索を実行
            result = searchGroupedByResourceType(processedQuery, resourceTypes, pageable);
        } else {
            // 通常の検索を実行
            result = searchStandard(processedQuery, resourceTypes, pageable);
        }

        // 検索履歴を保存（オプション）
        if (Boolean.TRUE.equals(searchRequestDto.getSaveSearch())) {
            SearchHistory searchHistory = saveSearchHistory(searchRequestDto, userId, result.getPagination().getTotalItems());
            result.setSearchId(searchHistory.getSearchId().toString());
        }

        // 検索時間を設定
        result.setQueryTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchHistoryResponseDto getSearchHistory(UUID userId, int page, int pageSize) {
        // ページサイズの制限を適用
        int limitedPageSize = Math.min(pageSize, maxPageSize);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limitedPageSize);

        // ユーザーの検索履歴を取得
        Page<SearchHistory> historyPage = searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // レスポンスDTOに変換
        SearchHistoryResponseDto responseDto = new SearchHistoryResponseDto();
        
        List<SearchHistoryResponseDto.SearchHistoryItemDto> historyItems = historyPage.getContent().stream()
                .map(this::convertToHistoryItemDto)
                .collect(Collectors.toList());
        
        responseDto.setHistory(historyItems);
        
        // ページネーション情報を設定
        SearchHistoryResponseDto.PaginationInfo paginationInfo = new SearchHistoryResponseDto.PaginationInfo();
        paginationInfo.setCurrentPage(historyPage.getNumber() + 1);
        paginationInfo.setPageSize(historyPage.getSize());
        paginationInfo.setTotalPages(historyPage.getTotalPages());
        paginationInfo.setTotalItems((int) historyPage.getTotalElements());
        
        responseDto.setPagination(paginationInfo);
        
        return responseDto;
    }

    @Override
    @Transactional
    public boolean deleteSearchHistory(UUID searchId, UUID userId) {
        // 検索履歴の存在確認
        SearchHistory searchHistory = searchHistoryRepository.findById(searchId)
                .orElseThrow(() -> new EntityNotFoundException("指定された検索履歴が存在しません。"));
        
        // ユーザーが所有者であることを確認
        if (!searchHistory.getUserId().equals(userId)) {
            throw new SecurityException("この検索履歴を削除する権限がありません。");
        }
        
        // 検索履歴を削除
        searchHistoryRepository.delete(searchHistory);
        return true;
    }

    @Override
    @Transactional
    public boolean updateSearchIndex(
            String resourceId,
            String resourceType,
            String title,
            String subtitle,
            String description,
            String attributes,
            String url,
            boolean isPublic,
            String accessRoles,
            UUID createdBy) {
        
        try {
            // 既存のインデックスを検索
            Optional<SearchIndex> existingIndex = searchIndexRepository.findByResourceIdAndResourceType(
                    resourceId, resourceType);
            
            SearchIndex searchIndex;
            if (existingIndex.isPresent()) {
                // 既存のインデックスを更新
                searchIndex = existingIndex.get();
                searchIndex.setTitle(title);
                searchIndex.setSubtitle(subtitle);
                searchIndex.setDescription(description);
                searchIndex.setAttributes(attributes);
                searchIndex.setUrl(url);
                searchIndex.setIsPublic(isPublic);
                searchIndex.setAccessRoles(accessRoles);
                searchIndex.setCreatedBy(createdBy);
            } else {
                // 新しいインデックスを作成
                searchIndex = SearchIndex.builder()
                        .resourceId(resourceId)
                        .resourceType(resourceType)
                        .title(title)
                        .subtitle(subtitle)
                        .description(description)
                        .attributes(attributes)
                        .url(url)
                        .isPublic(isPublic)
                        .accessRoles(accessRoles)
                        .createdBy(createdBy)
                        .build();
            }
            
            // インデックスを保存
            searchIndexRepository.save(searchIndex);
            return true;
        } catch (Exception e) {
            logger.error("検索インデックスの更新に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteSearchIndex(String resourceId, String resourceType) {
        try {
            // 指定されたリソースのインデックスを検索
            Optional<SearchIndex> index = searchIndexRepository.findByResourceIdAndResourceType(
                    resourceId, resourceType);
            
            // インデックスが存在する場合は削除
            if (index.isPresent()) {
                searchIndexRepository.delete(index.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("検索インデックスの削除に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    // ======== プライベートメソッド ========

    /**
     * 検索リクエストを検証
     */
    private void validateSearchRequest(SearchRequestDto searchRequestDto) {
        if (searchRequestDto.getQuery() == null || searchRequestDto.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("検索キーワードを指定してください。");
        }

        // リソースタイプのバリデーション
        if (searchRequestDto.getResourceTypes() != null) {
            List<String> validResourceTypes = Arrays.asList(
                    "engineers", "projects", "contracts", "invoices", "files", "clients", "matching");
            
            List<String> invalidTypes = searchRequestDto.getResourceTypes().stream()
                    .filter(type -> !validResourceTypes.contains(type))
                    .collect(Collectors.toList());
            
            if (!invalidTypes.isEmpty()) {
                throw new IllegalArgumentException("無効なリソースタイプが指定されています: " + String.join(", ", invalidTypes));
            }
        }
    }

    /**
     * 検索クエリを処理（MySQL全文検索用）
     */
    private String processSearchQuery(String query) {
        // 基本的なクエリの前処理
        String processed = query.trim();
        
        // MySQL全文検索用に特殊文字をエスケープ
        processed = processed.replaceAll("[+\\-><\\(\\)~*\"@]+", " ");
        
        // 各単語をAND検索として扱う
        String[] words = processed.split("\\s+");
        StringBuilder builder = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                if (builder.length() > 0) {
                    builder.append(" +");
                } else {
                    builder.append("+");
                }
                builder.append(word);
            }
        }
        
        return builder.toString();
    }

    /**
     * 通常の検索を実行
     */
    private SearchResultDto searchStandard(String processedQuery, List<String> resourceTypes, Pageable pageable) {
        // 全文検索を実行
        Page<SearchIndex> searchResults = searchIndexRepository.searchByQuery(processedQuery, resourceTypes, pageable);
        
        // 検索結果DTOの作成
        List<SearchResultDto.SearchResultItemDto> resultItems = searchResults.getContent().stream()
                .map(this::convertToResultItemDto)
                .collect(Collectors.toList());
        
        // ページネーション情報の設定
        SearchResultDto.PaginationInfo paginationInfo = new SearchResultDto.PaginationInfo();
        paginationInfo.setCurrentPage(searchResults.getNumber() + 1);
        paginationInfo.setPageSize(searchResults.getSize());
        paginationInfo.setTotalPages(searchResults.getTotalPages());
        paginationInfo.setTotalItems((int) searchResults.getTotalElements());
        
        // 結果DTOの作成
        SearchResultDto resultDto = new SearchResultDto();
        resultDto.setResults(resultItems);
        resultDto.setPagination(paginationInfo);
        
        return resultDto;
    }

    /**
     * リソースタイプ別にグループ化した検索を実行
     */
    private SearchResultDto searchGroupedByResourceType(String processedQuery, List<String> resourceTypes, Pageable pageable) {
        // 有効なリソースタイプの取得
        List<String> validResourceTypes = resourceTypes != null && !resourceTypes.isEmpty() ?
                resourceTypes : Arrays.asList("engineers", "projects", "contracts", "invoices", "files", "clients", "matching");
        
        Map<String, SearchResultDto.GroupedResultDto> groupedResults = new HashMap<>();
        int totalItems = 0;
        
        // 各リソースタイプごとに検索
        for (String resourceType : validResourceTypes) {
            Page<SearchIndex> typeResults = searchIndexRepository.searchByQueryAndResourceType(
                    processedQuery, resourceType, pageable);
            
            List<SearchResultDto.SearchResultItemDto> resultItems = typeResults.getContent().stream()
                    .map(this::convertToResultItemDto)
                    .collect(Collectors.toList());
            
            // リソースタイプの総件数を取得
            long totalForType = searchIndexRepository.countByQueryAndResourceType(processedQuery, resourceType);
            totalItems += resultItems.size();
            
            // グループ化された結果の作成
            SearchResultDto.GroupedResultDto groupedResult = new SearchResultDto.GroupedResultDto();
            groupedResult.setResults(resultItems);
            groupedResult.setCount(resultItems.size());
            groupedResult.setTotal((int) totalForType);
            
            groupedResults.put(resourceType, groupedResult);
        }
        
        // ページネーション情報の設定
        SearchResultDto.PaginationInfo paginationInfo = new SearchResultDto.PaginationInfo();
        paginationInfo.setCurrentPage(pageable.getPageNumber() + 1);
        paginationInfo.setPageSize(pageable.getPageSize());
        paginationInfo.setTotalItems(totalItems);
        // グループ化検索の場合、総ページ数は不明確なため1とする
        paginationInfo.setTotalPages(1);
        
        // 結果DTOの作成
        SearchResultDto resultDto = new SearchResultDto();
        resultDto.setGroupedResults(groupedResults);
        resultDto.setPagination(paginationInfo);
        
        return resultDto;
    }

    /**
     * SearchIndex エンティティを SearchResultItemDto に変換
     */
    private SearchResultDto.SearchResultItemDto convertToResultItemDto(SearchIndex index) {
        SearchResultDto.SearchResultItemDto itemDto = new SearchResultDto.SearchResultItemDto();
        itemDto.setId(index.getResourceId());
        itemDto.setType(index.getResourceType());
        itemDto.setTitle(index.getTitle());
        itemDto.setSubtitle(index.getSubtitle());
        itemDto.setDescription(index.getDescription());
        itemDto.setUrl(index.getUrl());
        
        // 属性の解析
        if (index.getAttributes() != null && !index.getAttributes().isEmpty()) {
            try {
                Map<String, Object> attributes = objectMapper.readValue(
                        index.getAttributes(), new TypeReference<Map<String, Object>>() {});
                itemDto.setAttributes(attributes);
            } catch (JsonProcessingException e) {
                logger.error("属性のJSONの解析に失敗しました: {}", e.getMessage());
                itemDto.setAttributes(new HashMap<>());
            }
        } else {
            itemDto.setAttributes(new HashMap<>());
        }
        
        // マッチングスコアは現在は固定値（実際の実装ではここにスコアリングロジックを追加）
        itemDto.setMatchingScore(1.0);
        
        return itemDto;
    }

    /**
     * 検索履歴を保存
     */
    private SearchHistory saveSearchHistory(SearchRequestDto searchRequestDto, UUID userId, int resultCount) {
        try {
            // JSONへの変換
            String resourceTypesJson = searchRequestDto.getResourceTypes() != null ?
                    objectMapper.writeValueAsString(searchRequestDto.getResourceTypes()) : null;
            String filtersJson = searchRequestDto.getFilters() != null ?
                    objectMapper.writeValueAsString(searchRequestDto.getFilters()) : null;
            
            // ソート情報の取得
            String sortField = null;
            String sortOrder = null;
            if (searchRequestDto.getSort() != null) {
                sortField = searchRequestDto.getSort().getField();
                sortOrder = searchRequestDto.getSort().getOrder();
            }
            
            // 検索履歴エンティティの作成
            SearchHistory searchHistory = SearchHistory.builder()
                    .userId(userId)
                    .query(searchRequestDto.getQuery())
                    .resourceTypes(resourceTypesJson)
                    .filters(filtersJson)
                    .sortField(sortField)
                    .sortOrder(sortOrder)
                    .groupByResourceType(searchRequestDto.getGroupByResourceType())
                    .resultCount(resultCount)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 検索履歴の保存
            return searchHistoryRepository.save(searchHistory);
        } catch (Exception e) {
            logger.error("検索履歴の保存に失敗しました: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * SearchHistory エンティティを SearchHistoryItemDto に変換
     */
    private SearchHistoryResponseDto.SearchHistoryItemDto convertToHistoryItemDto(SearchHistory history) {
        SearchHistoryResponseDto.SearchHistoryItemDto itemDto = new SearchHistoryResponseDto.SearchHistoryItemDto();
        itemDto.setId(history.getSearchId().toString());
        itemDto.setQuery(history.getQuery());
        itemDto.setCreatedAt(history.getCreatedAt());
        itemDto.setResultCount(history.getResultCount());
        
        // リソースタイプの変換
        if (history.getResourceTypes() != null && !history.getResourceTypes().isEmpty()) {
            try {
                List<String> resourceTypes = objectMapper.readValue(
                        history.getResourceTypes(), new TypeReference<List<String>>() {});
                itemDto.setResourceTypes(resourceTypes);
            } catch (JsonProcessingException e) {
                logger.error("リソースタイプのJSONの解析に失敗しました: {}", e.getMessage());
                itemDto.setResourceTypes(new ArrayList<>());
            }
        } else {
            itemDto.setResourceTypes(new ArrayList<>());
        }
        
        // フィルターの変換
        if (history.getFilters() != null && !history.getFilters().isEmpty()) {
            try {
                Map<String, Object> filters = objectMapper.readValue(
                        history.getFilters(), new TypeReference<Map<String, Object>>() {});
                itemDto.setFilters(filters);
            } catch (JsonProcessingException e) {
                logger.error("フィルターのJSONの解析に失敗しました: {}", e.getMessage());
                itemDto.setFilters(new HashMap<>());
            }
        } else {
            itemDto.setFilters(new HashMap<>());
        }
        
        return itemDto;
    }
}