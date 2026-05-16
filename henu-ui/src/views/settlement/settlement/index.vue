<template>
  <div class="app-container settle-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-coin"></i> 创作者收益结算流水</h2>
        <p>收益追踪 · 分润明细 · 结算管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['settlement:settlement:add']">新增</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" v-hasPermi="['settlement:settlement:remove']" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['settlement:settlement:export']">导出</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
          <el-form-item label="创作者ID" prop="creatorId">
            <el-input v-model="queryParams.creatorId" placeholder="创作者ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="订单ID" prop="orderId">
            <el-input v-model="queryParams.orderId" placeholder="来源订单ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="结算状态" prop="settlementStatus">
            <el-select v-model="queryParams.settlementStatus" placeholder="全部" clearable style="width:120px;">
              <el-option label="待结算" value="0" />
              <el-option label="已结算" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="结算时间" prop="settlementTime">
            <el-date-picker clearable v-model="queryParams.settlementTime" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div v-loading="loading" class="cards-container">
      <div v-if="settlementList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-coin"></i>
        <p>暂无结算流水记录</p>
      </div>

      <div class="settle-card" v-for="item in settlementList" :key="item.settlementId" :class="{ pending: item.settlementStatus === '0' }">
        <div class="card-left">
          <el-checkbox :value="ids.includes(item.settlementId)" @change="toggleSelect(item.settlementId)"></el-checkbox>
          <div class="settle-icon" :class="item.settlementStatus === '0' ? 'icon-pending' : 'icon-done'">
            <i :class="item.settlementStatus === '0' ? 'el-icon-time' : 'el-icon-circle-check'"></i>
          </div>
        </div>
        <div class="card-center">
          <div class="settle-title-row">
            <span class="settle-id">#{{ item.settlementId }}</span>
            <el-tag :type="item.settlementStatus === '0' ? 'warning' : 'success'" size="mini" effect="dark">
              {{ item.settlementStatus === '0' ? '待结算' : '已结算' }}
            </el-tag>
          </div>
          <div class="settle-meta">
            <span><i class="el-icon-user"></i> 创作者 {{ item.creatorId }}</span>
            <span><i class="el-icon-tickets"></i> 订单 #{{ item.orderId }}</span>
            <span v-if="item.createTime"><i class="el-icon-time"></i> {{ item.createTime }}</span>
            <span v-if="item.settlementTime"><i class="el-icon-circle-check"></i> 结算于 {{ parseTime(item.settlementTime, '{y}-{m}-{d}') }}</span>
          </div>
        </div>
        <div class="card-amount">
          <span class="amount-plus">+</span>
          <span class="amount-symbol">¥</span>
          <span class="amount-value">{{ item.profitAmount }}</span>
        </div>
        <div class="card-actions">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['settlement:settlement:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)" v-hasPermi="['settlement:settlement:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="520px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="创作者ID" prop="creatorId">
          <el-input v-model="form.creatorId" placeholder="请输入创作者ID" />
        </el-form-item>
        <el-form-item label="来源订单ID" prop="orderId">
          <el-input v-model="form.orderId" placeholder="请输入来源订单ID" />
        </el-form-item>
        <el-form-item label="分润金额" prop="profitAmount">
          <el-input v-model="form.profitAmount" placeholder="请输入分润金额" />
        </el-form-item>
        <el-form-item label="结算状态" prop="settlementStatus">
          <el-select v-model="form.settlementStatus" placeholder="请选择">
            <el-option label="待结算" value="0" />
            <el-option label="已结算" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算时间" prop="settlementTime">
          <el-date-picker clearable v-model="form.settlementTime" type="date" value-format="yyyy-MM-dd" placeholder="请选择结算时间" />
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
import { listSettlement, getSettlement, delSettlement, addSettlement, updateSettlement } from "@/api/settlement/settlement"

export default {
  name: "Settlement",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true,
      total: 0, settlementList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, creatorId: null, orderId: null, profitAmount: null, settlementStatus: null, settlementTime: null },
      form: {},
      rules: {
        creatorId: [{ required: true, message: "创作者ID不能为空", trigger: "blur" }],
        orderId: [{ required: true, message: "来源订单ID不能为空", trigger: "blur" }],
        profitAmount: [{ required: true, message: "分润金额不能为空", trigger: "blur" }],
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listSettlement(this.queryParams).then(response => { this.settlementList = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { settlementId: null, creatorId: null, orderId: null, profitAmount: null, settlementStatus: null, createTime: null, settlementTime: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleAdd() { this.reset(); this.open = true; this.title = "新增结算流水" },
    handleUpdate(row) {
      this.reset(); const settlementId = row.settlementId || this.ids
      getSettlement(settlementId).then(response => { this.form = response.data; this.open = true; this.title = "修改结算流水" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.settlementId != null) { updateSettlement(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() }) }
          else { addSettlement(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() }) }
        }
      })
    },
    handleDelete(row) {
      const settlementIds = row.settlementId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + settlementIds + '"的流水？').then(function() { return delSettlement(settlementIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    handleExport() { this.download('settlement/settlement/export', { ...this.queryParams }, `settlement_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style lang="scss" scoped>
.settle-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

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

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.settle-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 18px 22px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
  &.pending { border-left: 3px solid #f59e0b; }
}

.card-left { display: flex; align-items: center; gap: 12px; margin-right: 16px; flex-shrink: 0; }
.settle-icon {
  width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px;
  &.icon-pending { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #fff; box-shadow: 0 3px 8px rgba(245,158,11,0.3); }
  &.icon-done { background: linear-gradient(135deg, #10b981, #34d399); color: #fff; box-shadow: 0 3px 8px rgba(16,185,129,0.3); }
}

.card-center {
  flex: 1; min-width: 0;
  .settle-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 5px; }
  .settle-id { font-weight: 800; color: #475569; font-size: 14px; }
  .settle-meta { display: flex; gap: 16px; font-size: 12px; color: #94a3b8; flex-wrap: wrap; i { margin-right: 3px; } }
}

.card-amount {
  display: flex; align-items: baseline; flex-shrink: 0; margin: 0 16px;
  .amount-plus { font-size: 14px; color: #10b981; font-weight: 700; margin-right: 1px; }
  .amount-symbol { font-size: 14px; color: #10b981; font-weight: 600; margin-right: 2px; }
  .amount-value { font-size: 22px; font-weight: 800; color: #10b981; font-family: 'DIN', 'Consolas', monospace; }
}

.card-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; .el-button { font-weight: 500; } }

::v-deep .custom-dialog .el-dialog { border-radius: 16px; overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; padding: 18px 24px; }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner, ::v-deep .custom-dialog .el-textarea__inner {
  border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.1); }
}
</style>
