package jp.co.example.sesapp.common.auth.domain;

/**
 * アクション種別の列挙型。
 */
public enum ActionType {
    CREATE,       // 作成
    READ,         // 読取
    UPDATE,       // 更新
    DELETE,       // 削除
    UPDATE_SELF,  // 自分自身の情報更新
    APPROVE,      // 承認
    REJECT,       // 却下
    EXPORT,       // エクスポート
    IMPORT        // インポート
}