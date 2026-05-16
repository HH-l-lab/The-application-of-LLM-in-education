<template>
  <div class="app-container my-msg-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-bell"></i> 我的消息</h2>
        <p>收件箱 · 通知提醒 · 阅读状态</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-check" size="small" round @click="markAllRead" :disabled="unreadCount === 0">全部已读</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="queryParams.title" placeholder="搜索标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="isRead">
            <el-select v-model="queryParams.isRead" placeholder="全部" clearable style="width:120px;">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div class="stats-bar">
      <span class="stat-item">共 <b>{{ total }}</b> 条消息</span>
      <span class="stat-item unread-stat" v-if="unreadCount > 0"><i class="el-icon-message"></i> <b>{{ unreadCount }}</b> 条未读</span>
    </div>

    <div v-loading="loading" class="cards-container">
      <div v-if="messageList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-chat-line-square"></i>
        <p>暂无消息</p>
      </div>

      <div class="msg-card" v-for="item in messageList" :key="item.msgId" :class="{ unread: item.isRead === '0' }" @click="handleRead(item)">
        <div class="msg-left">
          <el-checkbox :value="ids.includes(item.msgId)" @change="toggleSelect(item.msgId)" @click.native.stop></el-checkbox>
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
            <span v-if="item.relatedId"><i class="el-icon-link"></i> 关联 #{{ item.relatedId }}</span>
            <span v-if="item.createTime"><i class="el-icon-time"></i> {{ item.createTime }}</span>
          </div>
        </div>
        <div class="msg-right">
          <el-button v-if="item.isRead === '0'" size="mini" type="text" icon="el-icon-view" @click.stop="handleRead(item)">标记已读</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click.stop="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 消息详情弹窗 -->
    <el-dialog title="消息详情" :visible.sync="detailOpen" width="600px" append-to-body class="custom-dialog">
      <div v-if="detailMsg">
        <h3 style="margin:0 0 12px;">{{ detailMsg.title }}</h3>
        <div v-html="detailMsg.content" style="color:#475569; line-height:1.8; font-size:14px;"></div>
        <div style="margin-top:16px; font-size:12px; color:#94a3b8;">
          <span v-if="detailMsg.createTime"><i class="el-icon-time"></i> {{ detailMsg.createTime }}</span>
          <span v-if="detailMsg.relatedId" style="margin-left:16px;"><i class="el-icon-link"></i> 关联ID: {{ detailMsg.relatedId }}</span>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMyMessage, markMessageRead, delMessage } from "@/api/message/message"

export default {
  name: "MyMessage",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: false,
      total: 0, unreadCount: 0, messageList: [],
      queryParams: { pageNum: 1, pageSize: 10, title: null, isRead: null },
      detailOpen: false, detailMsg: null,
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listMyMessage(this.queryParams).then(response => {
        this.messageList = response.rows; this.total = response.total; this.loading = false
        this.unreadCount = this.messageList.filter(m => m.isRead === '0').length
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleRead(item) {
      if (item.isRead === '0') {
        markMessageRead(item.msgId).then(() => {
          item.isRead = '1'
          this.unreadCount = this.messageList.filter(m => m.isRead === '0').length
        })
      }
      this.detailMsg = item
      this.detailOpen = true
    },
    markAllRead() {
      const unreadList = this.messageList.filter(m => m.isRead === '0')
      const promises = unreadList.map(m => markMessageRead(m.msgId))
      Promise.all(promises).then(() => {
        unreadList.forEach(m => { m.isRead = '1' })
        this.unreadCount = 0
        this.$modal.msgSuccess("已全部标记为已读")
      })
    },
    handleDelete(row) {
      const msgIds = row.msgId || this.ids
      this.$modal.confirm('是否确认删除该消息？').then(function() { return delMessage(msgIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
  }
}
</script>

<style lang="scss" scoped>
.my-msg-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #f59e0b; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #f59e0b; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.stats-bar {
  display: flex; gap: 20px; margin-bottom: 16px; padding: 0 4px;
  .stat-item { font-size: 13px; color: #64748b; b { color: #1e293b; font-size: 15px; } }
  .unread-stat { color: #f59e0b; i { margin-right: 3px; } b { color: #f59e0b; } }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.msg-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 16px 22px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s; cursor: pointer;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
  &.unread { border-left: 3px solid #f59e0b; background: #fffbeb; }
}

.msg-left { display: flex; align-items: center; gap: 12px; margin-right: 18px; flex-shrink: 0; }
.msg-icon {
  width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px;
  &.icon-unread { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #fff; box-shadow: 0 3px 8px rgba(245,158,11,0.3); }
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
</style>
