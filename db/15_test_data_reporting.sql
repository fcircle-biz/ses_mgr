-- SES Management System PostgreSQL Test Data
-- 15_test_data_reporting.sql: レポートと分析関連テストデータ
-- Created: 2025-05-06

-- ダッシュボードデータ
INSERT INTO dashboards (dashboard_id, dashboard_name, user_id, dashboard_type, layout_config, description, is_default, is_public, created_by)
VALUES
    (1, '全社KPIダッシュボード', NULL, '全社', '{"columns": 3, "rowHeight": 200}', '全社の主要KPI指標を表示するダッシュボード', TRUE, TRUE, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (2, '営業部ダッシュボード', NULL, '部門', '{"columns": 3, "rowHeight": 200}', '営業部門向けのKPI指標とパイプライン情報を表示するダッシュボード', TRUE, TRUE, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (3, 'エンジニアリング部ダッシュボード', NULL, '部門', '{"columns": 3, "rowHeight": 200}', 'エンジニアリング部門向けのKPI指標とリソース情報を表示するダッシュボード', TRUE, TRUE, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (4, '山田太郎のダッシュボード', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '個人', '{"columns": 2, "rowHeight": 250}', '営業部長用のカスタムダッシュボード', TRUE, FALSE, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (5, '田中次郎のダッシュボード', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '個人', '{"columns": 2, "rowHeight": 250}', 'エンジニアリング部長用のカスタムダッシュボード', TRUE, FALSE, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3');

-- ウィジェットデータ
INSERT INTO widgets (widget_id, dashboard_id, widget_name, widget_type, visualization_type, data_source, query_config, display_config, position_config, refresh_interval, created_by)
VALUES
    -- 全社KPIダッシュボード用ウィジェット
    (1, 1, '月次売上高', 'カード', 'カウンター', 'SQL', '{"query": "SELECT SUM(amount) FROM invoices WHERE EXTRACT(YEAR FROM invoice_date) = 2025 AND EXTRACT(MONTH FROM invoice_date) = 5", "type": "value"}', '{"unit": "円", "format": "currency", "comparison": true, "target": 100000000}', '{"x": 0, "y": 0, "w": 1, "h": 1}', 3600, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (2, 1, '月次粗利', 'カード', 'カウンター', 'SQL', '{"query": "SELECT SUM(gross_profit) FROM invoices WHERE EXTRACT(YEAR FROM invoice_date) = 2025 AND EXTRACT(MONTH FROM invoice_date) = 5", "type": "value"}', '{"unit": "円", "format": "currency", "comparison": true, "target": 30000000}', '{"x": 1, "y": 0, "w": 1, "h": 1}', 3600, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (3, 1, '稼働エンジニア数', 'カード', 'カウンター', 'SQL', '{"query": "SELECT COUNT(*) FROM engineers WHERE status = ''アサイン中''", "type": "value"}', '{"unit": "人", "format": "number", "comparison": true, "target": 50}', '{"x": 2, "y": 0, "w": 1, "h": 1}', 3600, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (4, 1, '月次売上推移', 'グラフ', '折れ線', 'SQL', '{"query": "SELECT EXTRACT(MONTH FROM invoice_date) as month, SUM(amount) as revenue FROM invoices WHERE EXTRACT(YEAR FROM invoice_date) = 2025 GROUP BY month ORDER BY month", "type": "series"}', '{"xAxis": "月", "yAxis": "売上高", "series": [{"name": "売上高", "color": "#4CAF50"}], "legend": true}', '{"x": 0, "y": 1, "w": 3, "h": 1}', 86400, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (5, 1, '粗利率推移', 'グラフ', '折れ線', 'SQL', '{"query": "SELECT EXTRACT(MONTH FROM invoice_date) as month, AVG(gross_profit_rate) as rate FROM invoices WHERE EXTRACT(YEAR FROM invoice_date) = 2025 GROUP BY month ORDER BY month", "type": "series"}', '{"xAxis": "月", "yAxis": "粗利率 (%)", "series": [{"name": "粗利率", "color": "#2196F3"}], "legend": true}', '{"x": 0, "y": 2, "w": 1, "h": 1}', 86400, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (6, 1, '案件フェーズ別構成', 'グラフ', '円', 'SQL', '{"query": "SELECT phase, COUNT(*) as count FROM projects GROUP BY phase", "type": "pie"}', '{"series": [{"name": "案件数", "color": ["#FF9800", "#FFEB3B", "#4CAF50", "#2196F3"]}], "legend": true}', '{"x": 1, "y": 2, "w": 1, "h": 1}', 86400, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (7, 1, 'エンジニアステータス別構成', 'グラフ', '円', 'SQL', '{"query": "SELECT status, COUNT(*) as count FROM engineers GROUP BY status", "type": "pie"}', '{"series": [{"name": "エンジニア数", "color": ["#4CAF50", "#FF9800", "#2196F3", "#F44336", "#9C27B0"]}], "legend": true}', '{"x": 2, "y": 2, "w": 1, "h": 1}', 86400, 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 営業部ダッシュボード用ウィジェット
    (8, 2, '新規案件数', 'カード', 'カウンター', 'SQL', '{"query": "SELECT COUNT(*) FROM projects WHERE EXTRACT(YEAR FROM created_at) = 2025 AND EXTRACT(MONTH FROM created_at) = 5", "type": "value"}', '{"unit": "件", "format": "number", "comparison": true, "target": 10}', '{"x": 0, "y": 0, "w": 1, "h": 1}', 3600, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (9, 2, '今月の受注案件数', 'カード', 'カウンター', 'SQL', '{"query": "SELECT COUNT(*) FROM projects WHERE phase = ''受注'' AND EXTRACT(YEAR FROM updated_at) = 2025 AND EXTRACT(MONTH FROM updated_at) = 5", "type": "value"}', '{"unit": "件", "format": "number", "comparison": true, "target": 5}', '{"x": 1, "y": 0, "w": 1, "h": 1}', 3600, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (10, 2, '今月の受注金額', 'カード', 'カウンター', 'SQL', '{"query": "SELECT SUM(p.required_personnel * pr.preferred_unit_price * 3) FROM projects p JOIN project_requirements pr ON p.project_id = pr.project_id WHERE p.phase = ''受注'' AND EXTRACT(YEAR FROM p.updated_at) = 2025 AND EXTRACT(MONTH FROM p.updated_at) = 5", "type": "value"}', '{"unit": "円", "format": "currency", "comparison": true, "target": 50000000}', '{"x": 2, "y": 0, "w": 1, "h": 1}', 3600, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (11, 2, '案件パイプライン', 'グラフ', '棒', 'SQL', '{"query": "SELECT phase, COUNT(*) as count FROM projects GROUP BY phase ORDER BY CASE phase WHEN ''リード'' THEN 1 WHEN ''提案'' THEN 2 WHEN ''交渉'' THEN 3 WHEN ''受注'' THEN 4 END", "type": "series"}', '{"xAxis": "フェーズ", "yAxis": "案件数", "series": [{"name": "案件数", "color": "#2196F3"}], "legend": false}', '{"x": 0, "y": 1, "w": 2, "h": 1}', 86400, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (12, 2, '顧客別案件数', 'グラフ', '円', 'SQL', '{"query": "SELECT c.customer_name, COUNT(p.project_id) as count FROM projects p JOIN customers c ON p.customer_id = c.customer_id GROUP BY c.customer_name", "type": "pie"}', '{"series": [{"name": "案件数", "color": ["#FF9800", "#FFEB3B", "#4CAF50", "#2196F3", "#9C27B0"]}], "legend": true}', '{"x": 2, "y": 1, "w": 1, "h": 1}', 86400, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    (13, 2, '案件リスト', '表', 'テーブル', 'SQL', '{"query": "SELECT p.project_id, p.project_name, c.customer_name, p.phase, p.required_personnel, p.start_date FROM projects p JOIN customers c ON p.customer_id = c.customer_id ORDER BY p.created_at DESC LIMIT 10", "type": "table"}', '{"columns": [{"title": "ID", "field": "project_id"}, {"title": "案件名", "field": "project_name"}, {"title": "顧客名", "field": "customer_name"}, {"title": "フェーズ", "field": "phase"}, {"title": "必要人数", "field": "required_personnel"}, {"title": "開始日", "field": "start_date"}]}', '{"x": 0, "y": 2, "w": 3, "h": 1}', 3600, 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2'),
    
    -- エンジニアリング部ダッシュボード用ウィジェット
    (14, 3, '稼働エンジニア数', 'カード', 'カウンター', 'SQL', '{"query": "SELECT COUNT(*) FROM engineers WHERE status = ''アサイン中''", "type": "value"}', '{"unit": "人", "format": "number", "comparison": true, "target": 50}', '{"x": 0, "y": 0, "w": 1, "h": 1}', 3600, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (15, 3, '稼働可能エンジニア数', 'カード', 'カウンター', 'SQL', '{"query": "SELECT COUNT(*) FROM engineers WHERE status = ''稼働可能''", "type": "value"}', '{"unit": "人", "format": "number", "comparison": false}', '{"x": 1, "y": 0, "w": 1, "h": 1}', 3600, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (16, 3, '稼働率', 'カード', 'カウンター', 'SQL', '{"query": "SELECT ROUND(COUNT(CASE WHEN status = ''アサイン中'' THEN 1 END)::numeric / COUNT(*)::numeric * 100, 2) FROM engineers WHERE status IN (''アサイン中'', ''稼働可能'')", "type": "value"}', '{"unit": "%", "format": "percent", "comparison": true, "target": 90}', '{"x": 2, "y": 0, "w": 1, "h": 1}', 3600, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (17, 3, 'スキル別エンジニア数', 'グラフ', '棒', 'SQL', '{"query": "SELECT s.skill_name, COUNT(DISTINCT es.engineer_id) as count FROM engineer_skills es JOIN skills s ON es.skill_id = s.skill_id GROUP BY s.skill_name ORDER BY count DESC LIMIT 10", "type": "series"}', '{"xAxis": "スキル", "yAxis": "エンジニア数", "series": [{"name": "エンジニア数", "color": "#4CAF50"}], "legend": false}', '{"x": 0, "y": 1, "w": 2, "h": 1}', 86400, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (18, 3, 'エンジニアステータス別構成', 'グラフ', '円', 'SQL', '{"query": "SELECT status, COUNT(*) as count FROM engineers GROUP BY status", "type": "pie"}', '{"series": [{"name": "エンジニア数", "color": ["#4CAF50", "#FF9800", "#2196F3", "#F44336", "#9C27B0"]}], "legend": true}', '{"x": 2, "y": 1, "w": 1, "h": 1}', 86400, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3'),
    (19, 3, '稼働可能エンジニアリスト', '表', 'テーブル', 'SQL', '{"query": "SELECT e.engineer_id, e.engineer_name, c.company_name, e.years_of_experience, e.availability_date, e.preferred_unit_price FROM engineers e JOIN companies c ON e.company_id = c.company_id WHERE e.status = ''稼働可能'' ORDER BY e.availability_date ASC LIMIT 10", "type": "table"}', '{"columns": [{"title": "ID", "field": "engineer_id"}, {"title": "名前", "field": "engineer_name"}, {"title": "所属会社", "field": "company_name"}, {"title": "経験年数", "field": "years_of_experience"}, {"title": "稼働可能日", "field": "availability_date"}, {"title": "希望単金", "field": "preferred_unit_price"}]}', '{"x": 0, "y": 2, "w": 3, "h": 1}', 3600, 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3');

-- KPI定義データ
INSERT INTO kpi_definitions (kpi_id, kpi_code, kpi_name, kpi_category, description, calculation_formula, unit, data_source, frequency, is_active, target_direction)
VALUES
    (1, 'REV_MONTHLY', '月次売上高', '売上', '月間の総売上高', 'SUM(amount) FROM invoices WHERE period = current_month', '円', 'invoices', '月次', TRUE, '上昇'),
    (2, 'REV_QUARTERLY', '四半期売上高', '売上', '四半期の総売上高', 'SUM(amount) FROM invoices WHERE period = current_quarter', '円', 'invoices', '四半期', TRUE, '上昇'),
    (3, 'REV_YEARLY', '年間売上高', '売上', '年間の総売上高', 'SUM(amount) FROM invoices WHERE period = current_year', '円', 'invoices', '年次', TRUE, '上昇'),
    (4, 'PROFIT_MONTHLY', '月次粗利', '利益', '月間の総粗利益', 'SUM(gross_profit) FROM invoices WHERE period = current_month', '円', 'invoices', '月次', TRUE, '上昇'),
    (5, 'PROFIT_RATE', '粗利率', '利益', '売上高に対する粗利益の割合', 'SUM(gross_profit) / SUM(amount) * 100 FROM invoices WHERE period = current_month', '%', 'invoices', '月次', TRUE, '上昇'),
    (6, 'ENG_UTILIZATION', 'エンジニア稼働率', '稼働率', 'アサイン中のエンジニア割合', 'COUNT(CASE WHEN status = ''アサイン中'' THEN 1 END) / COUNT(*) * 100 FROM engineers WHERE status IN (''アサイン中'', ''稼働可能'')', '%', 'engineers', '月次', TRUE, '上昇'),
    (7, 'PROJECTS_NEW', '新規案件獲得数', '案件', '期間内に獲得した新規案件数', 'COUNT(*) FROM projects WHERE created_at BETWEEN period_start AND period_end', '件', 'projects', '月次', TRUE, '上昇'),
    (8, 'PROJECTS_CLOSED', '案件成約率', '案件', '提案から受注に至った案件の割合', 'COUNT(CASE WHEN phase = ''受注'' THEN 1 END) / COUNT(*) * 100 FROM projects WHERE phase IN (''提案'', ''交渉'', ''受注'')', '%', 'projects', '月次', TRUE, '上昇'),
    (9, 'ENG_MATCHING', 'マッチング成功率', '技術者', '面談から内定に至った割合', 'COUNT(CASE WHEN status IN (''内定'', ''契約締結'') THEN 1 END) / COUNT(*) * 100 FROM matching WHERE status IN (''面談調整中'', ''面談実施済'', ''内定'', ''契約締結'', ''見送り'')', '%', 'matching', '月次', TRUE, '上昇'),
    (10, 'CUSTOMER_NEW', '新規顧客獲得数', '顧客', '期間内に獲得した新規顧客数', 'COUNT(*) FROM customers WHERE created_at BETWEEN period_start AND period_end', '社', 'customers', '四半期', TRUE, '上昇');

-- KPI目標値データ
INSERT INTO kpi_targets (target_id, kpi_id, target_year, target_month, target_quarter, target_value, minimum_value, maximum_value, target_department_id, notes, created_by)
VALUES
    -- 月次目標値（2025年1-6月）
    (1, 1, 2025, 1, NULL, 80000000, 70000000, 90000000, NULL, '1月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (2, 1, 2025, 2, NULL, 85000000, 75000000, 95000000, NULL, '2月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (3, 1, 2025, 3, NULL, 90000000, 80000000, 100000000, NULL, '3月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (4, 1, 2025, 4, NULL, 85000000, 75000000, 95000000, NULL, '4月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (5, 1, 2025, 5, NULL, 90000000, 80000000, 100000000, NULL, '5月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (6, 1, 2025, 6, NULL, 95000000, 85000000, 105000000, NULL, '6月の月次売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 四半期売上目標（2025年）
    (7, 2, 2025, NULL, 1, 255000000, 240000000, 270000000, NULL, '2025年第1四半期売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (8, 2, 2025, NULL, 2, 270000000, 250000000, 290000000, NULL, '2025年第2四半期売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 年間売上目標（2025年）
    (9, 3, 2025, NULL, NULL, 1100000000, 1000000000, 1200000000, NULL, '2025年年間売上目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 粗利関連の目標値（2025年1-6月）
    (10, 4, 2025, 1, NULL, 24000000, 20000000, 28000000, NULL, '1月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (11, 4, 2025, 2, NULL, 25500000, 22000000, 29000000, NULL, '2月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (12, 4, 2025, 3, NULL, 27000000, 24000000, 30000000, NULL, '3月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (13, 4, 2025, 4, NULL, 25500000, 22000000, 29000000, NULL, '4月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (14, 4, 2025, 5, NULL, 27000000, 24000000, 30000000, NULL, '5月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (15, 4, 2025, 6, NULL, 28500000, 25000000, 32000000, NULL, '6月の月次粗利目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 粗利率の目標値（2025年）
    (16, 5, 2025, NULL, NULL, 30.0, 28.0, 32.0, NULL, '2025年の粗利率目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- エンジニア稼働率の目標値（2025年）
    (17, 6, 2025, NULL, NULL, 90.0, 85.0, 95.0, NULL, '2025年のエンジニア稼働率目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 案件関連の目標値（2025年1-6月）
    (18, 7, 2025, 1, NULL, 8, 6, 10, NULL, '1月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (19, 7, 2025, 2, NULL, 8, 6, 10, NULL, '2月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (20, 7, 2025, 3, NULL, 10, 8, 12, NULL, '3月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (21, 7, 2025, 4, NULL, 8, 6, 10, NULL, '4月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (22, 7, 2025, 5, NULL, 10, 8, 12, NULL, '5月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (23, 7, 2025, 6, NULL, 10, 8, 12, NULL, '6月の新規案件獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 案件成約率の目標値（2025年）
    (24, 8, 2025, NULL, NULL, 50.0, 45.0, 55.0, NULL, '2025年の案件成約率目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- マッチング成功率の目標値（2025年）
    (25, 9, 2025, NULL, NULL, 60.0, 55.0, 65.0, NULL, '2025年のマッチング成功率目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    
    -- 新規顧客獲得数の目標値（2025年各四半期）
    (26, 10, 2025, NULL, 1, 3, 2, 4, NULL, '2025年第1四半期の新規顧客獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (27, 10, 2025, NULL, 2, 3, 2, 4, NULL, '2025年第2四半期の新規顧客獲得目標', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1');

-- KPI達成値データ
INSERT INTO kpi_achievements (kpi_id, target_id, achievement_year, achievement_month, achievement_quarter, achievement_date, achievement_value, achievement_percentage, target_department_id, notes, data_source)
VALUES
    -- 月次売上実績（2025年1-4月）
    (1, 1, 2025, 1, NULL, '2025-01-31', 83500000, 104.38, NULL, '1月の月次売上実績', 'システム'),
    (1, 2, 2025, 2, NULL, '2025-02-28', 87200000, 102.59, NULL, '2月の月次売上実績', 'システム'),
    (1, 3, 2025, 3, NULL, '2025-03-31', 92500000, 102.78, NULL, '3月の月次売上実績', 'システム'),
    (1, 4, 2025, 4, NULL, '2025-04-30', 86300000, 101.53, NULL, '4月の月次売上実績', 'システム'),
    
    -- 四半期売上実績（2025年第1四半期）
    (2, 7, 2025, NULL, 1, '2025-03-31', 263200000, 103.22, NULL, '2025年第1四半期売上実績', 'システム'),
    
    -- 粗利関連の実績（2025年1-4月）
    (4, 10, 2025, 1, NULL, '2025-01-31', 25050000, 104.38, NULL, '1月の月次粗利実績', 'システム'),
    (4, 11, 2025, 2, NULL, '2025-02-28', 26160000, 102.59, NULL, '2月の月次粗利実績', 'システム'),
    (4, 12, 2025, 3, NULL, '2025-03-31', 27750000, 102.78, NULL, '3月の月次粗利実績', 'システム'),
    (4, 13, 2025, 4, NULL, '2025-04-30', 25890000, 101.53, NULL, '4月の月次粗利実績', 'システム'),
    
    -- 粗利率の実績（2025年1-4月）
    (5, 16, 2025, 1, NULL, '2025-01-31', 30.0, 100.00, NULL, '1月の粗利率実績', 'システム'),
    (5, 16, 2025, 2, NULL, '2025-02-28', 30.0, 100.00, NULL, '2月の粗利率実績', 'システム'),
    (5, 16, 2025, 3, NULL, '2025-03-31', 30.0, 100.00, NULL, '3月の粗利率実績', 'システム'),
    (5, 16, 2025, 4, NULL, '2025-04-30', 30.0, 100.00, NULL, '4月の粗利率実績', 'システム'),
    
    -- エンジニア稼働率の実績（2025年1-4月）
    (6, 17, 2025, 1, NULL, '2025-01-31', 88.5, 98.33, NULL, '1月のエンジニア稼働率実績', 'システム'),
    (6, 17, 2025, 2, NULL, '2025-02-28', 89.2, 99.11, NULL, '2月のエンジニア稼働率実績', 'システム'),
    (6, 17, 2025, 3, NULL, '2025-03-31', 91.5, 101.67, NULL, '3月のエンジニア稼働率実績', 'システム'),
    (6, 17, 2025, 4, NULL, '2025-04-30', 90.8, 100.89, NULL, '4月のエンジニア稼働率実績', 'システム'),
    
    -- 案件関連の実績（2025年1-4月）
    (7, 18, 2025, 1, NULL, '2025-01-31', 9, 112.50, NULL, '1月の新規案件獲得実績', 'システム'),
    (7, 19, 2025, 2, NULL, '2025-02-28', 7, 87.50, NULL, '2月の新規案件獲得実績', 'システム'),
    (7, 20, 2025, 3, NULL, '2025-03-31', 11, 110.00, NULL, '3月の新規案件獲得実績', 'システム'),
    (7, 21, 2025, 4, NULL, '2025-04-30', 8, 100.00, NULL, '4月の新規案件獲得実績', 'システム'),
    
    -- 案件成約率の実績（2025年1-4月）
    (8, 24, 2025, 1, NULL, '2025-01-31', 48.5, 97.00, NULL, '1月の案件成約率実績', 'システム'),
    (8, 24, 2025, 2, NULL, '2025-02-28', 52.0, 104.00, NULL, '2月の案件成約率実績', 'システム'),
    (8, 24, 2025, 3, NULL, '2025-03-31', 51.5, 103.00, NULL, '3月の案件成約率実績', 'システム'),
    (8, 24, 2025, 4, NULL, '2025-04-30', 49.0, 98.00, NULL, '4月の案件成約率実績', 'システム'),
    
    -- マッチング成功率の実績（2025年1-4月）
    (9, 25, 2025, 1, NULL, '2025-01-31', 58.5, 97.50, NULL, '1月のマッチング成功率実績', 'システム'),
    (9, 25, 2025, 2, NULL, '2025-02-28', 61.0, 101.67, NULL, '2月のマッチング成功率実績', 'システム'),
    (9, 25, 2025, 3, NULL, '2025-03-31', 62.5, 104.17, NULL, '3月のマッチング成功率実績', 'システム'),
    (9, 25, 2025, 4, NULL, '2025-04-30', 59.0, 98.33, NULL, '4月のマッチング成功率実績', 'システム'),
    
    -- 新規顧客獲得数の実績（2025年第1四半期）
    (10, 26, 2025, NULL, 1, '2025-03-31', 4, 133.33, NULL, '2025年第1四半期の新規顧客獲得実績', 'システム');