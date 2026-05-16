<template>
  <div class="audit-container">
    <!-- Hero Header -->
    <div class="audit-header">
      <div class="header-content">
        <h1 class="header-title"><i class="el-icon-s-check"></i> 内容审核</h1>
        <p class="header-desc">为教育生态的质量把关。高效预览创作者素材，保障全知资源库的纯净与合规。</p>
      </div>
      <div class="header-stats">
        <div class="stat-box">
          <div class="stat-value text-warning">{{ total }}</div>
          <div class="stat-label">待审任务(件)</div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="audit-main">
      <div v-if="loading" class="loading-state">
        <i class="el-icon-loading"></i> 数据加载中...
      </div>
      
      <div v-else-if="courseList.length === 0" class="empty-state">
        <i class="el-icon-document-checked empty-icon"></i>
        <p>太棒啦！您的审核队列当前为空，所有创作者投稿已处理完毕。</p>
      </div>

      <div class="audit-grid" v-else>
        <div 
          v-for="item in courseList" :key="item.courseId"
          class="audit-card"
          @click="openAuditWorkspace(item)"
        >
          <div class="card-cover-wrapper">
            <img :src="item.coverImage ? getFullUrl(item.coverImage) : 'https://images.unsplash.com/photo-1614935151651-0bea6508ab6b?auto=format&fit=crop&w=400&q=80'" class="card-cover" />
            <div class="card-badge warning">待审核</div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.courseTitle || '未命名课程' }}</h3>
            <div class="card-meta">
              <span><i class="el-icon-user"></i> 创作者ID: {{ item.creatorId }}</span>
              <span><i class="el-icon-time"></i> {{ parseTime(item.createTime, '{y}-{m}-{d}') }}</span>
            </div>
            <div class="card-tags">
              <el-tag size="small" type="info">{{ selectDictLabel(dict.type.sys_subject, item.courseSubject) || '未知学科' }}</el-tag>
              <el-tag size="small" type="info">{{ selectDictLabel(dict.type.sys_grade, item.courseGrade) || '未知年级' }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 沉浸式审核工作台 Modal -->
    <el-dialog 
      :visible.sync="auditVisible" 
      fullscreen 
      custom-class="audit-workspace-dialog"
      :show-close="false"
      destroy-on-close
    >
      <template v-if="currentAudit">
        <div class="workspace-header">
          <div class="w-title"><i class="el-icon-video-camera-solid"></i> {{ currentAudit.courseTitle }} - 审查视图</div>
          <button class="w-close-btn" @click="auditVisible = false"><i class="el-icon-close"></i> 退出工作台</button>
        </div>

        <div class="workspace-body">
        
        <!-- 左侧媒体池 -->
        <div class="w-media-col">
          <div class="mac-video-container">
            <div class="mac-header">
              <span class="mac-dot red"></span>
              <span class="mac-dot yellow"></span>
              <span class="mac-dot green"></span>
              <span class="mac-title">流媒体审查监视器 / 源头验证</span>
            </div>
            <video v-if="currentAudit.videoUrl" controls class="video-player" :src="getFullUrl(currentAudit.videoUrl)"></video>
            <div v-else class="video-placeholder">
              <i class="el-icon-warning-outline" style="font-size: 40px; margin-bottom: 10px; color:#ef4444;"></i>
              <br/>流媒体加载失败，请检查源文件
            </div>
          </div>
        </div>

        <!-- 右侧信息与裁决中心 -->
        <div class="w-info-col">
          <div class="info-card">
            <h4 class="card-heading">📦 数据来源</h4>
            <div class="info-grid">
              <div class="info-item">
                <div class="i-label">归属学科</div>
                <div class="i-value">{{ selectDictLabel(dict.type.sys_subject, currentAudit.courseSubject) || '未设定' }}</div>
              </div>
              <div class="info-item">
                <div class="i-label">适用年级</div>
                <div class="i-value">{{ selectDictLabel(dict.type.sys_grade, currentAudit.courseGrade) || '未设定' }}</div>
              </div>
              <div class="info-item">
                <div class="i-label">教材版本</div>
                <div class="i-value">{{ selectDictLabel(dict.type.sys_textbooked, currentAudit.textbookEdition) || '未设定' }}</div>
              </div>
              <div class="info-item">
                <div class="i-label">视频定价</div>
                <div class="i-value text-gold">{{ currentAudit.price || '0.00' }} 元</div>
              </div>
            </div>
            
            <div class="data-form-card mt-4">
              <div class="form-header">
                <div class="f-title"><i class="el-icon-data-line"></i> 动态采集器配置预览</div>
              </div>
              
              <div v-if="!parsedFormFields || parsedFormFields.length === 0" class="empty-form">
                <p>该实验无需复杂表单采集 (配置为空)</p>
              </div>
              
              <div v-else class="form-grid">
                <div class="form-item" v-for="(field, index) in parsedFormFields" :key="index">
                  <label class="form-label">{{ field.label }}</label>
                  <div class="input-wrapper">
                    <input type="text" disabled :placeholder="field.placeholder" class="pro-input" />
                    <span class="input-append" v-if="field.unit">{{ field.unit }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 多模态 AI 智能提取控制极 -->
          <div class="action-card mt-4">
            <h4 class="card-heading text-primary"><i class="el-icon-magic-stick"></i> 核心 ASR & AI 提炼干预</h4>
            <div class="ai-console">
              <div class="console-output">
                <div v-for="(log, idx) in extLogs" :key="idx" :class="idx === extLogs.length - 1 && extFinished ? 'c-line text-success' : 'c-line'">{{ log }}</div>
                <div v-if="extLoading" class="c-line pulse">... AI 引擎跨模态推演中 ...</div>
              </div>
            </div>
            <div class="action-buttons mt-4">
              <el-button type="primary" plain class="fat-btn" @click="startExtraction" :loading="extLoading" :disabled="extFinished">对该视频进行总结</el-button>
              <el-button type="warning" class="fat-btn" @click="reportExtraction" :disabled="!extFinished">汇报</el-button>
            </div>
          </div>

          <!-- 裁决面板 -->
          <div class="action-card mt-4">
            <h4 class="card-heading text-primary">⚖️ 审核裁决</h4>
            
            <el-form label-position="top">
              <el-form-item label="反馈意见 (驳回时必填，将反馈给创作者)">
                <el-input 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入审核不通过的原因或改进建议..." 
                  v-model="auditMessage"
                ></el-input>
              </el-form-item>
            </el-form>

            <div class="action-buttons">
              <el-button type="danger" plain icon="el-icon-circle-close" class="fat-btn" @click="handleReject" :loading="submitLoading">驳回重构</el-button>
              <el-button type="success" icon="el-icon-success" class="fat-btn shadow-btn" @click="handleApprove" :loading="submitLoading">准予发布库</el-button>
            </div>
          </div>

        </div>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { listCourse, updateCourse } from "@/api/course/course"
import { parseTime } from "@/utils/henu"
import request from '@/utils/request'

export default {
  name: "AuditCourse",
  dicts: ['sys_textbooked', 'sys_grade', 'sys_coursetype', 'sys_subject'],
  data() {
    return {
      loading: true,
      submitLoading: false,
      total: 0,
      courseList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        auditStatus: '0', // 核心：永远只拉取待审核(0)
      },
      auditVisible: false,
      currentAudit: null,
      auditMessage: '',
      extLogs: [],
      extLoading: false,
      extFinished: false,
      extractedText: ''
    }
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    parseTime,
    getFullUrl(path) {
      if (!path) return '';
      if (path.startsWith('http')) return path;
      return process.env.VUE_APP_BASE_API + path;
    },
    openAuditWorkspace(item) {
      this.currentAudit = Object.assign({}, item);
      this.auditMessage = '';
      this.parsedFormFields = [];
      
      if (item.experimentFormConfig) {
        try {
          this.parsedFormFields = JSON.parse(item.experimentFormConfig);
        } catch(e) {
          console.error("审核端解析表单失败", e);
          this.parsedFormFields = [{ label: '解析错误', placeholder: 'JSON 格式不合法', unit: '' }];
        }
      }
      
      
      this.extLogs = ["正在探测数据库中是否已存留该视频的AI提炼记录..."];
      this.extLoading = true;
      this.extFinished = false;
      this.extractedText = '';
      
      this.auditVisible = true;

      // 打开时自动探测是否有现有记录
      request({
        url: '/experiment/extraction/summary/' + item.courseId,
        method: 'get'
      }).then(res => {
        this.extLoading = false;
        if (res.data && res.data.asrText) {
           this.extLogs = [
             "🔍 检测到全知数据库中已存在该流媒体的 ASR 物理记录。",
             "提示：您可以直接点击下方【汇报】按钮查阅原音拓本，",
             "或者点击【对该视频进行总结】强制覆写并重新执行全流程 AI 重组。"
           ];
           this.extractedText = res.data.asrText;
           this.extFinished = true; // 解禁汇报按钮
        } else {
           this.extLogs = ["就绪。未发现该视频的 ASR 存根，请点击左下方按钮启动自动剥离与提取。"];
        }
      }).catch(err => {
         this.extLoading = false;
         this.extLogs = ["网络检测超时，请手动触发重试。"];
      });
    },
    handleApprove() {
      const payload = {
        courseId: this.currentAudit.courseId,
        auditStatus: '1', // 审核通过
        auditMessage: this.auditMessage || '符合平台规范，同意入库发布。'
      };
      this.executeAudit(payload, '已通过并入库！');
    },
    startExtraction() {
      this.extLogs = ["正在定位物理源视频轨道...", "启动 FFmpeg 并行多线程音频切割..."];
      this.extLoading = true;
      this.extFinished = false;
      
      setTimeout(() => { if(this.extLoading) this.extLogs.push("音频信道锁存成功，建立百度智能云 ASR 并发连接..."); }, 2000);
      setTimeout(() => { if(this.extLoading) this.extLogs.push("DeepSeek 大模型上下文介入，正对音频字元进行知识主线重组..."); }, 6000);

      request({
        url: '/experiment/extraction/process/' + this.currentAudit.courseId,
        method: 'post'
      }).then(res => {
        this.extLoading = false;
        if(res.code === 200) {
            this.extLogs.push("✅ 各模态提取汇编完成，记录已持久化入库。");
            this.extLogs.push("请点击右侧【汇报】直接调阅底层真实ASR原音拓本。");
            this.extFinished = true;
            this.extractedText = res.data || "未提取到任何人声或文本";
        } else {
             this.extLogs.push("❌ 提取终端熔断: " + res.msg);
        }
      }).catch(err => {
         this.extLoading = false;
         this.extLogs.push("❌ 后端未连通或解析超时: 建议直接点击【汇报】查看已有数据，或检查后台报错日志。");
      });
    },
    reportExtraction() {
      this.extLogs = [this.extractedText];
    },
    handleReject() {
      if (!this.auditMessage.trim()) {
        this.$message.warning("请填写驳回理由，以便创作者针对性修改！");
        return;
      }
      const payload = {
        courseId: this.currentAudit.courseId,
        auditStatus: '2', // 驳回
        auditMessage: this.auditMessage
      };
      this.executeAudit(payload, '已驳回，打回创作者中心。');
    },
    executeAudit(payload, successTip) {
      this.submitLoading = true;
      updateCourse(payload).then(res => {
        this.submitLoading = false;
        this.$message.success(successTip);
        this.auditVisible = false;
        this.getList();
      }).catch(() => {
        this.submitLoading = false;
      });
    }
  }
}
</script>

<style scoped>
/* 整个容器底色与字体 */
.audit-container {
  padding: 20px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* 头部 Banner */
.audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 16px;
  padding: 30px 40px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.4);
}
.header-title { margin: 0 0 10px 0; font-size: 28px; font-weight: 700; color: #f8fafc; }
.header-desc { margin: 0; font-size: 15px; color: #cbd5e1; max-width: 600px; line-height: 1.5; }
.stat-box {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 12px;
  padding: 15px 30px;
  text-align: center;
}
.stat-value { font-size: 36px; font-weight: 800; line-height: 1; margin-bottom: 5px; color: #facc15; }
.stat-label { font-size: 13px; color: #e2e8f0; font-weight: 500; }

/* 列表展示区 */
.loading-state { text-align: center; padding: 60px; color: #64748b; font-size: 16px; }
.empty-state { text-align: center; padding: 80px 20px; color: #94a3b8; }
.empty-icon { font-size: 64px; margin-bottom: 20px; color: #cbd5e1; }

.audit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 20px;
}
.audit-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid #e2e8f0;
}
.audit-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}
.card-cover-wrapper { position: relative; width: 100%; height: 160px; overflow: hidden; background: #e2e8f0; }
.card-cover { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.audit-card:hover .card-cover { transform: scale(1.05); }
.card-badge {
  position: absolute; top: 10px; right: 10px;
  padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold;
}
.card-badge.warning { background: rgba(245, 158, 11, 0.9); color: white; backdrop-filter: blur(4px); }

.card-body { padding: 16px; }
.card-title { margin: 0 0 12px 0; font-size: 16px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-meta { display: flex; justify-content: space-between; font-size: 13px; color: #64748b; margin-bottom: 12px; }
.card-tags { display: flex; gap: 8px; flex-wrap: wrap; }


/* 全屏弹出工作台重写 */
::v-deep .audit-workspace-dialog {
  background-color: #f1f5f9;
  margin: 0 !important;
  display: flex !important;
  flex-direction: column !important;
}
::v-deep .audit-workspace-dialog .el-dialog__header { display: none !important; }
::v-deep .audit-workspace-dialog .el-dialog__body { padding: 0 !important; height: 100vh; display: flex; flex-direction: column; overflow: hidden; }

/* 工作台 Header */
.workspace-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  z-index: 10;
}
.w-title { font-size: 18px; font-weight: 600; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 8px; }
.w-close-btn { 
  background: #f1f5f9; border: none; padding: 8px 16px; border-radius: 6px; 
  color: #64748b; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.w-close-btn:hover { background: #e2e8f0; color: #0f172a; }

/* 工作台 Body */
.workspace-body {
  flex: 1;
  display: flex;
  padding: 24px;
  gap: 24px;
  overflow: hidden;
}

/* 左侧多媒体 */
.w-media-col { flex: 2; display: flex; flex-direction: column; min-width: 0; }
.mac-video-container {
  background: #1e293b;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  height: 100%;
}
.mac-header {
  background: #334155;
  padding: 10px 15px;
  display: flex;
  align-items: center;
}
.mac-dot { width: 12px; height: 12px; border-radius: 50%; margin-right: 8px; }
.mac-dot.red { background: #ff5f56; }
.mac-dot.yellow { background: #ffbd2e; }
.mac-dot.green { background: #27c93f; }
.mac-title { color: #94a3b8; font-size: 13px; font-weight: 500; margin-left: auto; letter-spacing: 0.5px; }

.video-player { width: 100%; flex: 1; background: #000; outline: none; object-fit: contain; }
.video-placeholder { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #64748b; background: #0f172a; }

/* 右侧面板 */
.w-info-col { flex: 1.2; display: flex; flex-direction: column; overflow-y: auto; padding-right: 4px; min-width: 450px;}
.info-card, .action-card {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;
}
.card-heading { margin: 0 0 16px 0; font-size: 16px; font-weight: 600; color: #1e293b; }
.text-primary { color: #3b82f6; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.i-label { font-size: 13px; color: #64748b; margin-bottom: 4px; }
.i-value { font-size: 14px; color: #0f172a; font-weight: 500; }
.text-gold { color: #d97706; font-weight: 700; font-size: 16px; }

.json-box {
  background: #f8fafc; padding: 12px; border-radius: 8px; font-size: 13px;
  color: #334155; border: 1px solid #e2e8f0; overflow-x: auto; max-height: 200px;
  margin: 0; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
.mt-4 { margin-top: 16px; }
.mb-2 { margin-bottom: 8px; }

.action-buttons { display: flex; gap: 12px; margin-top: 10px; }
.fat-btn { flex: 1; padding: 12px 0; font-size: 15px; font-weight: 600; border-radius: 8px; }
.shadow-btn { box-shadow: 0 4px 14px 0 rgba(16, 185, 129, 0.39); border: none; }

/* 模拟表单样式 (移植自伴学自习室) */
.data-form-card { background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; }
.form-header { background: #f1f5f9; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; }
.f-title { font-weight: 600; color: #334155; font-size: 14px; display: flex; align-items: center; gap: 8px; }
.empty-form { padding: 20px; text-align: center; color: #94a3b8; font-size: 13px; }
.form-grid { display: grid; grid-template-columns: 1fr; gap: 12px; padding: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: #475569; }
.input-wrapper { display: flex; align-items: stretch; border: 1px solid #cbd5e1; border-radius: 6px; overflow: hidden; background: white; }
.pro-input { flex: 1; border: none; padding: 8px 12px; font-size: 14px; color: #334155; outline: none; background: #e2e8f0; cursor: not-allowed; }
.input-append { background: #f1f5f9; padding: 0 12px; display: flex; align-items: center; justify-content: center; color: #64748b; font-size: 13px; border-left: 1px solid #cbd5e1; font-weight: 500; }

/* 模拟控制台样式 */
.ai-console {
  background-color: #0f172a;
  border-radius: 8px;
  padding: 16px;
  min-height: 120px;
  max-height: 300px;
  overflow-y: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
.console-output {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.c-line {
  color: #a7f3d0;
  font-size: 13px;
  word-break: break-all;
  white-space: pre-wrap;
}
.c-line::before {
  content: "> ";
  color: #3b82f6;
}
.c-line.text-success {
  color: #fce7f3;
}
.pulse {
  animation: pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  color: #fbbf24;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}
</style>
