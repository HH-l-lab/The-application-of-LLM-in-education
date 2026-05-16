<template>
  <div class="app-container order-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-s-finance"></i> 课程交易订单</h2>
        <p>订单管理 · 支付追踪 · 交易明细</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['order:order:add']">新增订单</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" v-hasPermi="['order:order:remove']" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['order:order:export']">导出</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="流水号" prop="orderNo">
            <el-input v-model="queryParams.orderNo" placeholder="订单流水号" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="购买者ID" prop="buyerId">
            <el-input v-model="queryParams.buyerId" placeholder="学生ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="课程ID" prop="courseId">
            <el-input v-model="queryParams.courseId" placeholder="课程ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="金额" prop="amount">
            <el-input v-model="queryParams.amount" placeholder="金额" clearable style="width:100px;" @keyup.enter.native="handleQuery" />
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

    <div v-loading="loading" class="cards-container">
      <div v-if="orderList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-shopping-bag-2"></i>
        <p>暂无订单记录</p>
      </div>

      <div class="order-card" v-for="item in orderList" :key="item.orderId">
        <div class="card-left">
          <el-checkbox :value="ids.includes(item.orderId)" @change="toggleSelect(item.orderId)"></el-checkbox>
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
            <span><i class="el-icon-user"></i> 学生 {{ item.buyerId }}</span>
            <span><i class="el-icon-video-camera"></i> 课程 {{ item.courseId }}</span>
            <span v-if="item.payTime"><i class="el-icon-time"></i> {{ parseTime(item.payTime, '{y}-{m}-{d}') }}</span>
          </div>
        </div>
        <div class="card-amount">
          <span class="amount-symbol">¥</span>
          <span class="amount-value">{{ item.amount || '0.00' }}</span>
        </div>
        <div class="card-right">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['order:order:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)" v-hasPermi="['order:order:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="订单流水号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单流水号" />
        </el-form-item>
        <el-form-item label="购买者ID" prop="buyerId">
          <el-input v-model="form.buyerId" placeholder="学生用户ID" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseId">
          <el-input v-model="form.courseId" placeholder="购买的课程ID" />
        </el-form-item>
        <el-form-item label="支付金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker clearable v-model="form.payTime" type="date" value-format="yyyy-MM-dd" placeholder="选择支付时间" style="width:100%;" />
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
import { listOrder, getOrder, delOrder, addOrder, updateOrder } from "@/api/order/order"

export default {
  name: "Order",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true,
      total: 0, orderList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, orderNo: null, buyerId: null, courseId: null, amount: null, payStatus: null, payTime: null },
      form: {},
      rules: {
        orderNo: [{ required: true, message: "订单流水号不能为空", trigger: "blur" }],
        buyerId: [{ required: true, message: "购买者ID不能为空", trigger: "blur" }],
        courseId: [{ required: true, message: "课程ID不能为空", trigger: "blur" }],
        amount: [{ required: true, message: "支付金额不能为空", trigger: "blur" }],
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => { this.orderList = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { orderId: null, orderNo: null, buyerId: null, courseId: null, amount: null, payStatus: null, createTime: null, payTime: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.orderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增交易订单" },
    handleUpdate(row) {
      this.reset(); const orderId = row.orderId || this.ids
      getOrder(orderId).then(response => { this.form = response.data; this.open = true; this.title = "修改交易订单" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.orderId != null) { updateOrder(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() }) }
          else { addOrder(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() }) }
        }
      })
    },
    handleDelete(row) {
      const orderIds = row.orderId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + orderIds + '"的订单？').then(function() { return delOrder(orderIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    handleExport() { this.download('order/order/export', { ...this.queryParams }, `order_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style lang="scss" scoped>
.order-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #f97316; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #f97316; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.order-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 18px 22px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
}

.card-left { display: flex; align-items: center; gap: 12px; margin-right: 18px; flex-shrink: 0; }
.order-icon {
  width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #f97316, #fb923c); color: #fff; font-size: 20px;
  box-shadow: 0 4px 10px rgba(249,115,22,0.25);
}

.card-center {
  flex: 1; min-width: 0;
  .order-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
  .order-no { font-size: 15px; font-weight: 700; color: #1e293b; font-family: 'Consolas', monospace; letter-spacing: 0.5px; }
  .order-meta { display: flex; gap: 18px; font-size: 12px; color: #94a3b8; i { margin-right: 3px; } }
}

.card-amount {
  display: flex; align-items: baseline; margin: 0 24px; flex-shrink: 0;
  .amount-symbol { font-size: 14px; color: #f97316; font-weight: 600; margin-right: 2px; }
  .amount-value { font-size: 22px; font-weight: 800; color: #f97316; font-family: 'DIN', 'Consolas', monospace; }
}

.card-right { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; margin-left: 8px; .el-button { font-weight: 500; } }

::v-deep .custom-dialog .el-dialog { border-radius: 16px; overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; padding: 18px 24px; }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #f97316; box-shadow: 0 0 0 3px rgba(249,115,22,0.1); } }
</style>
