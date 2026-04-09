-- 用户表
CREATE TABLE `user` (
        id INT PRIMARY KEY AUTO_INCREMENT,
        user_name VARCHAR(255) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        avatar VARCHAR(255) DEFAULT NULL,
        role TINYINT DEFAULT 0 ,
        status TINYINT DEFAULT 1 ,
        wallet DECIMAL(10, 2) NOT NULL DEFAULT 0,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ;

-- 商品表
CREATE TABLE good (
        id INT PRIMARY KEY AUTO_INCREMENT,
        good_name VARCHAR(255) NOT NULL,
        image VARCHAR(500),
        price DECIMAL(10, 2) NOT NULL,
        description VARCHAR(500) DEFAULT NULL,
        selling_status ENUM('未出售', '已出售') DEFAULT '未出售',
        seller_id INT NOT NULL,
        status TINYINT DEFAULT 1 ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ;

-- 标签字典表
CREATE TABLE tag (
        id INT PRIMARY KEY AUTO_INCREMENT,
        tag_name VARCHAR(255) UNIQUE NOT NULL
) ;

-- 商品-标签关联表
CREATE TABLE good_tag (
        id INT PRIMARY KEY AUTO_INCREMENT,
        good_id INT NOT NULL,
        tag_id INT NOT NULL,
        UNIQUE(good_id, tag_id)
) ;

-- 关注表（人对人）
CREATE TABLE follow (
        id INT PRIMARY KEY AUTO_INCREMENT,
        follower_id INT NOT NULL ,
        following_id INT NOT NULL ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(follower_id, following_id)
) ;

-- 收藏表（人对商品）
CREATE TABLE favorite (
        id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT NOT NULL ,
        good_id INT NOT NULL ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, good_id)
) ;

-- 评论表
CREATE TABLE comment (
        id INT PRIMARY KEY AUTO_INCREMENT,
        good_id INT NOT NULL ,
        user_id INT NOT NULL ,
        content VARCHAR(500) NOT NULL,
        status TINYINT DEFAULT 1 ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ;

-- 点赞评论表
CREATE TABLE comment_like (
        id INT PRIMARY KEY AUTO_INCREMENT,
        comment_id INT NOT NULL ,
        user_id INT NOT NULL ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(comment_id, user_id)
) ;

-- 订单表
CREATE TABLE `order` (
        id INT PRIMARY KEY AUTO_INCREMENT,
        good_id INT NOT NULL,
        buyer_id INT NOT NULL,
        seller_id INT NOT NULL,
        price DECIMAL(10, 2) NOT NULL,
        status TINYINT DEFAULT 0 COMMENT '0待确认 1已完成 2已取消',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 钱包流水表
CREATE TABLE wallet_log (
        id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT NOT NULL,
        amount DECIMAL(10, 2) NOT NULL COMMENT '正数充值，负数消费',
        balance_before DECIMAL(10, 2) NOT NULL,
        balance_after DECIMAL(10, 2) NOT NULL,
        type TINYINT NOT NULL COMMENT '1充值 2购买 3收入',
        related_id INT DEFAULT NULL COMMENT '关联订单ID',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);