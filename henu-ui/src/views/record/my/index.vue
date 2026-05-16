<template>
  <div class="app-container my-exp-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-s-management"></i> 我的实验</h2>
        <p>实验回顾 · 数据记录 · AI 诊断报告</p>
      </div>
      <div class="header-actions">
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleBatchDelete" :disabled="ids.length === 0">批量删除 ({{ ids.length }})</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="课程ID" prop="courseId">
            <el-input v-model="queryParams.courseId" placeholder="课程/实验ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div class="stats-bar">
      <span class="stat-item">共 <b>{{ total }}</b> 条实验记录</span>
    </div>

    <div v-loading="loading" class="cards-container">
      <div v-if="recordList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-notebook-2"></i>
        <p>暂无实验记录，快去自习室做实验吧！</p>
      </div>

      <div class="record-card" v-for="item in recordList" :key="item.recordId">
        <!-- 顶栏 -->
        <div class="card-top-bar">
          <div class="top-left">
            <el-checkbox :value="ids.includes(item.recordId)" @change="toggleSelect(item.recordId)"></el-checkbox>
            <span class="record-id">#{{ item.recordId }}</span>
            <span class="meta-item"><i class="el-icon-video-camera"></i> 课程 {{ item.courseId }}</span>
            <el-tag :type="item.status === '1' ? 'success' : item.status === '0' ? 'warning' : 'info'" size="mini" effect="dark">
              {{ item.status === '1' ? '已完成' : item.status === '0' ? '进行中' : '待处理' }}
            </el-tag>
            <div class="score-badge" v-if="item.score != null">
              <span class="score-val">{{ item.score }}</span><span class="score-label">分</span>
            </div>
          </div>
          <div class="top-right">
            <span v-if="item.createTime" class="time-text"><i class="el-icon-time"></i> {{ item.createTime }}</span>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444; margin-left:12px;" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>

        <!-- 实验数据（解析JSON为表格） -->
        <div class="card-body">
          <div class="content-block data-block">
            <div class="block-label"><i class="el-icon-s-data"></i> 实验数据</div>
            <div v-if="parseJson(item.experimentData)" class="data-table">
              <div class="data-row" v-for="(val, key) in parseJson(item.experimentData)" :key="key">
                <span class="data-key">{{ key }}</span>
                <span class="data-val">{{ val }}</span>
              </div>
            </div>
            <div v-else class="no-data">暂无数据</div>
          </div>

          <div class="content-block ai-block">
            <div class="block-label"><i class="el-icon-magic-stick"></i> AI 诊断分析</div>
            <div class="ai-text" :class="{ expanded: item._aiExpanded }">{{ item.aiAnalysis || '暂无分析' }}</div>
            <el-button v-if="item.aiAnalysis && item.aiAnalysis.length > 120" type="text" size="mini" @click="$set(item, '_aiExpanded', !item._aiExpanded)">{{ item._aiExpanded ? '收起' : '展开全部' }}</el-button>
          </div>
        </div>

        <!-- 底部报告下载 -->
        <div class="card-footer">
          <a v-if="item.reportUrl" :href="item.reportUrl" target="_blank" class="report-link"><i class="el-icon-download"></i> 下载实验报告 (Word)</a>
          <el-button v-else-if="item.recordId" type="text" size="small" class="report-link" @click="downloadReport(item)"><i class="el-icon-download"></i> 生成并下载实验报告</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listMyRecord, delRecord } from "@/api/record/record"

export default {
  name: "MyExperiment",
  data() {
    return {
      loading: true, showSearch: false, ids: [],
      total: 0, recordList: [],
      queryParams: { pageNum: 1, pageSize: 10, courseId: null },
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listMyRecord(this.queryParams).then(response => {
        this.recordList = response.rows; this.total = response.total; this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除该实验记录？').then(() => delRecord(row.recordId)).then(() => {
        this.getList(); this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleBatchDelete() {
      if (this.ids.length === 0) return
      this.$modal.confirm('确认删除选中的 ' + this.ids.length + ' 条记录？').then(() => delRecord(this.ids.join(','))).then(() => {
        this.getList(); this.ids = []; this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    downloadReport(item) {
      this.download('/experiment/report/generate/' + item.recordId, {}, `实验报告_${item.recordId}_${new Date().getTime()}.docx`)
    },
    parseJson(str) {
      if (!str) return null
      try {
        const obj = JSON.parse(str)
        if (typeof obj === 'object' && obj !== null && !Array.isArray(obj)) return obj
        if (Array.isArray(obj)) {
          const merged = {}
          obj.forEach(item => {
            if (typeof item === 'object') Object.assign(merged, item)
          })
          return Object.keys(merged).length > 0 ? merged : null
        }
        return null
      } catch (e) { return null }
    }
  }
}
</script>

<style lang="scss" scoped>
.my-exp-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #8b5cf6; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #8b5cf6; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.stats-bar {
  display: flex; gap: 20px; margin-bottom: 16px; padding: 0 4px;
  .stat-item { font-size: 13px; color: #64748b; b { color: #1e293b; font-size: 15px; } }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.record-card {
  background: #fff; border-radius: 16px; padding: 20px 24px; margin-bottom: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
}

.card-top-bar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;
  .top-left { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
  .record-id { font-weight: 800; color: #8b5cf6; font-size: 14px; }
  .meta-item { font-size: 13px; color: #475569; i { color: #94a3b8; margin-right: 3px; } }
  .top-right { .time-text { font-size: 12px; color: #94a3b8; i { margin-right: 3px; } } }
}

.score-badge {
  display: inline-flex; align-items: baseline; gap: 2px; background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: #fff;
  padding: 2px 10px; border-radius: 12px; box-shadow: 0 2px 6px rgba(139,92,246,0.3);
  .score-val { font-size: 16px; font-weight: 800; }
  .score-label { font-size: 11px; opacity: 0.8; }
}

.card-body { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.content-block {
  padding: 16px; border-radius: 12px; background: #f8fafc; border: 1px solid #f1f5f9;
  .block-label { font-size: 13px; font-weight: 700; margin-bottom: 10px; i { margin-right: 5px; } }
}
.data-block .block-label { color: #f59e0b; }
.ai-block .block-label { color: #8b5cf6; }

/* 数据表格样式（解析后的 JSON） */
.data-table { display: flex; flex-direction: column; gap: 6px; }
.data-row {
  display: flex; align-items: center; padding: 7px 12px; border-radius: 8px; background: #fff; border: 1px solid #edf2f9;
  .data-key {
    font-size: 13px; font-weight: 600; color: #475569; min-width: 80px; margin-right: 12px;
    &::after { content: '：'; }
  }
  .data-val { font-size: 14px; font-weight: 700; color: #1e293b; }
}
.no-data { font-size: 13px; color: #94a3b8; padding: 8px 0; }

.ai-text {
  font-size: 13px; color: #475569; line-height: 1.8; max-height: 80px; overflow: hidden; transition: max-height 0.4s ease; word-break: break-all;
  &.expanded { max-height: 2000px; }
}

.card-footer {
  margin-top: 14px; padding-top: 12px; border-top: 1px dashed #edf2f9;
  .report-link {
    font-size: 13px; color: #3b82f6; font-weight: 600; text-decoration: none;
    i { margin-right: 4px; }
    &:hover { color: #2563eb; text-decoration: underline; }
  }
}
</style>
