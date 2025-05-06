package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.search.SearchHistoryResponseDto;
import com.ses_mgr.common.dto.search.SearchRequestDto;
import com.ses_mgr.common.dto.search.SearchResultDto;
import com.ses_mgr.common.service.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;

/**
 * 検索REST APIコントローラー
 * 横断検索機能のエンドポイントを提供します
 */
@RestController
@RequestMapping("/api/v1/common/search")
public class SearchRestController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRestController.class);

    private final SearchService searchService;

    @Autowired
    public SearchRestController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 横断検索を実行
     *
     * @param searchRequestDto 検索リクエスト
     * @param authentication 認証情報
     * @return 検索結果
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<SearchResultDto>> search(
            @Valid @RequestBody SearchRequestDto searchRequestDto,
            Authentication authentication) {
        
        try {
            logger.info("検索リクエスト: query={}, resourceTypes={}", 
                    searchRequestDto.getQuery(), 
                    searchRequestDto.getResourceTypes());
            
            // 認証済みユーザーのIDを取得
            UUID userId = getUserId(authentication);
            
            // 検索実行
            SearchResultDto searchResult = searchService.search(searchRequestDto, userId);
            
            // 正常終了
            return ResponseEntity.ok(ApiResponseDto.success(searchResult));
        } catch (IllegalArgumentException e) {
            logger.warn("検索リクエストが不正: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error("INVALID_QUERY", e.getMessage()));
        } catch (Exception e) {
            logger.error("検索処理中にエラーが発生: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDto.error("SEARCH_ERROR", "検索処理中にエラーが発生しました。"));
        }
    }

    /**
     * 検索履歴を取得
     *
     * @param page ページ番号（デフォルト: 1）
     * @param pageSize ページサイズ（デフォルト: 20）
     * @param authentication 認証情報
     * @return 検索履歴
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDto<SearchHistoryResponseDto>> getSearchHistory(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int pageSize,
            Authentication authentication) {
        
        try {
            // 認証済みユーザーのIDを取得
            UUID userId = getUserId(authentication);
            
            // 検索履歴取得
            SearchHistoryResponseDto historyResponse = searchService.getSearchHistory(userId, page, pageSize);
            
            // 正常終了
            return ResponseEntity.ok(ApiResponseDto.success(historyResponse));
        } catch (Exception e) {
            logger.error("検索履歴の取得中にエラーが発生: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDto.error("HISTORY_ERROR", "検索履歴の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 検索履歴を削除
     *
     * @param id 検索履歴ID
     * @param authentication 認証情報
     * @return 削除結果
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteSearchHistory(
            @PathVariable String id,
            Authentication authentication) {
        
        try {
            // 認証済みユーザーのIDを取得
            UUID userId = getUserId(authentication);
            
            // 検索履歴IDをパース
            UUID searchId;
            try {
                searchId = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        ApiResponseDto.error("INVALID_ID", "無効な検索履歴IDが指定されています。"));
            }
            
            // 検索履歴削除
            boolean result = searchService.deleteSearchHistory(searchId, userId);
            
            if (result) {
                // 正常終了（コンテンツなし）
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        ApiResponseDto.error("DELETE_ERROR", "検索履歴の削除に失敗しました。"));
            }
        } catch (EntityNotFoundException e) {
            logger.warn("検索履歴が見つかりません: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定された検索履歴が存在しません。"));
        } catch (SecurityException e) {
            logger.warn("検索履歴の削除権限がありません: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponseDto.error("FORBIDDEN", "この検索履歴を削除する権限がありません。"));
        } catch (Exception e) {
            logger.error("検索履歴の削除中にエラーが発生: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDto.error("DELETE_ERROR", "検索履歴の削除中にエラーが発生しました。"));
        }
    }

    /**
     * 認証済みユーザーのIDを取得
     */
    private UUID getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new SecurityException("認証が必要です。");
        }
        
        Object principal = authentication.getPrincipal();
        
        // principal オブジェクトの型によってユーザーID取得方法が異なる可能性がある
        // 実際の認証システムの実装に合わせて調整が必要
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) principal;
            return UUID.fromString(userDetails.getUsername());
        } else {
            // 簡易実装（実際の環境に合わせて修正が必要）
            return UUID.fromString(principal.toString());
        }
    }
}