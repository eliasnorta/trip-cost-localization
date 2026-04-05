CREATE DATABASE IF NOT EXISTS fuel_calculator_localization

CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fuel_calculator_localization;

CREATE TABLE IF NOT EXISTS calculation_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    distance DOUBLE NOT NULL,
    consumption DOUBLE NOT NULL,
    price DOUBLE NOT NULL,
    total_fuel DOUBLE NOT NULL,
    total_cost DOUBLE NOT NULL,
    language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS localization_strings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    UNIQUE KEY unique_key_lang (`key`, `language`)
);

INSERT INTO localization_strings (`key`, `value`, `language`) VALUES
    ('distance.label', 'Distance (km)', 'en_US'),
    ('consumption.label', 'Fuel Consumption (L/100 km)', 'en_US'),
    ('price.label', 'Fuel Price (per liter)', 'en_US'),
    ('language.label', 'Language', 'en_US'),
    ('distance.prompt', 'Trip distance in kilometers', 'en_US'),
    ('consumption.prompt', 'Fuel consumption (L/100 km)', 'en_US'),
    ('price.prompt', 'Fuel price per liter', 'en_US'),
    ('calculate.button', 'Calculate Trip Cost', 'en_US'),
    ('result.label', 'Total fuel needed: {0} L | Total cost: {1}', 'en_US'),
    ('invalid.input', 'Invalid input', 'en_US'),
    ('database.error', 'Could not save calculation to database', 'en_US'),

    ('distance.label', 'Distance (km)', 'fr_FR'),
    ('consumption.label', 'Consommation de carburant (L/100 km)', 'fr_FR'),
    ('price.label', 'Prix du carburant (par litre)', 'fr_FR'),
    ('language.label', 'Langue', 'fr_FR'),
    ('distance.prompt', 'Distance du trajet en kilometres', 'fr_FR'),
    ('consumption.prompt', 'Consommation (L/100 km)', 'fr_FR'),
    ('price.prompt', 'Prix par litre', 'fr_FR'),
    ('calculate.button', 'Calculer le cout du trajet', 'fr_FR'),
    ('result.label', 'Carburant necessaire : {0} L | Cout total : {1}', 'fr_FR'),
    ('invalid.input', 'Entree invalide', 'fr_FR'),
    ('database.error', 'Impossible d''enregistrer le calcul en base de donnees', 'fr_FR'),

    ('distance.label', '距離 (km)', 'ja_JP'),
    ('consumption.label', '燃費 (L/100 km)', 'ja_JP'),
    ('price.label', '燃料価格 (1Lあたり)', 'ja_JP'),
    ('language.label', '言語', 'ja_JP'),
    ('distance.prompt', '移動距離を入力 (km)', 'ja_JP'),
    ('consumption.prompt', '燃費を入力 (L/100 km)', 'ja_JP'),
    ('price.prompt', '1Lあたりの価格を入力', 'ja_JP'),
    ('calculate.button', '旅行コストを計算', 'ja_JP'),
    ('result.label', '必要燃料量: {0} L | 合計コスト: {1}', 'ja_JP'),
    ('invalid.input', '入力が無効です', 'ja_JP'),
    ('database.error', '計算結果をデータベースに保存できません', 'ja_JP'),

    ('distance.label', 'مسافت (km)', 'fa_IR'),
    ('consumption.label', 'مصرف سوخت (L/100 km)', 'fa_IR'),
    ('price.label', 'قیمت سوخت (هر لیتر)', 'fa_IR'),
    ('language.label', 'زبان', 'fa_IR'),
    ('distance.prompt', 'مسافت سفر را وارد کنید', 'fa_IR'),
    ('consumption.prompt', 'مصرف سوخت را وارد کنید', 'fa_IR'),
    ('price.prompt', 'قیمت هر لیتر را وارد کنید', 'fa_IR'),
    ('calculate.button', 'محاسبه هزینه سفر', 'fa_IR'),
    ('result.label', 'سوخت مورد نیاز: {0} لیتر | هزینه کل: {1}', 'fa_IR'),
    ('invalid.input', 'ورودی نامعتبر است', 'fa_IR'),
    ('database.error', 'ذخیره محاسبه در پایگاه داده انجام نشد', 'fa_IR');
