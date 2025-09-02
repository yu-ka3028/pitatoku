let currentDeleteId = null;
let completeItemId = null;
let completeItemName = null;

// 削除ボタンクリック時の処理
document.addEventListener('DOMContentLoaded', function () {
  const deleteButtons = document.querySelectorAll('.delete-btn');

  deleteButtons.forEach((button) => {
    button.addEventListener('click', function () {
      const itemId = this.getAttribute('data-id');
      const itemName = this.getAttribute('data-name');

      // モーダルにアイテム名を表示
      document.getElementById(
        'deleteMessage'
      ).textContent = `「${itemName}」を削除しますか？この操作は取り消せません。`;

      // 削除対象のIDを保存
      currentDeleteId = itemId;

      // モーダルを表示
      showDeleteModal();
    });
  });

  // 完了ボタンクリック時の処理
  const completeButtons = document.querySelectorAll('.complete-btn');
  completeButtons.forEach((button) => {
    button.addEventListener('click', function () {
      const itemId = this.getAttribute('data-id');
      const itemName = this.getAttribute('data-name');

      // モーダルにアイテム名を表示
      document.getElementById(
        'completeMessage'
      ).textContent = `「${itemName}」を完了としてマークしますか？`;

      // 完了対象のIDと名前を保存
      completeItemId = itemId;
      completeItemName = itemName;

      // モーダルを表示
      showCompleteModal();
    });
  });
});

// 削除モーダルを表示
function showDeleteModal() {
  document.getElementById('deleteModal').classList.add('show');
}

// 削除モーダルを閉じる
function closeDeleteModal() {
  document.getElementById('deleteModal').classList.remove('show');
  currentDeleteId = null;
}

// 完了モーダルを表示
function showCompleteModal() {
  document.getElementById('completeModal').classList.add('show');
}

// 完了モーダルを閉じる
function closeCompleteModal() {
  document.getElementById('completeModal').classList.remove('show');
  completeItemId = null;
  completeItemName = null;
}

// 削除を実行
function confirmDelete() {
  if (currentDeleteId) {
    // フォームを作成してPOST送信
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/delete-item';

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'id';
    input.value = currentDeleteId;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
  }
}

// 完了を実行
function confirmComplete() {
  if (completeItemId) {
    // フォームを作成してPOST送信
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/complete-item';

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'id';
    input.value = completeItemId;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
  }
}

// 用途変更時にステータス表示を更新
function updateStatusDisplay() {
  const selectedType = document.getElementById('statusType').value;
  const statusCells = document.querySelectorAll('td:nth-child(3)'); // 状態列のセルを取得

  statusCells.forEach((cell) => {
    const currentText = cell.textContent;
    let newText = currentText;

    // 現在の表示名から用途別の表示名に変更
    if (selectedType === 'books') {
      newText = getBookDisplayName(currentText);
    } else if (selectedType === 'tasks') {
      newText = getTasksDisplayName(currentText);
    } else if (selectedType === 'inventory') {
      newText = getInventoryDisplayName(currentText);
    } else {
      // デフォルトの場合は元の表示名を復元
      newText = getDefaultDisplayName(currentText);
    }

    cell.textContent = newText;
  });
}

// 積み本用の表示名を取得
function getBookDisplayName(currentText) {
  const statusMap = {
    ToMore: '未購入',
    ToDo: '購入済み',
    'Now!!': '作業中',
    完了: '完了',
  };
  return statusMap[currentText] || currentText;
}

// 作業管理用の表示名を取得
function getTasksDisplayName(currentText) {
  const statusMap = {
    ToMore: 'ToMore',
    ToDo: 'ToDo',
    'Now!!': 'Now!!',
    完了: '完了',
  };
  return statusMap[currentText] || currentText;
}

// 生活品在庫管理用の表示名を取得
function getInventoryDisplayName(currentText) {
  const statusMap = {
    ToMore: 'あれば欲しい',
    ToDo: '在庫なし',
    'Now!!': '在庫あり',
    完了: '完了',
  };
  return statusMap[currentText] || currentText;
}

// デフォルトの表示名を取得
function getDefaultDisplayName(currentText) {
  const statusMap = {
    未購入: 'ToMore',
    購入済み: 'ToDo',
    作業中: 'Now!!',
    あれば欲しい: 'ToMore',
    在庫なし: 'ToDo',
    在庫あり: 'Now!!',
  };
  return statusMap[currentText] || currentText;
}

// 削除モーダル外クリックで閉じる
document.getElementById('deleteModal').addEventListener('click', function (e) {
  if (e.target === this) {
    closeDeleteModal();
  }
});

// 完了モーダル外クリックで閉じる
document
  .getElementById('completeModal')
  .addEventListener('click', function (e) {
    if (e.target === this) {
      closeCompleteModal();
    }
  });
