<template>
  <div class="app-container profile-container">
    <div class="profile-header-bg">
      <div class="glass-overlay"></div>
    </div>

    <el-row :gutter="24" class="profile-layout">
      <!-- 左侧：用户卡片 -->
      <el-col :span="7" :xs="24" class="left-col">
        <el-card class="box-card profile-card" shadow="hover">
          <div class="avatar-section">
            <div class="avatar-halo">
              <userAvatar />
            </div>
            <h2 class="user-name">{{ user.userName }}</h2>
            <div class="user-role">{{ roleGroup }}</div>
            <div class="user-dept" v-if="user.dept"><i class="el-icon-office-building"></i> {{ user.dept.deptName }} / {{ postGroup }}</div>
          </div>

          <div class="info-section">
            <div class="info-item">
              <div class="info-icon bg-blue"><i class="el-icon-mobile-phone"></i></div>
              <div class="info-content">
                <div class="info-label">联系电话</div>
                <div class="info-val">{{ user.phonenumber || '未绑定' }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon bg-purple"><i class="el-icon-message"></i></div>
              <div class="info-content">
                <div class="info-label">安全邮箱</div>
                <div class="info-val">{{ user.email || '未绑定' }}</div>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon bg-green"><i class="el-icon-time"></i></div>
              <div class="info-content">
                <div class="info-label">加入时间</div>
                <div class="info-val">{{ user.createTime }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 学习统计小卡片 (动态数据) -->
        <el-card class="box-card stats-card" shadow="hover">
           <div class="stat-box">
             <div class="stat-number">{{ purchasedCount }}</div>
             <div class="stat-text">已购课程</div>
           </div>
           <div class="stat-divider"></div>
           <div class="stat-box">
             <div class="stat-number">{{ experimentCount }}</div>
             <div class="stat-text">完成实验数</div>
           </div>
        </el-card>

      </el-col>

      <!-- 右侧：资料设定 -->
      <el-col :span="17" :xs="24" class="right-col">
        <el-card class="box-card settings-card" shadow="hover">
          <div slot="header" class="card-title">
            <span><i class="el-icon-setting"></i> 账户核心设置</span>
          </div>
          <el-tabs v-model="selectedTab" class="custom-tabs">
            <el-tab-pane label="基本资料" name="userinfo">
              <div class="tab-content-wrapper">
                <userInfo :user="user" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="安全与密码" name="resetPwd">
              <div class="tab-content-wrapper">
                <resetPwd />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      roleGroup: {},
      postGroup: {},
      purchasedCount: 0,
      experimentCount: 0,
      selectedTab: "userinfo"
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.getUser()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data || {}
        this.roleGroup = response.roleGroup || '默认用户'
        this.postGroup = response.postGroup || '无岗位'
        this.purchasedCount = response.purchasedCount || 0
        this.experimentCount = response.experimentCount || 0
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 0;
  background-color: #f4f7fc;
  min-height: calc(100vh - 84px);
  position: relative;
}

.profile-header-bg {
  height: 280px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 0;
  border-bottom-left-radius: 40px;
  border-bottom-right-radius: 40px;
  overflow: hidden;

  .glass-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('~@/assets/images/profile.jpg') no-repeat center center / cover;
    opacity: 0.2;
    mix-blend-mode: overlay;
  }
}

.profile-layout {
  position: relative;
  z-index: 1;
  padding: 40px 30px;
  margin: 0 !important;
}

/* 覆盖组件默认样式让其贴合现代设计 */
::v-deep .box-card {
  border-radius: 20px;
  border: none;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05) !important;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 左侧 Profile Card */
.profile-card {
  .avatar-section {
    text-align: center;
    padding: 30px 20px 20px;
    border-bottom: 1px dashed #edf2f9;

    .avatar-halo {
      display: inline-block;
      padding: 6px;
      border-radius: 50%;
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      margin-bottom: 20px;
      box-shadow: 0 8px 20px rgba(79, 172, 254, 0.3);

      ::v-deep .img-circle {
        border: 4px solid #fff;
        width: 120px;
        height: 120px;
      }
    }

    .user-name {
      font-size: 24px;
      font-weight: 800;
      color: #1e293b;
      margin: 0 0 8px 0;
    }

    .user-role {
      font-size: 14px;
      color: #3b82f6;
      background: rgba(59, 130, 246, 0.1);
      padding: 4px 16px;
      border-radius: 20px;
      display: inline-block;
      font-weight: 600;
      margin-bottom: 10px;
    }

    .user-dept {
      color: #64748b;
      font-size: 13px;
      i { margin-right: 4px; }
    }
  }

  .info-section {
    padding: 25px 20px;

    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      &:last-child { margin-bottom: 0; }

      .info-icon {
        width: 42px;
        height: 42px;
        border-radius: 12px;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 20px;
        color: white;
        margin-right: 15px;

        &.bg-blue { background: linear-gradient(135deg, #4facfe, #00f2fe); box-shadow: 0 4px 10px rgba(0,242,254,0.3); }
        &.bg-purple { background: linear-gradient(135deg, #a18cd1, #fbc2eb); box-shadow: 0 4px 10px rgba(161,140,209,0.3); }
        &.bg-green { background: linear-gradient(135deg, #0ba360, #3cba92); box-shadow: 0 4px 10px rgba(11,163,96,0.3); }
      }

      .info-content {
        flex: 1;

        .info-label {
          font-size: 12px;
          color: #94a3b8;
          margin-bottom: 4px;
        }

        .info-val {
          font-size: 14px;
          color: #1e293b;
          font-weight: 600;
        }
      }
    }
  }
}

.stats-card {
  ::v-deep .el-card__body {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 25px 20px;
  }
  
  .stat-box {
    text-align: center;
    
    .stat-number {
      font-size: 28px;
      font-weight: 800;
      color: #1e293b;
      margin-bottom: 5px;
    }
    .stat-text {
      font-size: 13px;
      color: #64748b;
    }
  }

  .stat-divider {
    width: 2px;
    height: 40px;
    background: #edf2f9;
  }
}

/* 右侧 Settings Card */
.settings-card {
  min-height: 720px;

  ::v-deep .el-card__header {
    border-bottom: 1px solid #f1f5f9;
    padding: 25px 30px;
  }

  .card-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    display: flex;
    align-items: center;

    i {
      color: #4facfe;
      margin-right: 8px;
      font-size: 22px;
    }
  }

  .custom-tabs {
    padding: 0 30px;

    ::v-deep .el-tabs__nav-wrap::after {
      height: 1px;
      background-color: #f1f5f9;
    }

    ::v-deep .el-tabs__item {
      height: 60px;
      line-height: 60px;
      font-size: 15px;
      font-weight: 500;
      color: #64748b;

      &.is-active {
        color: #3b82f6;
        font-weight: 600;
      }
    }

    ::v-deep .el-tabs__active-bar {
      height: 3px;
      border-radius: 3px;
      background-color: #3b82f6;
    }
  }

  .tab-content-wrapper {
    padding: 30px 10px;
    
    /* 深化子组件的输入框质感 */
    ::v-deep .el-input__inner {
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      padding: 0 15px;
      height: 44px;
      line-height: 44px;
      transition: all 0.3s;

      &:focus {
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
      }
    }

    ::v-deep .el-button--primary {
      border-radius: 8px;
      height: 44px;
      padding: 0 25px;
      font-weight: 600;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 15px rgba(37, 99, 235, 0.4);
      }
    }
  }
}
</style>
