# RDC工作室学习日记

---

## ==DAY 1==

​		*今天是开始做rdc项目的第一天，人在图书馆，下午线代给人上麻木了。一气之下旷掉英语上机课，居然又点名。*



- #### MVC三层架构

  - Model：
    - service调用数据，内含entity、dto等。
    - crud(dao)。
  - view ：展现数据
  - Controller ：接收请求，页面跳转，剩下交给service



- #### git分支

  - 当前执行 :

  - ```bash
    # 1. 确认当前在main分支，且工作区干净
    git status
    
    # 2. 创建dev分支并切换过去
    git checkout -b dev
    
    # 3. 推送到远程
    git push -u origin dev
    
    # 4. 确认你现在在dev分支
    git branch
    # 应该显示：
    # * dev
    #   main
    ```

  - 之后每天：

  - ```bash
    # 开发前 - 确保在dev分支
    git checkout dev
    
    # 写完代码后，小粒度提交
    git add 修改的文件
    git commit -m "feat: 完成了xxx功能"
    git push origin dev
    ```

  - 合并：

  - ```bash
    # 一个完整功能测试通过后
    git checkout main
    git merge dev
    git push origin main
    git checkout dev    # 切回dev继续开发
    ```

    

---

---

## ==DAY 2==

​		*全天满课，把政治和离散旷了。*

​		

- #### 实现细节——MySQL

  - **用户：**

  - *(本来想分为用户和管理员的，但注册的时候为了不麻烦，账号直接取自增结果。但两张表都会从1开始自增，也就是id会有重合的情况，故合并为一张“user”，然后用“role”来区分他们）*

    - ```sql
      - id INT PRIMARY KEY AUTO-INCREMENT,
      - user_name varchar(255) UNIQUE NOT NULL
      - password varchar(255) NOT NULL,
      - avatar varchar(255) default NULL,
      - role TINYINT DEFAULT 0,
      - status TINYINT DEFAULT 1,
      - wallet DECIMAL(10, 2) NOT NULL DEFAULT 0,
      - create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
      #本来不想加的，AI说方便以后的一些功能实现，并且插入时间是MySQL自动填入当前服务器的
      ```

      

  - **商品：**

    - ```sql
      - - id INT PRIMARY KEY AUTO-INCREMENT
        - good_name varchar(255) NOT NULL
        - image varchar(500)
        - price DECIMAL(10, 2) NOT NULL      
      #本来用float，AI警告我会有精度问题，所以选择了DECIMAL，意为最多十位数字，小数点保留2位
        - description varchar(500) default null
        - selling_status ENUM(‘未出售’, ‘已出售’) default ‘未出售’
        - seller_id INT NOT NULL
        - status TINYINT DEFAULT 1,
        - create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      - 
      ```

      **标签字典：(AI推荐)**

    - ```sql
      - id INT PRIMARY KEY
      - tag_name VARCHAR(255) UNIQUE
      ```

      

  - **good_tag表(AI推荐)**

    - ```sql
      - id INT PRIMARY KEY
      - good_id INT 
      - tag_id INT
      - UNIQUE(good_id, tag_id)
      ```

      

  - **关注收藏(当时把关注和收藏都放在一个叫“favorite”的表里，被AI痛批了)**

  - ```sql
    -- 关注表（人对人）
    CREATE TABLE follow (
        id INT PRIMARY KEY AUTO_INCREMENT,
        follower_id INT NOT NULL,     #关注者（我）
        following_id INT NOT NULL,    #被关注者（卖家）
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(follower_id, following_id)   #不能重复关注同一个人
    );
    
    -- 收藏表（人对商品）
    CREATE TABLE favorite (
        id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT NOT NULL,        # 谁收藏的
        good_id INT NOT NULL,        # 收藏了哪个商品
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, good_id)     # 不能重复收藏同一个商品
    );
    ```

  - **评论**

  - ```sql
    CREATE TABLE comment (
        id INT PRIMARY KEY AUTO_INCREMENT,
        good_id INT NOT NULL,        # 评论哪个商品（关键）
        user_id INT NOT NULL,        # 谁发的
        content varchar(500) NOT NULL,       
        status TINYINT DEFAULT 1,    # 1正常 0已删除（管理员用）
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    ```

  - **点赞评论**

  - ```sql
    CREATE TABLE comment_like (
        id INT PRIMARY KEY AUTO_INCREMENT,
        comment_id INT NOT NULL ,
        user_id INT NOT NULL ,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(comment_id, user_id) 
    ) ;
    ```

    

- #### 创建实体类：

  - 由于不能用框架，驼峰映射不能添加依赖。但在写dao的时候多加一行就解决了
