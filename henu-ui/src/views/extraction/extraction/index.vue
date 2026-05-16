<template>
  <div class="app-container extraction-page">

    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-cpu"></i> 视频 AI 知识提取中心</h2>
        <p>自动语音识别 · 大模型智能摘要 · 考点提炼</p>
      </div>
      <div class="header-actions">
        <el-input
          v-model="queryParams.courseId"
          placeholder="搜索课程ID"
          prefix-icon="el-icon-search"
          size="small"
          clearable
          style="width: 200px; margin-right: 12px;"
          @keyup.enter.native="handleQuery"
          @clear="handleQuery"
        />
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['extraction:extraction:add']">新增</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleBatchDelete" v-hasPermi="['extraction:extraction:remove']" :disabled="selectedIds.length === 0">批量删除 ({{ selectedIds.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['extraction:extraction:export']">导出</el-button>
      </div>
    </div>

    <!-- 数据卡片列表 -->
    <div v-loading="loading" class="cards-container">
      <div v-if="extractionList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-folder-opened"></i>
        <p>暂无提取结果</p>
      </div>

      <div class="extraction-card" v-for="item in extractionList" :key="item.extractionId">
        <div class="card-header">
          <div class="card-meta">
            <el-checkbox :value="selectedIds.includes(item.extractionId)" @change="toggleSelect(item.extractionId)"></el-checkbox>
            <span class="card-id">#{{ item.extractionId }}</span>
            <span class="card-course"><i class="el-icon-video-camera"></i> 课程 {{ item.courseId }}</span>
            <el-tag
              :type="item.extractionStatus === '1' ? 'success' : item.extractionStatus === '0' ? 'warning' : 'info'"
              size="mini"
              effect="dark"
            >
              {{ item.extractionStatus === '1' ? '已完成' : item.extractionStatus === '0' ? '处理中' : '待处理' }}
            </el-tag>
          </div>
          <div class="card-actions">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['extraction:extraction:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color: #ef4444;" @click="handleDelete(item)" v-hasPermi="['extraction:extraction:remove']">删除</el-button>
          </div>
        </div>

        <div class="card-body">
          <!-- ASR 语音转写 -->
          <div class="content-block asr-block">
            <div class="block-label"><i class="el-icon-microphone"></i> ASR 语音转写</div>
            <div class="block-text" :class="{ expanded: item._asrExpanded }">{{ item.asrText || '暂无内容' }}</div>
            <el-button v-if="item.asrText && item.asrText.length > 120" type="text" size="mini" @click="$set(item, '_asrExpanded', !item._asrExpanded)">
              {{ item._asrExpanded ? '收起' : '展开全文' }}
            </el-button>
          </div>

          <!-- AI 总结 -->
          <div class="content-block ai-block">
            <div class="block-label"><i class="el-icon-magic-stick"></i> AI 智能总结</div>
            <div class="block-text" :class="{ expanded: item._aiExpanded }">{{ item.aiSummary || '暂无内容' }}</div>
            <el-button v-if="item.aiSummary && item.aiSummary.length > 120" type="text" size="mini" @click="$set(item, '_aiExpanded', !item._aiExpanded)">
              {{ item._aiExpanded ? '收起' : '展开全文' }}
            </el-button>
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程ID" prop="courseId">
          <el-input v-model="form.courseId" placeholder="请输入课程ID" />
        </el-form-item>
        <el-form-item label="语音全文" prop="asrText">
          <el-input v-model="form.asrText" type="textarea" :rows="4" placeholder="ASR 语音转写全文" />
        </el-form-item>
        <el-form-item label="AI 总结" prop="aiSummary">
          <el-input v-model="form.aiSummary" type="textarea" :rows="4" placeholder="大模型提炼的课程总结及考点" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listExtraction, getExtraction, delExtraction, addExtraction, updateExtraction } from "@/api/extraction/extraction"

export default {
  name: "Extraction",
  data() {
    return {
      loading: true,
      ids: [],
      selectedIds: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      extractionList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseId: null,
        asrText: null,
        aiSummary: null,
        extractionStatus: null,
      },
      form: {},
      rules: {
        courseId: [
          { required: true, message: "课程ID不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listExtraction(this.queryParams).then(response => {
        this.extractionList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        extractionId: null,
        courseId: null,
        asrText: null,
        aiSummary: null,
        extractionStatus: null,
        createTime: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.extractionId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增知识提取记录"
    },
    handleUpdate(row) {
      this.reset()
      const extractionId = row.extractionId || this.ids
      getExtraction(extractionId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改知识提取记录"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.extractionId != null) {
            updateExtraction(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addExtraction(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const extractionIds = row.extractionId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + extractionIds + '"的数据项？').then(function() {
        return delExtraction(extractionIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    toggleSelect(id) {
      const idx = this.selectedIds.indexOf(id)
      if (idx > -1) {
        this.selectedIds.splice(idx, 1)
      } else {
        this.selectedIds.push(id)
      }
    },
    handleBatchDelete() {
      if (this.selectedIds.length === 0) return
      this.$modal.confirm('是否确认批量删除已选中的 ' + this.selectedIds.length + ' 条数据？').then(() => {
        return delExtraction(this.selectedIds.join(','))
      }).then(() => {
        this.selectedIds = []
        this.getList()
        this.$modal.msgSuccess("批量删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('extraction/extraction/export', {
        ...this.queryParams
      }, `extraction_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.extraction-page {
  padding: 25px 30px;
  background: #f4f7fc;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;

  h2 {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    i { color: #8b5cf6; margin-right: 8px; }
  }
  p {
    margin: 0;
    color: #94a3b8;
    font-size: 13px;
  }
  .header-actions {
    display: flex;
    align-items: center;
    .el-button { font-weight: 600; }
  }
}

.cards-container {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; }
  p { color: #94a3b8; font-size: 15px; margin: 0; }
}

/* 提取结果卡片 */
.extraction-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 28px;
  margin-bottom: 18px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  border: 1px solid #edf2f9;
  transition: box-shadow 0.3s, transform 0.2s;

  &:hover {
    box-shadow: 0 8px 25px rgba(0,0,0,0.06);
    transform: translateY(-1px);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 14px;

  .card-id {
    font-weight: 800;
    color: #94a3b8;
    font-size: 13px;
  }
  .card-course {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
    i { color: #8b5cf6; margin-right: 4px; }
  }
}

.card-actions .el-button { font-weight: 500; }

.card-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.content-block {
  padding: 16px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;

  .block-label {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
    i { margin-right: 5px; }
  }

  .block-text {
    font-size: 13px;
    color: #475569;
    line-height: 1.7;
    max-height: 72px;
    overflow: hidden;
    transition: max-height 0.4s ease;

    &.expanded { max-height: 1000px; }
  }
}

.asr-block .block-label { color: #0ea5e9; }
.ai-block .block-label { color: #8b5cf6; }

/* 对话框美化 */
::v-deep .custom-dialog .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #edf2f9;
    padding: 18px 24px;
  }
  .el-dialog__body { padding: 24px; }
}

::v-deep .el-textarea__inner {
  border-radius: 8px;
  border-color: #e2e8f0;
  &:focus { border-color: #8b5cf6; box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1); }
}

::v-deep .el-input__inner {
  border-radius: 8px;
  border-color: #e2e8f0;
  &:focus { border-color: #8b5cf6; }
}
</style>
