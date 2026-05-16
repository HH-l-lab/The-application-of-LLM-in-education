<template>
  <div class="login-page" @mousemove="onMouseMove">
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
        <p class="brand-sub">探索科学的奥秘，从这里开始</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" prefix-icon="el-icon-lock" @keyup.enter.native="handleLogin" />
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-row :gutter="12">
            <el-col :span="16">
              <el-input v-model="loginForm.code" placeholder="请输入验证码" prefix-icon="el-icon-key" @keyup.enter.native="handleLogin" />
            </el-col>
            <el-col :span="8">
              <img :src="codeUrl" @click="getCode" class="captcha-img" />
            </el-col>
          </el-row>
        </el-form-item>
        <div class="option-row">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </div>
        <el-button :loading="loading" class="submit-btn" @click="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div class="switch-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>

    <!-- 底部 -->
    <div class="auth-footer">{{ footerContent }}</div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

const sciIcons = ['🧪','🔬','🧬','🧲','🔋','⚡','⚛️','🔭','⚗️']

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      logoUrl: require('@/assets/logo/logo.png'),
      codeUrl: "",
      loading: false,
      captchaEnabled: true,
      redirect: undefined,
      particles: [],
      lastSpawn: { x: -100, y: -100, time: 0 },
      animId: null,
      loginForm: { username: "", password: "", rememberMe: false, code: "", uuid: "" },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      }
    }
  },
  watch: {
    $route: { handler(route) { this.redirect = route.query && route.query.redirect }, immediate: true }
  },
  created() { this.getCode(); this.getCookie() },
  mounted() { this.initCanvas(); this.animate() },
  beforeDestroy() { cancelAnimationFrame(this.animId) },
  methods: {
    // ========== 粒子引擎 ==========
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
    // ========== 验证码 ==========
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) { this.codeUrl = "data:image/gif;base64," + res.img; this.loginForm.uuid = res.uuid }
      })
    },
    getCookie() {
      const username = Cookies.get("username"), password = Cookies.get("password"), rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? "" : username,
        password: password === undefined ? "" : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        code: "", uuid: this.loginForm.uuid
      }
    },
    // ========== 登录 ==========
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else { Cookies.remove("username"); Cookies.remove("password"); Cookies.remove('rememberMe') }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => { this.loading = false; if (this.captchaEnabled) this.getCode() })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex; justify-content: center; align-items: center; min-height: 100vh;
  background: #0f172a; position: relative; overflow: hidden;
}
.bg-glow { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.5; animation: floatGlow 10s infinite alternate ease-in-out; }
.glow-blue { width: 400px; height: 400px; background: #3b82f6; top: -100px; left: -100px; }
.glow-purple { width: 500px; height: 500px; background: #8b5cf6; bottom: -150px; right: -100px; animation-delay: -5s; }
@keyframes floatGlow { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(50px, 50px) scale(1.1); } }
.particle-canvas { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 1; pointer-events: none; }
.glass-card {
  width: 420px; padding: 40px 36px 32px; border-radius: 24px; z-index: 10; position: relative;
  background: rgba(30, 41, 59, 0.4); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  transition: transform 0.4s, box-shadow 0.4s;
  &:hover { transform: translateY(-4px); box-shadow: 0 30px 60px -12px rgba(0,0,0,0.7); }
}
.logo-area { text-align: center; margin-bottom: 28px; }
.logo-circle {
  display: inline-flex; align-items: center; justify-content: center; width: 60px; height: 60px;
  border-radius: 50%; background: rgba(15, 23, 42, 0.5); border: 1px solid rgba(255,255,255,0.1); margin-bottom: 12px;
}
.logo-img { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; }
.brand-title { color: #f8fafc; font-size: 24px; font-weight: 800; letter-spacing: 3px; margin: 0; }
.brand-sub { color: #94a3b8; font-size: 13px; margin-top: 6px; }
// 表单
.auth-form {
  ::v-deep .el-input__inner {
    height: 48px; background: rgba(15, 23, 42, 0.6) !important; border: 1px solid rgba(255,255,255,0.1) !important;
    color: #f8fafc !important; border-radius: 10px; font-size: 15px; padding-left: 36px;
    &:focus { border-color: #60a5fa !important; box-shadow: 0 0 0 3px rgba(59,130,246,0.15) !important; background: rgba(15,23,42,0.8) !important; }
    &::placeholder { color: #64748b; }
  }
  ::v-deep .el-input__prefix { color: #60a5fa; left: 10px; }
  ::v-deep .el-form-item { margin-bottom: 20px; }
  ::v-deep .el-form-item__error { color: #f87171; }
}
.captcha-img { height: 48px; width: 100%; border-radius: 10px; cursor: pointer; display: block; opacity: 0.85; transition: all 0.3s; &:hover { opacity: 1; transform: scale(1.03); } }
.option-row {
  margin-bottom: 16px;
  ::v-deep .el-checkbox__label { color: #cbd5e1; font-size: 13px; }
  ::v-deep .el-checkbox__inner { background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.2); }
}
.submit-btn {
  width: 100%; height: 50px; border-radius: 12px; font-size: 17px; font-weight: 800; letter-spacing: 3px; border: none;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%); background-size: 200% auto; color: #fff;
  transition: all 0.4s;
  &:hover { background-position: right center; box-shadow: 0 10px 20px -5px rgba(139, 92, 246, 0.5); transform: translateY(-2px); }
}
.switch-link { text-align: center; margin-top: 18px; color: #94a3b8; font-size: 14px;
  a { color: #a5b4fc; font-weight: 600; text-decoration: none; &:hover { color: #e0e7ff; } }
}
.auth-footer { position: fixed; bottom: 0; width: 100%; text-align: center; color: rgba(255,255,255,0.2); font-size: 12px; padding: 14px 0; z-index: 10; }
</style>
