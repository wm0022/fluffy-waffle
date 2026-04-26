# 圣惟书店管理系统 - Code Wiki

## 1. 项目概述

圣惟书店管理系统是一个完整的图书管理解决方案，包含后端管理系统和前端用户界面。系统支持图书管理、订单处理、捐赠管理、会员管理等核心功能，旨在为书店提供高效、便捷的管理工具。

### 1.1 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端 | Spring Boot | - | 核心框架 |
| 后端 | MyBatis Plus | - | ORM框架 |
| 后端 | Redis | - | 缓存 |
| 后端 | JWT | - | 认证 |
| 后端 | MySQL | - | 数据库 |
| 后端 | Druid | - | 数据库连接池 |
| 前端 | Vue.js | - | 前端框架 |
| 前端 | Element UI | - | UI组件库 |

## 2. 项目结构

### 2.1 后端结构

```plaintext
/workspace/houduan/
├── src/
│   └── main/
│       ├── java/com/shengwei/tushuguanli/
│       │   ├── annotation/       # 自定义注解
│       │   ├── common/           # 通用类
│       │   ├── config/           # 配置类
│       │   ├── controller/       # 控制器
│       │   ├── entity/           # 实体类
│       │   ├── exception/        # 异常处理
│       │   ├── mapper/           # 数据访问层
│       │   ├── service/          # 业务逻辑层
│       │   ├── util/             # 工具类
│       │   └── TushuguanliApplication.java  # 启动类
│       └── resources/
│           ├── static/           # 静态资源
│           └── application.yml   # 配置文件
├── target/                      # 编译输出
├── uploads/                     # 上传文件
├── .gitignore                   # Git忽略文件
└── pom.xml                      # Maven依赖
```

### 2.2 前端结构

```plaintext
/workspace/qianduan/
├── admin/                      # 管理员前端
│   ├── src/
│   │   ├── api/                # API调用
│   │   ├── components/         # 组件
│   │   ├── directive/          # 指令
│   │   ├── layout/             # 布局
│   │   ├── router/             # 路由
│   │   ├── store/              # 状态管理
│   │   ├── styles/             # 样式
│   │   ├── utils/              # 工具
│   │   ├── views/              # 视图
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── .env                    # 环境配置
│   ├── package.json            # 依赖配置
│   └── vue.config.js           # Vue配置
└── user/                       # 用户前端
    ├── src/
    │   ├── api/                # API调用
    │   ├── layout/             # 布局
    │   ├── router/             # 路由
    │   ├── store/              # 状态管理
    │   ├── styles/             # 样式
    │   ├── utils/              # 工具
    │   ├── views/              # 视图
    │   ├── App.vue             # 根组件
    │   └── main.js             # 入口文件
    ├── .env                    # 环境配置
    ├── package.json            # 依赖配置
    └── vue.config.js           # Vue配置
```

## 3. 核心模块

### 3.1 用户认证模块

#### 3.1.1 管理员认证

**功能**：负责管理员的登录、登出和信息获取

**主要类**：
- [AuthController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/AuthController.java)：处理管理员认证相关请求
- [UserService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/UserService.java)：提供用户相关业务逻辑
- [JwtUtil](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/util/JwtUtil.java)：JWT工具类，用于生成和解析Token

**关键接口**：
- `POST /api/auth/login`：管理员登录
- `POST /api/auth/logout`：管理员登出
- `GET /api/auth/info`：获取当前管理员信息
- `POST /api/auth/change-password`：修改密码

#### 3.1.2 顾客认证

**功能**：负责顾客的注册、登录和信息管理

**主要类**：
- [CustomerAuthController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/CustomerAuthController.java)：处理顾客认证相关请求
- [CustomerService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/CustomerService.java)：提供顾客相关业务逻辑

### 3.2 图书管理模块

**功能**：负责图书的增删改查、上下架管理

**主要类**：
- [BookController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/BookController.java)：处理图书相关请求
- [BookInfoService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/BookInfoService.java)：提供图书相关业务逻辑
- [BookInfo](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/BookInfo.java)：图书实体类

**关键接口**：
- `GET /api/book/page`：分页查询图书列表
- `GET /api/book/hot`：获取热销图书
- `GET /api/book/new`：获取新品图书
- `GET /api/book/{bookId}`：根据ID查询图书详情
- `POST /api/book`：添加图书
- `PUT /api/book`：更新图书
- `DELETE /api/book/{bookId}`：删除图书
- `PUT /api/book/{bookId}/shelf`：上下架图书

### 3.3 订单管理模块

**功能**：负责订单的创建、支付、发货、完成、取消、退款等流程

**主要类**：
- [TradeOrderController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/TradeOrderController.java)：处理订单相关请求
- [TradeOrderService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/TradeOrderService.java)：提供订单相关业务逻辑
- [TradeOrder](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/TradeOrder.java)：订单实体类
- [TradeOrderItem](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/TradeOrderItem.java)：订单商品项实体类
- [TradeOrderRefund](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/TradeOrderRefund.java)：订单退款实体类

**关键接口**：
- `POST /api/order/create`：创建订单
- `POST /api/order/pay`：支付订单
- `GET /api/order/list`：获取订单列表
- `GET /api/order/items/{orderId}`：获取订单商品项
- `GET /api/order/all`：获取所有订单（管理员）
- `PUT /api/order/ship/{orderId}`：发货
- `PUT /api/order/complete/{orderId}`：完成订单
- `PUT /api/order/cancel/{orderNo}`：取消订单
- `POST /api/order/refund/apply`：申请退款
- `POST /api/order/refund/handle`：处理退款

### 3.4 捐赠管理模块

**功能**：负责捐赠的提交、审核、管理

**主要类**：
- [DonationController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/DonationController.java)：处理捐赠相关请求
- [DonationService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/DonationService.java)：提供捐赠相关业务逻辑
- [DonationRecord](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/DonationRecord.java)：捐赠记录实体类

**关键接口**：
- `POST /api/donation/submit`：提交捐赠申请
- `GET /api/donation/myList`：获取当前用户捐赠记录
- `GET /api/donation/all`：获取所有捐赠记录（管理员）
- `POST /api/donation/review`：审核捐赠记录
- `GET /api/donation/count`：统计捐赠信息

### 3.5 购物车模块

**功能**：负责购物车的管理

**主要类**：
- [ShoppingCartController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/ShoppingCartController.java)：处理购物车相关请求
- [ShoppingCartService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/ShoppingCartService.java)：提供购物车相关业务逻辑
- [ShoppingCart](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/ShoppingCart.java)：购物车实体类

### 3.6 会员管理模块

**功能**：负责会员信息的管理

**主要类**：
- [MemberController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/MemberController.java)：处理会员相关请求
- [MemberService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/MemberService.java)：提供会员相关业务逻辑
- [MemberInfo](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/MemberInfo.java)：会员信息实体类

### 3.7 库存管理模块

**功能**：负责图书库存的管理

**主要类**：
- [InventoryController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/InventoryController.java)：处理库存相关请求
- [InventoryService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/InventoryService.java)：提供库存相关业务逻辑
- [Inventory](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/Inventory.java)：库存实体类

### 3.8 服务反馈模块

**功能**：负责用户服务反馈的管理

**主要类**：
- [ServiceFeedbackController](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/controller/ServiceFeedbackController.java)：处理服务反馈相关请求
- [ServiceFeedbackService](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/service/ServiceFeedbackService.java)：提供服务反馈相关业务逻辑
- [ServiceFeedback](file:///workspace/houduan/src/main/java/com/shengwei/tushuguanli/entity/ServiceFeedback.java)：服务反馈实体类

## 4. 核心类与函数

### 4.1 通用类

#### 4.1.1 Result

**功能**：统一返回结果类，用于API接口返回标准化的响应格式

**主要方法**：
- `success(T data)`：返回成功结果，带数据
- `success(String message, T data)`：返回成功结果，带消息和数据
- `error(String message)`：返回错误结果，带消息
- `error(int code, String message)`：返回错误结果，带错误码和消息

#### 4.1.2 PageQuery

**功能**：分页查询参数类，用于接收分页相关参数

**主要字段**：
- `pageNum`：页码
- `pageSize`：每页大小
- `sortBy`：排序字段
- `order`：排序方式

#### 4.1.3 PageResult

**功能**：分页查询结果类，用于返回分页查询结果

**主要字段**：
- `total`：总记录数
- `pages`：总页数
- `current`：当前页码
- `size`：每页大小
- `records`：数据列表

### 4.2 工具类

#### 4.2.1 JwtUtil

**功能**：JWT工具类，用于生成和解析Token

**主要方法**：
- `generateToken(Long userId, String username)`：生成Token
- `parseToken(String token)`：解析Token
- `getUserIdFromToken(String token)`：从Token中获取用户ID

### 4.3 配置类

#### 4.3.1 SecurityConfig

**功能**：安全配置类，用于配置Spring Security

**主要配置**：
- 密码加密方式
- 认证规则
- 权限控制

#### 4.3.2 WebConfig

**功能**：Web配置类，用于配置Web相关设置

**主要配置**：
- 拦截器配置
- 资源访问规则

#### 4.3.3 JwtAuthInterceptor

**功能**：JWT认证拦截器，用于验证Token

**主要方法**：
- `preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)`：预处理请求，验证Token

#### 4.3.4 RedisConfig

**功能**：Redis配置类，用于配置Redis连接

### 4.4 实体类

#### 4.4.1 BaseEntity

**功能**：基础实体类，包含通用字段

**主要字段**：
- `id`：主键ID
- `createTime`：创建时间
- `updateTime`：更新时间
- `deleted`：删除标记

#### 4.4.2 SysUser

**功能**：系统用户实体类，用于管理员用户

**主要字段**：
- `username`：用户名
- `password`：密码
- `nickname`：昵称
- `roleId`：角色ID
- `status`：状态

#### 4.4.3 Customer

**功能**：顾客实体类，用于普通用户

**主要字段**：
- `username`：用户名
- `password`：密码
- `nickname`：昵称
- `phone`：电话
- `email`：邮箱
- `address`：地址

#### 4.4.4 BookInfo

**功能**：图书信息实体类

**主要字段**：
- `bookName`：书名
- `author`：作者
- `isbn`：ISBN
- `publisher`：出版社
- `price`：价格
- `categoryId`：分类ID
- `shelfStatus`：上架状态
- `stock`：库存
- `isHot`：是否热销
- `isNew`：是否新品
- `isDonation`：是否捐赠
- `coverImage`：封面图片
- `description`：描述

#### 4.4.5 TradeOrder

**功能**：交易订单实体类

**主要字段**：
- `orderNo`：订单号
- `userId`：用户ID
- `totalAmount`：总金额
- `status`：状态
- `paymentTime`：支付时间
- `shippingAddress`：收货地址
- `shippingPhone`：收货电话
- `shippingName`：收货人

### 4.5 服务类

#### 4.5.1 BookInfoServiceImpl

**功能**：图书信息服务实现类

**主要方法**：
- `pageBookList(Integer pageNum, Integer pageSize, Map<String, Object> params)`：分页查询图书列表
- `getHotBooks(Integer limit)`：获取热销图书
- `getNewBooks(Integer limit)`：获取新品图书
- `addBook(BookInfo bookInfo)`：添加图书
- `updateBook(BookInfo bookInfo)`：更新图书
- `deleteBook(Long bookId)`：删除图书
- `updateShelfStatus(Long bookId, Integer shelfStatus)`：更新上架状态

#### 4.5.2 TradeOrderServiceImpl

**功能**：交易订单服务实现类

**主要方法**：
- `createOrder(Long userId, List<CartItemVO> cartItems)`：创建订单
- `payOrder(String orderNo)`：支付订单
- `getOrderList(Long userId)`：获取用户订单列表
- `getAllOrders(String orderNo, Integer status)`：获取所有订单（管理员）
- `shipOrder(Long orderId)`：发货
- `completeOrder(Long orderId)`：完成订单
- `cancelOrder(String orderNo)`：取消订单
- `applyRefund(String orderNo, Long userId, String reason)`：申请退款
- `handleRefund(Long refundId, Integer status, String remark)`：处理退款

#### 4.5.3 DonationServiceImpl

**功能**：捐赠服务实现类

**主要方法**：
- `submitDonation(DonationRecord record)`：提交捐赠申请
- `getUserDonations(Long userId)`：获取用户捐赠记录
- `getAllDonations(Integer status)`：获取所有捐赠记录（管理员）
- `reviewDonation(Long id, Integer status, String reviewRemark, Long reviewerId)`：审核捐赠记录
- `countDonations()`：统计捐赠信息

## 5. 数据库设计

### 5.1 主要表结构

#### 5.1.1 sys_user

**功能**：系统用户表，存储管理员用户信息

**主要字段**：
- `id`：主键ID
- `username`：用户名
- `password`：密码
- `nickname`：昵称
- `role_id`：角色ID
- `status`：状态
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

#### 5.1.2 customer

**功能**：顾客表，存储普通用户信息

**主要字段**：
- `id`：主键ID
- `username`：用户名
- `password`：密码
- `nickname`：昵称
- `phone`：电话
- `email`：邮箱
- `address`：地址
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

#### 5.1.3 book_info

**功能**：图书信息表，存储图书基本信息

**主要字段**：
- `id`：主键ID
- `book_name`：书名
- `author`：作者
- `isbn`：ISBN
- `publisher`：出版社
- `price`：价格
- `category_id`：分类ID
- `shelf_status`：上架状态
- `stock`：库存
- `is_hot`：是否热销
- `is_new`：是否新品
- `is_donation`：是否捐赠
- `cover_image`：封面图片
- `description`：描述
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

#### 5.1.4 trade_order

**功能**：交易订单表，存储订单信息

**主要字段**：
- `id`：主键ID
- `order_no`：订单号
- `user_id`：用户ID
- `total_amount`：总金额
- `status`：状态
- `payment_time`：支付时间
- `shipping_address`：收货地址
- `shipping_phone`：收货电话
- `shipping_name`：收货人
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

#### 5.1.5 trade_order_item

**功能**：订单商品项表，存储订单中的商品信息

**主要字段**：
- `id`：主键ID
- `order_id`：订单ID
- `book_id`：图书ID
- `quantity`：数量
- `price`：单价
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

#### 5.1.6 donation_record

**功能**：捐赠记录表，存储捐赠信息

**主要字段**：
- `id`：主键ID
- `user_id`：用户ID
- `donation_type`：捐赠类型
- `donation_amount`：捐赠金额
- `status`：状态
- `reviewer_id`：审核人ID
- `review_remark`：审核备注
- `create_time`：创建时间
- `update_time`：更新时间
- `deleted`：删除标记

## 6. 系统配置

### 6.1 后端配置

**配置文件**：[application.yml](file:///workspace/houduan/src/main/resources/application.yml)

**主要配置项**：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| server.port | 8080 | 服务端口 |
| server.servlet.context-path | /api | 上下文路径 |
| spring.application.name | shengwei-bookstore | 应用名称 |
| spring.redis | 配置 | Redis连接配置 |
| spring.datasource | 配置 | 数据库连接配置 |
| spring.mybatis-plus | 配置 | MyBatis Plus配置 |
| jwt | 配置 | JWT配置 |
| upload.path | ./uploads | 文件上传路径 |
| logging | 配置 | 日志配置 |

### 6.2 前端配置

**配置文件**：
- [admin/.env](file:///workspace/qianduan/admin/.env)：管理员前端环境配置
- [user/.env](file:///workspace/qianduan/user/.env)：用户前端环境配置

## 7. 系统运行

### 7.1 后端运行

**环境要求**：
- JDK 1.8+
- MySQL 5.7+
- Redis 5.0+

**启动步骤**：
1. 导入数据库脚本：`shengwei_bookstore.sql`
2. 修改数据库连接配置：`application.yml`
3. 启动Redis服务
4. 运行 `TushuguanliApplication.java` 主类
5. 访问 API 地址：http://localhost:8080/api
6. 访问 Druid 监控：http://localhost:8080/api/druid/

### 7.2 前端运行

**环境要求**：
- Node.js 10+
- npm 6+

**启动步骤**：
1. 进入前端目录（admin 或 user）
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run serve`
4. 访问前端页面：根据终端输出的地址访问

## 8. 关键功能流程

### 8.1 图书管理流程

1. 管理员登录系统
2. 进入图书管理页面
3. 执行图书的增删改查操作
4. 对图书进行上下架管理
5. 查看图书统计信息

### 8.2 订单处理流程

1. 顾客登录系统
2. 添加图书到购物车
3. 从购物车创建订单
4. 支付订单
5. 管理员发货
6. 顾客确认收货，完成订单
7. 如需退款，顾客申请退款，管理员处理退款

### 8.3 捐赠流程

1. 顾客登录系统
2. 提交捐赠申请
3. 管理员审核捐赠申请
4. 捐赠申请审核通过后，系统记录捐赠信息
5. 管理员可以查看捐赠统计信息

## 9. 安全措施

### 9.1 认证与授权

- 使用JWT进行身份认证
- 密码使用Spring Security的PasswordEncoder加密
- 权限控制使用自定义注解 `@RequirePermission`
- 防止横向越权，从Token中获取用户ID，不接受前端传入

### 9.2 输入验证

- 对所有API接口参数进行验证
- 防止SQL注入、XSS等攻击

### 9.3 日志记录

- 系统操作日志记录
- 异常日志记录

## 10. 项目扩展

### 10.1 功能扩展

- 图书分类管理
- 会员等级与积分系统
- 图书推荐系统
- 在线支付集成
- 物流跟踪系统

### 10.2 技术扩展

- 引入缓存提高性能
- 引入消息队列处理异步任务
- 引入微服务架构，拆分业务模块
- 引入容器化部署

## 11. 总结

圣惟书店管理系统是一个功能完整的图书管理解决方案，涵盖了图书管理、订单处理、捐赠管理、会员管理等核心功能。系统采用Spring Boot + Vue.js技术栈，具有良好的扩展性和可维护性。

系统的主要特点包括：
- 完整的图书管理功能，支持图书的增删改查和上下架管理
- 完善的订单处理流程，支持订单的创建、支付、发货、完成、取消、退款
- 捐赠管理功能，支持捐赠的提交、审核、管理
- 会员管理功能，支持会员信息的管理
- 库存管理功能，支持图书库存的管理
- 服务反馈功能，支持用户服务反馈的管理
- 安全的认证与授权机制，使用JWT进行身份认证
- 良好的代码结构和文档，便于维护和扩展

系统可以根据实际需求进行扩展和定制，满足不同规模书店的管理需求。