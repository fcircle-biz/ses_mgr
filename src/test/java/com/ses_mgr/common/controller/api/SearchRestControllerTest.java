package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.search.SearchHistoryResponseDto;
import com.ses_mgr.common.dto.search.SearchRequestDto;
import com.ses_mgr.common.dto.search.SearchResultDto;
import com.ses_mgr.common.service.search.SearchService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SearchRestController searchRestController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UUID searchId;
    private SearchRequestDto searchRequestDto;
    private SearchResultDto searchResultDto;
    private SearchHistoryResponseDto searchHistoryResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // JSR310対応（LocalDateTimeなど）

        // テスト用のIDを初期化
        userId = UUID.randomUUID();
        searchId = UUID.randomUUID();

        // テスト用の検索リクエスト作成
        searchRequestDto = new SearchRequestDto();
        searchRequestDto.setQuery("Java 開発");
        searchRequestDto.setResourceTypes(Arrays.asList("engineers", "projects"));
        searchRequestDto.setFilters(new HashMap<>());
        searchRequestDto.setGroupByResourceType(false);
        searchRequestDto.setSaveSearch(true);

        // テスト用の検索結果作成
        searchResultDto = new SearchResultDto();
        
        List<SearchResultDto.SearchResultItemDto> results = new ArrayList<>();
        
        SearchResultDto.SearchResultItemDto item1 = new SearchResultDto.SearchResultItemDto();
        item1.setId("e12345");
        item1.setType("engineers");
        item1.setTitle("鈴木 一郎");
        item1.setSubtitle("Javaエンジニア");
        item1.setDescription("Java, Spring Bootでの開発経験10年。金融系システム開発のスペシャリスト。");
        item1.setUrl("/engineers/e12345");
        item1.setMatchingScore(0.95);
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("skills", Arrays.asList("Java", "Spring", "MySQL", "AWS"));
        attributes1.put("experience_years", 10);
        item1.setAttributes(attributes1);
        
        SearchResultDto.SearchResultItemDto item2 = new SearchResultDto.SearchResultItemDto();
        item2.setId("p67890");
        item2.setType("projects");
        item2.setTitle("金融系Webアプリケーション開発");
        item2.setSubtitle("株式会社ABCテクノロジー");
        item2.setDescription("Java / Spring Bootを使用した金融系Webアプリケーションの開発案件。");
        item2.setUrl("/projects/p67890");
        item2.setMatchingScore(0.92);
        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("skills", Arrays.asList("Java", "Spring Boot", "MySQL", "AWS"));
        attributes2.put("location", "東京");
        item2.setAttributes(attributes2);
        
        results.add(item1);
        results.add(item2);
        
        searchResultDto.setResults(results);
        
        SearchResultDto.PaginationInfo paginationInfo = new SearchResultDto.PaginationInfo();
        paginationInfo.setCurrentPage(1);
        paginationInfo.setPageSize(20);
        paginationInfo.setTotalPages(1);
        paginationInfo.setTotalItems(2);
        
        searchResultDto.setPagination(paginationInfo);
        searchResultDto.setSearchId(searchId.toString());
        searchResultDto.setQueryTimeMs(120L);

        // テスト用の検索履歴レスポンス作成
        searchHistoryResponseDto = new SearchHistoryResponseDto();
        
        List<SearchHistoryResponseDto.SearchHistoryItemDto> historyItems = new ArrayList<>();
        
        SearchHistoryResponseDto.SearchHistoryItemDto historyItem = new SearchHistoryResponseDto.SearchHistoryItemDto();
        historyItem.setId(searchId.toString());
        historyItem.setQuery("Java 開発");
        historyItem.setResourceTypes(Arrays.asList("engineers", "projects"));
        historyItem.setFilters(Collections.singletonMap("skills", Arrays.asList("Java", "Spring")));
        historyItem.setCreatedAt(LocalDateTime.now());
        historyItem.setResultCount(2);
        
        historyItems.add(historyItem);
        
        searchHistoryResponseDto.setHistory(historyItems);
        
        SearchHistoryResponseDto.PaginationInfo historyPagination = new SearchHistoryResponseDto.PaginationInfo();
        historyPagination.setCurrentPage(1);
        historyPagination.setPageSize(20);
        historyPagination.setTotalPages(1);
        historyPagination.setTotalItems(1);
        
        searchHistoryResponseDto.setPagination(historyPagination);
    }

    @Test
    void search_WithValidRequest_ShouldReturnResults() throws Exception {
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定
        when(searchService.search(any(SearchRequestDto.class), eq(userId))).thenReturn(searchResultDto);

        // テスト実行
        mockMvc.perform(post("/api/v1/common/search")
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.results").isArray())
                .andExpect(jsonPath("$.data.results.length()").value(2))
                .andExpect(jsonPath("$.data.results[0].id").value("e12345"))
                .andExpect(jsonPath("$.data.results[0].type").value("engineers"))
                .andExpect(jsonPath("$.data.results[0].title").value("鈴木 一郎"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.data.searchId").value(searchId.toString()))
                .andExpect(jsonPath("$.success").value(true));

        // 検証
        verify(searchService).search(any(SearchRequestDto.class), eq(userId));
    }

    @Test
    void search_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // 有効なクエリだが検索処理で例外が発生するケース
        searchRequestDto.setQuery("invalid query");
        
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定
        when(searchService.search(any(SearchRequestDto.class), eq(userId)))
                .thenThrow(new IllegalArgumentException("検索キーワードが不正です。"));

        // テスト実行
        mockMvc.perform(post("/api/v1/common/search")
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequestDto)))
                .andDo(result -> {
                    System.out.println("Response content: " + result.getResponse().getContentAsString());
                    System.out.println("Response status: " + result.getResponse().getStatus());
                })
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("INVALID_QUERY"))
                .andExpect(jsonPath("$.error.message").value("検索キーワードが不正です。"))
                .andExpect(jsonPath("$.success").value(false));

        // 検証
        verify(searchService).search(any(SearchRequestDto.class), eq(userId));
    }

    @Test
    void getSearchHistory_ShouldReturnHistoryItems() throws Exception {
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定
        when(searchService.getSearchHistory(eq(userId), eq(1), eq(20))).thenReturn(searchHistoryResponseDto);

        // テスト実行
        mockMvc.perform(get("/api/v1/common/search/history")
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                })
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.history").isArray())
                .andExpect(jsonPath("$.data.history.length()").value(1))
                .andExpect(jsonPath("$.data.history[0].id").value(searchId.toString()))
                .andExpect(jsonPath("$.data.history[0].query").value("Java 開発"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.totalItems").value(1))
                .andExpect(jsonPath("$.success").value(true));

        // 検証
        verify(searchService).getSearchHistory(eq(userId), eq(1), eq(20));
    }

    @Test
    void deleteSearchHistory_WithValidId_ShouldReturnNoContent() throws Exception {
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定
        when(searchService.deleteSearchHistory(eq(searchId), eq(userId))).thenReturn(true);

        // テスト実行
        mockMvc.perform(delete("/api/v1/common/search/history/" + searchId)
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                }))
                .andExpect(status().isNoContent());

        // 検証
        verify(searchService).deleteSearchHistory(eq(searchId), eq(userId));
    }

    @Test
    void deleteSearchHistory_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定 - 存在しない検索履歴
        when(searchService.deleteSearchHistory(eq(searchId), eq(userId)))
                .thenThrow(new EntityNotFoundException("指定された検索履歴が存在しません。"));

        // テスト実行
        mockMvc.perform(delete("/api/v1/common/search/history/" + searchId)
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("指定された検索履歴が存在しません。"))
                .andExpect(jsonPath("$.success").value(false));

        // 検証
        verify(searchService).deleteSearchHistory(eq(searchId), eq(userId));
    }

    @Test
    void deleteSearchHistory_WithNoPermission_ShouldReturnForbidden() throws Exception {
        // Authentication モックの設定
        when(authentication.getPrincipal()).thenReturn(userId.toString());
        
        // SearchService モックの設定 - 権限がない
        when(searchService.deleteSearchHistory(eq(searchId), eq(userId)))
                .thenThrow(new SecurityException("この検索履歴を削除する権限がありません。"));

        // テスト実行
        mockMvc.perform(delete("/api/v1/common/search/history/" + searchId)
                .with(request -> {
                    request.setUserPrincipal(authentication);
                    return request;
                }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("この検索履歴を削除する権限がありません。"))
                .andExpect(jsonPath("$.success").value(false));

        // 検証
        verify(searchService).deleteSearchHistory(eq(searchId), eq(userId));
    }
}