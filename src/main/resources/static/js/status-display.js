// 用途変更時にステータス表示を更新する共通関数
function updateStatusDisplay() {
  const selectedType = document.getElementById('statusType').value;
  const statusOptions = document.querySelectorAll('#item-status option[value]');

  console.log('updateStatusDisplay called with type:', selectedType);
  console.log('Found options:', statusOptions.length);

  statusOptions.forEach((option) => {
    if (option.value === '') return; // 空のオプションはスキップ

    let newText = '';
    const currentText = option.textContent.trim();

    console.log(
      'Processing option:',
      option.value,
      'current text:',
      currentText
    );

    // 用途別の表示名に変更
    if (selectedType === 'books') {
      newText = option.getAttribute('data-books');
    } else if (selectedType === 'tasks') {
      newText = option.getAttribute('data-tasks');
    } else if (selectedType === 'inventory') {
      newText = option.getAttribute('data-inventory');
    } else {
      newText = option.getAttribute('data-default');
    }

    console.log(
      'Updating option:',
      option.value,
      'from',
      currentText,
      'to',
      newText
    );

    // innerHTMLを使用して確実に更新
    option.innerHTML = newText;
  });
}

// ページ読み込み時に初期化
document.addEventListener('DOMContentLoaded', function () {
  console.log('Status display initialized');
  updateStatusDisplay();
});
