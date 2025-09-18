# ぴた得 MVP 開発ノート

## プロジェクト概要

**ぴた得**は、アイテムの状態管理を行う Web アプリケーションです。積み本、作業管理、生活品在庫管理など、異なる用途に応じてアイテムの状態を管理できます。

## アーキテクチャ概要

### 技術スタック

- **バックエンド**: Spring Boot 3.5.4 (Java 17)
- **データベース**: MySQL
- **フロントエンド**: Thymeleaf + HTML/CSS/JavaScript
- **ビルドツール**: Gradle
- **ORM**: Spring Data JPA (Hibernate)

### アーキテクチャパターン

- **MVC パターン**: Spring MVC を使用
- **レイヤードアーキテクチャ**: Controller → Service → Repository → Entity
- **Dependency Injection**: Spring Framework の DI コンテナを活用

## コード構造の詳細分析

### 1. エントリーポイント

```java
// PitatokuApplication.java
@SpringBootApplication
public class PitatokuApplication {
    public static void main(String[] args) {
        SpringApplication.run(PitatokuApplication.class, args);
        System.out.println("Hello World");
    }
}
```

**理解ポイント:**

- `@SpringBootApplication`アノテーションで Spring Boot アプリケーションを定義
- 自動設定、コンポーネントスキャン、設定クラスを統合
- メインクラスは最小限の実装

### 2. データモデル

#### Dashboard エンティティ

```java
@Entity
@Table(name = "dashboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "memo")
    private String memo;

    @Column(name = "status_type", nullable = false)
    private String statusType;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**理解ポイント:**

- JPA エンティティとしてデータベーステーブルとマッピング
- Lombok の`@Data`で getter/setter を自動生成
- `@Enumerated(EnumType.STRING)`で enum を文字列として保存
- カスタムコンストラクタも定義

#### Status 列挙型

```java
public enum Status {
    INTERESTED("ToMore"),
    PURCHASED("ToDo"),
    WORKING("Now!!"),
    COMPLETED("完了");

    private final String displayName;

    public String getDisplayNameByType(String type) {
        switch(type) {
            case "books": return getBookDisplayName();
            case "tasks": return getTasksDisplayName();
            case "inventory": return getInventoryDisplayName();
            default: return getDisplayName();
        }
    }
}
```

**理解ポイント:**

- 用途別に異なる表示名を提供
- ポリモーフィズムを活用した柔軟な表示制御
- 各用途（books, tasks, inventory）に特化した表示名

### 3. データアクセス層

#### DashboardRepository

```java
@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    Long countByStatus(Status status);
    List<Dashboard> findByStatusNot(Status status);
}
```

**理解ポイント:**

- Spring Data JPA のリポジトリパターン
- メソッド名によるクエリ自動生成
- カスタムクエリメソッドの定義

### 4. ビジネスロジック層

#### AllStatusService

```java
@Service
public class AllStatusService {
    public Map<String, Object> calculateInventoryStatus() {
        long interestedCount = dashboardRepository.countByStatus(Status.INTERESTED);
        long purchasedCount = dashboardRepository.countByStatus(Status.PURCHASED);
        long workingCount = dashboardRepository.countByStatus(Status.WORKING);

        String status;
        String statusClass;

        if (purchasedCount <= workingCount || purchasedCount == 0) {
            status = "Take Action!";
            statusClass = "down";
        } else if (purchasedCount == workingCount * 2) {
            status = "Nice!!";
            statusClass = "nice";
        } else {
            status = "Take Action!";
            statusClass = "high";
        }

        return result;
    }
}
```

**理解ポイント:**

- 在庫状況の判定ロジック
- ビジネスルールの実装（購入済みと作業中の比率による判定）
- ログ出力によるデバッグ支援

### 5. プレゼンテーション層

#### DashboardController

```java
@Controller
public class DashboardController {
    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private AllStatusService allStatusService;

    @RequestMapping("/dashboard")
    public String dashboard(Model model) {
        List<Dashboard> items = dashboardRepository.findByStatusNot(Status.COMPLETED);
        Map<String, Object> statusData = allStatusService.calculateInventoryStatus();
        model.addAttribute("items", items);
        model.addAttribute("statusData", statusData);
        return "dashboard";
    }

    @PostMapping("/add-item")
    public String handleForm(
        @RequestParam("item_name") String itemName,
        @RequestParam("status") String status,
        @RequestParam(value = "memo", required = false) String memo,
        @RequestParam(value = "status_type", required = false) String statusType
    ) {
        // フォームデータの処理
    }
}
```

**理解ポイント:**

- Spring MVC のコントローラーパターン
- `@RequestMapping`と`@PostMapping`によるルーティング
- `@RequestParam`によるフォームデータの受け取り
- Model オブジェクトによるビューへのデータ渡し
- リダイレクトによる画面遷移

### 6. フロントエンド

#### HTML テンプレート（Thymeleaf）

```html
<!-- dashboard.html -->
<tr th:each="item : ${items}">
  <td th:text="${item.itemName}">アイテム名</td>
  <td
    class="status-cell"
    th:data-status="${item.status}"
    th:data-status-type="${item.statusType}"
    th:text="${item.status.displayName}"
  >
    状態
  </td>
  <td th:text="${item.memo}">メモ</td>
</tr>
```

**理解ポイント:**

- Thymeleaf のテンプレートエンジン
- `th:each`による繰り返し処理
- `th:text`によるデータ表示
- `th:data-*`によるカスタムデータ属性

#### JavaScript（動的表示制御）

```javascript
function updateStatusDisplay() {
  const statusCells = document.querySelectorAll('.status-cell');
  statusCells.forEach((cell) => {
    const statusEnum = cell.getAttribute('data-status');
    const statusType = cell.getAttribute('data-status-type');

    if (statusType === 'books') {
      if (statusEnum === 'INTERESTED') newText = '未購入';
      // 他の条件分岐...
    }
    cell.textContent = `${newText}/${typeLabel}`;
  });
}
```

**理解ポイント:**

- DOM 操作による動的表示制御
- データ属性を活用した状態管理
- 用途別表示名の動的切り替え

## 現在のコードの特徴

### 良い点

1. **明確なレイヤー分離**: MVC パターンに従った構造
2. **Spring Boot の活用**: 自動設定とアノテーションによる簡潔な実装
3. **柔軟な状態管理**: 用途別の表示名切り替え機能
4. **レスポンシブデザイン**: CSS によるモバイル対応
5. **データベース設計**: JPA による型安全なデータアクセス

### 改善が必要な点

1. **エラーハンドリング**: 例外処理が不十分
2. **バリデーション**: 入力値の検証が不足
3. **コードの重複**: JavaScript の重複コード
4. **マジックナンバー**: ハードコードされた値
5. **ログ出力**: デバッグ用の System.out.println が残存

## リファクタリング提案

### 1. エラーハンドリングの改善

**現在のコード:**

```java
@PostMapping("/update-item")
public String updateItem(@RequestParam("id") Long id, ...) {
    Dashboard item = dashboardRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("アイテムが見つかりません"));
    // ...
}
```

**改善案:**

```java
@PostMapping("/update-item")
public String updateItem(@RequestParam("id") Long id, ...) {
    try {
        Dashboard item = dashboardRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException("アイテムが見つかりません: " + id));
        // ...
    } catch (ItemNotFoundException e) {
        logger.error("アイテム更新エラー: {}", e.getMessage());
        return "redirect:/dashboard?error=item_not_found";
    }
}

// カスタム例外クラス
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
```

### 2. バリデーションの追加

**現在のコード:**

```java
@PostMapping("/add-item")
public String handleForm(
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    // ...
) {
    // バリデーションなし
}
```

**改善案:**

```java
@PostMapping("/add-item")
public String handleForm(
    @RequestParam("item_name") @NotBlank @Size(max = 100) String itemName,
    @RequestParam("status") @NotBlank String status,
    @RequestParam(value = "memo", required = false) @Size(max = 500) String memo,
    @RequestParam(value = "status_type", required = false) String statusType,
    BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "add-item";
    }
    // ...
}
```

### 3. 定数の外部化

**現在のコード:**

```java
if (purchasedCount == workingCount * 2) {
    status = "Nice!!";
    statusClass = "nice";
}
```

**改善案:**

```java
public class InventoryConstants {
    public static final int NICE_RATIO_MULTIPLIER = 2;
    public static final String NICE_STATUS = "Nice!!";
    public static final String NICE_CLASS = "nice";
}

// 使用例
if (purchasedCount == workingCount * InventoryConstants.NICE_RATIO_MULTIPLIER) {
    status = InventoryConstants.NICE_STATUS;
    statusClass = InventoryConstants.NICE_CLASS;
}
```

### 4. JavaScript の共通化

**現在のコード:**

```javascript
// dashboard.htmlとadd-item.htmlで重複
function updateStatusDisplay() {
  // 同じロジックが複数箇所に存在
}
```

**改善案:**

```javascript
// status-display.js（共通ファイル）
class StatusDisplayManager {
  static updateStatusDisplay() {
    // 共通ロジック
  }

  static getDisplayName(statusEnum, statusType) {
    // 表示名取得ロジック
  }
}
```

### 5. ログ出力の改善

**現在のコード:**

```java
public static void main(String[] args) {
    SpringApplication.run(PitatokuApplication.class, args);
    System.out.println("Hello World"); // デバッグ用出力
}
```

**改善案:**

```java
public static void main(String[] args) {
    SpringApplication.run(PitatokuApplication.class, args);
    // System.out.printlnを削除し、適切なログ設定を使用
}
```

## 次の機能実装に向けた準備

### 1. 認証・認可機能

- Spring Security の導入
- ユーザー管理機能
- セッション管理

### 2. API 化

- REST API エンドポイントの追加
- JSON レスポンスの実装
- フロントエンドの SPA 化

### 3. データベース設計の拡張

- ユーザーテーブルの追加
- カテゴリ管理機能
- 履歴管理機能

### 4. パフォーマンス改善

- キャッシュ機能の導入
- ページネーションの実装
- データベースクエリの最適化

## 学習ポイント

### Java 初学者向けの重要概念

1. **アノテーション**: `@Controller`, `@Service`, `@Repository`の役割
2. **依存性注入**: `@Autowired`による DI
3. **JPA**: エンティティとリポジトリの関係
4. **Spring MVC**: コントローラーとビューの連携
5. **Thymeleaf**: テンプレートエンジンの基本

### 設計パターン

1. **MVC パターン**: 責任の分離
2. **リポジトリパターン**: データアクセスの抽象化
3. **サービス層パターン**: ビジネスロジックの分離
4. **DTO パターン**: データ転送オブジェクト（今後の拡張で使用予定）

この MVP 段階のコードを理解することで、Spring Boot アプリケーションの基本的な構造と実装パターンを学習できます。次の機能実装に向けて、上記のリファクタリング提案を参考に、より保守性の高いコードに改善していくことをお勧めします。
