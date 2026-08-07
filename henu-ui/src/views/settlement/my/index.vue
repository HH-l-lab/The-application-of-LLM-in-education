<template>
  <div class="app-container my-revenue-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-coin"></i> 我的收益</h2>
        <p>结算流水 · 收益明细 · 提现管理</p>
      </div>
      <div class="header-actions">
        <el-button type="success" icon="el-icon-wallet" size="small" round @click="handleSettle" :disabled="pendingRevenue <= 0">一键结算 (¥{{ pendingRevenue.toFixed(2) }})</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="8">
        <div class="summary-card card-total">
          <div class="s-icon"><i class="el-icon-data-line"></i></div>
          <div class="s-info">
            <div class="s-label">累计总收益</div>
            <div class="s-value">¥{{ totalRevenue.toFixed(2) }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card card-pending">
          <div class="s-icon"><i class="el-icon-time"></i></div>
          <div class="s-info">
            <div class="s-label">待结算</div>
            <div class="s-value">¥{{ pendingRevenue.toFixed(2) }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card card-settled">
          <div class="s-icon"><i class="el-icon-circle-check"></i></div>
          <div class="s-info">
            <div class="s-label">已结算</div>
            <div class="s-value">¥{{ settledRevenue.toFixed(2) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="状态" prop="settlementStatus">
            <el-select v-model="queryParams.settlementStatus" placeholder="全部" clearable style="width:120px;">
              <el-option label="待结算" value="0" />
              <el-option label="已结算" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div v-loading="loading" class="cards-container">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <i class="el-icon-coin"></i>
        <p>暂无收益记录，发布优质付费课程即可开始赚取收益</p>
      </div>

      <div class="settle-card" v-for="item in list" :key="item.settlementId" :class="{ pending: item.settlementStatus === '0' }">
        <div class="card-left">
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
            <span class="order-ref"><i class="el-icon-tickets"></i> 订单 #{{ item.orderId }}</span>
          </div>
          <div class="settle-meta">
            <span v-if="item.createTime"><i class="el-icon-time"></i> 产生时间 {{ item.createTime }}</span>
            <span v-if="item.settlementTime"><i class="el-icon-circle-check"></i> 结算时间 {{ parseTime(item.settlementTime, '{y}-{m}-{d}') }}</span>
          </div>
        </div>
        <div class="card-amount">
          <span class="amount-plus">+</span>
          <span class="amount-symbol">¥</span>
          <span class="amount-value">{{ item.profitAmount }}</span>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listMySettlement, settleAll } from "@/api/settlement/settlement"
import { getCreatorStats } from "@/api/dashboard"

export default {
  name: "MyRevenue",
  data() {
    return {
      loading: true, showSearch: false,
      total: 0, list: [],
      totalRevenue: 0, pendingRevenue: 0, settledRevenue: 0,
      queryParams: { pageNum: 1, pageSize: 10, settlementStatus: null },
    }
  },
  created() { this.getList(); this.getStats() },
  methods: {
    getList() {
      this.loading = true
      listMySettlement(this.queryParams).then(res => {
        this.list = res.rows; this.total = res.total; this.loading = false
      })
    },
    getStats() {
      getCreatorStats().then(res => {
        const d = res.data
        this.totalRevenue = d.totalRevenue || 0
        this.pendingRevenue = d.pendingRevenue || 0
        this.settledRevenue = d.settledRevenue || 0
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSettle() {
      this.$modal.confirm('确认将所有待结算收益一键结算？结算后资金将标记为已到账。').then(() => {
        return settleAll()
      }).then(res => {
        this.$modal.msgSuccess(res.msg || '结算成功')
        this.getList()
        this.getStats()
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.my-revenue-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #f59e0b; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.summary-row { margin-bottom: 20px; }
.summary-card {
  display: flex; align-items: center; padding: 20px; border-radius: 16px; background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(0,0,0,0.06); }
  .s-icon { width: 46px; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-right: 16px; flex-shrink: 0; }
  .s-label { font-size: 13px; color: #94a3b8; font-weight: 600; margin-bottom: 3px; }
  .s-value { font-size: 22px; font-weight: 800; color: #1e293b; font-family: 'DIN', 'Consolas', monospace; }
}
.card-total .s-icon { background: rgba(245,158,11,0.1); color: #f59e0b; }
.card-pending .s-icon { background: rgba(99,102,241,0.1); color: #6366f1; }
.card-settled .s-icon { background: rgba(16,185,129,0.1); color: #10b981; }
.card-settled .s-value { color: #10b981; }

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; }
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
.card-left { margin-right: 16px; flex-shrink: 0; }
.settle-icon {
  width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px;
  &.icon-pending { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #fff; box-shadow: 0 3px 8px rgba(245,158,11,0.3); }
  &.icon-done { background: linear-gradient(135deg, #10b981, #34d399); color: #fff; box-shadow: 0 3px 8px rgba(16,185,129,0.3); }
}
.card-center {
  flex: 1; min-width: 0;
  .settle-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 5px; }
  .settle-id { font-weight: 800; color: #475569; font-size: 13px; }
  .order-ref { font-size: 12px; color: #94a3b8; i { margin-right: 2px; } }
  .settle-meta { display: flex; gap: 18px; font-size: 12px; color: #94a3b8; i { margin-right: 3px; } }
}
.card-amount {
  display: flex; align-items: baseline; flex-shrink: 0; margin: 0 8px;
  .amount-plus { font-size: 14px; color: #10b981; font-weight: 700; margin-right: 1px; }
  .amount-symbol { font-size: 14px; color: #10b981; font-weight: 600; margin-right: 2px; }
  .amount-value { font-size: 22px; font-weight: 800; color: #10b981; font-family: 'DIN', 'Consolas', monospace; }
}

@media (max-width: 992px) {
  .my-revenue-page { padding: 16px; }
  .summary-row .el-col { width: 100% !important; }
  .summary-card { padding: 16px; }
  .summary-card .s-value { font-size: 20px; }
  .settle-card { flex-wrap: wrap; padding: 14px 16px; }
  .filter-card { padding: 16px; }
}
</style>
