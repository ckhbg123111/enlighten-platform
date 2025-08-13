USE `enlighten_platform`;

-- 将历史数据中的分类 form 统一改名为 mode（幂等）
UPDATE `user_article_config`
SET `category` = 'mode'
WHERE `category` = 'form';


