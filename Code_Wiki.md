# 圣惟书店管理系统 (Shengwei Bookstore) Code Wiki

## 1. 项目整体架构 (Overall Architecture)

本项目采用前后端分离的架构模式，分为后端服务与前端管理/用户平台。

### 1.1 后端架构 (Backend)
- **核心框架**: Spring Boot 2.7.18 + Java 8
- **持久层框架**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0.33
- **缓存**: Redis
- **安全与认证**: Spring Security + JWT (JSON Web Token)
- **连接池**: Druid 1.2.16
- **工具类库**: Hutool 5.8.16, Apache POI

**分层设计**:
- `controller`: 控制层，负责处理HTTP请求与响应。
- `service` & `service/impl`: 业务逻辑层，处理核心业务规则。
- `mapper`: 数据访问层，与MyBatis-Plus结合实现数据库操作。
- `entity`: 实体层，与数据库表结构一一映射。
- `config`: 配置层，包含跨域、安全、MyBatis-Plus、Redis等全局配置。
- `common`: 公共层，定义统一的响应结果(`Result`)和分页模型(`PageQuery`, `PageResult`)。

### 1.2 前端架构 (Frontend)
前端分为两个独立的子项目：
- **Admin端** (`qianduan/admin`): 面向书店管理员与员工的后台管理系统。
- **User端** (`qianduan/user`): 面向顾客的购书与捐赠前台系统。

**技术栈**:
- **核心框架**: Vue 2.6.14
- **路由管理**: Vue Router 3.5.1
- **状态管理**: Vuex 3.6.2
- **UI组件库**: Element UI 2.15.13
- **网络请求**: Axios 0.27.2
- **数据可视化**: ECharts 5.4.2

---

## 2. 主要模块职责 (Main Module Responsibilities)

### 2.1 认证与权限模块 (Auth & Permission)
- **职责**: 负责用户/顾客的注册、登录、密码修改，以及管理员的RBAC(基于角色的访问控制)权限校验。
- **核心表**: `sys_user`, `sys_role`, `sys_menu`, `sys_role_menu`, `sys_user_role`

### 2.2 图书管理模块 (Book Management)
- **职责**: 负责图书信息的增删改查、图书分类、新品/热销图书展示以及图书封面的上传。
- **核心表**: `book_info`

### 2.3 库存管理模块 (Inventory Management)
- **职责**: 跟踪图书的库存数量、可用数量与锁定数量，记录仓库位置与盘点状态。
- **核心表**: `inventory`

### 2.4 订单与购物车模块 (Trade & Cart)
- **职责**: 处理顾客将图书加入购物车、生成订单、支付结算以及订单退款等交易流程。
- **核心表**: `trade_order`, `trade_order_item`, `trade_order_refund`, `shopping_cart`

### 2.5 捐赠管理模块 (Donation)
- **职责**: 记录爱心人士的图书捐赠申请、审核流程以及捐赠公示。包含对捐赠人信息的管理。
- **核心表**: `donation_person`, `donation_record`, `donor_info`

### 2.6 客户与评价模块 (Customer & Review)
- **职责**: 管理顾客账户信息、会员等级与积分；管理顾客对所购图书的评价、评分及审核。
- **核心表**: `customer`, `book_review`

---

## 3. 关键类与函数说明 (Key Classes & Functions)

### 3.1 后端关键类 (Backend)
- **`WebConfig`** (`com.shengwei.tushuguanli.config.WebConfig`)
  - **说明**: WebMvc配置类，注册了`JwtAuthInterceptor`和`PermissionInterceptor`，并配置了静态资源(如图书封面)的路径映射。同时定义了无需认证的API白名单。
- **`JwtAuthInterceptor`** (`com.shengwei.tushuguanli.config.JwtAuthInterceptor`)
  - **说明**: JWT拦截器，在请求处理前拦截，从HTTP Header中解析Bearer Token，校验其合法性，并将`userId`注入到请求属性中供后续Controller使用。
- **`SecurityConfig`** (`com.shengwei.tushuguanli.config.SecurityConfig`)
  - **说明**: Spring Security配置，关闭CSRF并配置全局CORS，将实际的鉴权交由MVC层的拦截器处理。
- **`GlobalExceptionHandler`** (`com.shengwei.tushuguanli.exception.GlobalExceptionHandler`)
  - **说明**: 全局异常处理器，捕获`BusinessException`及其他系统异常，统一返回JSON格式的错误信息。

### 3.2 前端关键文件 (Frontend)
- **`router/index.js`**
  - **说明**: Vue Router配置。包含路由拦截器(`router.beforeEach`)，负责检查本地`token`状态，拦截未登录访问。Admin端还包含基于动态菜单(`hasMenuPermission`)的RBAC路由权限控制逻辑。
- **`utils/request.js`**
  - **说明**: Axios请求封装。配置了基础URL，统一在请求头中注入`Authorization` Token，并统一处理后端的401、403等HTTP错误状态码。
- **`store/index.js`**
  - **说明**: Vuex状态管理。存储当前登录用户的Token、个人信息以及菜单权限列表。

---

## 4. 依赖关系 (Dependencies)

### 4.1 后端核心依赖 (`pom.xml`)
- `spring-boot-starter-web`: 构建RESTful API。
- `mybatis-plus-boot-starter`: 简化MyBatis的CRUD操作。
- `mysql-connector-j`: MySQL数据库驱动。
- `druid-spring-boot-starter`: 阿里开源的高性能数据库连接池。
- `jjwt`: 生成与解析JWT Token。
- `hutool-all`: 提供丰富的Java工具方法(如字符串处理、日期操作等)。
- `spring-boot-starter-data-redis`: 提供Redis缓存支持。

### 4.2 前端核心依赖 (`package.json`)
- `vue` & `vue-router` & `vuex`: Vue全家桶。
- `element-ui`: 基于Vue 2的桌面端组件库，提供表格、表单、弹窗等丰富的UI组件。
- `axios`: 客户端HTTP请求库。
- `echarts`: 百度开源的图表库，用于后台管理系统的数据统计分析展示。
- `sass` & `sass-loader`: CSS预处理器。

---

## 5. 项目运行方式 (How to Run)

### 5.1 环境要求
- JDK 1.8+
- Node.js 14+ / 16+
- MySQL 8.0+
- Redis

### 5.2 数据库准备
1. 启动 MySQL 服务。
2. 创建名为 `fohow_con` 的数据库，字符集推荐使用 `utf8mb4`。
3. 执行根目录下的 `shengwei_bookstore.sql` 文件导入表结构与初始数据。

### 5.3 后端运行
1. 进入后端目录：`cd houduan`
2. 修改配置文件 `src/main/resources/application.yml`：
   - 更新 MySQL 连接的 `username` 和 `password`。
   - 更新 Redis 连接的 `password` (如有)。
3. 使用 Maven 编译并启动：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   *后端服务将运行在 `http://localhost:8080`，API 前缀为 `/api`*

### 5.4 前端运行
前端包含两个独立系统，需分别启动。

**启动 Admin 管理端**:
1. 进入 Admin 目录：`cd qianduan/admin`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run serve`
   *访问地址通常为 `http://localhost:8081` (具体参考控制台输出)*

**启动 User 客户端**:
1. 进入 User 目录：`cd qianduan/user`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run serve`
   *访问地址通常为 `http://localhost:8082` (具体参考控制台输出)*
