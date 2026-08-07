<template>
  <div class="dashboard-container">
    <!-- 顶部 Banner -->
    <div class="welcome-banner">
      <div class="banner-particles">
        <span v-for="i in 6" :key="i" :class="'particle p' + i"></span>
      </div>
      <div class="welcome-text">
        <div class="greeting">Hi，{{ nickName }} 👋</div>
        <h1 class="main-title">欢迎开启智能伴学之旅</h1>
        <p class="sub-title">Henu 智能伴学系统 — 基于大语言模型的沉浸式实验学习平台</p>
        <div class="actions">
          <el-button class="gradient-btn" size="medium" @click="goToRoom">
            <i class="el-icon-monitor"></i> 进入自习室
          </el-button>
          <el-button class="outline-btn" size="medium" @click="goToCreator">
            <i class="el-icon-upload2"></i> 创作者中心
          </el-button>
        </div>
      </div>
      <div class="welcome-image">
        <img :src="avatar || require('@/assets/images/profile.jpg')" class="avatar-hero" alt="Hero">
      </div>
    </div>

    <!-- 数据看板 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card" :class="'card-' + (index+1)">
          <div class="stat-icon-wrap">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ stat.title }}</div>
            <div class="stat-number">
              <count-to :start-val="0" :end-val="stat.value" :duration="2600" />
            </div>
          </div>
          <div class="stat-wave"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 下半区 -->
    <el-row :gutter="20" class="content-row">
      <!-- 核心能力 -->
      <el-col :span="16">
        <div class="section-card">
          <div class="section-header">
            <span class="section-title"><i class="el-icon-s-opportunity"></i> 核心能力矩阵</span>
          </div>
          <div class="feature-grid">
            <div class="feature-item" v-for="(feat, idx) in features" :key="idx">
              <div class="feat-icon" :style="{ background: feat.color }">
                <i :class="feat.icon"></i>
              </div>
              <div class="feat-body">
                <h4>{{ feat.title }}</h4>
                <p>{{ feat.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 系统公告 -->
      <el-col :span="8">
        <div class="section-card notice-card">
          <div class="section-header">
            <span class="section-title"><i class="el-icon-message-solid"></i> 系统公告</span>
            <el-tag size="mini" effect="dark" type="danger" v-if="notices.length > 0">{{ notices.length }}条</el-tag>
          </div>
          <div class="notice-list" v-if="notices.length > 0">
            <div class="notice-item" v-for="(n, idx) in notices" :key="idx">
              <div class="notice-dot" :class="n.noticeType === '1' ? 'dot-notify' : 'dot-announce'"></div>
              <div class="notice-body">
                <div class="notice-title">{{ n.noticeTitle }}</div>
                <div class="notice-meta">
                  <el-tag :type="n.noticeType === '1' ? '' : 'success'" size="mini" effect="plain">{{ n.noticeType === '1' ? '通知' : '公告' }}</el-tag>
                  <span class="notice-time">{{ n.createTime }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="empty-notice" v-else>
            <i class="el-icon-bell"></i>
            <p>暂无公告</p>
            <span>管理员可在「系统管理 → 通知公告」中发布</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import { getDashboardStats, getDashboardNotices } from '@/api/dashboard'
import { mapGetters } from 'vuex'

export default {
  name: "Index",
  components: { CountTo },
  computed: {
    ...mapGetters(['nickName', 'avatar'])
  },
  data() {
    return {
      stats: [
        { title: '在线课程总数', value: 0, icon: 'el-icon-reading' },
        { title: '注册用户数', value: 0, icon: 'el-icon-user' },
        { title: '实验记录总数', value: 0, icon: 'el-icon-document-checked' },
        { title: '交易订单数', value: 0, icon: 'el-icon-s-order' },
      ],
      features: [
        { title: '视频 AI 知识提取', desc: '大模型自动分析教学视频内容，生成结构化知识摘要与关键帧标注', icon: 'el-icon-video-camera-solid', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { title: '大模型深度解析', desc: '融合讯飞星火大模型，提供毫秒级智能纠错与原理讲解', icon: 'el-icon-magic-stick', color: 'linear-gradient(135deg, #13f1fc 0%, #0470dc 100%)' },
        { title: '结构化实验报告', desc: '动态生成表单结构，一键导出精密的数据观测报表', icon: 'el-icon-pie-chart', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
        { title: '创作交易枢纽', desc: '创作者上传实验视频，智能审核上架、定价、分润结算', icon: 'el-icon-goods', color: 'linear-gradient(135deg, #ff0844 0%, #ffb199 100%)' }
      ],
      notices: []
    };
  },
  created() {
    getDashboardStats().then(res => {
      const d = res.data;
      this.stats[0].value = d.courseCount || 0;
      this.stats[1].value = d.userCount || 0;
      this.stats[2].value = d.recordCount || 0;
      this.stats[3].value = d.orderCount || 0;
    });
    getDashboardNotices().then(res => {
      this.notices = res.data || [];
    });
  },
  methods: {
    safeGo(path) {
      const resolved = this.$router.resolve(path)
      if (resolved.resolved.matched.length > 0) {
        this.$router.push(path)
      } else {
        this.$message.warning('该功能菜单尚未分配给您的角色，请联系管理员开通权限')
      }
    },
    goToRoom() { this.$router.push('/learningroom') },
    goToCreator() { this.$router.push('/creator-course') }
  }
};
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 28px 30px;
  background: #f0f2f8;
  min-height: calc(100vh - 84px);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* ========== Banner ========== */
.welcome-banner {
  display: flex; justify-content: space-between; align-items: center; position: relative; overflow: hidden;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  border-radius: 24px; padding: 50px 60px; margin-bottom: 24px;
  box-shadow: 0 20px 60px rgba(15,12,41,0.4);
}
.banner-particles {
  position: absolute; inset: 0; pointer-events: none;
  .particle {
    position: absolute; border-radius: 50%; opacity: 0.15; animation: float 6s infinite ease-in-out;
    &.p1 { width: 300px; height: 300px; background: #667eea; top: -80px; right: -50px; }
    &.p2 { width: 200px; height: 200px; background: #764ba2; bottom: -60px; left: 10%; animation-delay: 1s; }
    &.p3 { width: 120px; height: 120px; background: #00f2fe; top: 30%; left: 40%; animation-delay: 2s; }
    &.p4 { width: 80px; height: 80px; background: #43e97b; bottom: 20%; right: 30%; animation-delay: 3s; }
    &.p5 { width: 60px; height: 60px; background: #ff0844; top: 10%; left: 15%; animation-delay: 4s; }
    &.p6 { width: 160px; height: 160px; background: #4facfe; bottom: -40px; right: 20%; animation-delay: 2.5s; }
  }
}
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.welcome-text {
  z-index: 1; max-width: 600px;
  .greeting { font-size: 16px; color: rgba(255,255,255,0.6); font-weight: 500; margin-bottom: 8px; letter-spacing: 1px; }
  .main-title { font-size: 38px; font-weight: 800; color: #fff; margin: 0 0 12px; line-height: 1.2; letter-spacing: -0.5px; }
  .sub-title { font-size: 16px; color: rgba(255,255,255,0.65); line-height: 1.7; margin-bottom: 28px; }
  .actions { display: flex; gap: 14px; }
}
.gradient-btn {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%) !important; border: none !important;
  border-radius: 50px !important; padding: 13px 32px !important; font-weight: 700 !important; font-size: 15px !important; color: #fff !important;
  box-shadow: 0 6px 20px rgba(79,172,254,0.45); transition: all 0.3s !important;
  &:hover { transform: translateY(-2px); box-shadow: 0 10px 30px rgba(79,172,254,0.6); }
}
.outline-btn {
  border-radius: 50px !important; border: 2px solid rgba(255,255,255,0.3) !important;
  background: rgba(255,255,255,0.08) !important; color: #fff !important; padding: 13px 32px !important;
  font-weight: 600 !important; backdrop-filter: blur(10px);
  &:hover { background: rgba(255,255,255,0.18) !important; border-color: rgba(255,255,255,0.5) !important; }
}
.welcome-image {
  z-index: 1;
  .avatar-hero {
    width: 200px; height: 200px; border-radius: 50%; border: 4px solid rgba(255,255,255,0.2);
    box-shadow: 0 15px 40px rgba(0,0,0,0.3); object-fit: cover;
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    &:hover { transform: scale(1.06) rotate(3deg); border-color: rgba(255,255,255,0.5); }
  }
}

/* ========== Stats ========== */
.stats-row { margin-bottom: 24px; }
.stat-card {
  position: relative; overflow: hidden; display: flex; align-items: center;
  background: #fff; border-radius: 18px; padding: 24px; transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid #edf2f9;
  &:hover { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(0,0,0,0.08); }
}
.stat-icon-wrap {
  width: 54px; height: 54px; border-radius: 14px; display: flex; align-items: center;
  justify-content: center; font-size: 26px; margin-right: 18px; transition: all 0.3s; flex-shrink: 0;
}
.stat-info { flex: 1; z-index: 1; }
.stat-label { font-size: 13px; color: #94a3b8; font-weight: 600; margin-bottom: 6px; }
.stat-number { font-size: 28px; font-weight: 800; color: #1e293b; }
.stat-wave {
  position: absolute; bottom: -30px; right: -20px; width: 100px; height: 100px;
  border-radius: 50%; opacity: 0.06; transition: all 0.3s;
}
.card-1 .stat-icon-wrap { background: rgba(99,102,241,0.1); color: #6366f1; }
.card-1 .stat-wave { background: #6366f1; }
.card-1:hover .stat-icon-wrap { background: #6366f1; color: #fff; }
.card-2 .stat-icon-wrap { background: rgba(139,92,246,0.1); color: #8b5cf6; }
.card-2 .stat-wave { background: #8b5cf6; }
.card-2:hover .stat-icon-wrap { background: #8b5cf6; color: #fff; }
.card-3 .stat-icon-wrap { background: rgba(16,185,129,0.1); color: #10b981; }
.card-3 .stat-wave { background: #10b981; }
.card-3:hover .stat-icon-wrap { background: #10b981; color: #fff; }
.card-4 .stat-icon-wrap { background: rgba(245,158,11,0.1); color: #f59e0b; }
.card-4 .stat-wave { background: #f59e0b; }
.card-4:hover .stat-icon-wrap { background: #f59e0b; color: #fff; }

/* ========== Section Cards ========== */
.section-card {
  background: #fff; border-radius: 18px; padding: 24px; border: 1px solid #edf2f9;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}
.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.section-title {
  font-size: 17px; font-weight: 700; color: #1e293b;
  i { margin-right: 8px; color: #6366f1; }
}

/* Feature Grid */
.feature-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.feature-item {
  display: flex; padding: 20px; border-radius: 14px; background: #f8fafc; border: 1px solid #edf2f9;
  transition: all 0.3s;
  &:hover { background: #fff; border-color: #e2e8f0; transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.04); }
  .feat-icon {
    width: 48px; height: 48px; min-width: 48px; border-radius: 12px;
    display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px;
    margin-right: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  }
  .feat-body {
    h4 { margin: 0 0 6px; font-size: 15px; font-weight: 700; color: #1e293b; }
    p { margin: 0; font-size: 13px; color: #64748b; line-height: 1.6; }
  }
}

/* Notice Card */
.notice-card { min-height: 380px; }
.notice-list { display: flex; flex-direction: column; gap: 12px; }
.notice-item {
  display: flex; align-items: flex-start; padding: 14px 16px; border-radius: 12px;
  background: #f8fafc; border: 1px solid #edf2f9; transition: all 0.3s; cursor: default;
  &:hover { background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.04); transform: translateX(4px); }
}
.notice-dot {
  width: 8px; height: 8px; border-radius: 50%; margin-top: 6px; margin-right: 12px; flex-shrink: 0;
  &.dot-notify { background: #6366f1; box-shadow: 0 0 6px rgba(99,102,241,0.4); }
  &.dot-announce { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.4); }
}
.notice-body { flex: 1; min-width: 0; }
.notice-title { font-size: 14px; font-weight: 600; color: #1e293b; margin-bottom: 6px; line-height: 1.4; }
.notice-meta {
  display: flex; align-items: center; gap: 8px;
  .notice-time { font-size: 12px; color: #94a3b8; }
}
.empty-notice {
  text-align: center; padding: 60px 20px;
  i { font-size: 40px; color: #cbd5e1; display: block; margin-bottom: 10px; }
  p { color: #94a3b8; font-size: 14px; margin: 0 0 6px; }
  span { font-size: 12px; color: #cbd5e1; }
}

/* Responsive */
@media (max-width: 1200px) {
  .welcome-banner { padding: 40px; }
  .welcome-image img { width: 160px; height: 160px; }
}
@media (max-width: 900px) {
  .welcome-banner { flex-direction: column; text-align: center; padding: 30px; .actions { justify-content: center; } }
  .feature-grid { grid-template-columns: 1fr; }
  .stats-row .el-col { width: 50%; }
  .stat-card { padding: 16px; }
  .stat-icon-wrap { width: 42px; height: 42px; font-size: 20px; margin-right: 12px; }
  .stat-number { font-size: 22px; }
  .content-row .el-col { width: 100%; }
  .welcome-image img { width: 140px; height: 140px; }
  .dashboard-container { padding: 16px; }
  .main-title { font-size: 26px; }
}
@media (max-width: 480px) {
  .stats-row .el-col { width: 100%; }
}
</style>
