<template>
  <div class="member-page">
    <!-- ========== 用户信息头部横幅（带背景）========== -->
    <section class="profile-banner">
      <div class="banner-inner">
        <!-- 核心信息 -->
        <div class="user-core">
          <h2 class="user-nickname">{{ userInfo.realName || userInfo.username || '未设置' }}</h2>
          <p class="user-meta">
            <span class="meta-tag"><i class="el-icon-user"></i> {{ userInfo.username || '-' }}</span>
            <span class="meta-tag" v-if="genderText !== '-'">
              <i :class="userInfo.gender === 1 ? 'el-icon-male' : 'el-icon-female'"></i>
              {{ genderText }}
            </span>
          </p>
        </div>

        <!-- 会员卡片悬浮信息 -->
        <div class="member-badge-card" :class="'level-' + memberInfo.level">
          <div class="badge-header">
            <i class="el-icon-star-on badge-icon"></i>
            <span class="badge-level-name">{{ memberInfo.levelName }}</span>
          </div>
          <div class="badge-body">
            <div class="badge-stat">
              <span class="stat-num">{{ memberInfo.points || 0 }}</span>
              <span class="stat-label">积分</span>
            </div>
            <div class="badge-divider"></div>
            <div class="badge-stat">
              <span class="stat-num stat-amount">&yen;{{ formatAmount(memberInfo.totalAmount) }}</span>
              <span class="stat-label">累计消费</span>
            </div>
            <div class="badge-divider"></div>
            <div class="badge-stat">
              <span class="stat-num stat-discount">{{ memberInfo.discountText }}</span>
              <span class="stat-label">折扣</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 升级进度条（嵌入横幅底部） -->
      <div class="level-progress-bar" v-if="memberInfo.level < 4">
        <div class="progress-text">
          <span>距 <strong>{{ nextLevelName }}</strong> 还需消费</span>
          <span class="gap-amount">&yen;{{ nextLevelAmount }}</span>
        </div>
        <div class="progress-track">
          <div
            class="progress-fill"
            :style="{ width: progressPercentage + '%' }"
            :class="{ 'is-high': progressPercentage >= 90 }"
          ></div>
        </div>
      </div>
      <div class="level-progress-bar maxed" v-else>
        <i class="el-icon-trophy"></i> 恭喜！您已是最高等级会员
      </div>
    </section>

    <!-- ========== 主体内容双列布局 ========== -->
    <div class="main-layout">
      <!-- 左栏：基本信息 -->
      <aside class="left-panel">
        <!-- 个人资料卡 -->
        <div class="panel-card profile-card">
          <div class="card-head">
            <h3><i class="el-icon-postcard"></i> 基本信息</h3>
            <button
              v-if="!editing"
              class="text-action-btn edit-trigger"
              @click="startEdit"
            >
              <i class="el-icon-edit-outline"></i> 编辑资料
            </button>
            <div v-else class="edit-actions">
              <button class="btn-save" @click="saveProfile" :loading="saving">
                <i class="el-icon-check"></i> 保存
              </button>
              <button class="btn-cancel" @click="cancelEdit">
                取消
              </button>
            </div>
          </div>

          <!-- 展示模式 -->
          <div v-if="!editing" class="info-grid">
            <div class="info-cell" v-for="(item, idx) in displayFields" :key="'disp-' + idx">
              <div class="cell-icon-wrap" :style="{ background: item.bg }">
                <i :class="item.icon"></i>
              </div>
              <div class="cell-content">
                <span class="cell-label">{{ item.label }}</span>
                <span class="cell-value" :title="getValue(item.key)">{{ getValue(item.key) || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 编辑模式 -->
          <div v-else class="edit-form-area">
            <el-form ref="profileFormRef" :model="profileForm" label-position="top" size="small">
              <div class="form-grid">
                <el-form-item label="真实姓名">
                  <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio-button :label="1">男</el-radio-button>
                    <el-radio-button :label="2">女</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="身份证号">
                  <el-input v-model="profileForm.idCard" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="民族">
                  <el-input v-model="profileForm.nation" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="籍贯">
                  <el-input v-model="profileForm.nativePlace" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="出生日期">
                  <el-date-picker
                    v-model="profileForm.birthDate"
                    type="date"
                    value-format="yyyy-MM-dd"
                    style="width: 100%;"
                    placeholder="选择日期"
                  />
                </el-form-item>
                <el-form-item label="年龄">
                  <el-input-number v-model="profileForm.age" :min="0" :max="150" controls-position="right" style="width:100%;" />
                </el-form-item>
                <el-form-item label="学历">
                  <el-select v-model="profileForm.education" style="width:100%;" clearable placeholder="请选择">
                    <el-option v-for="edu in eduOptions" :key="edu" :label="edu" :value="edu" />
                  </el-select>
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="住址" class="full-width">
                  <el-input v-model="profileForm.address" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="阅读喜好" class="full-width">
                  <el-input
                    v-model="profileForm.preferences"
                    type="textarea"
                    :rows="3"
                    placeholder="描述您的阅读偏好..."
                  />
                </el-form-item>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 快捷操作入口 -->
        <div class="panel-card quick-actions">
          <div class="action-grid">
            <a class="quick-entry" href="/customer/home">
              <div class="entry-icon bg-blue">
                <i class="el-icon-goods"></i>
              </div>
              <span>去逛逛</span>
            </a>
            <a class="quick-entry" href="/customer/cart">
              <div class="entry-icon bg-orange">
                <i class="el-icon-shopping-cart-full"></i>
              </div>
              <span>购物车</span>
            </a>
            <a class="quick-entry" href="/customer/donation">
              <div class="entry-icon bg-red">
                <i class="el-icon-present"></i>
              </div>
              <span>公益捐赠</span>
            </a>
            <a class="quick-entry" href="/customer/charity">
              <div class="entry-icon bg-green">
                <i class="el-icon-s-promotion"></i>
              </div>
              <span>公益专区</span>
            </a>
          </div>
        </div>
      </aside>

      <!-- 右栏：会员规则 & 其他 -->
      <main class="right-panel">
        <!-- 会员等级规则表 -->
        <div class="panel-card rules-card">
          <div class="card-head">
            <h3><i class="el-icon-medal"></i> 会员等级权益</h3>
          </div>
          <div class="rules-table-wrap">
            <table class="rules-table">
              <thead>
                <tr>
                  <th>等级</th>
                  <th>升级条件</th>
                  <th>购书折扣</th>
                  <th>积分规则</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(rule, idx) in levelRules"
                  :key="'rule-' + idx"
                  :class="{ 'is-current': idx === memberInfo.level }"
                >
                  <td>
                    <span class="rule-level-dot" :style="{ background: ruleColor(idx) }"></span>
                    {{ rule.levelName }}
                  </td>
                  <td>{{ rule.condition }}</td>
                  <td>{{ rule.discount }}</td>
                  <td>{{ rule.points }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 账户安全提示 -->
        <div class="panel-card security-card">
          <div class="card-head">
            <h3><i class="el-icon-lock"></i> 账户安全</h3>
          </div>
          <div class="security-items">
            <div class="security-row">
              <span class="sec-label"><i class="el-icon-mobile-phone"></i> 手机号</span>
              <span class="sec-value">{{ maskedPhone(userInfo.phone) || '未绑定' }}</span>
              <span class="sec-status" :class="{ bound: !!userInfo.phone }">
                {{ userInfo.phone ? '已绑定' : '去绑定' }}
              </span>
            </div>
            <div class="security-row">
              <span class="sec-label"><i class="el-icon-message"></i> 邮箱</span>
              <span class="sec-value">{{ maskedEmail(userInfo.email) || '未绑定' }}</span>
              <span class="sec-status" :class="{ bound: !!userInfo.email }">
                {{ userInfo.email ? '已绑定' : '去绑定' }}
              </span>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 页面骨架屏（首次加载时显示） -->
    <div v-if="initialLoading" class="skeleton-overlay">
      <div class="skeleton-text w-200"></div>
      <div class="skeleton-text w-120"></div>
    </div>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

const PROFILE_CACHE_KEY = 'member_profile_cache'

export default {
  name: 'MemberCenter',
  data() {
    return {
      initialLoading: true,
      editing: false,
      saving: false,
      profileForm: {},
      memberInfo: {
        level: 0,
        levelName: '非会员',
        points: 0,
        totalAmount: '0.00',
        discount: 1.0,
        discountText: '无折扣'
      },
      levelRules: [
        { levelName: '非会员', condition: '累计消费 < ¥188', discount: '原价', points: '¥1 = 1积分' },
        { levelName: '普通会员', condition: '累计消费 ≥ ¥188', discount: '95折', points: '¥1 = 1积分' },
        { levelName: '银卡会员', condition: '累计消费 ≥ ¥388', discount: '9折', points: '¥1 = 1积分' },
        { levelName: '金卡会员', condition: '累计消费 ≥ ¥1888', discount: '85折', points: '¥1 = 1积分' },
        { levelName: '钻石卡会员', condition: '累计消费 ≥ ¥2888', discount: '8折', points: '¥1 = 1积分' }
      ],
      levelThresholds: [0, 188, 388, 1888, 2888],
      eduOptions: ['小学', '初中', '高中', '大专', '本科', '硕士', '博士'],
      // 展示字段定义（图标+标签+key+背景色）
      displayFields: [
        { label: '姓名', key: 'realName', icon: 'el-icon-postcard', bg: '#eef6ff' },
        { label: '性别', key: '_genderText', icon: 'el-icon-male', bg: '#fef0f0' },
        { label: '身份证号', key: 'idCard', icon: 'el-icon-bank-card', bg: '#fdf6ec' },
        { label: '民族', key: 'nation', icon: 'el-icon-place', bg: '#ecf5ff' },
        { label: '籍贯', key: 'nativePlace', icon: 'el-icon-map-location', bg: '#fef0f0' },
        { label: '出生日期', key: 'birthDate', icon: 'el-icon-date', bg: '#f0f9eb' },
        { label: '年龄', key: 'age', icon: 'el-icon-time', bg: '#fdf6ec' },
        { label: '学历', key: 'education', icon: 'el-icon-notebook-2', bg: '#ecf5ff' },
        { label: '手机号', key: 'phone', icon: 'el-icon-mobile-phone', bg: '#f0f9eb' },
        { label: '邮箱', key: 'email', icon: 'el-icon-message', bg: '#eef6ff' },
        { label: '住址', key: 'address', icon: 'el-icon-office-building', bg: '#fdf6ec' },
        { label: '阅读喜好', key: 'preferences', icon: 'el-icon-star-off', bg: '#fef0f0' }
      ]
    }
  },

  computed: {
    ...mapGetters(['userInfo']),

    genderText() {
      if (this.userInfo && this.userInfo.gender === 1) return '男'
      if (this.userInfo && this.userInfo.gender === 2) return '女'
      return '-'
    },

    nextLevelName() {
      const names = ['非会员', '普通会员', '银卡会员', '金卡会员', '钻石卡会员']
      return names[this.memberInfo.level + 1] || '钻石卡会员'
    },

    nextLevelAmount() {
      if (this.memberInfo.level >= 4) return '0.00'
      const currentAmount = parseFloat(this.memberInfo.totalAmount) || 0
      const nextThreshold = this.levelThresholds[this.memberInfo.level + 1] || 2888
      return Math.max(nextThreshold - currentAmount, 0).toFixed(2)
    },

    progressPercentage() {
      const currentAmount = parseFloat(this.memberInfo.totalAmount)
      if (this.memberInfo.level >= 4) return 100
      const currentThreshold = this.levelThresholds[this.memberInfo.level] || 0
      const nextThreshold = this.levelThresholds[this.memberInfo.level + 1] || 2888
      if (nextThreshold <= currentThreshold) return 100
      const pct = ((currentAmount - currentThreshold) / (nextThreshold - currentThreshold)) * 100
      return Math.min(Math.max(pct, 0), 100)
    }
  },

  created() {
    this.loadWithCache()
  },

  methods: {

    /* ========== 带缓存的数据加载 ========== */

    async loadWithCache() {
      this.initialLoading = true

      try {
        // 并行加载用户信息和会员信息
        const [userRes, memberRes] = await Promise.all([
          api.customer.getInfo().catch(() => null),
          api.member.getInfo().catch(() => null)
        ])

        if (userRes) {
          this.$store.commit('SET_USER_INFO', userRes)
        }
        if (memberRes) {
          this.memberInfo = {
            level: memberRes.memberLevel || 0,
            levelName: memberRes.levelName || '非会员',
            points: memberRes.points || 0,
            totalAmount: memberRes.totalAmount || '0.00',
            discount: memberRes.discount || 1.0,
            discountText: memberRes.discountText || '无折扣'
          }
        }

        // 写入本地缓存（下次打开可秒开）
        this.writeCache()

      } catch (error) {
        console.error('加载数据失败:', error)
        // 尝试从缓存恢复
        this.restoreCache()
      }

      this.initialLoading = false
    },

    writeCache() {
      try {
        localStorage.setItem(PROFILE_CACHE_KEY, JSON.stringify({
          userInfo: this.$store.state.userInfo,
          memberInfo: this.memberInfo,
          timestamp: Date.now()
        }))
      } catch (e) { /* ignore quota */ }
    },

    restoreCache() {
      try {
        const raw = localStorage.getItem(PROFILE_CACHE_KEY)
        if (!raw) return
        const cache = JSON.parse(raw)
        // 缓存有效期 10 分钟
        if (Date.now() - cache.timestamp > 10 * 60 * 1000) {
          localStorage.removeItem(PROFILE_CACHE_KEY)
          return
        }
        if (cache.userInfo) this.$store.commit('SET_USER_INFO', cache.userInfo)
        if (cache.memberInfo) this.memberInfo = cache.memberInfo
      } catch (e) { /* ignore */ }
    },

    /* ========== 字段取值（兼容 _genderText 虚拟字段） ========== */

    getValue(key) {
      if (key === '_genderText') return this.genderText
      return (this.userInfo && this.userInfo[key]) || ''
    },

    /* ========== 编辑 / 保存 ========== */

    startEdit() {
      const ui = this.userInfo || {}
      this.profileForm = {
        id: ui.id,
        realName: ui.realName || '',
        gender: ui.gender || null,
        idCard: ui.idCard || '',
        nation: ui.nation || '',
        nativePlace: ui.nativePlace || '',
        birthDate: ui.birthDate || '',
        age: ui.age || null,
        education: ui.education || '',
        phone: ui.phone || '',
        email: ui.email || '',
        address: ui.address || '',
        preferences: ui.preferences || ''
      }
      this.editing = true
    },

    cancelEdit() {
      this.editing = false
    },

    async saveProfile() {
      this.saving = true
      try {
        await api.customer.updateProfile(this.profileForm)
        this.$message.success('保存成功')
        this.editing = false
        await this.loadUserProfile()
        this.writeCache()
      } catch (e) {
        this.$message.error(e?.message || '保存失败')
      } finally {
        this.saving = false
      }
    },

    async loadUserProfile() {
      try {
        const res = await api.customer.getInfo()
        if (res) this.$store.commit('SET_USER_INFO', res)
      } catch (e) { /* silent */ }
    },

    /* ========== 工具方法 ========== */

    formatAmount(val) {
      return Number(val || 0).toFixed(2)
    },

    ruleColor(idx) {
      const colors = ['#909399', '#409eff', '#c0c4cc', '#e6a23c', '#b37feb']
      return colors[idx] || '#909399'
    },

    maskedPhone(phone) {
      if (!phone || phone.length < 7) return phone || ''
      return phone.slice(0, 3) + '****' + phone.slice(-4)
    },

    maskedEmail(email) {
      if (!email || !email.includes('@')) return email || ''
      const [local, domain] = email.split('@')
      if (local.length <= 2) return email
      return local[0] + '***@' + domain
    }
  }
}
</script>

<style lang="scss" scoped>
/* ==================== 变量 ==================== */
$primary: #c45a3b;
$primary-light: #e8735a;
$bg-page: #f8f6f3;
$border-color: #eeebe8;
$text-main: #333;
$text-muted: #999;
$radius-sm: 8px;
$radius-md: 12px;
$shadow-card: 0 2px 14px rgba(0, 0, 0, 0.04);

/* ==================== 页面容器 ==================== */
.member-page {
  min-height: calc(100vh - 60px);
  background: $bg-page;
}

/* ==================== 用户信息头部横幅 ==================== */
.profile-banner {
  background: linear-gradient(135deg, $primary 0%, $primary-light 50%, #d4876a 100%);
  padding: 36px 40px 24px;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -60%;
    right: -10%;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: rgba(255,255,255,0.06);
  }
  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -5%;
    width: 260px;
    height: 260px;
    border-radius: 50%;
    background: rgba(255,255,255,0.04);
  }
}

.banner-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 28px;
  position: relative;
  z-index: 2;
}

/* ---------- 核心信息 ---------- */
.user-core {
  flex: 1; min-width: 0;
  .user-nickname {
    font-size: 26px; font-weight: 800; margin: 0 0 8px;
    text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }
  .user-meta {
    margin: 0; font-size: 13px; opacity: 0.85;
    display: flex; gap: 16px;
    .meta-tag {
      display: inline-flex; align-items: center; gap: 4px;
      background: rgba(255,255,255,0.15); padding: 3px 10px; border-radius: 14px;
      backdrop-filter: blur(4px); font-weight: 500;
    }
  }
}

/* ---------- 会员卡片 ---------- */
.member-badge-card {
  flex-shrink: 0;
  min-width: 280px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: $radius-md;
  padding: 16px 20px;
  color: #fff;

  .badge-header {
    display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
    .badge-icon { font-size: 20px; color: #ffd700; }
    .badge-level-name { font-size: 17px; font-weight: 700; letter-spacing: 0.5px; }
  }
  .badge-body {
    display: flex; align-items: center; justify-content: space-between;
  }
  .badge-stat {
    display: flex; flex-direction: column; align-items: center; gap: 2px;
    .stat-num { font-size: 19px; font-weight: 800; line-height: 1.2; }
    .stat-label { font-size: 11px; opacity: 0.75; }
  }
  .badge-divider { width: 1px; height: 34px; background: rgba(255,255,255,0.25); }
}

/* 等级颜色变体 */
.member-badge-card.level-1 { background: rgba(64,158,255,0.22); border-color: rgba(64,158,255,0.35); }
.member-badge-card.level-2 { background: rgba(230,162,60,0.22); border-color: rgba(230,162,60,0.35); }
.member-badge-card.level-3 { background: rgba(245,108,108,0.22); border-color: rgba(245,108,108,0.35); }
.member-badge-card.level-4 { background: rgba(156,39,176,0.22); border-color: rgba(156,39,176,0.35); }

/* ---------- 进度条 ---------- */
.level-progress-bar {
  max-width: 1200px; margin: 18px auto 0; padding: 0 40px; position: relative; z-index: 2;
  .progress-text {
    display: flex; justify-content: space-between; font-size: 13px; color: rgba(255,255,255,0.9); margin-bottom: 8px;
    strong { color: #ffd700; }
  }
  .gap-amount {
    font-size: 18px; font-weight: 700; color: #fff;
    text-shadow: 0 1px 6px rgba(0,0,0,0.2);
  }
  .progress-track {
    height: 8px; background: rgba(255,255,255,0.2); border-radius: 4px; overflow: hidden;
    .progress-fill {
      height: 100%; border-radius: 4px;
      background: linear-gradient(90deg, #ffd700, #ffb347);
      transition: width 0.8s cubic-bezier(.22,1,.36,1);
      &.is-high { background: linear-gradient(90deg, #67c23a, #95de64); }
    }
  }
  &.maxed {
    display: inline-block; background: rgba(255,255,255,0.15); padding: 6px 20px;
    border-radius: 20px; font-size: 13px; font-weight: 600;
    i { margin-right: 4px; color: #ffd700; }
  }
}

/* ==================== 双列主体布局 ==================== */
.main-layout {
  max-width: 1200px; margin: 24px auto; display: grid;
  grid-template-columns: 1fr 420px; gap: 20px; padding: 0 20px;
  align-items: start;
}

/* ========== 面板卡片通用样式 ========== */
.panel-card {
  background: #fff; border-radius: $radius-md; box-shadow: $shadow-card;
  padding: 22px 24px; margin-bottom: 20px;
}
.card-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px;
  h3 {
    font-size: 16px; font-weight: 700; color: $text-main; margin: 0;
    i { margin-right: 6px; color: $primary; font-size: 17px; }
  }
}

/* ---------- 文字按钮 ---------- */
.text-action-btn {
  border: none; background: none; cursor: pointer;
  font-size: 13px; color: $primary; font-weight: 600; padding: 4px 0;
  display: inline-flex; align-items: center; gap: 4px;
  &:hover { color: darken($primary, 8%); }
  i { font-size: 14px; }
}
.edit-actions {
  display: flex; gap: 8px;
  .btn-save, .btn-cancel {
    padding: 5px 14px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none;
  }
  .btn-save { background: $primary; color: #fff; font-weight: 600; }
  .btn-cancel { background: #f0f0f0; color: #666; }
}

/* ==================== 信息网格展示 ==================== */
.info-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
}
.info-cell {
  display: flex; align-items: center; gap: 12px; padding: 10px 12px;
  border-radius: $radius-sm; background: #fafaf9; transition: background 0.2s;
  &:hover { background: #f5f4f2; }
  .cell-icon-wrap {
    width: 38px; height: 38px; border-radius: 10px;
    display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    i { font-size: 17px; color: $primary; }
  }
  .cell-content {
    min-width: 0; display: flex; flex-direction: column; gap: 2px;
    .cell-label { font-size: 11px; color: $text-muted; }
    .cell-value {
      font-size: 14px; color: $text-main; font-weight: 500;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
  }
}

/* ==================== 编辑表单 ==================== */
.edit-form-area {
  .form-grid {
    display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px;
    .full-width { grid-column: span 2; }
  }
  ::v-deep .el-form-item { margin-bottom: 14px; }
  ::v-deep .el-input__inner { border-radius: 6px; }
}

/* ==================== 快捷操作 ==================== */
.quick-actions {
  padding-bottom: 4px;
}
.action-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px;
}
.quick-entry {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 14px 8px; border-radius: $radius-sm; text-decoration: none;
  color: $text-main; transition: all 0.22s ease;
  &:hover {
    transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    background: #fafafa;
  }
  .entry-icon {
    width: 44px; height: 44px; border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    i { font-size: 21px; color: #fff; }
  }
  span { font-size: 12px; font-weight: 500; }
}
.bg-blue   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.bg-orange { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.bg-red    { background: linear-gradient(135deg, #f56c6c, #f78989); }
.bg-green  { background: linear-gradient(135deg, #67c23a, #85ce61); }

/* ==================== 右侧面板 ========== */

/* ---- 会员等级规则表 ---- */
.rules-table-wrap { overflow-x: auto; }
.rules-table {
  width: 100%; border-collapse: collapse; font-size: 13px;
  th, td {
    padding: 11px 14px; text-align: left; border-bottom: 1px solid $border-color;
    white-space: nowrap;
  }
  th { color: $text-muted; font-weight: 600; background: #fafaf9; }
  td { color: $text-main; }
  tr.is-current > td { background: #fff8f0; font-weight: 600; color: $primary; }
  tr:hover > td { background: #fcfbfa; }
  .rule-level-dot {
    display: inline-block; width: 10px; height: 10px; border-radius: 50%;
    vertical-align: middle; margin-right: 6px;
  }
}

/* ---- 安全提示 ---- */
.security-items { display: flex; flex-direction: column; gap: 14px; }
.security-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; background: #fafaf9; border-radius: $radius-sm;
  .sec-label {
    display: flex; align-items: center; gap: 6px; font-size: 13px; color: $text-muted; font-weight: 500;
    i { color: $primary; }
  }
  .sec-value { font-size: 13px; color: $text-main; font-family: monospace; }
  .sec-status {
    font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 10px;
    background: #fee; color: #f56c6c;
    &.bound { background: #eef9ee; color: #67c23a; }
  }
}

/* ==================== 骨架屏 ==================== */
.skeleton-overlay {
  position: fixed; inset: 0; z-index: 99;
  background: $bg-page; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 16px;
  pointer-events: none;
}
.skeleton-text {
  height: 20px; border-radius: 6px; background: #e8e4e0;
  animation: skeleton-pulse 1.2s infinite alternate;
}
.w-200 { width: 200px; }
.w-120 { width: 120px; }

@keyframes skeleton-pulse {
  from { opacity: 0.5; } to { opacity: 1; }
}

/* ==================== 响应式适配 ==================== */
@media screen and (max-width: 1024px) {
  .main-layout { grid-template-columns: 1fr 340px; }
  .member-badge-card { min-width: 220px; }
  .info-grid { grid-template-columns: 1fr; }
  .form-grid { grid-template-columns: 1fr !important; .full-width { grid-column: span 1; } }
}
@media screen and (max-width: 768px) {
  .profile-banner { padding: 24px 16px 16px; }
  .banner-inner { flex-direction: column; text-align: center; gap: 18px; }
  .user-nickname { font-size: 22px; }
  .user-meta { justify-content: center; }
  .member-badge-card { min-width: unset; width: 100%; box-sizing: border-box; }
  .badge-body { justify-content: space-evenly; }
  .main-layout {
    grid-template-columns: 1fr; padding: 0 12px;
  }
  .level-progress-bar { padding: 0 16px; }
  .action-grid { grid-template-columns: repeat(4, 1fr); }
  .rules-table { font-size: 12px; th, td { padding: 8px 10px; } }
}
@media screen and (max-width: 480px) {
  .action-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
