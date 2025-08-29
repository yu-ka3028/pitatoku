# 開発記録・実装ノート

## 概要

このファイルは、ぴた得プロジェクトの開発過程での実装判断、設計理由、変数の関連性などを記録し、将来的なコードリファクタリングや機能拡張時の参考資料として作成されています。

## アーキテクチャ設計の理由

### 1. Spring Boot 3.5.4 + Java 17 を選択した理由

- **LTS サポート**: Java 17 は長期サポート版で安定性が高い
- **パフォーマンス**: Spring Boot 3.x は GraalVM ネイティブイメージ対応で高速化
- **セキュリティ**: Jakarta EE 9+ への移行によりセキュリティ強化
- **開発効率**: Spring Boot の自動設定により開発速度向上

### 2. MVC パターンを採用した理由

- **責任分離**: Controller（表示制御）、Service（ビジネスロジック）、Repository（データアクセス）の明確な分離
- **テスタビリティ**: 各層を独立してテスト可能
- **保守性**: 機能追加・変更時の影響範囲を最小化

## データベース設計の詳細

### Dashboard エンティティの設計理由
補足：そもそも@Entityとは[公式](https://spring-boot.jp/database/entity-repository/36)
SQL文を直接書かなくても、Javaのコードだけでテーブル設計可能。（さらにリポジトリでデータの保存・更新・検索・削除などを操作可能）
- Railsのモデル：ActiveRecordを使って、RubyコードでDB操作
- Javaのエンティティ：JPAを使って、JavaコードでDB操作
Spring MVCと組み合わせることでコード保守性と可読性を向上

#### フィールド選択の理由

```java
@Entity
@Table(name = "dashboard")
public class Dashboard {
  private Long id;           // 主キー（自動採番）
  private String itemName;   // アイテム名（必須）
  private Status status;     // 状態（ENUM）
  private String memo;       // メモ（任意）
  private LocalDateTime updatedAt; // 最終更新日時
}
```

**設計判断**:

- `id`: JPA の `@GeneratedValue` で自動採番。シンプルで確実
- `itemName`: `nullable = false` で必須項目化。データ整合性確保
- `status`: ENUM 使用で型安全性確保。状態の一貫性を保証
- `memo`: 任意項目。ユーザーの自由な記録を可能に
- `updatedAt`: 変更履歴追跡。将来的な履歴管理機能の準備

#### テーブル名を "dashboard" にした理由

- **シンプル**: 複雑な命名規則を避けて理解しやすく
- **拡張性**: 将来的にユーザー別ダッシュボードなども対応可能
- **直感的**: 機能とテーブル名が一致

### Status 列挙型の設計

#### 状態の定義理由

```java
public enum Status {
  INTERESTED("ToMore"),   // 気になる → 購入検討中
  PURCHASED("ToDo"),      // 購入済み → 作業待ち
  WORKING("Now!!"),       // 作業中 → 現在進行中
  COMPLETED("完了");       // 完了 → 終了済み
}
```

**状態遷移の設計思想**:

1. **INTERESTED**: 興味を持った段階（在庫計算から除外）
2. **PURCHASED**: 購入済みで作業待ち（在庫としてカウント）
3. **WORKING**: 現在作業中（在庫としてカウント）
4. **COMPLETED**: 完了済み（在庫計算から除外）

**表示名の選択理由**:

- `ToMore`: より多くのことを学びたい意欲
- `ToDo`: やるべきことリスト
- `Now!!`: 現在進行中の強調
- `完了`: 日本語で明確

## ビジネスロジックの実装理由

### AllStatusService の在庫判定ロジック

#### 判定条件の設計理由

```java
if (purchasedCount <= workingCount || purchasedCount == 0) {
    status = "Take Action!";
    statusClass = "down";
} else if (purchasedCount == workingCount * 2) {
    status = "Nice!!";
    statusClass = "nice";
} else {
    status = "Take it easy!";
    statusClass = "high";
}
```

**ロジックの根拠**:

- **Take Action!**: 購入済みが作業中以下 → 新しいことに取り組む準備ができている
- **Nice!!**: 購入済み = 作業中 × 2 → 適正なバランス（1 つ作業中、1 つ待機）
- **Take it easy!**: 購入済み > 作業中 × 2 → 過剰な購入、ペースダウンを推奨

**係数 "2" を選択した理由**:

- 経験的に適正なバランスと考えられる
- 将来的に設定可能にする予定

#### 完了アイテムを除外する理由

```java
// 完了以外のアイテムのみを対象とする
long interestedCount = dashboardRepository.countByStatus(Status.INTERESTED);
long purchasedCount = dashboardRepository.countByStatus(Status.PURCHASED);
long workingCount = dashboardRepository.countByStatus(Status.WORKING);
```

**設計判断**:

- 完了済みは過去の実績として扱う
- 現在の在庫状況のみで判定したい
- 履歴管理機能追加時の準備

## コントローラーの設計理由

### DashboardController の実装判断

#### ルーティング設計

```java
@RequestMapping("/dashboard")    // メイン画面
@RequestMapping("/add-item")     // 追加画面
@RequestMapping("/edit-item")    // 編集画面
```

**URL 設計の理由**:

- **RESTful**: リソース指向の設計
- **直感的**: 機能と URL が一致
- **拡張性**: 将来的な機能追加に対応

#### POST メソッドの選択理由

```java
@PostMapping("/add-item")
@PostMapping("/update-item")
@PostMapping("/delete-item")
@PostMapping("/complete-item")
```

**設計判断**:

- **データ変更**: GET は冪等性のため、データ変更には POST を使用
- **セキュリティ**: CSRF 対策のため POST を選択
- **一貫性**: 全ての変更操作で POST を統一

#### リダイレクト処理の理由

```java
return "redirect:/dashboard";
```

**設計判断**:

- **PRG パターン**: Post/Redirect/Get パターンでブラウザバック問題を回避
- **ユーザビリティ**: 操作完了後はメイン画面に戻る
- **データ整合性**: 二重送信を防止

## リポジトリ層の設計理由

### DashboardRepository の実装
補足：そもそもリポジトリとは[公式](https://spring-boot.jp/basics/annotations/53#mokuji_5)
- Spring Data JPAが提供するインターフェース（JpaRepository）を継承して、エンティティのデータを保存(save)・更新・検索(findById)・削除可能にする
- Springのデータアクセス層におけるエラーハンドリングを簡単にする

#### カスタムクエリメソッドの選択

```java
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
  Long countByStatus(Status status);
  List<Dashboard> findByStatusNot(Status status);
}
```

**メソッド選択の理由**:

- `countByStatus`: 在庫計算で必要。JPA の命名規則で自動生成
- `findByStatusNot`: 完了以外のアイテム取得。フィルタリングを DB レベルで実行

**JPA の命名規則を採用した理由**:

- **保守性**: カスタムクエリより保守が容易
- **可読性**: メソッド名で意図が明確
- **パフォーマンス**: JPA が最適化されたクエリを生成

## フロントエンド設計の理由

### Thymeleaf を選択した理由

- **サーバーサイドレンダリング**: SEO 対応、初期表示速度
- **Spring Boot 統合**: 自動設定、セキュリティ統合
- **学習コスト**: チーム内で理解しやすい

### CSS 設計の判断
※基本的にはAIにお任せ中...

```css
/* 状態別の色分け */
.status-pattern.down {
  background-color: #ff6b6b;
} /* 赤：注意 */
.status-pattern.nice {
  background-color: #51cf66;
} /* 緑：良好 */
.status-pattern.high {
  background-color: #ffd43b;
} /* 黄：過剰 */
```

**色選択の理由**:

- **直感的**: 交通信号と同じ色の意味
- **アクセシビリティ**: 色覚異常者にも配慮
- **一貫性**: 全画面で統一された色使い

## 設定ファイルの設計理由

### application.properties の設定判断

#### データベース設定

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**設定理由**:

- `ddl-auto=update`: 開発時の自動スキーマ更新
- `show-sql=true`: デバッグ時の SQL 確認
- `format_sql=true`: SQL の可読性向上

#### ログ設定

```properties
logging.level.com.example.demo=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n
```

**設定理由**:

- **開発効率**: 適切なログレベルでデバッグ支援
- **運用**: 本番環境での問題追跡
- **可読性**: タイムスタンプ、ログレベル、クラス名を明確に

## Docker 設定の設計理由

### docker-compose.yaml の設計判断

#### イメージ切り替えの理由
./gradlew bootRunで起動するとdocker使用しないため、紛らわしいので切り替えで開発

```yaml
# ローカル開発用
image: pitatoku:latest

# AWS 環境用
image: 293210009733.dkr.ecr.ap-northeast-1.amazonaws.com/pitatoku:latest
```

**設計理由**:

- **環境分離**: 開発・本番環境の明確な分離
- **デプロイ効率**: ECR からの直接取得で高速化
- **管理容易性**: コメントアウトで簡単な切り替え

#### ヘルスチェックの実装
db起動前に次の処理に入り接続エラーとなったため導入

```yaml
healthcheck:
  test: ['CMD', 'mysqladmin', 'ping', '-h', 'localhost']
  timeout: 10s
  retries: 3
  interval: 5s
  start_period: 10s
```

**実装理由**:

- **起動順序**: アプリケーションが DB 起動完了を待つ
- **安定性**: DB 接続エラーを防止
- **運用**: コンテナの健全性監視

## 変数の関連性と依存関係

### 主要な変数の関連図

```
Dashboard Entity
├── id (PK) → Repository.findById()
├── itemName → 表示・検索・編集
├── status → 在庫計算・フィルタリング・表示
├── memo → 表示・編集
└── updatedAt → 表示・ソート

Status Enum
├── INTERESTED → countByStatus() → 在庫計算から除外
├── PURCHASED → countByStatus() → 在庫としてカウント
├── WORKING → countByStatus() → 在庫としてカウント
└── COMPLETED → findByStatusNot() → 一覧から除外

AllStatusService
├── interestedCount → 表示のみ
├── purchasedCount → 判定ロジック
├── workingCount → 判定ロジック
├── status → 表示・CSS クラス
└── statusClass → CSS クラス決定
```

### データフローの追跡

#### アイテム追加フロー

1. `DashboardController.handleForm()` → フォームデータ受信
2. `getStatus()` → 文字列を ENUM に変換
3. `new Dashboard()` → エンティティ作成
4. `dashboardRepository.save()` → DB 保存
5. `redirect:/dashboard` → 画面遷移

#### 在庫計算フロー

1. `DashboardController.dashboard()` → 画面表示
2. `allStatusService.calculateInventoryStatus()` → 計算実行
3. `dashboardRepository.countByStatus()` → 各状態の件数取得
4. 判定ロジック → 状態決定
5. `model.addAttribute()` → 画面に渡す

## 今後のリファクタリングポイント

### 1. 設定の外部化

```java
// 現在: ハードコーディング
if (purchasedCount == workingCount * 2) {

// 将来: 設定ファイル化
@Value("${inventory.balance.ratio:2}")
private int balanceRatio;
```

### 2. バリデーション強化

```java
// 現在: 最小限のバリデーション
@RequestParam("item_name") String itemName

// 将来: Bean Validation
@NotBlank(message = "アイテム名は必須です")
@Size(max = 100, message = "アイテム名は100文字以内で入力してください")
private String itemName;
```

### 3. エラーハンドリング改善

```java
// 現在: RuntimeException
.orElseThrow(() -> new RuntimeException("アイテムが見つかりません"))

// 将来: カスタム例外
.orElseThrow(() -> new ItemNotFoundException("アイテムが見つかりません"))
```

### 4. ログ出力の統一

```java
// 現在: System.out.println
System.out.println("Hello World");

// 将来: ログフレームワーク
logger.info("アプリケーション起動完了");
```

## パフォーマンス考慮事項

### 1. N+1 問題の回避

- 現在: 個別の countByStatus()呼び出し
- 将来: 一括取得クエリの検討

### 2. キャッシュ戦略

- 現在: キャッシュなし
- 将来: Redis によるセッション・データキャッシュ

### 3. データベース最適化

- 現在: 基本的なインデックス
- 将来: 複合インデックスの検討

## セキュリティ考慮事項

### 1. 入力値検証

- 現在: 最小限の検証
- 将来: XSS 対策、SQL インジェクション対策の強化

### 2. 認証・認可

- 現在: なし
- 将来: Spring Security による認証機能

### 3. データ保護

- 現在: 平文保存
- 将来: 機密データの暗号化

## テスト戦略

### 1. 単体テスト

- Controller 層: MockMvc によるテスト
- Service 層: モックを使用したビジネスロジックテスト
- Repository 層: @DataJpaTest によるテスト

### 2. 統合テスト

- @SpringBootTest による全体テスト
- Testcontainers による DB 統合テスト

### 3. E2E テスト

- Selenium によるブラウザテスト

---

## 更新履歴

- 2024-01-XX: 初版作成
- 2024-01-XX: アーキテクチャ設計理由を追加
- 2024-01-XX: 変数の関連性図を追加
