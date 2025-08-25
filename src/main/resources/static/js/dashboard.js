let currentDeleteId = null;

// 削除ボタンクリック時の処理
document.addEventListener('DOMContentLoaded', function() {
  const deleteButtons = document.querySelectorAll('.delete-btn');
  
  deleteButtons.forEach(button => {
    button.addEventListener('click', function() {
      const itemId = this.getAttribute('data-id');
      const itemName = this.getAttribute('data-name');
      
      // モーダルにアイテム名を表示
      document.getElementById('deleteMessage').textContent = 
        `「${itemName}」を削除しますか？この操作は取り消せません。`;
      
      // 削除対象のIDを保存
      currentDeleteId = itemId;
      
      // モーダルを表示
      showDeleteModal();
    });
  });
});

// モーダルを表示
function showDeleteModal() {
  document.getElementById('deleteModal').classList.add('show');
}

// モーダルを閉じる
function closeDeleteModal() {
  document.getElementById('deleteModal').classList.remove('show');
  currentDeleteId = null;
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

// モーダル外クリックで閉じる
document.getElementById('deleteModal').addEventListener('click', function(e) {
  if (e.target === this) {
    closeDeleteModal();
  }
});
