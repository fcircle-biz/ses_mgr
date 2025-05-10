# UI設計・モックアップ作成ガイドライン

> **重要**: コミットおよびプッシュは、必ず明示的な指示があるまで実行しないでください。  
> 担当者からの具体的な許可なく変更をリポジトリに反映することは厳禁です。

## 目次

1. [概要](#概要)
2. [UI設計書テンプレート利用手順](#ui設計書テンプレート利用手順)
3. [モックアップテンプレート利用手順](#モックアップテンプレート利用手順)
4. [モックアップ作成時の注意事項](#モックアップ作成時の注意事項)
5. [UI設計書への統合時の注意事項](#ui設計書への統合時の注意事項)
6. [その他の注意事項](#その他の注意事項)

## 概要

本ドキュメントは、SES業務システムのUI設計およびモックアップ作成に関するガイドラインをまとめたものです。UI設計書の作成、モックアップの実装、それらの統合、およびバージョン管理において留意すべき点を記載しています。本ガイドラインに従うことで、一貫性のあるUIと効率的な開発プロセスを実現することを目指します。

## UI設計書テンプレート利用手順

### テンプレートの概要

`UI設計書テンプレート.html`は、統一的な画面設計ドキュメントを作成するためのテンプレートです。このテンプレートを使用することで、一貫性のあるドキュメントを効率的に作成することができます。

### テンプレートの使用方法

1. `/docs/00_作業手順/UI設計/UI設計書テンプレート.html`をコピーして、新しい画面設計ドキュメントを作成する機能領域の画面フォルダに配置し、ファイル名をリネームします。例: `XXX-001_○○○画面.html`

2. 新しく作成したファイル内のプレースホルダーを、実際の画面情報に置き換えます。以下のプレースホルダーを編集してください：

#### 基本情報のプレースホルダー

| プレースホルダー | 説明 | 例 |
|-----------------|-----|-----|
| `{PARENT_LINK}` | 親画面設計書へのリンクパス | `案件管理画面設計.html` |
| `{PARENT_NAME}` | 親カテゴリ名 | `案件管理` |
| `{SCREEN_ID}` | 画面ID | `PRJ-001` |
| `{SCREEN_NAME}` | 画面の完全名称 | `案件一覧画面` |
| `{SCREEN_NAME_SHORT}` | 画面の短縮名称 | `案件一覧` |
| `{SCREEN_DESCRIPTION}` | 画面の簡潔な説明 | `全案件の一覧表示・検索・フィルタリングを行う画面` |
| `{TARGET_USERS}` | 対象ユーザー | `営業担当者、営業管理者、経営層` |
| `{PREVIOUS_SCREEN}` | 遷移元画面 | `CMN-002: ダッシュボード` |
| `{NEXT_SCREEN}` | 遷移先画面 | `PRJ-003: 案件詳細、PRJ-002: 案件登録` |

#### モックアップ関連のプレースホルダー

| プレースホルダー | 説明 | 例 |
|-----------------|-----|-----|
| `{MOCKUP_CONTAINER_ID}` | モックアップコンテナのID | `project-list-mockup` |
| `{MOCKUP_DESCRIPTION}` | モックアップの説明 | `全案件の一覧表示、検索、フィルタリングが可能な画面です。` |

#### コンテンツ関連のプレースホルダー

| プレースホルダー | 説明 |
|-----------------|-----|
| `{SCREEN_COMPONENTS}` | 画面構成要素の詳細テーブル |
| `{SCREEN_BEHAVIOR}` | 画面動作仕様の説明 |
| `{NOTES}` | 画面に関する特記事項 |
| `{CREATION_DATE}` | 作成日（YYYY年MM月DD日形式） |
| `{VERSION}` | ドキュメントのバージョン |

### 画面構成要素の記述例

`{SCREEN_COMPONENTS}`には、以下のような形式でコンポーネントのテーブルを記述します：

```html
<h3>1. 検索・フィルター部分</h3>
<table>
    <tr>
        <th>項目ID</th>
        <th>項目名</th>
        <th>種類</th>
        <th>説明</th>
    </tr>
    <tr>
        <td>xxx_001</td>
        <td>検索ボックス</td>
        <td>テキスト入力</td>
        <td>フリーワード検索</td>
    </tr>
    <!-- 他の項目を追加 -->
</table>
```

### 画面動作仕様の記述例

`{SCREEN_BEHAVIOR}`には、以下のような形式で動作仕様を記述します：

```html
<ol>
    <li>画面初期表示時は、デフォルト条件でデータを表示する</li>
    <li>検索条件を指定して検索ボタンをクリックすると、条件に合致するデータが表示される</li>
    <!-- 他の動作仕様を追加 -->
</ol>
```

### 注意事項

1. モックアップビューワーを正しく機能させるためには、対応するモックアップHTMLファイルが `mockups/` ディレクトリに存在する必要があります。

2. CSS（mockup-viewer.css）とJavaScript（mockup-viewer.js）ファイルへのパスが正しいことを確認してください。

3. 画面IDは既存の命名規則に従って一貫性を保つようにしてください。

## モックアップテンプレート利用手順

### テンプレートの概要

`モックアップテンプレート.html`は、統一的なモックアップを作成するためのテンプレートです。このテンプレートを使用することで、一貫性のあるUI設計を効率的に実装することができます。

### テンプレートの使用方法

1. `/docs/00_作業手順/UI設計/モックアップテンプレート.html`をコピーして、新しいモックアップを作成する機能領域のmockupsフォルダに配置し、ファイル名をリネームします。
   例: `XXX-001.html`（XXXは機能領域の略称、001は連番）

2. 新しく作成したファイル内のプレースホルダーを、実際の画面情報に置き換えます。

### プレースホルダー一覧

#### 基本情報のプレースホルダー

| プレースホルダー | 説明 | 例 |
|-----------------|-----|-----|
| `{SCREEN_TITLE}` | ブラウザタブに表示されるタイトル | `案件一覧` |
| `{SCREEN_NAME}` | 画面に表示される名称 | `案件一覧` |
| `{USER_NAME}` | ログインユーザー名 | `佐藤 一郎` |
| `{USER_INITIALS}` | ユーザーのイニシャル（アバター表示用） | `SI` |

#### ナビゲーション関連のプレースホルダー

| プレースホルダー | 説明 | 例 |
|-----------------|-----|-----|
| `{DASHBOARD_ACTIVE}` | ダッシュボードメニューのアクティブ状態 | `active` または空白 |
| `{PROJECT_ACTIVE}` | 案件管理メニューのアクティブ状態 | `active` または空白 |
| `{PROJECT_LIST_ACTIVE}` | 案件一覧サブメニューのアクティブ状態 | `active` または空白 |
| `{PROJECT_REGISTER_ACTIVE}` | 新規案件登録サブメニューのアクティブ状態 | `active` または空白 |
| `{ENGINEER_ACTIVE}` | 技術者管理メニューのアクティブ状態 | `active` または空白 |
| `{ENGINEER_LIST_ACTIVE}` | 技術者一覧サブメニューのアクティブ状態 | `active` または空白 |
| `{SKILL_MANAGE_ACTIVE}` | スキル管理サブメニューのアクティブ状態 | `active` または空白 |
| `{MATCHING_ACTIVE}` | マッチングメニューのアクティブ状態 | `active` または空白 |
| `{CONTRACT_ACTIVE}` | 契約管理メニューのアクティブ状態 | `active` または空白 |
| `{ADDITIONAL_MENU_ITEMS}` | 追加メニュー項目（必要に応じて） | 新しいメニュー項目のHTML |
| `{BREADCRUMB_ITEMS}` | パンくずリストの中間項目 | `<li class="breadcrumb-item"><a href="#">案件管理</a></li>` |

#### コンテンツ関連のプレースホルダー

| プレースホルダー | 説明 | 例 |
|-----------------|-----|-----|
| `{HEADER_BUTTONS}` | ヘッダー部分のボタン | `<button class="btn btn-primary"><i class="bi bi-plus-lg me-1"></i>新規登録</button>` |
| `{MAIN_CONTENT}` | メインコンテンツ領域 | 検索フォーム、テーブル、カードなどのHTML |
| `{CUSTOM_STYLES}` | カスタムCSSスタイル | 画面固有のスタイル定義 |
| `{CUSTOM_SCRIPTS}` | カスタムJavaScript | 画面固有のスクリプト |

### メインコンテンツの構成例

#### 検索フォームの例

```html
<!-- 検索・フィルターパネル -->
<div class="card mb-4">
    <div class="card-header">
        <h5 class="card-title mb-0">検索条件</h5>
    </div>
    <div class="card-body">
        <form>
            <div class="row g-3">
                <div class="col-md-4">
                    <label for="keyword" class="form-label">キーワード</label>
                    <input type="text" class="form-control" id="keyword" placeholder="検索キーワード">
                </div>
                <!-- 他のフォーム項目 -->
                <div class="col-12 text-end">
                    <button type="button" class="btn btn-outline-secondary me-2">クリア</button>
                    <button type="submit" class="btn btn-primary">検索</button>
                </div>
            </div>
        </form>
    </div>
</div>
```

#### データテーブルの例

```html
<!-- 一覧表示 -->
<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="card-title mb-0">検索結果</h5>
        <span class="text-muted">全25件中 1-10件を表示</span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>名称</th>
                        <!-- 他のヘッダー項目 -->
                        <th style="width: 120px;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><a href="#">ID-001</a></td>
                        <td>項目名</td>
                        <!-- 他のデータ項目 -->
                        <td>
                            <div class="btn-group">
                                <button type="button" class="btn btn-sm btn-outline-secondary action-button" title="詳細">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <button type="button" class="btn btn-sm btn-outline-secondary action-button" title="編集">
                                    <i class="bi bi-pencil"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    <!-- 他の行 -->
                </tbody>
            </table>
        </div>
    </div>
    <div class="card-footer d-flex justify-content-between align-items-center">
        <div class="text-muted">全25件中 1-10件を表示</div>
        <nav aria-label="Page navigation">
            <ul class="pagination mb-0">
                <li class="page-item disabled">
                    <a class="page-link" href="#" aria-label="Previous">
                        <span aria-hidden="true">&laquo;</span>
                    </a>
                </li>
                <li class="page-item active"><a class="page-link" href="#">1</a></li>
                <li class="page-item"><a class="page-link" href="#">2</a></li>
                <li class="page-item"><a class="page-link" href="#">3</a></li>
                <li class="page-item">
                    <a class="page-link" href="#" aria-label="Next">
                        <span aria-hidden="true">&raquo;</span>
                    </a>
                </li>
            </ul>
        </nav>
    </div>
</div>
```

## モックアップ作成時の注意事項

### 1. デザインの一貫性

- Bootstrap 5のコンポーネントを基本とし、システム全体で一貫したデザインを維持すること
- 色彩コードは定義された色コード（プライマリカラー：#0d6efd など）を使用すること
- フォントはシステム標準の「Hiragino Kaku Gothic ProN, Hiragino Sans, Meiryo, sans-serif」を使用すること
- アイコンはBootstrap Icons（bi-*）を使用すること

### 2. レスポンシブデザイン

- すべてのモックアップは以下のデバイスサイズに対応すること
  - デスクトップ（1200px以上）
  - タブレット（768px〜1199px）
  - モバイル（767px以下）
- レスポンシブデザインのためにBootstrapのグリッドシステムを適切に使用すること
- モックアップビューワーのデバイス切替機能で検証可能にすること

### 3. ファイル命名規則

- モックアップファイルは「画面ID.html」の形式で命名すること
  - 例：BIL-001.html, ENG-002.html など
- 関連するCSS、JavaScript、画像ファイルは適切なサブディレクトリに配置すること

### 4. アクセシビリティ

- フォームラベルと入力フィールドは適切に関連付けること
- 適切なARIA属性を使用すること
- 色のみに依存した情報伝達を避けること（アイコンや文字を併用する）
- タブナビゲーションが適切に動作することを確認すること

## UI設計書への統合時の注意事項

### 1. モックアップビューワーの設定

- すべてのUI設計書に以下のCSS参照を追加すること
  ```html
  <link rel="stylesheet" href="../../css/mockup-viewer.css">
  ```
- モックアップビューワー用のdivとスクリプトを正しく配置すること
  ```html
  <div id="mockup-viewer"></div>
  
  <script src="../../js/mockup-viewer.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', function() {
      new MockupViewer('mockup-viewer', {
        defaultHeight: '700px',
        mockups: [
          {
            name: '画面名',
            path: 'mockups/画面ID.html'
          }
        ]
      });
    });
  </script>
  ```

### 2. 既存ワイヤーフレームの置換

- ワイヤーフレームプレースホルダを完全に削除し、モックアップビューワーで置き換えること
- ワイヤーフレーム関連のCSSを残してもよいが、使用されないことを確認すること

### 3. 画面仕様の整合性

- モックアップで実装した内容がUI設計書の仕様と一致していることを確認すること
- 不一致があった場合は、モックアップかUI設計書のいずれかを修正し、整合性を取ること
- 設計変更が必要な場合は、別途承認プロセスを経ること


## その他の注意事項

### 1. パフォーマンス

- 大きな画像ファイルは最適化すること
- 外部リソースの参照は最小限に抑えること
- JavaScriptは非同期読み込みを考慮すること

### 2. セキュリティ

- モックアップ内で機密情報（実際のユーザー名、メールアドレスなど）を使用しないこと
- フォームのaction属性は空か"#"に設定すること
- インラインJavaScriptは避け、外部ファイルに分離すること

### 3. 著作権

- すべてのコンポーネント・リソースが適切なライセンスの下で使用されていることを確認すること
- 外部リソースを使用する場合は著作権表示を適切に行うこと

---

本ガイドラインは随時更新される可能性があります。最新版を常に確認し、作業を進めてください。

最終更新日：2025年5月5日