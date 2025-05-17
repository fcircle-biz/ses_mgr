/**
 * SES管理システム 共通スクリプト
 */
$(document).ready(function() {
    // アラートの自動非表示
    setTimeout(function() {
        $('.alert-dismissible').alert('close');
    }, 5000);

    // ツールチップの有効化
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    // ポップオーバーの有効化
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    // フォームの検証スタイルの有効化
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // データテーブルの初期化
    if ($.fn.DataTable && document.querySelector('.data-table')) {
        $('.data-table').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/ja.json'
            },
            responsive: true
        });
    }

    // セレクト2の初期化
    if ($.fn.select2 && document.querySelector('.select2')) {
        $('.select2').select2({
            theme: 'bootstrap-5',
            width: '100%'
        });
    }

    // 日付ピッカーの初期化
    if ($.fn.datepicker && document.querySelector('.datepicker')) {
        $('.datepicker').datepicker({
            format: 'yyyy/mm/dd',
            autoclose: true,
            todayHighlight: true,
            language: 'ja'
        });
    }

    // 確認ダイアログ
    $('body').on('click', '.confirm-action', function(e) {
        e.preventDefault();
        const form = $(this).closest('form');
        const message = $(this).data('confirm-message') || '本当にこの操作を行いますか？';
        
        if (confirm(message)) {
            form.submit();
        }
    });

    // アクティブなナビゲーションアイテムの設定
    const currentPath = window.location.pathname;
    $('.navbar-nav .nav-link').each(function() {
        const href = $(this).attr('href');
        if (href && currentPath.startsWith(href) && href !== '/') {
            $(this).addClass('active');
        }
    });
});