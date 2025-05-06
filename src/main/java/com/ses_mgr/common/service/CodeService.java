package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.CodeCategoryDto;
import com.ses_mgr.common.dto.CodeValueDetailDto;
import com.ses_mgr.common.dto.CodeValueDto;

import java.util.List;

/**
 * コード値サービスインターフェース
 * Code service interface
 */
public interface CodeService {

    /**
     * コード値カテゴリ一覧を取得
     * Get all code categories
     *
     * @return コード値カテゴリのリスト
     */
    List<CodeCategoryDto> getAllCategories();

    /**
     * 特定カテゴリのコード値一覧を取得
     * Get code values by category
     *
     * @param category   カテゴリID
     * @param keyword    検索キーワード（オプション）
     * @param parent     親コード値（オプション）
     * @param activeOnly アクティブなコード値のみ取得するフラグ
     * @return コード値のリスト
     */
    List<CodeValueDto> getCodesByCategory(String category, String keyword, String parent, boolean activeOnly);

    /**
     * カテゴリ情報を取得
     * Get category information
     *
     * @param category カテゴリID
     * @return カテゴリ情報
     */
    CodeCategoryDto getCategoryInfo(String category);

    /**
     * 特定コード値の詳細を取得
     * Get code value detail
     *
     * @param category カテゴリID
     * @param code     コード値
     * @return コード値詳細
     */
    CodeValueDetailDto getCodeDetail(String category, String code);
}