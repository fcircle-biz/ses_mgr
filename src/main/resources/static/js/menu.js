/**
 * メニュー画面用JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // レスポンシブデモ用メニュー切り替え
    const tabletToggle = document.getElementById('tablet-toggle');
    const tabletSidebar = document.getElementById('tablet-sidebar');
    
    if (tabletToggle && tabletSidebar) {
        tabletToggle.addEventListener('click', function() {
            tabletSidebar.classList.toggle('show');
        });
    }
    
    const mobileToggle = document.getElementById('mobile-toggle');
    const mobileSidebar = document.getElementById('mobile-sidebar');
    const mobileOverlay = document.getElementById('mobile-overlay');
    
    if (mobileToggle && mobileSidebar && mobileOverlay) {
        mobileToggle.addEventListener('click', function() {
            mobileSidebar.classList.toggle('show');
            mobileOverlay.classList.toggle('show');
        });
        
        mobileOverlay.addEventListener('click', function() {
            mobileSidebar.classList.remove('show');
            mobileOverlay.classList.remove('show');
        });
    }
    
    // アクセス権限テーブルのフィルタリング
    const roleFilterButtons = document.querySelectorAll('.role-filter');
    const roleRows = document.querySelectorAll('.role-access-table tbody tr');
    
    if (roleFilterButtons.length > 0 && roleRows.length > 0) {
        roleFilterButtons.forEach(button => {
            button.addEventListener('click', function() {
                const role = this.getAttribute('data-role');
                
                // ボタンのアクティブ状態を切り替え
                roleFilterButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                
                // 行の表示を切り替え
                if (role === 'all') {
                    roleRows.forEach(row => row.style.display = '');
                } else {
                    roleRows.forEach(row => {
                        const hasAccess = row.querySelector(`.${role}-access.access-granted`);
                        row.style.display = hasAccess ? '' : 'none';
                    });
                }
            });
        });
    }
    
    // メニューデモ用のサブメニュー展開
    const demoMenuItems = document.querySelectorAll('.sidebar-menu-item.has-submenu');
    
    if (demoMenuItems.length > 0) {
        demoMenuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                const submenu = this.nextElementSibling;
                
                if (submenu.style.maxHeight) {
                    submenu.style.maxHeight = null;
                    this.classList.remove('open');
                } else {
                    submenu.style.maxHeight = submenu.scrollHeight + 'px';
                    this.classList.add('open');
                }
            });
        });
    }
});