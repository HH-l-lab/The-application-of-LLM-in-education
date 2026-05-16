<template>
  <div class="app-container creator-dash">
    <div class="page-header">
      <h2><i class="el-icon-data-board"></i> 创作者中心看板</h2>
      <p>实时数据 · 收益追踪 · 内容分析</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="dash-card card-play">
          <div class="card-icon"><i class="el-icon-video-play"></i></div>
          <div class="card-info">
            <div class="card-label">总播放量</div>
            <div class="card-value">{{ totalPlayCount.toLocaleString() }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dash-card card-course">
          <div class="card-icon"><i class="el-icon-folder-opened"></i></div>
          <div class="card-info">
            <div class="card-label">已上传课程</div>
            <div class="card-value">{{ courseCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dash-card card-order">
          <div class="card-icon"><i class="el-icon-s-order"></i></div>
          <div class="card-info">
            <div class="card-label">订单数</div>
            <div class="card-value">{{ orderCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dash-card card-revenue">
          <div class="card-icon"><i class="el-icon-money"></i></div>
          <div class="card-info">
            <div class="card-label">累计收益</div>
            <div class="card-value revenue">¥{{ totalRevenue.toFixed(2) }}</div>
            <div class="card-sub">
              <span class="pending-tag">待结算 ¥{{ pendingRevenue.toFixed(2) }}</span>
              <span class="settled-tag">已结算 ¥{{ settledRevenue.toFixed(2) }}</span>
            </div>
          </div>
          <el-button type="warning" size="mini" round class="settle-btn" @click="$router.push('/settlement/my')" v-if="pendingRevenue > 0">去结算</el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 管理员专属：分润比例配置 -->
    <div class="share-rate-card" v-if="isAdmin">
      <div class="share-rate-header">
        <i class="el-icon-setting"></i>
        <span>全局分润比例配置</span>
        <el-tag size="mini" type="danger" effect="plain" style="margin-left:10px">仅管理员可见</el-tag>
      </div>
      <div class="share-rate-body">
        <div class="rate-display">
          <div class="rate-number">{{ shareRate }}<span class="rate-unit">%</span></div>
          <div class="rate-desc">创作者每笔订单收入占比</div>
        </div>
        <div class="rate-control">
          <el-slider v-model="shareRate" :min="0" :max="100" :step="1" show-input :show-input-controls="true" style="flex:1" />
        </div>
        <el-button type="primary" size="small" round icon="el-icon-check" @click="saveShareRate" :loading="savingRate" style="margin-top: 12px;">保存修改</el-button>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span><i class="el-icon-pie-chart"></i> 课程播放量排行</span>
          </div>
          <div ref="playChart" style="width: 100%; height: 360px;"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getCreatorStats } from '@/api/dashboard';
import { listCourse } from '@/api/creator/course';
import request from '@/utils/request';

export default {
  name: 'CreatorDashboard',
  data() {
    return {
      chart: null,
      totalPlayCount: 0,
      courseCount: 0,
      orderCount: 0,
      totalRevenue: 0,
      pendingRevenue: 0,
      settledRevenue: 0,
      isAdmin: false,
      shareRate: 50,
      savingRate: false,
    };
  },
  created() {
    this.fetchStats();
    this.fetchCourseChart();
    this.checkAdmin();
  },
  beforeDestroy() {
    if (this.chart) this.chart.dispose();
  },
  methods: {
    fetchStats() {
      getCreatorStats().then(res => {
        const d = res.data;
        this.totalPlayCount = d.totalPlayCount || 0;
        this.courseCount = d.courseCount || 0;
        this.orderCount = d.orderCount || 0;
        this.totalRevenue = d.totalRevenue || 0;
        this.pendingRevenue = d.pendingRevenue || 0;
        this.settledRevenue = d.settledRevenue || 0;
      });
    },
    fetchCourseChart() {
      listCourse({ pageNum: 1, pageSize: 20 }).then(res => {
        const rows = (res.rows || []).sort((a, b) => (b.playCount || 0) - (a.playCount || 0)).slice(0, 10);
        this.$nextTick(() => {
          this.initChart(rows);
        });
      });
    },
    initChart(rows) {
      const chartDom = this.$refs.playChart;
      if (!chartDom) return;
      this.chart = echarts.init(chartDom);
      const names = rows.map(r => r.courseTitle || '未命名');
      const values = rows.map(r => r.playCount || 0);
      this.chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '5%', bottom: '3%', containLabel: true },
        xAxis: { type: 'value', name: '播放量' },
        yAxis: { type: 'category', data: names.reverse(), axisLabel: { width: 120, overflow: 'truncate' } },
        series: [{
          type: 'bar',
          data: values.reverse(),
          barWidth: 22,
          itemStyle: {
            borderRadius: [0, 8, 8, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#6366f1' },
              { offset: 1, color: '#a78bfa' }
            ])
          }
        }]
      });
    },
    checkAdmin() {
      const roles = this.$store.state.user.roles || [];
      this.isAdmin = roles.includes('admin') || roles.includes('ladmin');
      if (this.isAdmin) {
        this.fetchShareRate();
      }
    },
    fetchShareRate() {
      request({ url: '/dashboard/shareRate', method: 'get' }).then(res => {
        if (res.code === 200) this.shareRate = res.data;
      });
    },
    saveShareRate() {
      this.savingRate = true;
      request({ url: '/dashboard/shareRate', method: 'put', data: { shareRate: this.shareRate } }).then(res => {
        this.savingRate = false;
        if (res.code === 200) {
          this.$message.success('分润比例已更新为 ' + this.shareRate + '%');
        } else {
          this.$message.error(res.msg || '更新失败');
        }
      }).catch(() => { this.savingRate = false; });
    }
  }
};
</script>

<style lang="scss" scoped>
.creator-dash { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  margin-bottom: 24px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #6366f1; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
}

.dash-card {
  display: flex; align-items: center; background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,0.06); }

  .card-icon {
    width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center;
    font-size: 26px; margin-right: 18px; flex-shrink: 0;
  }
  .card-info { flex: 1; }
  .card-label { font-size: 13px; color: #94a3b8; font-weight: 600; margin-bottom: 4px; }
  .card-value { font-size: 26px; font-weight: 800; color: #1e293b; }
  .card-value.revenue { color: #10b981; }
}

.card-play .card-icon { background: rgba(99,102,241,0.1); color: #6366f1; }
.card-course .card-icon { background: rgba(14,165,233,0.1); color: #0ea5e9; }
.card-order .card-icon { background: rgba(249,115,22,0.1); color: #f97316; }
.card-revenue .card-icon { background: rgba(16,185,129,0.1); color: #10b981; }
.card-revenue { position: relative; }
.card-sub { display: flex; gap: 10px; margin-top: 4px; }
.pending-tag { font-size: 11px; color: #f59e0b; font-weight: 600; }
.settled-tag { font-size: 11px; color: #10b981; font-weight: 600; }
.settle-btn { position: absolute; top: 14px; right: 14px; font-weight: 700; }

.chart-card {
  background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  .chart-header {
    font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 16px;
    i { color: #6366f1; margin-right: 6px; }
  }
}

.share-rate-card {
  margin-top: 20px;
  background: linear-gradient(135deg, #f0f0ff 0%, #fff5f5 100%);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e0e0ff;
  box-shadow: 0 2px 10px rgba(99,102,241,0.06);

  .share-rate-header {
    display: flex; align-items: center; font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 18px;
    i { color: #6366f1; margin-right: 8px; font-size: 20px; }
  }

  .share-rate-body {
    display: flex; flex-direction: column; align-items: flex-start;
  }

  .rate-display {
    margin-bottom: 14px;
    .rate-number {
      font-size: 48px; font-weight: 800; color: #6366f1; line-height: 1;
      .rate-unit { font-size: 22px; font-weight: 600; }
    }
    .rate-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }
  }

  .rate-control {
    width: 100%; display: flex; align-items: center; gap: 16px;
  }
}
</style>
