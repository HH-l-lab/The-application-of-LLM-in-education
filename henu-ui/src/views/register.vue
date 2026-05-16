<template>
  <div class="register-page" @mousemove="onMouseMove">
    <!-- 背景光晕 -->
    <div class="bg-glow glow-blue"></div>
    <div class="bg-glow glow-purple"></div>
    <!-- 粒子画布 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <!-- 核心卡片 -->
    <div class="glass-card">
      <!-- Logo 区域 -->
      <div class="logo-area">
        <div class="logo-circle"><img :src="logoUrl" class="logo-img" /></div>
        <h1 class="brand-title">{{ title }}</h1>
        <p class="brand-sub">创建您的学习账号</p>
      </div>

      <!-- 注册表单 -->
      <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" prefix-icon="el-icon-lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请确认密码" prefix-icon="el-icon-lock" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item prop="deptId">
              <el-select v-model="registerForm.deptId" placeholder="选择学校" style="width:100%" filterable>
                <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="postId">
              <el-select v-model="registerForm.postId" placeholder="选择年级" style="width:100%">
                <el-option v-for="p in postOptions" :key="p.postId" :label="p.postName" :value="p.postId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="roleId">
          <el-select v-model="registerForm.roleId" placeholder="选择身份" style="width:100%">
            <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-row :gutter="12">
            <el-col :span="16">
              <el-input v-model="registerForm.code" placeholder="请输入验证码" prefix-icon="el-icon-key" @keyup.enter.native="handleRegister" />
            </el-col>
            <el-col :span="8">
              <img :src="codeUrl" @click="getCode" class="captcha-img" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-button :loading="loading" class="submit-btn" @click="handleRegister">
          <span v-if="!loading">注 册 账 号</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div class="switch-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>

    <!-- 底部 -->
    <div class="auth-footer">{{ footerContent }}</div>
  </div>
</template>

<script>
import { getCodeImg, register } from "@/api/login"
import axios from 'axios'
import defaultSettings from '@/settings'

const sciIcons = ['🧪','🔬','🧬','🧲','🔋','⚡','⚛️','🔭','⚗️']

export default {
  name: "Register",
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.registerForm.password !== value) { callback(new Error("两次输入的密码不一致")) } else { callback() }
    }
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      logoUrl: require('@/assets/logo/logo.png'),
      codeUrl: "",
      loading: false,
      captchaEnabled: true,
      particles: [],
      lastSpawn: { x: -100, y: -100, time: 0 },
      animId: null,
      registerForm: { username: "", password: "", confirmPassword: "", code: "", uuid: "", deptId: null, postId: null, roleId: null },
      registerRules: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" }, { min: 2, max: 20, message: '长度2到20之间', trigger: 'blur' }],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" }, { min: 5, max: 20, message: "长度5到20之间", trigger: "blur" }],
        confirmPassword: [{ required: true, trigger: "blur", message: "请确认密码" }, { validator: equalToPassword, trigger: "blur" }],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      deptOptions: [],
      postOptions: [],
      roleOptions: []
    }
  },
  created() { this.getCode(); this.loadRegisterOptions() },
  mounted() { this.initCanvas(); this.animate() },
  beforeDestroy() { cancelAnimationFrame(this.animId) },
  methods: {
    // ========== 粒子引擎（同登录页） ==========
    initCanvas() {
      const c = this.$refs.particleCanvas; if (!c) return
      c.width = window.innerWidth; c.height = window.innerHeight
      window.addEventListener('resize', () => { c.width = window.innerWidth; c.height = window.innerHeight })
    },
    onMouseMove(e) {
      const now = Date.now()
      const dx = e.clientX - this.lastSpawn.x, dy = e.clientY - this.lastSpawn.y
      if (Math.sqrt(dx*dx+dy*dy) > 30 && now - this.lastSpawn.time > 40) {
        this.particles.push({ x: e.clientX, y: e.clientY, icon: sciIcons[Math.floor(Math.random()*sciIcons.length)],
          vx: (Math.random()-0.5)*2.5, vy: (Math.random()-1)*2.5-0.5, size: Math.random()*15+15,
          life: 1, decay: Math.random()*0.02+0.015, rot: Math.random()*Math.PI*2, rotSpd: (Math.random()-0.5)*0.1 })
        this.lastSpawn = { x: e.clientX, y: e.clientY, time: now }
        if (this.particles.length > 35) this.particles.shift()
      }
    },
    animate() {
      const c = this.$refs.particleCanvas; if (!c) return
      const ctx = c.getContext('2d'); ctx.clearRect(0, 0, c.width, c.height)
      for (let i = this.particles.length-1; i >= 0; i--) {
        const p = this.particles[i]
        p.x += p.vx; p.y += p.vy; p.life -= p.decay; p.rot += p.rotSpd; p.vx *= 0.99; p.vy -= 0.02
        ctx.save(); ctx.translate(p.x, p.y); ctx.rotate(p.rot); ctx.globalAlpha = Math.max(0, p.life)
        ctx.font = `${p.size}px Arial`; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
        ctx.fillText(p.icon, 0, 0); ctx.restore()
        if (p.life <= 0) this.particles.splice(i, 1)
      }
      this.animId = requestAnimationFrame(this.animate)
    },
    // ========== 加载注册选项 ==========
    loadRegisterOptions() {
      // 直接使用 axios 绕过 request.js 拦截器，避免未登录状态下弹出"登录过期"
      const baseUrl = process.env.VUE_APP_BASE_API
      axios.get(baseUrl + '/register/options').then(res => {
        if (res.data && res.data.code === 200) {
          this.deptOptions = res.data.depts || []
          this.postOptions = res.data.posts || []
          this.roleOptions = res.data.roles || []
        }
      }).catch(() => {
        // 静默失败，不弹窗
        console.warn('注册选项加载失败，请确认后端已重启')
      })
    },
    // ========== 验证码 ==========
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) { this.codeUrl = "data:image/gif;base64," + res.img; this.registerForm.uuid = res.uuid }
      })
    },
    // ========== 注册 ==========
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          register(this.registerForm).then(res => {
            if (res.msg && res.msg.includes('审核')) {
              this.$alert('注册成功，请等待审核，目前账号处于停用状态！', '注册提示', { type: 'warning', confirmButtonText: '我知道了' }).then(() => {
                this.$router.push("/login")
              }).catch(() => {})
            } else {
              const username = this.registerForm.username
              this.$alert("恭喜你，账号 " + username + " 注册成功！", '注册成功', { type: 'success' }).then(() => {
                this.$router.push("/login")
              }).catch(() => {})
            }
          }).catch(() => { this.loading = false; if (this.captchaEnabled) this.getCode() })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  display: flex; justify-content: center; align-items: center; min-height: 100vh;
  background: #0f172a; position: relative; overflow: hidden;
}
.bg-glow { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.5; animation: floatGlow 10s infinite alternate ease-in-out; }
.glow-blue { width: 400px; height: 400px; background: #3b82f6; top: -100px; left: -100px; }
.glow-purple { width: 500px; height: 500px; background: #8b5cf6; bottom: -150px; right: -100px; animation-delay: -5s; }
@keyframes floatGlow { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(50px, 50px) scale(1.1); } }
.particle-canvas { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 1; pointer-events: none; }
.glass-card {
  width: 460px; padding: 36px 36px 28px; border-radius: 24px; z-index: 10; position: relative;
  background: rgba(30, 41, 59, 0.4); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  transition: transform 0.4s, box-shadow 0.4s;
  &:hover { transform: translateY(-4px); box-shadow: 0 30px 60px -12px rgba(0,0,0,0.7); }
}
.logo-area { text-align: center; margin-bottom: 24px; }
.logo-circle {
  display: inline-flex; align-items: center; justify-content: center; width: 56px; height: 56px;
  border-radius: 50%; background: rgba(15, 23, 42, 0.5); border: 1px solid rgba(255,255,255,0.1); margin-bottom: 10px;
}
.logo-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.brand-title { color: #f8fafc; font-size: 22px; font-weight: 800; letter-spacing: 3px; margin: 0; }
.brand-sub { color: #94a3b8; font-size: 13px; margin-top: 6px; }
// 表单
.auth-form {
  ::v-deep .el-input__inner {
    height: 44px; background: rgba(15, 23, 42, 0.6) !important; border: 1px solid rgba(255,255,255,0.1) !important;
    color: #f8fafc !important; border-radius: 10px; font-size: 14px; padding-left: 36px;
    &:focus { border-color: #60a5fa !important; box-shadow: 0 0 0 3px rgba(59,130,246,0.15) !important; background: rgba(15,23,42,0.8) !important; }
    &::placeholder { color: #64748b; }
  }
  ::v-deep .el-input__prefix { color: #60a5fa; left: 10px; }
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-form-item__error { color: #f87171; }
  // 下拉框样式
  ::v-deep .el-select .el-input__inner { padding-left: 14px; height: 44px; }
  ::v-deep .el-select .el-input__prefix { display: none; }
}
.captcha-img { height: 44px; width: 100%; border-radius: 10px; cursor: pointer; display: block; opacity: 0.85; transition: all 0.3s; &:hover { opacity: 1; transform: scale(1.03); } }
.submit-btn {
  width: 100%; height: 48px; border-radius: 12px; font-size: 17px; font-weight: 800; letter-spacing: 3px; border: none;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%); background-size: 200% auto; color: #fff;
  transition: all 0.4s;
  &:hover { background-position: right center; box-shadow: 0 10px 20px -5px rgba(139, 92, 246, 0.5); transform: translateY(-2px); }
}
.switch-link { text-align: center; margin-top: 16px; color: #94a3b8; font-size: 14px;
  a { color: #a5b4fc; font-weight: 600; text-decoration: none; &:hover { color: #e0e7ff; } }
}
.auth-footer { position: fixed; bottom: 0; width: 100%; text-align: center; color: rgba(255,255,255,0.2); font-size: 12px; padding: 14px 0; z-index: 10; }
</style>
