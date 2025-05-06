package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.CodeService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * テスト用コード値REST APIコントローラ（認証なし）
 * Test Code REST API controller (no authentication)
 */
@RestController
@RequestMapping("/api/v1/public/codes")
public class TestCodeRestController {

    private static final Logger logger = LoggerFactory.getLogger(TestCodeRestController.class);

    private final CodeService codeService;

    @Autowired
    public TestCodeRestController(CodeService codeService) {
        this.codeService = codeService;
    }

    /**
     * コード値カテゴリ一覧取得
     * Get all code categories
     *
     * @return コード値カテゴリのリスト
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CodeCategoryDto> categories = codeService.getAllCategories();
            return ResponseEntity.ok(ApiResponseDto.success(categories));
        } catch (Exception e) {
            logger.error("コード値カテゴリ一覧取得エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "コード値カテゴリ一覧の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定カテゴリのコード値一覧取得
     * Get code values by category
     *
     * @param category   カテゴリID
     * @param keyword    検索キーワード（オプション）
     * @param parent     親コード値（オプション）
     * @param activeOnly アクティブなコード値のみ取得するフラグ（デフォルト: true）
     * @return コード値一覧
     */
    @GetMapping("/{category}")
    public ResponseEntity<?> getCodesByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String parent,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        try {
            // カテゴリ情報の取得
            CodeCategoryDto categoryInfo = codeService.getCategoryInfo(category);
            
            // コード値一覧の取得
            List<CodeValueDto> codes = codeService.getCodesByCategory(category, keyword, parent, activeOnly);
            
            // レスポンスの作成
            CodeValueListResponseDto responseDto = CodeValueListResponseDto.builder()
                    .category(categoryInfo)
                    .codes(codes)
                    .build();
            
            return ResponseEntity.ok(ApiResponseDto.success(responseDto));
        } catch (EntityNotFoundException e) {
            logger.error("指定されたカテゴリが見つかりません: {}", category, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定されたカテゴリが存在しません。"));
        } catch (Exception e) {
            logger.error("コード値一覧取得エラー: カテゴリ={}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "コード値一覧の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定コード値の詳細取得
     * Get code value detail
     *
     * @param category カテゴリID
     * @param code     コード値
     * @return コード値詳細
     */
    @GetMapping("/{category}/{code}")
    public ResponseEntity<?> getCodeDetail(
            @PathVariable String category,
            @PathVariable String code) {
        try {
            CodeValueDetailDto codeDetail = codeService.getCodeDetail(category, code);
            return ResponseEntity.ok(ApiResponseDto.success(codeDetail));
        } catch (EntityNotFoundException e) {
            logger.error("指定されたカテゴリまたはコード値が見つかりません: カテゴリ={}, コード={}", category, code, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定されたカテゴリまたはコード値が存在しません。"));
        } catch (Exception e) {
            logger.error("コード値詳細取得エラー: カテゴリ={}, コード={}", category, code, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "コード値詳細の取得中にエラーが発生しました。"));
        }
    }
}