<template>
  <div class="app-container form-builder-page">
    <!-- 顶部说明区 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-s-operation"></i> 实验数据采集表构建器</h2>
        <p class="sub-text">拖拽排序、快速添加字段，一键生成 JSON 配置</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="addField" round>添加字段</el-button>
        <el-button type="success" icon="el-icon-document-checked" @click="generateJson" round :disabled="fields.length === 0">生成 JSON</el-button>
      </div>
    </div>

    <!-- 字段列表（可拖拽排序） -->
    <div class="fields-area" v-if="fields.length > 0">
      <draggable v-model="fields" handle=".drag-handle" animation="300" ghost-class="ghost-card" class="fields-grid">
        <transition-group type="transition" name="flip-list">
          <div class="field-card" v-for="(field, index) in fields" :key="field.id">
            <div class="card-top">
              <div class="drag-handle"><i class="el-icon-rank"></i></div>
              <div class="field-index">{{ index + 1 }}</div>
              <el-button class="delete-btn" type="text" icon="el-icon-close" @click="removeField(index)"></el-button>
            </div>
            <div class="field-inputs">
              <el-input v-model="field.label" placeholder="如: 小车质量" size="small">
                <template slot="prepend">名称</template>
              </el-input>
              <el-input v-model="field.unit" placeholder="如: kg" size="small">
                <template slot="prepend">单位</template>
              </el-input>
              <el-input v-model="field.placeholder" placeholder="如: 请输入0.5" size="small">
                <template slot="prepend">提示</template>
              </el-input>
            </div>
          </div>
        </transition-group>
      </draggable>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <i class="el-icon-document-add"></i>
      <p>还没有字段，点击上方「添加字段」开始构建</p>
    </div>

    <!-- 预览区域 -->
    <div class="preview-section" v-if="fields.length > 0">
      <h3><i class="el-icon-view"></i> 实时预览</h3>
      <div class="preview-form">
        <div class="preview-item" v-for="field in fields" :key="'p-' + field.id">
          <label>{{ field.label || '未命名' }} <span class="unit-tag" v-if="field.unit">({{ field.unit }})</span></label>
          <el-input :placeholder="field.placeholder || '请输入'" size="small" disabled></el-input>
        </div>
      </div>
    </div>

    <!-- JSON 输出弹窗 -->
    <el-dialog title="采集器 JSON 配置" :visible.sync="jsonVisible" width="600px" center>
      <div class="json-block">
        <pre>{{ jsonOutput }}</pre>
      </div>
      <span slot="footer">
        <el-button @click="jsonVisible = false">关闭</el-button>
        <el-button type="primary" @click="copyJson">复制 JSON</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable'

export default {
  name: 'FormBuilder',
  components: { draggable },
  data() {
    return {
      idCounter: 0,
      fields: [],
      jsonVisible: false,
      jsonOutput: ''
    }
  },
  methods: {
    addField() {
      this.fields.push({
        id: ++this.idCounter,
        label: '',
        unit: '',
        placeholder: ''
      })
    },
    removeField(index) {
      this.fields.splice(index, 1)
    },
    generateJson() {
      const result = this.fields.map((f, i) => ({
        key: 'field_' + (i + 1),
        label: f.label || '未命名',
        unit: f.unit || '',
        placeholder: f.placeholder || ''
      }))
      // 每条数据一行，紧凑好看
      const lines = result.map(item => '  ' + JSON.stringify(item))
      this.jsonOutput = '[\n' + lines.join(',\n') + '\n]'
      this.jsonVisible = true
    },
    copyJson() {
      const textarea = document.createElement('textarea')
      textarea.value = this.jsonOutput
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('JSON 已复制到剪贴板！')
    }
  }
}
</script>

<style lang="scss" scoped>
.form-builder-page {
  padding: 25px 30px;
  background: #f4f7fc;
  min-height: calc(100vh - 84px);
}

/* 顶部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;

  h2 {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    i { color: #3b82f6; margin-right: 8px; }
  }
  .sub-text {
    margin: 0;
    color: #94a3b8;
    font-size: 14px;
  }
  .header-actions {
    display: flex;
    gap: 12px;
    .el-button { font-weight: 600; }
  }
}

/* 字段卡片 */
.fields-area {
  margin-bottom: 30px;
}

.fields-grid {
  > span { /* transition-group 渲染为 span */
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }
}

.field-card {
  background: #fff;
  padding: 16px;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #edf2f9;
  transition: box-shadow 0.3s, transform 0.2s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0,0,0,0.06);
    transform: translateY(-1px);
  }
}

.card-top {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.drag-handle {
  cursor: grab;
  color: #cbd5e1;
  font-size: 18px;
  padding: 2px 4px;
  &:hover { color: #3b82f6; }
}

.field-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  margin-left: 4px;
}

.field-inputs {
  display: flex;
  flex-direction: column;
  gap: 8px;

  ::v-deep .el-input-group__prepend {
    background: #f8fafc;
    border-color: #e2e8f0;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
    padding: 0 10px;
    min-width: 36px;
    text-align: center;
  }
  ::v-deep .el-input__inner {
    border-color: #e2e8f0;
    border-radius: 0 6px 6px 0;
    &:focus { border-color: #3b82f6; }
  }
}

.delete-btn {
  font-size: 18px;
  color: #cbd5e1;
  margin-left: auto;
  &:hover { color: #ef4444; }
}

/* 拖拽动效 */
.ghost-card {
  opacity: 0.4;
  border: 2px dashed #3b82f6 !important;
}
.flip-list-move {
  transition: transform 0.3s;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  border: 2px dashed #e2e8f0;
  margin-bottom: 30px;

  i {
    font-size: 56px;
    color: #cbd5e1;
    margin-bottom: 16px;
    display: block;
  }
  p {
    color: #94a3b8;
    font-size: 15px;
    margin: 0;
  }
}

/* 实时预览 */
.preview-section {
  background: #fff;
  border-radius: 16px;
  padding: 25px 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);

  h3 {
    margin: 0 0 20px;
    font-size: 16px;
    color: #1e293b;
    font-weight: 600;
    i { color: #3b82f6; margin-right: 6px; }
  }
}

.preview-form {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.preview-item {
  label {
    display: block;
    font-size: 13px;
    color: #475569;
    font-weight: 600;
    margin-bottom: 6px;
  }
  .unit-tag {
    color: #94a3b8;
    font-weight: 400;
  }
}

/* JSON弹窗 */
.json-block {
  background: #1e293b;
  border-radius: 10px;
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;

  pre {
    margin: 0;
    color: #a5f3fc;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 14px;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; }
  .el-dialog__body { padding: 20px 25px; }
}
</style>
