<template>
  <div class="role-manage">
    <el-card>
      <div slot="header">
        <span>角色管理</span>
        <el-button type="primary" size="small" style="float: right;" icon="el-icon-plus" @click="openDialog(null)">新增角色</el-button>
      </div>
      <el-table :data="roles" border stripe size="small">
        <el-table-column prop="roleId" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="warning" size="mini" @click="openPermDialog(scope.row)">分配权限</el-button>
            <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="editRole ? '编辑角色' : '新增角色'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="roleForm" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" :disabled="!!editRole" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRole">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="分配权限" :visible.sync="permDialogVisible" width="500px">
      <el-tree
        ref="menuTree"
        :data="menuTree"
        show-checkbox
        node-key="menuId"
        :default-checked-keys="checkedMenus"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <span slot="footer">
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'RoleManage',
  data() {
    return {
      roles: [],
      dialogVisible: false,
      permDialogVisible: false,
      editRole: null,
      roleForm: { roleName: '', roleCode: '', description: '', status: 1 },
      roleRules: {
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
      },
      menuTree: [],
      checkedMenus: [],
      currentRoleId: null
    }
  },
  created() {
    this.loadRoles()
    this.loadMenus()
  },
  methods: {
    async loadRoles() {
      try {
        this.roles = await api.role.list()
      } catch (e) {
        this.roles = [
          { roleId: 1, roleName: '超级管理员', roleCode: 'SUPER_ADMIN', description: '拥有所有权限', status: 1 },
          { roleId: 2, roleName: '店员', roleCode: 'STAFF', description: '管理员后台操作权限', status: 1 },
          { roleId: 3, roleName: '用户', roleCode: 'USER', description: '普通用户/会员', status: 1 }
        ]
      }
    },
    async loadMenus() {
      try {
        this.menuTree = await api.menu.tree()
      } catch (e) {
        this.menuTree = [
          { menuId: 1, menuName: '首页', children: [] },
          { menuId: 2, menuName: '图书管理', children: [
            { menuId: 21, menuName: '图书列表', children: [] },
            { menuId: 22, menuName: '添加图书', children: [] }
          ]},
          { menuId: 3, menuName: '库存管理', children: [] },
          { menuId: 4, menuName: '订单管理', children: [] },
          { menuId: 5, menuName: '用户管理', children: [] },
          { menuId: 6, menuName: '评价管理', children: [] },
          { menuId: 7, menuName: '捐赠管理', children: [] },
          { menuId: 8, menuName: '角色管理', children: [] }
        ]
      }
    },
    openDialog(role) {
      this.editRole = role
      if (role) {
        this.roleForm = { ...role }
      } else {
        this.roleForm = { roleName: '', roleCode: '', description: '', status: 1 }
      }
      this.dialogVisible = true
    },
    async submitRole() {
      this.$refs.roleForm.validate(async (valid) => {
        if (!valid) return
        try {
          if (this.editRole) {
            await api.role.update(this.roleForm)
          } else {
            await api.role.save(this.roleForm)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadRoles()
        } catch (e) {
          this.$message.error('保存失败')
        }
      })
    },
    async handleDelete(role) {
      if (role.roleCode === 'SUPER_ADMIN') {
        this.$message.warning('不能删除超级管理员角色')
        return
      }
      try {
        await this.$confirm('确定删除该角色？', '提示', { type: 'warning' })
        await api.role.delete(role.roleId)
        this.$message.success('删除成功')
        this.loadRoles()
      } catch (e) {}
    },
    async openPermDialog(role) {
      this.currentRoleId = role.roleId
      try {
        const menus = await api.role.getMenus(role.roleId)
        this.checkedMenus = menus || []
      } catch (e) {
        this.checkedMenus = []
      }
      this.permDialogVisible = true
    },
    async savePermissions() {
      const menuIds = this.$refs.menuTree.getCheckedKeys()
      try {
        await api.role.assignMenus(this.currentRoleId, menuIds)
        this.$message.success('权限分配成功')
        this.permDialogVisible = false
      } catch (e) {
        this.$message.error('权限分配失败')
      }
    }
  }
}
</script>
