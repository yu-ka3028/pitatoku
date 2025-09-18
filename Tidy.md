# ぴた得 リファクタリング プルリクエスト計画

## 概要

このドキュメントは、ぴた得アプリケーションのリファクタリングを最小単位のプルリクエストに分割した計画書です。各プルリクエストは具体的な目的、対象ファイル、変更内容と理由を明確に定義しています。

---

## プルリクエスト一覧


### PR #3: カスタム例外クラスの導入

**目的**: 汎用的な RuntimeException を具体的な例外クラスに置き換え、エラーハンドリングを改善する

**対象ファイル**:

- `src/main/java/com/example/demo/exception/ItemNotFoundException.java` (新規作成)
- `src/main/java/com/example/demo/controller/DashboardController.java`

**変更内容**:

```java
// 新規ファイル: ItemNotFoundException.java
package com.example.demo.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

// DashboardController.java の変更
// 変更前
Dashboard item = dashboardRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("アイテムが見つかりません"));

// 変更後
import com.example.demo.exception.ItemNotFoundException;

Dashboard item = dashboardRepository.findById(id)
    .orElseThrow(() -> new ItemNotFoundException("アイテムが見つかりません: " + id));
```

**選択理由**:

- 具体的な例外クラスにより、エラーの種類を明確化
- 例外処理の一元化が可能
- デバッグ時の問題特定が容易

---

### PR #4: 依存性注入のコンストラクタインジェクション化

**目的**: @Autowired フィールドインジェクションをコンストラクタインジェクションに変更し、テスト容易性と不変性を向上させる

**対象ファイル**:

- `src/main/java/com/example/demo/controller/DashboardController.java`
- `src/main/java/com/example/demo/service/AllStatusService.java`

**変更内容**:

```java
// DashboardController.java の変更
// 変更前
@Controller
public class DashboardController {
    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private AllStatusService allStatusService;

// 変更後
@Controller
public class DashboardController {
    private final DashboardRepository dashboardRepository;
    private final AllStatusService allStatusService;

    public DashboardController(DashboardRepository dashboardRepository,
                              AllStatusService allStatusService) {
        this.dashboardRepository = dashboardRepository;
        this.allStatusService = allStatusService;
    }
```

**選択理由**:

- フィールドを final にでき、不変性を保証
- テスト時にモックの注入が容易
- Spring 推奨の依存性注入方法

---

### PR #5: 入力値の null チェック統一

**目的**: フォームデータの null チェック処理を統一し、コードの一貫性を向上させる

**対象ファイル**:

- `src/main/java/com/example/demo/controller/DashboardController.java`

**変更内容**:

```java
// 変更前
Dashboard newItem = new Dashboard(itemName, getStatus(status),
    memo != null ? memo : "",
    statusType != null ? statusType : "default",
    updatedAt);

// 変更後
private String getDefaultIfNull(String value, String defaultValue) {
    return value != null ? value : defaultValue;
}

Dashboard newItem = new Dashboard(itemName, getStatus(status),
    getDefaultIfNull(memo, ""),
    getDefaultIfNull(statusType, "default"),
    updatedAt);
```

**選択理由**:

- null チェック処理の重複を削減
- デフォルト値の管理を一元化
- コードの可読性向上

---

### PR #6: JavaScript 重複コードの共通化

**目的**: HTML ファイル内の重複した JavaScript コードを外部ファイルに抽出し、保守性を向上させる

**対象ファイル**:

- `src/main/resources/static/js/status-display.js` (新規作成)
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/add-item.html`
- `src/main/resources/templates/edit-item.html`

**変更内容**:

```javascript
// 新規ファイル: status-display.js
class StatusDisplayManager {
  static updateStatusDisplay() {
    const statusCells = document.querySelectorAll('.status-cell');
    if (statusCells.length === 0) {
      console.error('No status cells found!');
      return;
    }

    statusCells.forEach((cell) => {
      const statusEnum = cell.getAttribute('data-status');
      const statusType = cell.getAttribute('data-status-type');
      const originalText = cell.textContent.trim();

      const { newText, typeLabel } = this.getDisplayInfo(
        statusEnum,
        statusType
      );

      if (statusType !== 'default') {
        cell.textContent = `${newText}/${typeLabel}`;
      } else {
        cell.textContent = newText;
      }
    });
  }

  static getDisplayInfo(statusEnum, statusType) {
    // 表示ロジックの実装
  }
}
```

**選択理由**:

- 重複コードの削減
- 単一責任の原則に従った設計
- テスト可能な JavaScript コード

---

### PR #7: ログレベルの最適化

**目的**: 過度なデバッグログを削減し、本番環境に適したログレベルに調整する

**対象ファイル**:

- `src/main/resources/application.properties`

**変更内容**:

```properties
# 変更前
logging.level.org.thymeleaf=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.com.example.demo=INFO

# 変更後
logging.level.org.thymeleaf=WARN
logging.level.org.springframework.web=WARN
logging.level.com.example.demo=INFO
logging.level.com.example.demo.service.AllStatusService=DEBUG
```

**選択理由**:

- 本番環境でのログ出力量を削減
- 必要な情報のみをログ出力
- パフォーマンスの向上

---

### PR #8: メソッドの抽出とリファクタリング

**目的**: DashboardController の長いメソッドを小さなメソッドに分割し、可読性を向上させる

**対象ファイル**:

- `src/main/java/com/example/demo/controller/DashboardController.java`

**変更内容**:

```java
// 変更前
@PostMapping("/add-item")
public String handleForm(
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    @RequestParam(value = "memo", required = false) String memo,
    @RequestParam(value = "status_type", required = false) String statusType
){
    LocalDateTime updatedAt = LocalDateTime.now();
    Dashboard newItem = new Dashboard(itemName, getStatus(status),
        memo != null ? memo : "",
        statusType != null ? statusType : "default",
        updatedAt);
    dashboardRepository.save(newItem);
    return "redirect:/dashboard";
}

// 変更後
@PostMapping("/add-item")
public String handleForm(
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    @RequestParam(value = "memo", required = false) String memo,
    @RequestParam(value = "status_type", required = false) String statusType
) {
    Dashboard newItem = createDashboardItem(itemName, status, memo, statusType);
    dashboardRepository.save(newItem);
    return "redirect:/dashboard";
}

private Dashboard createDashboardItem(String itemName, String status,
                                    String memo, String statusType) {
    LocalDateTime updatedAt = LocalDateTime.now();
    return new Dashboard(itemName, getStatus(status),
        getDefaultIfNull(memo, ""),
        getDefaultIfNull(statusType, "default"),
        updatedAt);
}
```

**選択理由**:

- 単一責任の原則に従った設計
- メソッドの再利用性向上
- テストの容易性向上

---

## 実装優先順位

### 高優先度（即座に実装推奨）

1. **PR #1**: デバッグ用 System.out.println の削除
2. **PR #3**: カスタム例外クラスの導入
3. **PR #4**: 依存性注入のコンストラクタインジェクション化

### 中優先度（短期間で実装）

4. **PR #2**: マジックナンバーの定数化
5. **PR #5**: 入力値の null チェック統一
6. **PR #7**: ログレベルの最適化

### 低優先度（中長期的に実装）

7. **PR #6**: JavaScript 重複コードの共通化
8. **PR #8**: メソッドの抽出とリファクタリング

## 実装時の注意点

1. **テストの実行**: 各プルリクエスト実装後に既存の機能が正常に動作することを確認
2. **段階的実装**: 一度に複数のプルリクエストを実装せず、一つずつ実装・テスト・マージ
3. **コミットメッセージ**: 各変更の目的と影響範囲を明確に記載
4. **レビュー**: コードレビューを通じて品質を確保

## 期待される効果

- **保守性の向上**: コードの可読性と理解しやすさの向上
- **テスト容易性**: 単体テストの書きやすさ向上
- **エラー処理の改善**: より具体的で有用なエラーメッセージ
- **パフォーマンス向上**: 不要なログ出力の削減
- **コードの一貫性**: 統一されたコーディングスタイル

この計画に従って段階的にリファクタリングを実施することで、コードの品質を向上させながら、既存機能への影響を最小限に抑えることができます。
