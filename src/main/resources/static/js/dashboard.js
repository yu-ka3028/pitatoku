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
  const statusCells = document.querySelectorAll('.status-cell');

  console.log('updateStatusDisplay called with type:', selectedType);
  console.log('Found status cells:', statusCells.length);

  if (statusCells.length === 0) {
    console.error(
      'No status cells found! Check if .status-cell class is properly set.'
    );
    return;
  }

  statusCells.forEach((cell) => {
    const statusEnum = cell.getAttribute('data-status');
    let newText = cell.textContent;
    let typeLabel = '';

    console.log(
      'Processing cell with status:',
      statusEnum,
      'current text:',
      cell.textContent
    );

    // 用途別の表示名に変更
    if (selectedType === 'books') {
      newText = getBookDisplayNameByStatus(statusEnum);
      typeLabel = '積み本';
    } else if (selectedType === 'tasks') {
      newText = getTasksDisplayNameByStatus(statusEnum);
      typeLabel = '作業管理';
    } else if (selectedType === 'inventory') {
      newText = getInventoryDisplayNameByStatus(statusEnum);
      typeLabel = '生活品在庫管理';
    } else {
      // デフォルトの場合は元の表示名を復元
      newText = getDefaultDisplayNameByStatus(statusEnum);
      typeLabel = 'デフォルト';
    }

    // 用途が選択されている場合は「状態/用途」の形式で表示
    if (selectedType !== 'default') {
      cell.textContent = `${newText}/${typeLabel}`;
    } else {
      cell.textContent = newText;
    }

    console.log('Updated cell text to:', cell.textContent);
  });
}

// 積み本用の表示名を取得（Status enumから）
function getBookDisplayNameByStatus(statusEnum) {
  const statusMap = {
    INTERESTED: '未購入',
    PURCHASED: '購入済み',
    WORKING: '作業中',
    COMPLETED: '完了',
  };
  return statusMap[statusEnum] || statusEnum;
}

// 作業管理用の表示名を取得（Status enumから）
function getTasksDisplayNameByStatus(statusEnum) {
  const statusMap = {
    INTERESTED: 'ToMore',
    PURCHASED: 'ToDo',
    WORKING: 'Now!!',
    COMPLETED: '完了',
  };
  return statusMap[statusEnum] || statusEnum;
}

// 生活品在庫管理用の表示名を取得（Status enumから）
function getInventoryDisplayNameByStatus(statusEnum) {
  const statusMap = {
    INTERESTED: 'あれば欲しい',
    PURCHASED: '在庫なし',
    WORKING: '在庫あり',
    COMPLETED: '完了',
  };
  return statusMap[statusEnum] || statusEnum;
}

// デフォルトの表示名を取得（Status enumから）
function getDefaultDisplayNameByStatus(statusEnum) {
  const statusMap = {
    INTERESTED: 'ToMore',
    PURCHASED: 'ToDo',
    WORKING: 'Now!!',
    COMPLETED: '完了',
  };
  return statusMap[statusEnum] || statusEnum;
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
