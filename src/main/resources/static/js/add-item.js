// 用途変更時にステータス表示を更新
function updateStatusDisplay() {
  const selectedType = document.getElementById('statusType').value;
  const statusOptions = document.querySelectorAll('#item-status option[value]'); // 値を持つオプションのみを取得

  console.log('Selected type:', selectedType);
  console.log('Found options:', statusOptions.length);

  statusOptions.forEach((option) => {
    let newText = option.textContent;

    // 用途別の表示名に変更
    if (selectedType === 'books') {
      newText = option.getAttribute('data-books');
    } else if (selectedType === 'tasks') {
      newText = option.getAttribute('data-tasks');
    } else if (selectedType === 'inventory') {
      newText = option.getAttribute('data-inventory');
    } else {
      // デフォルトの場合は元の表示名を復元
      newText = option.getAttribute('data-default');
    }

    console.log(
      'Updating option:',
      option.value,
      'from',
      option.textContent,
      'to',
      newText
    );
    option.textContent = newText;
  });
}

// ページ読み込み時に初期化
document.addEventListener('DOMContentLoaded', function () {
  // 初期状態でデフォルト表示を設定
  updateStatusDisplay();
});
