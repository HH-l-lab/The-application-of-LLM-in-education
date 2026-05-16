<template>
  <div class="app-container my-order-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-shopping-bag-1"></i> 我的订单</h2>
        <p>购课记录 · 支付凭证 · 交易明细</p>
      </div>
      <div class="header-actions">
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="流水号" prop="orderNo">
            <el-input v-model="queryParams.orderNo" placeholder="订单流水号" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="支付时间" prop="payTime">
            <el-date-picker clearable v-model="queryParams.payTime" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <div class="stats-bar">
      <span class="stat-item">共 <b>{{ total }}</b> 笔订单</span>
      <span class="stat-item total-amount">累计消费 <b>¥{{ totalAmount }}</b></span>
    </div>

    <div v-loading="loading" class="cards-container">
      <div v-if="orderList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-shopping-bag-2"></i>
        <p>暂无订单记录</p>
      </div>

      <div class="order-card" v-for="item in orderList" :key="item.orderId">
        <div class="card-left">
          <div class="order-icon">
            <i class="el-icon-tickets"></i>
          </div>
        </div>
        <div class="card-center">
          <div class="order-title-row">
            <span class="order-no">{{ item.orderNo || '—' }}</span>
            <el-tag :type="item.payStatus === '1' ? 'success' : item.payStatus === '0' ? 'warning' : 'danger'" size="mini" effect="dark">
              {{ item.payStatus === '1' ? '已支付' : item.payStatus === '0' ? '待支付' : '已取消' }}
            </el-tag>
          </div>
          <div class="order-meta">
            <span><i class="el-icon-video-camera"></i> 课程 #{{ item.courseId }}</span>
            <span v-if="item.payTime"><i class="el-icon-time"></i> {{ parseTime(item.payTime, '{y}-{m}-{d}') }}</span>
          </div>
        </div>
        <div class="card-amount">
          <span class="amount-symbol">¥</span>
          <span class="amount-value">{{ item.amount || '0.00' }}</span>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listMyOrder } from "@/api/order/order"

export default {
  name: "MyOrder",
  data() {
    return {
      loading: true, showSearch: false,
      total: 0, totalAmount: '0.00', orderList: [],
      queryParams: { pageNum: 1, pageSize: 10, orderNo: null, payTime: null },
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listMyOrder(this.queryParams).then(response => {
        this.orderList = response.rows; this.total = response.total; this.loading = false
        const sum = this.orderList.reduce((acc, o) => acc + (parseFloat(o.amount) || 0), 0)
        this.totalAmount = sum.toFixed(2)
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
  }
}
</script>

<style lang="scss" scoped>
.my-order-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

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

.stats-bar {
  display: flex; gap: 24px; margin-bottom: 16px; padding: 0 4px;
  .stat-item { font-size: 13px; color: #64748b; b { color: #1e293b; font-size: 15px; } }
  .total-amount { color: #10b981; b { color: #10b981; } }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.order-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 18px 22px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
}

.card-left { margin-right: 18px; flex-shrink: 0; }
.order-icon {
  width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #10b981, #34d399); color: #fff; font-size: 20px;
  box-shadow: 0 4px 10px rgba(16,185,129,0.25);
}

.card-center {
  flex: 1; min-width: 0;
  .order-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
  .order-no { font-size: 15px; font-weight: 700; color: #1e293b; font-family: 'Consolas', monospace; letter-spacing: 0.5px; }
  .order-meta { display: flex; gap: 18px; font-size: 12px; color: #94a3b8; i { margin-right: 3px; } }
}

.card-amount {
  display: flex; align-items: baseline; margin: 0 8px; flex-shrink: 0;
  .amount-symbol { font-size: 14px; color: #10b981; font-weight: 600; margin-right: 2px; }
  .amount-value { font-size: 22px; font-weight: 800; color: #10b981; font-family: 'DIN', 'Consolas', monospace; }
}
</style>
