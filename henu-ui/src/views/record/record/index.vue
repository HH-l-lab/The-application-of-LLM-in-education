<template>
  <div class="app-container record-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-data-analysis"></i> 学生实验过程记录</h2>
        <p>实验数据 · AI 诊断分析 · 评分与报告</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['record:record:add']">新增记录</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" v-hasPermi="['record:record:remove']" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['record:record:export']">导出</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="学生ID" prop="studentId">
            <el-input v-model="queryParams.studentId" placeholder="学生ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="课程ID" prop="courseId">
            <el-input v-model="queryParams.courseId" placeholder="课程/实验ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="评分" prop="score">
            <el-input v-model="queryParams.score" placeholder="0-100" clearable style="width:100px;" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div v-loading="loading" class="cards-container">
      <div v-if="recordList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-notebook-2"></i>
        <p>暂无实验记录</p>
      </div>

      <div class="record-card" v-for="item in recordList" :key="item.recordId">
        <div class="card-top-bar">
          <div class="top-left">
            <el-checkbox :value="ids.includes(item.recordId)" @change="toggleSelect(item.recordId)"></el-checkbox>
            <span class="record-id">#{{ item.recordId }}</span>
            <span class="meta-item"><i class="el-icon-user"></i> 学生 {{ item.studentId }}</span>
            <span class="meta-item"><i class="el-icon-video-camera"></i> 课程 {{ item.courseId }}</span>
            <el-tag :type="item.status === '1' ? 'success' : item.status === '0' ? 'warning' : 'info'" size="mini" effect="dark">
              {{ item.status === '1' ? '已完成' : item.status === '0' ? '进行中' : '待处理' }}
            </el-tag>
            <div class="score-badge" v-if="item.score != null">
              <span class="score-val">{{ item.score }}</span><span class="score-label">分</span>
            </div>
          </div>
          <div class="top-right">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['record:record:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)" v-hasPermi="['record:record:remove']">删除</el-button>
          </div>
        </div>

        <div class="card-body">
          <div class="content-block data-block">
            <div class="block-label"><i class="el-icon-document"></i> 实验数据 (JSON)</div>
            <div class="block-text" :class="{ expanded: item._dataExpanded }">{{ item.experimentData || '暂无数据' }}</div>
            <el-button v-if="item.experimentData && item.experimentData.length > 100" type="text" size="mini" @click="$set(item, '_dataExpanded', !item._dataExpanded)">{{ item._dataExpanded ? '收起' : '展开' }}</el-button>
          </div>
          <div class="content-block ai-block">
            <div class="block-label"><i class="el-icon-magic-stick"></i> AI 诊断分析</div>
            <div class="block-text" :class="{ expanded: item._aiExpanded }">{{ item.aiAnalysis || '暂无分析' }}</div>
            <el-button v-if="item.aiAnalysis && item.aiAnalysis.length > 100" type="text" size="mini" @click="$set(item, '_aiExpanded', !item._aiExpanded)">{{ item._aiExpanded ? '收起' : '展开' }}</el-button>
          </div>
        </div>

        <div class="card-footer" v-if="item.reportUrl">
          <a :href="item.reportUrl" target="_blank" class="report-link"><i class="el-icon-download"></i> 下载实验报告 (Word)</a>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseId">
          <el-input v-model="form.courseId" placeholder="请输入课程/实验ID" />
        </el-form-item>
        <el-form-item label="AI 诊断" prop="aiAnalysis">
          <el-input v-model="form.aiAnalysis" type="textarea" :rows="3" placeholder="大模型预检分析与建议" />
        </el-form-item>
        <el-form-item label="报告地址" prop="reportUrl">
          <el-input v-model="form.reportUrl" placeholder="Word报告下载地址" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input v-model="form.score" placeholder="0-100" style="width:120px;" />
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="删除标志" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRecord, getRecord, delRecord, addRecord, updateRecord } from "@/api/record/record"

export default {
  name: "Record",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true,
      total: 0, recordList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, studentId: null, courseId: null, experimentData: null, aiAnalysis: null, reportUrl: null, score: null, status: null },
      form: {},
      rules: {
        studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
        courseId: [{ required: true, message: "课程ID不能为空", trigger: "blur" }],
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listRecord(this.queryParams).then(response => { this.recordList = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { recordId: null, studentId: null, courseId: null, experimentData: null, aiAnalysis: null, reportUrl: null, score: null, status: null, delFlag: null, createTime: null, updateTime: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleAdd() { this.reset(); this.open = true; this.title = "新增实验记录" },
    handleUpdate(row) {
      this.reset(); const recordId = row.recordId || this.ids
      getRecord(recordId).then(response => { this.form = response.data; this.open = true; this.title = "修改实验记录" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.recordId != null) { updateRecord(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() }) }
          else { addRecord(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() }) }
        }
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + recordIds + '"的记录？').then(function() { return delRecord(recordIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    handleExport() { this.download('record/record/export', { ...this.queryParams }, `record_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style lang="scss" scoped>
.record-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #10b981; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #10b981; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
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
  .record-id { font-weight: 800; color: #94a3b8; font-size: 13px; }
  .meta-item { font-size: 13px; color: #475569; i { color: #94a3b8; margin-right: 3px; } }
  .top-right .el-button { font-weight: 500; }
}

.score-badge {
  display: inline-flex; align-items: baseline; gap: 2px; background: linear-gradient(135deg, #10b981, #059669); color: #fff;
  padding: 2px 10px; border-radius: 12px; box-shadow: 0 2px 6px rgba(16,185,129,0.3);
  .score-val { font-size: 16px; font-weight: 800; }
  .score-label { font-size: 11px; opacity: 0.8; }
}

.card-body { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.content-block {
  padding: 14px; border-radius: 10px; background: #f8fafc; border: 1px solid #f1f5f9;
  .block-label { font-size: 13px; font-weight: 600; margin-bottom: 8px; i { margin-right: 5px; } }
  .block-text { font-size: 13px; color: #475569; line-height: 1.7; max-height: 60px; overflow: hidden; transition: max-height 0.4s ease; word-break: break-all;
    &.expanded { max-height: 800px; }
  }
}
.data-block .block-label { color: #f59e0b; }
.ai-block .block-label { color: #8b5cf6; }

.card-footer {
  margin-top: 12px; padding-top: 12px; border-top: 1px dashed #edf2f9;
  .report-link {
    font-size: 13px; color: #3b82f6; font-weight: 600; text-decoration: none;
    i { margin-right: 4px; }
    &:hover { color: #2563eb; text-decoration: underline; }
  }
}

::v-deep .custom-dialog .el-dialog { border-radius: 16px; overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; padding: 18px 24px; }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner, ::v-deep .custom-dialog .el-textarea__inner {
  border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #10b981; box-shadow: 0 0 0 3px rgba(16,185,129,0.1); }
}
</style>
