package com.ses_mgr.common.service.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.search.SearchHistoryResponseDto;
import com.ses_mgr.common.dto.search.SearchRequestDto;
import com.ses_mgr.common.dto.search.SearchResultDto;
import com.ses_mgr.common.entity.search.SearchHistory;
import com.ses_mgr.common.entity.search.SearchIndex;
import com.ses_mgr.common.repository.search.SearchHistoryRepository;
import com.ses_mgr.common.repository.search.SearchIndexRepository;
import com.ses_mgr.common.service.search.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private SearchIndexRepository searchIndexRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SearchServiceImpl searchService;

    private UUID userId;
    private UUID searchId;
    private SearchHistory searchHistory;
    private SearchIndex searchIndex;
    private List<SearchIndex> searchResults;

    @BeforeEach
    void setUp() {
        // テスト用のIDを初期化
        userId = UUID.randomUUID();
        searchId = UUID.randomUUID();

        // サービスの設定値を注入
        ReflectionTestUtils.setField(searchService, "maxPageSize", 50);
        ReflectionTestUtils.setField(searchService, "defaultPageSize", 20);

        // テスト用の検索履歴を作成
        searchHistory = new SearchHistory();
        searchHistory.setSearchId(searchId);
        searchHistory.setUserId(userId);
        searchHistory.setQuery("Java 開発");
        searchHistory.setResourceTypes("[\"engineers\", \"projects\"]");
        searchHistory.setFilters("{\"skills\": [\"Java\", \"Spring\"]}");
        searchHistory.setSortField("matching_score");
        searchHistory.setSortOrder("desc");
        searchHistory.setGroupByResourceType(false);
        searchHistory.setResultCount(2);
        searchHistory.setCreatedAt(LocalDateTime.now());

        // テスト用の検索インデックスを作成
        searchIndex = new SearchIndex();
        searchIndex.setIndexId(1L);
        searchIndex.setResourceId("e12345");
        searchIndex.setResourceType("engineers");
        searchIndex.setTitle("鈴木 一郎");
        searchIndex.setSubtitle("Javaエンジニア");
        searchIndex.setDescription("Java, Spring Bootでの開発経験10年。金融系システム開発のスペシャリスト。");
        searchIndex.setAttributes("{\"skills\": [\"Java\", \"Spring\", \"MySQL\", \"AWS\"], \"experience_years\": 10}");
        searchIndex.setUrl("/engineers/e12345");
        searchIndex.setIsPublic(false);
        searchIndex.setAccessRoles("[\"ROLE_ADMIN\", \"ROLE_MANAGER\"]");
        searchIndex.setCreatedBy(UUID.randomUUID());
        searchIndex.setUpdatedAt(LocalDateTime.now());

        // テスト用の検索結果リストを作成
        searchResults = new ArrayList<>();
        searchResults.add(searchIndex);

        SearchIndex searchIndex2 = new SearchIndex();
        searchIndex2.setIndexId(2L);
        searchIndex2.setResourceId("p67890");
        searchIndex2.setResourceType("projects");
        searchIndex2.setTitle("金融系Webアプリケーション開発");
        searchIndex2.setSubtitle("株式会社ABCテクノロジー");
        searchIndex2.setDescription("Java / Spring Bootを使用した金融系Webアプリケーションの開発案件。");
        searchIndex2.setAttributes("{\"skills\": [\"Java\", \"Spring Boot\", \"MySQL\", \"AWS\"], \"location\": \"東京\"}");
        searchIndex2.setUrl("/projects/p67890");
        searchIndex2.setIsPublic(true);
        searchIndex2.setAccessRoles("[\"ROLE_ADMIN\", \"ROLE_MANAGER\", \"ROLE_USER\"]");
        searchIndex2.setCreatedBy(UUID.randomUUID());
        searchIndex2.setUpdatedAt(LocalDateTime.now());
        
        searchResults.add(searchIndex2);
    }

    @Test
    void search_WithValidRequest_ShouldReturnResults() {
        // 検索リクエストの作成
        SearchRequestDto requestDto = new SearchRequestDto();
        requestDto.setQuery("Java 開発");
        requestDto.setResourceTypes(Arrays.asList("engineers", "projects"));
        requestDto.setGroupByResourceType(false);
        requestDto.setSaveSearch(true);

        // モックの設定
        when(searchIndexRepository.searchByQuery(anyString(), anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(searchResults));
        when(searchHistoryRepository.save(any(SearchHistory.class)))
                .thenReturn(searchHistory);

        // 検索実行
        SearchResultDto result = searchService.search(requestDto, userId);

        // 検証
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        assertEquals("e12345", result.getResults().get(0).getId());
        assertEquals("engineers", result.getResults().get(0).getType());
        assertEquals("鈴木 一郎", result.getResults().get(0).getTitle());
        assertEquals("Javaエンジニア", result.getResults().get(0).getSubtitle());
        assertEquals("p67890", result.getResults().get(1).getId());
        assertEquals("projects", result.getResults().get(1).getType());

        // ページネーション情報の検証
        assertNotNull(result.getPagination());
        assertEquals(1, result.getPagination().getCurrentPage());
        assertEquals(2, result.getPagination().getPageSize());

        // 検索履歴保存の検証
        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void search_WithGrouping_ShouldReturnGroupedResults() {
        // 検索リクエストの作成
        SearchRequestDto requestDto = new SearchRequestDto();
        requestDto.setQuery("Java 開発");
        requestDto.setResourceTypes(Arrays.asList("engineers", "projects"));
        requestDto.setGroupByResourceType(true);
        requestDto.setSaveSearch(false);

        // モックの設定 - リソースタイプごとの検索結果
        // engineers リソースタイプの検索結果
        when(searchIndexRepository.searchByQueryAndResourceType(anyString(), eq("engineers"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(searchResults.get(0))));
        when(searchIndexRepository.countByQueryAndResourceType(anyString(), eq("engineers")))
                .thenReturn(1L);

        // projects リソースタイプの検索結果
        when(searchIndexRepository.searchByQueryAndResourceType(anyString(), eq("projects"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(searchResults.get(1))));
        when(searchIndexRepository.countByQueryAndResourceType(anyString(), eq("projects")))
                .thenReturn(1L);

        // 検索実行
        SearchResultDto result = searchService.search(requestDto, userId);

        // 検証
        assertNotNull(result);
        assertNull(result.getResults()); // グループ化した場合は results は null
        assertNotNull(result.getGroupedResults());
        assertEquals(2, result.getGroupedResults().size());
        
        // engineers グループの検証
        assertTrue(result.getGroupedResults().containsKey("engineers"));
        assertNotNull(result.getGroupedResults().get("engineers").getResults());
        assertEquals(1, result.getGroupedResults().get("engineers").getResults().size());
        assertEquals("e12345", result.getGroupedResults().get("engineers").getResults().get(0).getId());
        
        // projects グループの検証
        assertTrue(result.getGroupedResults().containsKey("projects"));
        assertNotNull(result.getGroupedResults().get("projects").getResults());
        assertEquals(1, result.getGroupedResults().get("projects").getResults().size());
        assertEquals("p67890", result.getGroupedResults().get("projects").getResults().get(0).getId());

        // 検索履歴は保存されない
        verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void search_WithInvalidQuery_ShouldThrowException() {
        // 空のクエリでリクエストを作成
        SearchRequestDto requestDto = new SearchRequestDto();
        requestDto.setQuery("");

        // 検索実行と例外の検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            searchService.search(requestDto, userId);
        });
        
        assertEquals("検索キーワードを指定してください。", exception.getMessage());
    }

    @Test
    void getSearchHistory_ShouldReturnHistoryItems() {
        // テスト用の検索履歴リストを作成
        List<SearchHistory> historyList = new ArrayList<>();
        historyList.add(searchHistory);

        // モックの設定
        when(searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(historyList));

        // 検索履歴取得
        SearchHistoryResponseDto result = searchService.getSearchHistory(userId, 1, 20);

        // 検証
        assertNotNull(result);
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
        assertEquals(searchId.toString(), result.getHistory().get(0).getId());
        assertEquals("Java 開発", result.getHistory().get(0).getQuery());
        assertEquals(2, result.getHistory().get(0).getResultCount());
        
        // ページネーション情報の検証
        assertNotNull(result.getPagination());
        assertEquals(1, result.getPagination().getCurrentPage());
        assertEquals(1, result.getPagination().getPageSize());
        assertEquals(1, result.getPagination().getTotalPages());
        assertEquals(1, result.getPagination().getTotalItems());
    }

    @Test
    void deleteSearchHistory_WithValidId_ShouldReturnTrue() {
        // モックの設定
        when(searchHistoryRepository.findById(searchId))
                .thenReturn(Optional.of(searchHistory));

        // 検索履歴削除
        boolean result = searchService.deleteSearchHistory(searchId, userId);

        // 検証
        assertTrue(result);
        verify(searchHistoryRepository).delete(searchHistory);
    }

    @Test
    void deleteSearchHistory_WithInvalidId_ShouldThrowException() {
        // 存在しない検索履歴IDで設定
        when(searchHistoryRepository.findById(searchId))
                .thenReturn(Optional.empty());

        // 検索履歴削除と例外の検証
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            searchService.deleteSearchHistory(searchId, userId);
        });
        
        assertEquals("指定された検索履歴が存在しません。", exception.getMessage());
    }

    @Test
    void deleteSearchHistory_WithWrongUser_ShouldThrowException() {
        // 別のユーザーIDを設定
        UUID differentUserId = UUID.randomUUID();
        
        // 検索履歴は存在するが、別のユーザーのものとして設定
        when(searchHistoryRepository.findById(searchId))
                .thenReturn(Optional.of(searchHistory));

        // 検索履歴削除と例外の検証
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            searchService.deleteSearchHistory(searchId, differentUserId);
        });
        
        assertEquals("この検索履歴を削除する権限がありません。", exception.getMessage());
    }

    @Test
    void updateSearchIndex_WithNewResource_ShouldCreateIndex() {
        // 新しいリソースとして設定
        when(searchIndexRepository.findByResourceIdAndResourceType("e12345", "engineers"))
                .thenReturn(Optional.empty());
        when(searchIndexRepository.save(any(SearchIndex.class)))
                .thenReturn(searchIndex);

        // 検索インデックス更新
        boolean result = searchService.updateSearchIndex(
                "e12345", "engineers", "鈴木 一郎", "Javaエンジニア",
                "Java, Spring Bootでの開発経験10年。", "{\"skills\": [\"Java\", \"Spring\"]}",
                "/engineers/e12345", false, "[\"ROLE_ADMIN\"]", userId);

        // 検証
        assertTrue(result);
        verify(searchIndexRepository).save(any(SearchIndex.class));
    }

    @Test
    void updateSearchIndex_WithExistingResource_ShouldUpdateIndex() {
        // 既存のリソースとして設定
        when(searchIndexRepository.findByResourceIdAndResourceType("e12345", "engineers"))
                .thenReturn(Optional.of(searchIndex));
        when(searchIndexRepository.save(any(SearchIndex.class)))
                .thenReturn(searchIndex);

        // 検索インデックス更新
        boolean result = searchService.updateSearchIndex(
                "e12345", "engineers", "鈴木 一郎（更新）", "Javaエンジニア",
                "Java, Spring Bootでの開発経験10年。", "{\"skills\": [\"Java\", \"Spring\"]}",
                "/engineers/e12345", false, "[\"ROLE_ADMIN\"]", userId);

        // 検証
        assertTrue(result);
        verify(searchIndexRepository).save(searchIndex);
        assertEquals("鈴木 一郎（更新）", searchIndex.getTitle());
    }

    @Test
    void deleteSearchIndex_WithExistingResource_ShouldReturnTrue() {
        // 既存のリソースとして設定
        when(searchIndexRepository.findByResourceIdAndResourceType("e12345", "engineers"))
                .thenReturn(Optional.of(searchIndex));

        // 検索インデックス削除
        boolean result = searchService.deleteSearchIndex("e12345", "engineers");

        // 検証
        assertTrue(result);
        verify(searchIndexRepository).delete(searchIndex);
    }

    @Test
    void deleteSearchIndex_WithNonExistingResource_ShouldReturnFalse() {
        // 存在しないリソースとして設定
        when(searchIndexRepository.findByResourceIdAndResourceType("e99999", "engineers"))
                .thenReturn(Optional.empty());

        // 検索インデックス削除
        boolean result = searchService.deleteSearchIndex("e99999", "engineers");

        // 検証
        assertFalse(result);
        verify(searchIndexRepository, never()).delete(any(SearchIndex.class));
    }
}