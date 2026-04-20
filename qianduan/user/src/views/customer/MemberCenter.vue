<template>
  <div class="member-center">
    <div class="member-header">
      <h2>个人中心</h2>
    </div>

    <el-card class="basic-info-card">
      <div slot="header">
        <span>基本信息</span>
        <el-button v-if="!editing" type="text" style="float: right;" @click="startEdit">编辑</el-button>
        <span v-else style="float: right;">
          <el-button type="primary" size="small" @click="saveProfile">保存</el-button>
          <el-button size="small" @click="cancelEdit">取消</el-button>
        </span>
      </div>
      <div v-if="!editing" class="info-display">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item"><span class="label">用户名：</span><span class="value">{{ userInfo.username || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">姓名：</span><span class="value">{{ userInfo.realName || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">性别：</span><span class="value">{{ genderText }}</span></div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item"><span class="label">身份证号：</span><span class="value">{{ userInfo.idCard || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">民族：</span><span class="value">{{ userInfo.nation || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">籍贯：</span><span class="value">{{ userInfo.nativePlace || '-' }}</span></div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item"><span class="label">出生日期：</span><span class="value">{{ userInfo.birthDate || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">年龄：</span><span class="value">{{ userInfo.age || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">学历：</span><span class="value">{{ userInfo.education || '-' }}</span></div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item"><span class="label">电话：</span><span class="value">{{ userInfo.phone || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">邮箱：</span><span class="value">{{ userInfo.email || '-' }}</span></div>
          </el-col>
          <el-col :span="8">
            <div class="info-item"><span class="label">住址：</span><span class="value">{{ userInfo.address || '-' }}</span></div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item"><span class="label">喜好：</span><span class="value">{{ userInfo.preferences || '-' }}</span></div>
          </el-col>
        </el-row>
      </div>
      <div v-else class="info-edit">
        <el-form ref="profileForm" :model="profileForm" label-width="100px" size="small">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="姓名">
                <el-input v-model="profileForm.realName" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="身份证号">
                <el-input v-model="profileForm.idCard" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="民族">
                <el-input v-model="profileForm.nation" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="籍贯">
                <el-input v-model="profileForm.nativePlace" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="出生日期">
                <el-date-picker v-model="profileForm.birthDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="年龄">
                <el-input-number v-model="profileForm.age" :min="0" :max="150" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="学历">
                <el-select v-model="profileForm.education" style="width: 100%;" clearable>
                  <el-option label="小学" value="小学" />
                  <el-option label="初中" value="初中" />
                  <el-option label="高中" value="高中" />
                  <el-option label="大专" value="大专" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="电话">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" />
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="住址">
                <el-input v-model="profileForm.address" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="喜好">
                <el-input v-model="profileForm.preferences" type="textarea" :rows="2" placeholder="请输入您的阅读喜好" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>

    <el-card class="member-info-card">
      <div slot="header">
        <span>我的会员信息</span>
      </div>
      <div class="member-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item"><span class="label">会员等级：</span><span class="value" :class="'level-' + memberInfo.level">{{ memberInfo.levelName }}</span></div>
          </el-col>
          <el-col :span="6">
            <div class="info-item"><span class="label">当前积分：</span><span class="value points">{{ memberInfo.points }}</span></div>
          </el-col>
          <el-col :span="6">
            <div class="info-item"><span class="label">累计消费：</span><span class="value amount">¥{{ memberInfo.totalAmount }}</span></div>
          </el-col>
          <el-col :span="6">
            <div class="info-item"><span class="label">当前折扣：</span><span class="value discount">{{ memberInfo.discountText }}</span></div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="level-rules-card">
      <div slot="header">
        <span>会员等级规则</span>
      </div>
      <el-table :data="levelRules" style="width: 100%" :show-header="true" size="small">
        <el-table-column prop="levelName" label="会员等级" width="150" />
        <el-table-column prop="condition" label="升级条件" width="200" />
        <el-table-column prop="discount" label="购书折扣" width="150" />
        <el-table-column prop="points" label="积分规则" />
      </el-table>
    </el-card>

    <el-card class="next-level-card">
      <div slot="header">
        <span>升级进度</span>
      </div>
      <div class="progress-info">
        <p v-if="memberInfo.level < 4">
          距离下一等级 <strong>{{ nextLevelName }}</strong> 还需消费：
          <span class="highlight">¥{{ nextLevelAmount }}</span>
        </p>
        <p v-else>
          恭喜！您已是最高等级会员！
        </p>
        <el-progress :percentage="progressPercentage" :color="progressColor" :stroke-width="20" />
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'MemberCenter',
  data() {
    return {
      editing: false,
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
        { levelName: '非会员', condition: '累计消费 < 188元', discount: '原价', points: '1元=1积分' },
        { levelName: '普通会员', condition: '累计消费 ≥ 188元', discount: '95折', points: '1元=1积分' },
        { levelName: '银卡会员', condition: '累计消费 ≥ 388元', discount: '9折', points: '1元=1积分' },
        { levelName: '金卡会员', condition: '累计消费 ≥ 1888元', discount: '85折', points: '1元=1积分' },
        { levelName: '钻石卡会员', condition: '累计消费 ≥ 2888元', discount: '8折', points: '1元=1积分' }
      ],
      levelThresholds: [0, 188, 388, 1888, 2888]
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    userId() {
      // 兼容处理：如果 userInfo 是旧的 Result 包装格式（有 code 字段），从 data 中取 id
      const uid = this.userInfo?.id || this.userInfo?.data?.id
      return uid || 1
    },
    genderText() {
      if (this.userInfo.gender === 1) return '男'
      if (this.userInfo.gender === 2) return '女'
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
      const progress = ((currentAmount - currentThreshold) / (nextThreshold - currentThreshold)) * 100
      return Math.min(Math.max(progress, 0), 100)
    },
    progressColor() {
      if (this.progressPercentage < 30) return '#f56c6c'
      if (this.progressPercentage < 60) return '#e6a23c'
      if (this.progressPercentage < 90) return '#409eff'
      return '#67c23a'
    }
  },
  created() {
    this.loadMemberInfo()
    this.loadUserProfile()
  },
  methods: {
    async loadUserProfile() {
      try {
        const res = await api.user.getById(this.userId)
        if (res) {
          this.$store.commit('SET_USER_INFO', res)
        }
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    },
    async loadMemberInfo() {
      try {
        const res = await api.member.getInfo(this.userId)
        if (res) {
          this.memberInfo = {
            level: res.memberLevel || 0,
            levelName: res.levelName || '非会员',
            points: res.points || 0,
            totalAmount: res.totalAmount || '0.00',
            discount: res.discount || 1.0,
            discountText: res.discountText || '无折扣'
          }
        }
      } catch (error) {
        console.error('加载会员信息失败:', error)
      }
    },
    startEdit() {
      this.profileForm = {
        id: this.userId,
        realName: this.userInfo.realName || '',
        gender: this.userInfo.gender || null,
        idCard: this.userInfo.idCard || '',
        nation: this.userInfo.nation || '',
        nativePlace: this.userInfo.nativePlace || '',
        birthDate: this.userInfo.birthDate || '',
        age: this.userInfo.age || null,
        education: this.userInfo.education || '',
        phone: this.userInfo.phone || '',
        email: this.userInfo.email || '',
        address: this.userInfo.address || '',
        preferences: this.userInfo.preferences || ''
      }
      this.editing = true
    },
    cancelEdit() {
      this.editing = false
    },
    async saveProfile() {
      try {
        await api.user.update(this.profileForm)
        this.$message.success('保存成功')
        this.editing = false
        this.loadUserProfile()
      } catch (e) {
        this.$message.error('保存失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.member-center {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;

  .member-header {
    margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }

  .basic-info-card {
    margin-bottom: 20px;

    .info-display {
      .info-item {
        margin-bottom: 15px;
        font-size: 14px;

        .label { color: #909399; }
        .value { color: #303133; }
      }
    }
  }

  .member-info-card {
    margin-bottom: 20px;

    .member-info {
      .info-item {
        margin-bottom: 15px;
        font-size: 16px;

        .label { color: #606266; font-weight: bold; }
        .value {
          color: #303133;
          &.level-1 { color: #409eff; font-weight: bold; }
          &.level-2 { color: #e6a23c; font-weight: bold; }
          &.level-3 { color: #f56c6c; font-weight: bold; }
          &.level-4 { color: #9c27b0; font-weight: bold; }
          &.points { color: #e6a23c; font-size: 20px; font-weight: bold; }
          &.amount { color: #f56c6c; font-size: 20px; font-weight: bold; }
          &.discount { color: #67c23a; font-size: 20px; font-weight: bold; }
        }
      }
    }
  }

  .level-rules-card { margin-bottom: 20px; }

  .next-level-card {
    .progress-info {
      p {
        font-size: 16px;
        margin-bottom: 15px;
        color: #606266;
        strong { color: #409eff; }
        .highlight { color: #f56c6c; font-size: 20px; font-weight: bold; }
      }
    }
  }
}
</style>
