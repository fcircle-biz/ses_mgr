/**
 * SES業務システム共通JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // メニュートグル処理
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('mobile-open');
        });
        
        // サイドバー外クリックで閉じる
        document.addEventListener('click', function(event) {
            if (window.innerWidth <= 992) {
                const isClickInsideSidebar = sidebar.contains(event.target);
                const isClickOnToggle = menuToggle.contains(event.target);
                
                if (!isClickInsideSidebar && !isClickOnToggle && sidebar.classList.contains('mobile-open')) {
                    sidebar.classList.remove('mobile-open');
                }
            }
        });
    }
    
    // サブメニュートグル
    const menuItems = document.querySelectorAll('.nav-link.has-submenu');
    
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const parent = this.closest('.nav-item');
            
            if (parent.classList.contains('menu-open')) {
                parent.classList.remove('menu-open');
            } else {
                // 他のメニューを閉じる
                document.querySelectorAll('.nav-item.menu-open').forEach(openMenu => {
                    if (openMenu !== parent) {
                        openMenu.classList.remove('menu-open');
                    }
                });
                
                parent.classList.add('menu-open');
            }
        });
    });
    
    // フォームバリデーション
    const forms = document.querySelectorAll('.needs-validation');
    
    if (forms.length > 0) {
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', function(event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                
                form.classList.add('was-validated');
            }, false);
        });
    }
    
    // トーストメッセージ
    const toastElements = document.querySelectorAll('.toast');
    if (toastElements.length > 0) {
        Array.from(toastElements).forEach(toastEl => {
            const toast = new bootstrap.Toast(toastEl);
            toast.show();
        });
    }
    
    // ツールチップの初期化
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    if (tooltipTriggerList.length > 0) {
        Array.from(tooltipTriggerList).forEach(tooltipTriggerEl => {
            new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
    
    // ポップオーバーの初期化
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    if (popoverTriggerList.length > 0) {
        Array.from(popoverTriggerList).forEach(popoverTriggerEl => {
            new bootstrap.Popover(popoverTriggerEl);
        });
    }
    
    // 削除確認ダイアログ
    const deleteButtons = document.querySelectorAll('.delete-confirm');
    if (deleteButtons.length > 0) {
        Array.from(deleteButtons).forEach(button => {
            button.addEventListener('click', function(e) {
                if (!confirm('本当に削除しますか？この操作は取り消せません。')) {
                    e.preventDefault();
                }
            });
        });
    }
    
    // 日付選択ピッカー (Bootstrap Datepicker使用時)
    const datepickers = document.querySelectorAll('.datepicker');
    if (datepickers.length > 0 && typeof $.fn.datepicker === 'function') {
        $(datepickers).datepicker({
            format: 'yyyy-mm-dd',
            autoclose: true,
            language: 'ja',
            todayHighlight: true
        });
    }
    
    // ファイル選択の表示名更新
    const fileInputs = document.querySelectorAll('.custom-file-input');
    if (fileInputs.length > 0) {
        Array.from(fileInputs).forEach(input => {
            input.addEventListener('change', function(e) {
                const fileName = e.target.files[0]?.name || 'ファイルを選択';
                const label = e.target.nextElementSibling;
                if (label) {
                    label.textContent = fileName;
                }
            });
        });
    }
});

/**
 * ローディングオーバーレイの表示・非表示
 */
const loadingOverlay = {
    show: function() {
        const overlay = document.createElement('div');
        overlay.id = 'loading-overlay';
        overlay.style.position = 'fixed';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        overlay.style.display = 'flex';
        overlay.style.justifyContent = 'center';
        overlay.style.alignItems = 'center';
        overlay.style.zIndex = '9999';
        
        const spinner = document.createElement('div');
        spinner.className = 'spinner-border text-light';
        spinner.setAttribute('role', 'status');
        
        const span = document.createElement('span');
        span.className = 'visually-hidden';
        span.textContent = 'Loading...';
        
        spinner.appendChild(span);
        overlay.appendChild(spinner);
        document.body.appendChild(overlay);
    },
    
    hide: function() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.remove();
        }
    }
};