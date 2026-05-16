<template>
  <div class="app-container message-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-chat-dot-round"></i> 站内消息管理</h2>
        <p>系统通知 · 消息推送 · 状态追踪</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['message:message:add']">发送消息</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" v-hasPermi="['message:message:remove']" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['message:message:export']">导出</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="queryParams.userId" placeholder="接收方ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="消息标题" prop="title">
            <el-input v-model="queryParams.title" placeholder="搜索标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="阅读状态" prop="isRead">
            <el-select v-model="queryParams.isRead" placeholder="全部" clearable style="width:120px;">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务ID" prop="relatedId">
            <el-input v-model="queryParams.relatedId" placeholder="关联ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div v-loading="loading" class="cards-container">
      <div v-if="messageList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-chat-line-square"></i>
        <p>暂无消息记录</p>
      </div>

      <div class="msg-card" v-for="item in messageList" :key="item.msgId" :class="{ unread: item.isRead === '0' }">
        <div class="msg-left">
          <el-checkbox :value="ids.includes(item.msgId)" @change="toggleSelect(item.msgId)"></el-checkbox>
          <div class="msg-icon" :class="item.isRead === '0' ? 'icon-unread' : 'icon-read'">
            <i :class="item.isRead === '0' ? 'el-icon-message' : 'el-icon-message-solid'"></i>
          </div>
        </div>
        <div class="msg-center">
          <div class="msg-title-row">
            <span class="msg-title">{{ item.title }}</span>
            <el-tag :type="item.isRead === '0' ? 'danger' : 'info'" size="mini" effect="dark">{{ item.isRead === '0' ? '未读' : '已读' }}</el-tag>
            <el-tag v-if="item.type" size="mini" effect="plain">{{ item.type }}</el-tag>
          </div>
          <div class="msg-content">{{ item.content || '无内容' }}</div>
          <div class="msg-meta">
            <span><i class="el-icon-user"></i> 用户 {{ item.userId }}</span>
            <span v-if="item.relatedId"><i class="el-icon-link"></i> 关联 #{{ item.relatedId }}</span>
            <span v-if="item.createTime"><i class="el-icon-time"></i> {{ item.createTime }}</span>
          </div>
        </div>
        <div class="msg-right">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['message:message:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)" v-hasPermi="['message:message:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入接收方用户ID" />
        </el-form-item>
        <el-form-item label="消息标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="消息内容">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="阅读状态" prop="isRead">
          <el-select v-model="form.isRead" placeholder="请选择">
            <el-option label="未读" value="0" />
            <el-option label="已读" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联业务ID" prop="relatedId">
          <el-input v-model="form.relatedId" placeholder="如课程ID或订单ID" />
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
import { listMessage, getMessage, delMessage, addMessage, updateMessage } from "@/api/message/message"

export default {
  name: "Message",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true,
      total: 0, messageList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, userId: null, title: null, content: null, type: null, isRead: null, relatedId: null },
      form: {},
      rules: {
        userId: [{ required: true, message: "接收方用户ID不能为空", trigger: "blur" }],
        title: [{ required: true, message: "消息标题不能为空", trigger: "blur" }],
        content: [{ required: true, message: "消息内容不能为空", trigger: "blur" }],
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listMessage(this.queryParams).then(response => { this.messageList = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { msgId: null, userId: null, title: null, content: null, type: null, isRead: null, relatedId: null, createTime: null, createBy: null, updateTime: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.msgId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "发送站内消息" },
    handleUpdate(row) {
      this.reset(); const msgId = row.msgId || this.ids
      getMessage(msgId).then(response => { this.form = response.data; this.open = true; this.title = "修改站内消息" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.msgId != null) { updateMessage(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() }) }
          else { addMessage(this.form).then(() => { this.$modal.msgSuccess("发送成功"); this.open = false; this.getList() }) }
        }
      })
    },
    handleDelete(row) {
      const msgIds = row.msgId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + msgIds + '"的消息？').then(function() { return delMessage(msgIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    handleExport() { this.download('message/message/export', { ...this.queryParams }, `message_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style lang="scss" scoped>
.message-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #0ea5e9; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #0ea5e9; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.msg-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 16px 22px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
  &.unread { border-left: 3px solid #0ea5e9; }
}

.msg-left { display: flex; align-items: center; gap: 12px; margin-right: 18px; flex-shrink: 0; }
.msg-icon {
  width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px;
  &.icon-unread { background: linear-gradient(135deg, #0ea5e9, #38bdf8); color: #fff; box-shadow: 0 3px 8px rgba(14,165,233,0.3); }
  &.icon-read { background: #f1f5f9; color: #94a3b8; }
}

.msg-center {
  flex: 1; min-width: 0;
  .msg-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
  .msg-title { font-size: 15px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .msg-content { font-size: 13px; color: #64748b; line-height: 1.5; max-height: 40px; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
  .msg-meta { display: flex; gap: 16px; font-size: 12px; color: #94a3b8; i { margin-right: 3px; } }
}

.msg-right { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; margin-left: 16px; .el-button { font-weight: 500; } }

::v-deep .custom-dialog .el-dialog { border-radius: 16px; overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; padding: 18px 24px; }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #0ea5e9; } }
</style>
