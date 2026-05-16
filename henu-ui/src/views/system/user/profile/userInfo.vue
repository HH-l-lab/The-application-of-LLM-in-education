<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px">
    <el-form-item label="用户昵称" prop="nickName">
      <el-input v-model="form.nickName" maxlength="30" />
    </el-form-item>
    <el-form-item label="手机号码" prop="phonenumber">
      <el-input v-model="form.phonenumber" maxlength="11" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email" maxlength="50" />
    </el-form-item>
    <el-form-item label="性别">
      <el-radio-group v-model="form.sex">
        <el-radio label="0">男</el-radio>
        <el-radio label="1">女</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="所属学校">
      <el-select v-model="form.deptId" placeholder="选择学校" filterable style="width: 100%">
        <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
      </el-select>
    </el-form-item>
    <el-form-item label="年级">
      <el-select v-model="form.postId" placeholder="选择年级" style="width: 100%">
        <el-option v-for="p in postOptions" :key="p.postId" :label="p.postName" :value="p.postId" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="mini" @click="submit">保存</el-button>
      <el-button type="danger" size="mini" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserProfile } from "@/api/system/user"
import axios from 'axios'

export default {
  props: {
    user: {
      type: Object
    }
  },
  data() {
    return {
      form: {},
      deptOptions: [],
      postOptions: [],
      // 表单校验
      rules: {
        nickName: [
          { required: true, message: "用户昵称不能为空", trigger: "blur" }
        ],
        email: [
          { required: true, message: "邮箱地址不能为空", trigger: "blur" },
          { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }
        ],
        phonenumber: [
          { required: true, message: "手机号码不能为空", trigger: "blur" },
          { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
        ]
      }
    }
  },
  watch: {
    user: {
      handler(user) {
        if (user) {
          this.form = {
            nickName: user.nickName,
            phonenumber: user.phonenumber,
            email: user.email,
            sex: user.sex,
            deptId: user.deptId || null,
            postId: null
          }
          // 加载用户当前的 postId（岗位/年级）
          if (user.postIds && user.postIds.length > 0) {
            this.form.postId = user.postIds[0]
          }
        }
      },
      immediate: true
    }
  },
  created() {
    this.loadOptions()
  },
  methods: {
    loadOptions() {
      // 使用 raw axios 绕过登录拦截器（和注册页一致的匿名接口）
      const baseUrl = process.env.VUE_APP_BASE_API
      axios.get(baseUrl + '/register/options').then(res => {
        if (res.data && res.data.code === 200) {
          this.deptOptions = res.data.depts || []
          this.postOptions = res.data.posts || []
        }
      }).catch(() => {})
    },
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 构造提交数据，包含 deptId 和 postIds
          const submitData = {
            nickName: this.form.nickName,
            phonenumber: this.form.phonenumber,
            email: this.form.email,
            sex: this.form.sex,
            deptId: this.form.deptId
          }
          if (this.form.postId) {
            submitData.postIds = [this.form.postId]
          }
          updateUserProfile(submitData).then(() => {
            this.$modal.msgSuccess("修改成功")
            this.user.phonenumber = this.form.phonenumber
            this.user.email = this.form.email
            this.user.deptId = this.form.deptId
          })
        }
      })
    },
    close() {
      this.$tab.closePage()
    }
  }
}
</script>
