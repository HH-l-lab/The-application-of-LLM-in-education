<template>
  <div class="edu-pro-container">


    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="bg-glow"></div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="pulse-dot"></span> 2026 全新沉浸式实验工作台
        </div>
        <h1 class="hero-title">
          打破物理界限， <br/>
          <span class="text-gradient">开启高阶实验探索。</span>
        </h1>
        <p class="hero-desc">
          汇聚全球顶尖实验视频，结合 DeepSeek 垂直大模型为您提供伴读指导。无论是物理探究、化学配平还是生物观察，这里都有您的进阶跳板。
        </p>
      </div>
    </section>

    <!-- 主体目录选择区域 -->
    <main class="main-content">
      
      <!-- 学科大类选择 Tabs -->
      <div class="subject-tabs-wrapper">
        <div class="subject-tabs">
          <div 
             v-for="sub in subjects" :key="sub.id"
             class="subject-tab"
             :class="{ 'active': activeSubject && activeSubject.id === sub.id }"
             @click="selectSubject(sub)"
          >
            <span class="tab-emoji">{{ sub.emoji }}</span>
            <span class="tab-name">{{ sub.name }}</span>
          </div>
        </div>
      </div>

      <!-- 条件筛选区域 -->
      <transition name="fade-in-up">
        <div class="filters-container" v-if="activeSubject">
          <div class="filter-glass-card">
            
            <!-- 年级筛选 -->
            <div class="filter-row">
              <div class="filter-label"><i class="el-icon-school"></i> 适用年级</div>
              <div class="filter-options">
                <div 
                  v-for="grade in activeSubject.grades" :key="grade"
                  class="filter-pill"
                  :class="{ 'active': filters.grade === grade }"
                  @click="setFilter('grade', grade)"
                >
                  {{ grade }}
                </div>
              </div>
            </div>

            <!-- 教材筛选 -->
            <transition name="slide-down">
              <div class="filter-row mt-4" v-if="filters.grade">
                <div class="filter-label"><i class="el-icon-collection"></i> 教材版本</div>
                <div class="filter-options">
                  <div 
                    v-for="edition in editions" :key="edition"
                    class="filter-pill"
                    :class="{ 'active': filters.edition === edition }"
                    @click="setFilter('edition', edition)"
                  >
                    {{ edition }}
                  </div>
                </div>
              </div>
            </transition>

          </div>
        </div>
      </transition>

      <!-- 实验列表卡片网格 -->
      <transition name="fade-in-up">
        <section class="course-grid-section" v-if="filters.edition">
          <div class="section-header" style="display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 15px;">
            <div>
              <h2 class="section-title">精选实验资源库</h2>
              <p class="section-desc">{{ activeSubject.name }} · {{ filters.grade }} · {{ filters.edition }}</p>
            </div>
            <div class="search-bar-wrapper" style="width: 300px;">
              <el-input 
                v-model="searchQuery" 
                placeholder="搜索实验名称或关键字..." 
                prefix-icon="el-icon-search"
                clearable
              ></el-input>
            </div>
          </div>

          <!-- 分组按章节展示 -->
          <div class="chapter-group" v-for="(exps, chapter) in groupedExperiments" :key="chapter">
            <h3 class="chapter-title"><i class="el-icon-reading text-highlight"></i> 章节：{{ chapter }}</h3>
            <div class="course-grid">
              
              <div 
                v-for="exp in exps" :key="exp.id"
                class="premium-course-card group"
                @click="enterWorkspace(exp)"
              >
                <div class="card-img-box">
                  <img :src="exp.cover" class="card-img" />
                  <div class="card-badge">{{ activeSubject.name }}</div>
                  <div class="card-badge-right" v-if="exp.tag">{{ exp.tag }}</div>
                </div>
                
                <div class="card-content">
                  <div class="card-meta">
                    <span class="rating">
                      <i class="el-icon-star-on text-amber"></i> {{ exp.rating }}
                    </span>
                    <span class="students">
                      <i class="el-icon-user"></i> {{ exp.students }}人在学
                    </span>
                  </div>
                  <h4 class="card-title">{{ exp.label }}</h4>
                  <div class="card-footer">
                    <span class="price-free" v-if="!exp.price || exp.price === 0">限时免费</span>
                    <span class="price-paid" v-else>¥ {{ exp.price }}</span>
                    <button class="join-btn">立即进入</button>
                  </div>
                </div>
              </div>

            </div>
          </div>
          
          <div v-if="Object.keys(groupedExperiments).length === 0" class="empty-state">
            <div class="empty-icon"><i class="el-icon-folder-opened"></i></div>
            <p class="empty-text">该分类下暂无已发布的实验，请切换年级或教材~</p>
          </div>
        </section>
      </transition>

      <!-- 实验工作台 本体 (滑动切入) -->
      <transition name="slide-workspace">
        <section class="workspace-section" id="workspace" v-show="currentExperiment.id">
          
          <div class="workspace-header-card">
            <div class="workspace-title-box">
              <div class="w-icon"><i class="el-icon-s-platform"></i></div>
              <div>
                <h2 class="w-title">{{ currentExperiment.label }}</h2>
                <div class="w-subtitle">正在进行高阶实验数据推演与模型伴学...</div>
              </div>
            </div>
            <button class="close-workspace-btn" @click="closeWorkspace">
              <i class="el-icon-close"></i> 退出工作台
            </button>
          </div>

          <div class="workspace-grid" :class="{ 'chat-closed': !showChat }">
            <!-- 左侧核心区 -->
            <div class="w-main-col">
              <!-- 视频播放器 (Mac风格) -->
              <div class="mac-video-container">
                <div class="mac-header">
                  <span class="mac-dot red"></span>
                  <span class="mac-dot yellow"></span>
                  <span class="mac-dot green"></span>
                  <span class="mac-title">主媒体流监控 / 资源 ID: {{ currentExperiment.id }}</span>
                </div>
                <video v-if="currentExperiment.videoUrl" controls class="video-player" :src="currentExperiment.videoUrl"></video>
                <div v-else class="video-placeholder">
                  <i class="el-icon-video-camera" style="font-size: 40px; margin-bottom: 10px; color:#4f46e5;"></i>
                  <br/>媒体资源调度中...
                </div>
              </div>

              <!-- 数据采集表单区 -->
              <div class="data-form-card">
                <div class="form-header">
                  <div class="f-title"><i class="el-icon-data-line"></i> 智能数据采集器</div>
                  <button class="export-btn" @click="downloadReport"><i class="el-icon-download"></i> 导出报告</button>
                </div>
                
                <div v-if="!currentExperiment.id || currentExperiment.formFields.length === 0" class="empty-form">
                  <p>当前实验为观测型，无需录入复杂数学变量。</p>
                </div>
                
                <div v-else class="form-grid">
                  <div class="form-item" v-for="(field, index) in currentExperiment.formFields" :key="index">
                    <label class="form-label">{{ field.label }}</label>
                    <div class="input-wrapper">
                      <input type="text" v-model="experimentData[field.key]" :placeholder="field.placeholder" class="pro-input" />
                      <span class="input-append" v-if="field.unit">{{ field.unit }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="form-footer" v-if="currentExperiment.formFields && currentExperiment.formFields.length > 0">
                  <button class="pro-submit-btn" @click="submitPrecheck">
                    <i class="el-icon-magic-stick"></i> 启动 AI 数据预检与推演
                  </button>
                </div>
              </div>
            </div>

            <!-- 右侧AI智库聊天界面 -->
            <div class="w-side-col" :class="{ 'is-hidden': !showChat }">
              <!-- 收缩/展开控制按钮 -->
              <div class="chat-toggle-wrapper">
                <button class="chat-toggle-btn" @click="showChat = !showChat" :title="showChat ? '收起 AI 导师' : '展开 AI 导师'">
                  <i :class="showChat ? 'el-icon-arrow-right' : 'el-icon-chat-dot-round'"></i>
                </button>
              </div>

              <div class="chat-glass-panel">
                <div class="chat-header">
                  <div class="c-info">
                    <div class="c-avatar">🤖</div>
                    <div>
                      <div class="c-name">DeepSeek 导师</div>
                      <div class="c-status">
                        <span class="status-dot" :class="isConnected ? 'online' : 'offline'"></span>
                        {{ isConnected ? '计算资源已连接' : '网络已断开' }}
                      </div>
                    </div>
                  </div>
                </div>

                <div class="chat-body" ref="chatBox">
                  <div v-for="(msg, index) in chatList" :key="index" class="msg-row" :class="msg.isSelf ? 'msg-right' : 'msg-left'">
                    <div class="msg-avatar" v-if="!msg.isSelf">🤖</div>
                    <div class="msg-bubble" :class="msg.isSelf ? 'bubble-user' : 'bubble-ai'">
                      <div v-if="msg.isSelf">{{ msg.content }}</div>
                      <div v-else v-html="renderMarkdown(msg.content)" class="markdown-body custom-md-style"></div>
                    </div>
                    <div class="msg-avatar" v-if="msg.isSelf">👤</div>
                  </div>
                  <div v-if="chatList.length === 0" class="empty-chat-tip">
                    <i class="el-icon-chat-dot-round" style="font-size:32px; color:#cbd5e1; margin-bottom:10px;"></i><br/>
                    随时向我提问，或点击左侧开始预检
                  </div>
                </div>

                <div class="chat-input-area">
                  <button class="c-summary-btn" @click="fetchSummary" :disabled="!isConnected" title="获取视频AI总结及实验概要">
                    <i class="el-icon-document"></i>
                  </button>
                  <input type="text" v-model="inputMsg" placeholder="输入你想咨询的物理/化学原理..." @keyup.enter="sendMessage" class="c-input" />
                  <button class="c-send-btn" @click="sendMessage" :disabled="!isConnected">
                    <i class="el-icon-position"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

        </section>
      </transition>
    </main>
  </div>
</template>

<script>
import { listCourse, playCourse } from "@/api/course/course";
import request from '@/utils/request';
import { marked } from 'marked';
import katex from 'katex';
import 'katex/dist/katex.min.css';

export default {
  name: "LearningRoomEduPro",
  dicts: ['sys_subject', 'sys_grade', 'sys_textbooked'],
  data() {
    return {
      isScrolled: false,
      // 学科配置数据
      subjects: [
        { 
          id: 'physics', name: '物理', emoji: '⚙️',
          grades: ['初二', '初三', '高一', '高二', '高三']
        },
        { 
          id: 'biology', name: '生物', emoji: '🧬',
          grades: ['初一', '初二', '初三', '高一', '高二', '高三']
        },
        { 
          id: 'chemistry', name: '化学', emoji: '🧪',
          grades: ['初三', '高一', '高二', '高三']
        }
      ],
      editions: ['人教版', '苏教版', '鲁科版', '粤教版'],
      
      activeSubject: null,
      filters: { grade: '', edition: '' },
      searchQuery: '',
      
      // 全量实验库数据 (从数据库动态加载)
      allExperiments: [],

      // 工作台状态
      showChat: true,
      currentExperiment: { id: null, label: '', videoUrl: '', formFields: [] },
      experimentData: {},
      
      // WebSocket 状态
      ws: null,
      isConnected: false,
      chatList: [],
      inputMsg: ''
    }
  },
  computed: {
    // 过滤出符合条件的实验，并按章节(Chapter)分组
    groupedExperiments() {
      if (!this.activeSubject || !this.filters.grade || !this.filters.edition) return {};
      
      const filtered = this.allExperiments.filter(e => {
        const matchSubject = e.subjectId === this.activeSubject.id;
        const matchGrade = e.grade === this.filters.grade;
        const matchEdition = e.edition === this.filters.edition;
        const matchQuery = !this.searchQuery || e.label.toLowerCase().includes(this.searchQuery.toLowerCase());
        return matchSubject && matchGrade && matchEdition && matchQuery;
      });
      
      const groups = {};
      filtered.forEach(exp => {
        if (!groups[exp.chapter]) groups[exp.chapter] = [];
        groups[exp.chapter].push(exp);
      });
      return groups;
    }
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
    this.getList();
    this.initWebSocket();
  },
  destroyed() {
    window.removeEventListener('scroll', this.handleScroll);
    if (this.ws) this.ws.close();
  },
  methods: {
    // 从后端动态拉取课程库列表
    getList() {
      listCourse({ auditStatus: '1' }).then(response => {
        const rows = response.rows || [];
        this.allExperiments = rows.map(dbItem => {
          // 由于您尚未在数据库建表阶段严格定义科目的四级目录（目前 biz_course 表只有 subject, tags 等少数外键），
          // 这里我们为了适应高级 UI 组件的映射，暂时对后端传回的结构做一次安全包装。
          // 实际开发中可以通过 biz_course 关联查询 sys_catalog 得到这些字典树。
          return {
            id: dbItem.courseId,
            subjectId: this.getSubjectId(this.selectDictLabel(this.dict.type.sys_subject, dbItem.courseSubject) || dbItem.courseSubject),
            grade: this.selectDictLabel(this.dict.type.sys_grade, dbItem.courseGrade) || dbItem.courseGrade || '高一',
            edition: this.selectDictLabel(this.dict.type.sys_textbooked, dbItem.textbookEdition) || dbItem.textbookEdition || '人教版',
            chapter: dbItem.courseChapter || '未分类章节',
            label: dbItem.courseTitle || '未命名课程',
            videoUrl: process.env.VUE_APP_BASE_API + dbItem.videoUrl,
            cover: dbItem.coverImage ? (process.env.VUE_APP_BASE_API + dbItem.coverImage) : 'https://images.unsplash.com/photo-1614935151651-0bea6508ab6b?auto=format&fit=crop&w=600&q=80',
            rating: '5.0',
            students: (dbItem.viewCount !== undefined && dbItem.viewCount !== null) ? dbItem.viewCount : (Math.floor(Math.random() * 500) + 100),
            price: dbItem.price || 0,
            tag: dbItem.tags || '精品',
            originData: dbItem 
          };
        });
        
        // 如果数据库为空，保留几个占位符让 UI 不显得完全空白
        if (this.allExperiments.length === 0) {
          this.insertMockData();
        }
      }).catch(err => {
         console.warn("未连接到后端数据库，启动单机离线 Mock 模式", err);
         this.insertMockData();
      });
    },
    // 字典映射助手方法
    getSubjectId(cnName) {
      if (!cnName) return 'physics';
      if (cnName.includes('生物')) return 'biology';
      if (cnName.includes('化学')) return 'chemistry';
      return 'physics';
    },
    insertMockData() {
       this.allExperiments = [
        { id: 1001, subjectId: 'physics', grade: '高一', edition: '人教版', chapter: '第一章 运动的描述', label: '探究小车速度随时间变化的规律 (由于您的数据库目前为空，此为离线数据)', rating: '4.9', students: 12500, price: 0, cover: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80', tag: '经典' },
        { id: 1002, subjectId: 'physics', grade: '高一', edition: '人教版', chapter: '第三章 牛顿运动定律', label: '探究加速度与力、质量的关系', rating: '5.0', students: 24000, price: 9.9, cover: 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80', tag: '重难点' },
        { id: 1003, subjectId: 'physics', grade: '高二', edition: '人教版', chapter: '第二章 恒定电流', label: '描绘小电珠的伏安特性曲线', rating: '4.8', students: 8200, price: 0, cover: 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=600&q=80', tag: '热门' }
       ];
    },
    handleScroll() {
      this.isScrolled = window.scrollY > 20;
    },
    selectSubject(sub) {
      if (this.activeSubject && this.activeSubject.id === sub.id) return;
      this.activeSubject = sub;
      this.filters.grade = '';
      this.filters.edition = '';
      this.closeWorkspace();
    },
    setFilter(key, val) {
      this.filters[key] = val;
      if (key === 'grade') this.filters.edition = '';
      this.closeWorkspace();
      
      // 当选中"教材版本"后，自动往下翻到课程列表
      if (key === 'edition') {
        this.$nextTick(() => {
          setTimeout(() => {
            const gridEl = document.querySelector('.course-grid-section');
            if (gridEl) {
              gridEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
          }, 150);
        });
      }
    },
    enterWorkspace(exp) {
      if (!exp.price || exp.price === 0) {
        // 免费课程直接进入
        this.doEnterWorkspace(exp);
        return;
      }
      
      // 付费课程，拦截校验权限
      request({
        url: '/order/order/checkAccess/' + exp.id,
        method: 'get'
      }).then(res => {
        if (res.data === true) {
          // 已购买或本人上传，直接放行
          this.doEnterWorkspace(exp);
        } else {
          // 尚未购买，弹窗引导购买
          this.$confirm(`该优质实验课程为付费内容，价格为 ¥ ${exp.price}。是否立即模拟支付并解锁课程？`, '付费解锁提示', {
            confirmButtonText: '微信/支付宝支付',
            cancelButtonText: '我再看看',
            type: 'warning'
          }).then(() => {
             request({
               url: '/order/order/buy',
               method: 'post',
               data: { courseId: exp.id, amount: exp.price }
             }).then(buyRes => {
               if (buyRes.code === 200) {
                 this.$message.success('支付成功！已为您永久解锁该课程。');
                 this.doEnterWorkspace(exp);
               } else {
                 this.$message.error(buyRes.msg || '支付失败，请刷新重试');
               }
             });
          }).catch(() => {
             this.$message.info('已取消支付');
          });
        }
      }).catch(err => {
         this.$message.error('权限校验失败，无法加载课程状态');
      });
    },
    doEnterWorkspace(exp) {
      this.currentExperiment.id = exp.id;
      this.currentExperiment.label = exp.label;
      this.currentExperiment.videoUrl = exp.videoUrl; 
      this.experimentData = {};
      this.chatList = [];

      // 播放量 +1
      playCourse(exp.id).catch(() => {});
      
      // 读取后端配置的动态采集器 JSON 表单
      if (exp.originData && exp.originData.experimentFormConfig) {
        try {
          this.currentExperiment.formFields = JSON.parse(exp.originData.experimentFormConfig);
        } catch (e) {
          console.error("解析实验表单配置失败", e);
          this.currentExperiment.formFields = [
            { key: 'error', label: '配置解析错误', unit: '', placeholder: '请检查后台 JSON 格式' }
          ];
        }
      } else {
        // 如果后端没有配置 JSON，则回退为默认观测型
        this.currentExperiment.formFields = [
          { key: 'observation', label: '实验现象记录', unit: '', placeholder: '简述观察到的变化' }
        ];
      }

      this.$nextTick(() => {
        setTimeout(() => {
          const wsEl = document.getElementById('workspace');
          if (wsEl) {
            wsEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 100);
      });
    },
    closeWorkspace() {
      this.currentExperiment.id = null;
    },
    
    // ======== AI & Backend Interaction ========
    initWebSocket() {
      // 动态生成 WebSocket URL，不论是在本地、生产还是内网穿透（ngrok）环境下，都基于当前 window.location 自动拼接
      // 通过引入 process.env.VUE_APP_BASE_API 走前端代理( proxy ) 转发，从而被外部如 ngrok/手机等正确识别
      const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
      const baseApi = process.env.VUE_APP_BASE_API || '';
      let fullWsUrl = '';
      
      if (baseApi.startsWith('http')) {
        // 如果 VUE_APP_BASE_API 是完整地址 (如 http://x.x.x.x:8080)
        fullWsUrl = baseApi.replace('http', 'ws') + `/websocket/learningRoom`;
      } else {
        // 如果是相对路径 (如 /prod-api)
        fullWsUrl = `${protocol}${window.location.host}${baseApi}/websocket/learningRoom`;
      }
      
      this.ws = new WebSocket(fullWsUrl);
      
      this.ws.onopen = () => { this.isConnected = true; };
      this.ws.onmessage = (event) => {
        if (this.chatList.length > 0) {
          const lastMsg = this.chatList[this.chatList.length - 1];
          if (!lastMsg.isSelf) {
            lastMsg.content += event.data;
          } else {
            this.chatList.push({ isSelf: false, content: event.data });
          }
        } else {
          this.chatList.push({ isSelf: false, content: event.data });
        }
        this.scrollToBottom();
      };
      this.ws.onclose = () => { this.isConnected = false; };
      this.ws.onerror = (e) => {
        console.error("WS Error", e);
        this.isConnected = false;
      };
    },
    sendMessage() {
      if (!this.inputMsg.trim() || !this.isConnected) return;
      this.chatList.push({ isSelf: true, content: this.inputMsg });
      this.ws.send(this.inputMsg);
      this.inputMsg = '';
      this.scrollToBottom();
    },
    submitPrecheck() {
      if (!this.currentExperiment.id) return;
      this.showChat = true; // 自动展开AI聊天框
      
      // 构建带单位的数据字典
      const dataWithUnits = {};
      const fields = this.currentExperiment.formFields || [];
      for (const key in this.experimentData) {
        const fieldDef = fields.find(f => f.key === key);
        const unit = (fieldDef && fieldDef.unit) ? fieldDef.unit : '';
        dataWithUnits[key] = this.experimentData[key] + unit;
      }
      
      // 组装前端展示给用户看到的自然语言文本
      let dataStr = "";
      for (const key in dataWithUnits) {
        dataStr += ` ${key}: ${dataWithUnits[key]} `;
      }
      const displayMsg = `导师您好，我已经完成了【${this.currentExperiment.label}】，我的实验数据是：${dataStr}。请求诊断验证。`;
      
      // 组装实际发送给 AI 的 Prompt
      const actualPrompt = `导师您好，我在执行【${this.currentExperiment.label}】。学生录入数据: ${JSON.stringify(dataWithUnits)}。请通过力学/化学/生物等定理推演该数据的公差合理性及可能的问题。特别注意：请在回复的结尾，单独另起一行，用格式“评估得分：[0-100的数字]” 给出此次实验的评分，例如“评估得分：88”。`;
      
      this.inputMsg = '';
      this.chatList.push({ isSelf: true, content: displayMsg });
      this.ws.send(actualPrompt);
      this.scrollToBottom();
    },
    downloadReport() {
      if (!this.currentExperiment.id) return;

      let aiAnalysisStr = "暂无AI诊断建议";
      // 提取最后一条来自 AI 的回复作为 AI 分析结果落库
      for (let i = this.chatList.length - 1; i >= 0; i--) {
          if (!this.chatList[i].isSelf) {
              aiAnalysisStr = this.chatList[i].content;
              break;
          }
      }

      // 构建带单位的最终数据，用于生成Word报告
      const dataWithUnits = {};
      const fields = this.currentExperiment.formFields || [];
      for (const key in this.experimentData) {
        const fieldDef = fields.find(f => f.key === key);
        const unit = (fieldDef && fieldDef.unit) ? fieldDef.unit : '';
        dataWithUnits[key] = this.experimentData[key] + unit;
      }

      const payload = {
          courseId: this.currentExperiment.id,
          experimentData: JSON.stringify(dataWithUnits),
          aiAnalysis: aiAnalysisStr
      };

      request({
          url: '/experiment/report/submit',
          method: 'post',
          data: payload
      }).then(res => {
          if (res.code === 200 && res.data) {
              const recordId = res.data;
              this.download('/experiment/report/generate/' + recordId, {}, `标准实验报告_${this.currentExperiment.label}_${new Date().getTime()}.docx`);
              this.$message.success('合规实验报告生成中，稍后将自动下载！');
          } else {
              this.$message.error(res.msg || '提交实验数据失败');
          }
      }).catch(err => {
          this.$message.error('请求异常，无法生成实验记录');
      });
    },
    fetchSummary() {
      if (!this.currentExperiment.id || !this.isConnected) return;
      // 模拟自己发送一条获取概要的请求
      this.chatList.push({ isSelf: true, content: '请给我一份上面视频的AI实验概要和知识点总结。' });
      this.scrollToBottom();
      
      request({
        url: '/experiment/extraction/summary/' + this.currentExperiment.id,
        method: 'get'
      }).then(res => {
        if (res.data && res.data.aiSummary) {
          this.chatList.push({ isSelf: false, content: res.data.aiSummary });
        } else {
          this.chatList.push({ isSelf: false, content: '目前后台暂未生成该实验视频的AI提炼概要，请等待视频处理完成或联系教师重试。' });
        }
        this.scrollToBottom();
      }).catch(err => {
        this.chatList.push({ isSelf: false, content: '获取实验概要失败：' + (err.msg || '网络异常') });
        this.scrollToBottom();
      });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.chatBox;
        if (box) box.scrollTop = box.scrollHeight;
      });
    },
    renderMarkdown(text) {
      if (!text) return '';
      
      const mathBlocks = [];
      let i = 0;
      
      // 提取数学公式并用占位符替换，防止 marked 将公式内部的 _ 和 * 错误解析为 Markdown
      let processedText = text
        .replace(/\\\[(.*?)\\\]/gs, (match, p1) => {
            mathBlocks.push({ math: p1, displayMode: true });
            return `@@MATH_BLOCK_${i++}@@`;
        })
        .replace(/\\\((.*?)\\\)/gs, (match, p1) => {
            mathBlocks.push({ math: p1, displayMode: false });
            return `@@MATH_BLOCK_${i++}@@`;
        })
        .replace(/\$\$(.*?)\$\$/gs, (match, p1) => {
            mathBlocks.push({ math: p1, displayMode: true });
            return `@@MATH_BLOCK_${i++}@@`;
        })
        .replace(/(^|\s)\(\s*([a-zA-Z_]+\{.*?\}.*?)\s*\)(?=\s|$|<)/g, (match, p1, p2) => {
            // 特殊匹配 DeepSeek 未知格式的行内公式如 ( F_{计算} = ... )
            mathBlocks.push({ math: p2, displayMode: false });
            return `${p1}@@MATH_BLOCK_${i++}@@`;
        });
      
      // 让 marked 正常渲染剩下的文本，不干扰公式内部
      let html = marked.parse(processedText);
      
      // 将真实渲染好的 KaTeX DOM 还原进去
      mathBlocks.forEach((block, idx) => {
         let rendered = '';
         try {
             rendered = katex.renderToString(block.math, { displayMode: block.displayMode, throwOnError: false });
         } catch(e) {
             rendered = block.displayMode ? `$$ ${block.math} $$` : `$ ${block.math} $`;
         }
         html = html.replace(`@@MATH_BLOCK_${idx}@@`, rendered);
      });
      
      return html;
    }
  }
}
</script>

<style scoped>
/* ================== Global & Reset ================== */
.edu-pro-container {
  min-height: 100vh;
  background-color: #f8fafc; /* slate-50 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #0f172a; /* slate-900 */
}
* { box-sizing: border-box; }

.text-highlight { color: #4f46e5; } /* indigo-600 */
.text-amber { color: #f59e0b; }
.text-gradient {
  background: linear-gradient(to right, #4f46e5, #9333ea);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* ================== Header ================== */
.premium-header {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  padding: 1.25rem 0;
  transition: all 0.3s ease;
  background: transparent;
}
.premium-header.scrolled {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  padding: 0.75rem 0;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1.5rem;
}
.logo-box {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  background: linear-gradient(135deg, #4f46e5, #9333ea);
  padding: 8px;
  border-radius: 12px;
  color: white;
  font-size: 20px;
}
.logo-text {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

/* ================== Hero Section ================== */
.hero-section {
  position: relative;
  padding: 5rem 1.5rem 4rem;
  overflow: hidden;
  text-align: center;
}
.bg-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(ellipse at top, rgba(129, 140, 248, 0.4) 0%, rgba(255,255,255,0) 70%);
  pointer-events: none;
  z-index: 0;
}
.hero-content {
  position: relative;
  z-index: 10;
  max-width: 800px;
  margin: 0 auto;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 999px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  color: #4338ca;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 2rem;
}
.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4f46e5;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(79, 70, 229, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(79, 70, 229, 0); }
  100% { box-shadow: 0 0 0 0 rgba(79, 70, 229, 0); }
}
.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 1.5rem;
  letter-spacing: -1px;
}
.hero-desc {
  font-size: 1.25rem;
  color: #475569;
  line-height: 1.8;
  max-width: 600px;
  margin: 0 auto;
}

/* ================== Main Content & Navigation ================== */
.main-content {
  max-width: 1280px;
  margin: 0 auto 5rem;
  padding: 0 1.5rem;
  position: relative;
  z-index: 10;
}

/* Tabs */
.subject-tabs-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}
.subject-tabs {
  display: inline-flex;
  background: white;
  padding: 8px;
  border-radius: 20px;
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05);
  gap: 10px;
}
.subject-tab {
  padding: 12px 30px;
  border-radius: 14px;
  font-size: 18px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}
.subject-tab:hover {
  background: #f8fafc;
  color: #0f172a;
}
.subject-tab.active {
  background: #0f172a;
  color: white;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
}

/* Filters */
.filters-container {
  max-width: 900px;
  margin: 0 auto 3rem;
}
.filter-glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid white;
  border-radius: 24px;
  padding: 24px 30px;
  box-shadow: 0 20px 40px -15px rgba(0,0,0,0.05);
}
.filter-row {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 12px;
}
.mt-4 { margin-top: 1rem; }
.filter-label {
  font-weight: 700;
  color: #334155;
  font-size: 15px;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.filter-pill {
  padding: 8px 20px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-pill:hover {
  border-color: #cbd5e1;
  color: #0f172a;
}
.filter-pill.active {
  background: #4f46e5;
  border-color: #4f46e5;
  color: white;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.3);
}

/* ================== Course Grid ================== */
.course-grid-section {
  padding-top: 2rem;
  border-top: 1px solid #e2e8f0;
}
.section-header {
  margin-bottom: 2rem;
}
.section-title {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 5px;
}
.section-desc {
  color: #64748b;
  font-size: 1.1rem;
}
.chapter-group {
  margin-bottom: 3rem;
}
.chapter-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1.5rem;
  padding-left: 10px;
  border-left: 4px solid #4f46e5;
}
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
}
.premium-course-card {
  max-width: 350px;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}
.premium-course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1);
}
.card-img-box {
  position: relative;
  height: 200px;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s;
}
.premium-course-card:hover .card-img {
  transform: scale(1.05);
}
.card-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(4px);
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #0f172a;
}
.card-badge-right {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #4f46e5;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: white;
}
.card-content {
  padding: 24px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.card-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 12px;
}
.card-title {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.4;
  margin-bottom: auto;
  transition: color 0.3s;
}
.premium-course-card:hover .card-title {
  color: #4f46e5;
}
.card-footer {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price-free {
  font-size: 18px;
  font-weight: 800;
  color: #10b981;
}
.price-paid {
  font-size: 18px;
  font-weight: 800;
  color: #ef4444;
}
.join-btn {
  padding: 8px 20px;
  border-radius: 999px;
  background: #f8fafc;
  color: #0f172a;
  font-weight: 600;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: background 0.3s, color 0.3s;
}
.premium-course-card:hover .join-btn {
  background: #0f172a;
  color: white;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
}
.empty-icon { font-size: 60px; margin-bottom: 20px; opacity: 0.5; }

/* ================== Workspace ================== */
.workspace-section {
  margin-top: 4rem;
  background: #fff;
  border-radius: 32px;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.1);
  padding: 30px;
  border: 1px solid #e2e8f0;
}
.workspace-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}
.workspace-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
}
.w-icon {
  background: #4f46e5;
  color: white;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.w-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px 0;
}
.w-subtitle {
  color: #64748b;
  font-size: 14px;
}
.close-workspace-btn {
  background: #fef2f2;
  color: #ef4444;
  border: none;
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.close-workspace-btn:hover { background: #fee2e2; }

.workspace-grid {
  display: grid;
  grid-template-columns: 2fr 1.5fr;
  gap: 30px;
  transition: grid-template-columns 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  align-items: stretch;
}
.workspace-grid.chat-closed {
  grid-template-columns: 1fr 0px;
  gap: 0;
}

/* W: Main Col */
.w-main-col {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}
.mac-video-container {
  background: #0f172a;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.2);
  margin-bottom: 30px;
}
.mac-header {
  height: 40px;
  background: #1e293b;
  display: flex;
  align-items: center;
  padding: 0 16px;
}
.mac-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}
.mac-dot.red { background: #ef4444; }
.mac-dot.yellow { background: #f59e0b; }
.mac-dot.green { background: #10b981; }
.mac-title {
  color: #94a3b8;
  font-size: 13px;
  margin-left: 10px;
  font-family: monospace;
}
.video-player {
  width: 100%;
  height: 520px;
  display: block;
  object-fit: contain;
  background-color: #000;
}
.video-placeholder {
  height: 520px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-weight: 500;
}

/* Form */
.data-form-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  flex: 1;
  overflow-y: auto;
}
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.f-title { font-size: 1.3rem; font-weight: 800; color: #1e293b; display: flex; align-items: center; gap: 8px;}
.export-btn {
  background: white;
  border: 1px solid #cbd5e1;
  padding: 6px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}
.export-btn:hover { background: #f1f5f9; }
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}
.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-label { font-size: 14px; font-weight: 600; color: #475569; }
.input-wrapper {
  position: relative;
  display: flex;
}
.pro-input {
  flex: 1;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  outline: none;
  font-size: 16px;
  background-color: white;
  transition: 0.2s;
}
.pro-input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}
.input-append {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}
.form-footer {
  margin-top: 24px;
  text-align: right;
  border-top: 1px dashed #cbd5e1;
  padding-top: 20px;
}
.pro-submit-btn {
  background: linear-gradient(to right, #10b981, #059669);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}
.pro-submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 25px -5px rgba(16, 185, 129, 0.4);
}

/* W: Side Col (Chat) */
.w-side-col {
  height: calc(100vh - 120px);
  position: relative;
}
.w-side-col.is-hidden .chat-glass-panel {
  opacity: 0;
  pointer-events: none;
  transform: translateX(20px);
}
.chat-glass-panel {
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05);
  overflow: hidden;
  min-width: 400px;
}
.chat-toggle-wrapper {
  position: relative;
  width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.chat-toggle-btn {
  position: absolute;
  left: -15px;
  background: white;
  border: 1px solid #e2e8f0;
  color: #64748b;
  width: 32px;
  height: 64px;
  border-radius: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  transition: all 0.2s;
}
.chat-toggle-btn:hover {
  background: #f8fafc;
  color: #4f46e5;
  transform: scale(1.05);
}
.chat-header {
  padding: 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}
.c-info { display: flex; align-items: center; gap: 12px; }
.c-avatar { font-size: 28px; }
.c-name { font-weight: 700; color: #0f172a; font-size: 15px; }
.c-status { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; margin-top:2px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.online { background: #10b981; box-shadow: 0 0 10px #10b981; }
.status-dot.offline { background: #ef4444; }

.chat-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fcfcfd;
}
.empty-chat-tip { text-align: center; color: #94a3b8; margin-top: 40%; font-size: 14px; }
.msg-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 24px; }
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }
.msg-avatar {
  background: white;
  width: 36px; height: 36px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 20px;
}
.msg-bubble {
  max-width: 80%;
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: anywhere;
  overflow: hidden;
}
.custom-md-style {
  all: revert;
  font-size: 14px;
  line-height: 1.6;
}
.custom-md-style p {
  margin-top: 6px;
  margin-bottom: 6px;
}
.custom-md-style ul, .custom-md-style ol {
  padding-left: 20px;
  margin-top: 6px;
  margin-bottom: 6px;
}
.custom-md-style li {
  margin-bottom: 4px;
}
.custom-md-style pre {
  background: #f1f5f9;
  padding: 10px;
  border-radius: 8px;
  overflow-x: auto;
  max-width: 100%;
}
.custom-md-style code {
  background: #f1f5f9;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
}
.custom-md-style table {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}
.custom-md-style th, .custom-md-style td {
  border: 1px solid #e2e8f0;
  padding: 8px;
}
.bubble-ai {
  background: white;
  border: 1px solid #e2e8f0;
  border-top-left-radius: 4px;
  color: #334155;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
}
.bubble-user {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  border-top-right-radius: 4px;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.2);
}

.chat-input-area {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
}
.c-input {
  flex: 1;
  padding: 12px 16px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  outline: none;
  font-size: 14px;
  transition: 0.2s;
}
.c-input:focus { border-color: #4f46e5; background: white; }
.c-send-btn {
  background: #4f46e5;
  color: white;
  border: none;
  width: 44px; height: 44px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  font-size: 18px;
  transition: 0.2s;
}
.c-send-btn:hover { background: #4338ca; transform: scale(1.05); }
.c-send-btn:disabled { background: #cbd5e1; cursor: not-allowed; transform: scale(1); }

/* Animations */
.fade-in-up-enter-active, .fade-in-up-leave-active, .slide-workspace-enter-active {
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-in-up-enter, .fade-in-up-leave-to, .slide-workspace-enter {
  opacity: 0;
  transform: translateY(30px);
}
.slide-down-enter-active { transition: all 0.3s ease-out; }
.slide-down-enter, .slide-down-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
