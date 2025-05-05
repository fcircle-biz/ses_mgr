/**
 * Dashboard specific JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // 売上推移グラフの初期化
    initSalesChart();
    
    // 稼働率グラフの初期化
    initUtilizationChart();
    
    // 案件ステータスグラフの初期化
    initProjectStatusChart();
    
    // 期間選択切り替えイベント
    const periodButtons = document.querySelectorAll('.period-selector .btn');
    if (periodButtons.length > 0) {
        periodButtons.forEach(button => {
            button.addEventListener('click', function() {
                // 他のボタンからactiveクラスを削除
                periodButtons.forEach(btn => btn.classList.remove('active'));
                
                // クリックされたボタンにactiveクラスを追加
                this.classList.add('active');
                
                // 選択された期間に応じてグラフデータを更新
                const period = this.getAttribute('data-period');
                updateChartsForPeriod(period);
            });
        });
    }
    
    // 通知アイテムのクリックイベント
    const notificationItems = document.querySelectorAll('.notification-item');
    if (notificationItems.length > 0) {
        notificationItems.forEach(item => {
            item.addEventListener('click', function() {
                // 既読状態に変更
                this.classList.remove('unread');
                
                // 実際の実装では通知既読APIを呼び出す
                // markNotificationAsRead(this.getAttribute('data-notification-id'));
            });
        });
    }
});

/**
 * 売上推移グラフの初期化
 */
function initSalesChart() {
    const salesChartElem = document.getElementById('salesChart');
    if (!salesChartElem) return;
    
    const ctx = salesChartElem.getContext('2d');
    
    // サンプルデータ - 実際の実装ではAPIから動的に取得
    const monthlyData = {
        labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
        datasets: [{
            label: '売上',
            data: [9800, 10500, 9200, 11300, 12500, 13800],
            borderColor: '#0d6efd',
            backgroundColor: 'rgba(13, 110, 253, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
        }, {
            label: '目標',
            data: [10000, 10000, 10000, 12000, 12000, 12000],
            borderColor: '#dc3545',
            borderDash: [5, 5],
            borderWidth: 2,
            fill: false,
            tension: 0,
            pointRadius: 0
        }]
    };
    
    window.salesChart = new Chart(ctx, {
        type: 'line',
        data: monthlyData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: function(value) {
                            return (value / 10000).toFixed(1) + '万';
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + (context.parsed.y / 10000).toFixed(1) + '万円';
                        }
                    }
                },
                legend: {
                    position: 'top'
                }
            }
        }
    });
}

/**
 * 稼働率グラフの初期化
 */
function initUtilizationChart() {
    const utilizationChartElem = document.getElementById('utilizationChart');
    if (!utilizationChartElem) return;
    
    const ctx = utilizationChartElem.getContext('2d');
    
    // サンプルデータ - 実際の実装ではAPIから動的に取得
    const utilizationData = {
        labels: ['Java', 'JavaScript', 'Python', 'C#', 'PHP', 'その他'],
        datasets: [{
            data: [42, 23, 15, 8, 7, 5],
            backgroundColor: [
                '#0d6efd',
                '#6f42c1',
                '#20c997',
                '#ffc107',
                '#fd7e14',
                '#6c757d'
            ],
            borderWidth: 1
        }]
    };
    
    window.utilizationChart = new Chart(ctx, {
        type: 'doughnut',
        data: utilizationData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

/**
 * 案件ステータスグラフの初期化
 */
function initProjectStatusChart() {
    const statusChartElem = document.getElementById('projectStatusChart');
    if (!statusChartElem) return;
    
    const ctx = statusChartElem.getContext('2d');
    
    // サンプルデータ - 実際の実装ではAPIから動的に取得
    const statusData = {
        labels: ['進行中', '契約前', '交渉中', '提案中', '遅延'],
        datasets: [{
            data: [24, 12, 8, 5, 3],
            backgroundColor: [
                '#28a745',
                '#0d6efd',
                '#ffc107',
                '#17a2b8',
                '#dc3545'
            ],
            borderWidth: 1
        }]
    };
    
    window.projectStatusChart = new Chart(ctx, {
        type: 'bar',
        data: statusData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

/**
 * 選択された期間に応じてグラフを更新
 */
function updateChartsForPeriod(period) {
    // 実際の実装では、APIから選択された期間のデータを取得する
    // 今回はサンプルデータを使用
    
    let salesData;
    
    switch(period) {
        case 'year':
            salesData = {
                labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                datasets: [{
                    label: '売上',
                    data: [9800, 10500, 9200, 11300, 12500, 13800, 11200, 10900, 12800, 14200, 13500, 15100],
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }, {
                    label: '目標',
                    data: [10000, 10000, 10000, 12000, 12000, 12000, 12000, 12000, 13000, 13000, 13000, 13000],
                    borderColor: '#dc3545',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    fill: false,
                    tension: 0,
                    pointRadius: 0
                }]
            };
            break;
            
        case 'half':
            salesData = {
                labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
                datasets: [{
                    label: '売上',
                    data: [9800, 10500, 9200, 11300, 12500, 13800],
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }, {
                    label: '目標',
                    data: [10000, 10000, 10000, 12000, 12000, 12000],
                    borderColor: '#dc3545',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    fill: false,
                    tension: 0,
                    pointRadius: 0
                }]
            };
            break;
            
        case 'quarter':
            salesData = {
                labels: ['4月', '5月', '6月'],
                datasets: [{
                    label: '売上',
                    data: [11300, 12500, 13800],
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }, {
                    label: '目標',
                    data: [12000, 12000, 12000],
                    borderColor: '#dc3545',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    fill: false,
                    tension: 0,
                    pointRadius: 0
                }]
            };
            break;
    }
    
    if (window.salesChart && salesData) {
        window.salesChart.data.labels = salesData.labels;
        window.salesChart.data.datasets = salesData.datasets;
        window.salesChart.update();
    }
}