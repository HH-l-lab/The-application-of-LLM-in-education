<template>
  <div class="app-container diag-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-data-analysis"></i> 个人学情诊断中心</h2>
        <p>基于真实学习数据，三大学科 × 年级维度精准定位</p>
      </div>
      <el-tag effect="dark" size="small" type="warning"><i class="el-icon-refresh"></i> 实时数据</el-tag>
    </div>

    <div v-loading="loading" class="diag-body">
      <!-- 三个学科雷达 -->
      <el-row :gutter="16">
        <el-col :span="8" v-for="(sub, idx) in diagData" :key="idx">
          <div class="section-card radar-card" :class="'sub-' + idx">
            <div class="radar-header">
              <span class="sub-emoji">{{ emojis[idx] }}</span>
              <span class="sub-title">{{ sub.subject }}</span>
            </div>
            <div :ref="'radar' + idx" class="chart-area"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 年级明细表格 -->
      <div class="section-card detail-card" v-for="(sub, idx) in diagData" :key="'t'+idx">
        <div class="detail-header">
          <span>{{ emojis[idx] }} {{ sub.subject }} — 各年级明细</span>
        </div>
        <div class="grade-grid">
          <div class="grade-item" v-for="g in sub.grades" :key="g.grade">
            <div class="grade-name">{{ g.grade }}</div>
            <div class="grade-metrics">
              <div class="metric">
                <div class="m-val" :class="g.userExp > g.avgExp ? 'val-high' : 'val-low'">{{ g.userExp }}</div>
                <div class="m-label">实验次数</div>
                <div class="m-avg">均{{ fmtN(g.avgExp) }}</div>
              </div>
              <div class="metric">
                <div class="m-val" :class="g.userScore > g.avgScore ? 'val-high' : 'val-low'">{{ fmtN(g.userScore) }}</div>
                <div class="m-label">平均分</div>
                <div class="m-avg">均{{ fmtN(g.avgScore) }}</div>
              </div>
              <div class="metric">
                <div class="m-val" :class="g.userCourse > g.avgCourse ? 'val-high' : 'val-low'">{{ g.userCourse }}</div>
                <div class="m-label">已购课程</div>
                <div class="m-avg">均{{ fmtN(g.avgCourse) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDiagnosticStats } from '@/api/dashboard'

export default {
  name: "StudentProfileView",
  data() {
    return {
      loading: true, diagData: [],
      charts: [],
      emojis: ['⚛️', '🧪', '🧬'],
      colors: ['#6366f1', '#f59e0b', '#10b981']
    }
  },
  created() { this.fetchData() },
  beforeDestroy() { this.charts.forEach(c => c && c.dispose()) },
  methods: {
    fetchData() {
      this.loading = true
      getDiagnosticStats().then(res => {
        this.diagData = res.data || []
        this.loading = false
        this.$nextTick(() => this.renderAll())
      })
    },
    fmtN(v) { return v != null ? Number(v).toFixed(1) : '0.0' },
    renderAll() {
      this.diagData.forEach((sub, idx) => {
        const refArr = this.$refs['radar' + idx]
        const dom = Array.isArray(refArr) ? refArr[0] : refArr
        if (!dom) return
        const chart = echarts.init(dom)
        this.charts.push(chart)

        const grades = sub.grades || []
        // 每个年级3个轴: 实验次数, 平均分, 已购课程
        const indicators = []
        const userData = []
        const avgData = []
        grades.forEach(g => {
          const maxExp = Math.max(g.userExp || 0, g.avgExp || 0, 3)
          const maxCourse = Math.max(g.userCourse || 0, g.avgCourse || 0, 2)
          indicators.push({ name: g.grade + '\n实验', max: Math.ceil(maxExp * 1.8) || 5 })
          indicators.push({ name: g.grade + '\n成绩', max: 100 })
          indicators.push({ name: g.grade + '\n课程', max: Math.ceil(maxCourse * 2.5) || 5 })
          userData.push(g.userExp || 0, g.userScore || 0, g.userCourse || 0)
          avgData.push(g.avgExp || 0, g.avgScore || 0, g.avgCourse || 0)
        })
        const color = this.colors[idx]
        chart.setOption({
          tooltip: { trigger: 'item', confine: true },
          legend: {
            data: ['我的学情', '全平台均值'],
            bottom: 0,
            itemWidth: 14, itemHeight: 10,
            textStyle: { color: '#64748b', fontSize: 12 },
            selectedMode: true
          },
          radar: {
            indicator: indicators,
            shape: 'polygon',
            splitNumber: 4,
            radius: '62%',
            center: ['50%', '48%'],
            axisName: { color: '#64748b', fontSize: 10, fontWeight: 600 },
            splitArea: { areaStyle: { color: ['rgba(0,0,0,0)', 'rgba(0,0,0,0.02)', 'rgba(0,0,0,0)', 'rgba(0,0,0,0.02)'] } },
            splitLine: { lineStyle: { color: '#e2e8f0' } },
            axisLine: { lineStyle: { color: '#e2e8f0' } }
          },
          series: [{
            type: 'radar',
            data: [
              {
                value: userData, name: '我的学情', symbol: 'circle', symbolSize: 5,
                lineStyle: { width: 2, color: color },
                itemStyle: { color: color },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: color.replace(')', ',0.3)').replace('rgb', 'rgba') }, { offset: 1, color: color.replace(')', ',0.05)').replace('rgb', 'rgba') }]) }
              },
              {
                value: avgData, name: '全平台均值', symbol: 'diamond', symbolSize: 4,
                lineStyle: { width: 1.5, type: 'dashed', color: '#94a3b8' },
                itemStyle: { color: '#94a3b8' },
                areaStyle: { color: 'rgba(148,163,184,0.08)' }
              }
            ]
          }]
        })
        window.addEventListener('resize', () => chart.resize())
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.diag-page { padding: 25px 30px; background: #f0f2f8; min-height: calc(100vh - 84px); }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 22px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #6366f1; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
}

.section-card {
  background: #fff; border-radius: 18px; padding: 22px; border: 1px solid #edf2f9;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03); margin-bottom: 16px;
}

/* Radar Cards */
.radar-card { text-align: center; transition: all 0.3s; &:hover { transform: translateY(-3px); box-shadow: 0 10px 28px rgba(0,0,0,0.06); } }
.radar-header { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 8px; }
.sub-emoji { font-size: 24px; }
.sub-title { font-size: 17px; font-weight: 800; color: #1e293b; }
.chart-area { width: 100%; height: 340px; }
.legend-bar { display: flex; justify-content: center; gap: 18px; margin-top: 2px; }
.leg { font-size: 11px; color: #94a3b8; display: flex; align-items: center; gap: 4px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot-avg { background: #94a3b8; }
.sub-0 { border-top: 3px solid #6366f1; }
.sub-1 { border-top: 3px solid #f59e0b; }
.sub-2 { border-top: 3px solid #10b981; }

/* Detail */
.detail-header { font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 14px; }
.grade-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.grade-item {
  flex: 1; min-width: 120px; padding: 14px; border-radius: 12px; background: #f8fafc; border: 1px solid #edf2f9;
  transition: all 0.3s; &:hover { background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .grade-name { font-size: 13px; font-weight: 700; color: #475569; text-align: center; margin-bottom: 10px; }
}
.grade-metrics { display: flex; flex-direction: column; gap: 6px; }
.metric { display: flex; align-items: center; justify-content: space-between; }
.m-val { font-size: 14px; font-weight: 800; font-family: 'DIN', 'Consolas', monospace; min-width: 32px; }
.val-high { color: #10b981; }
.val-low { color: #f59e0b; }
.m-label { font-size: 11px; color: #94a3b8; flex: 1; text-align: center; }
.m-avg { font-size: 10px; color: #cbd5e1; min-width: 40px; text-align: right; }

@media (max-width: 992px) {
  .diag-page { padding: 16px; }
  .diag-body .el-col { width: 100% !important; }
  .chart-area { height: 260px; }
  .section-card { padding: 16px; }
  .grade-item { min-width: 45%; }
}
</style>
