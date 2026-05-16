<template>
  <div class="app-container creator-page">
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-upload2"></i> 创作者上传中心</h2>
        <p>视频上传 · 素材管理 · 审核追踪</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-upload" size="small" round @click="handleAdd">上传新素材</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport">导出列表</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起' : '筛选' }}</el-button>
      </div>
    </div>

    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="标题" prop="courseTitle">
            <el-input v-model="queryParams.courseTitle" placeholder="课程标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="类型" prop="courseType">
            <el-select v-model="queryParams.courseType" placeholder="全部" clearable>
              <el-option v-for="dict in dict.type.sys_coursetype" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核" prop="auditStatus">
            <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable>
              <el-option v-for="dict in dict.type.sys_review" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="学科" prop="courseSubject">
            <el-select v-model="queryParams.courseSubject" placeholder="全部" clearable>
              <el-option v-for="dict in dict.type.sys_subject" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="教材" prop="textbookEdition">
            <el-select v-model="queryParams.textbookEdition" placeholder="全部" clearable>
              <el-option v-for="dict in dict.type.sys_textbooked" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="courseGrade">
            <el-select v-model="queryParams.courseGrade" placeholder="全部" clearable>
              <el-option v-for="dict in dict.type.sys_grade" :key="dict.value" :label="dict.label" :value="dict.value" />
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
      <div v-if="courseList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-folder-opened"></i>
        <p>还没有上传任何素材</p>
      </div>

      <div class="course-card" v-for="item in courseList" :key="item.courseId">
        <div class="card-left">
          <el-checkbox :value="ids.includes(item.courseId)" @change="toggleSelect(item.courseId)"></el-checkbox>
          <div class="cover-wrap">
            <image-preview :src="item.coverImage" :width="80" :height="56" />
          </div>
        </div>
        <div class="card-center">
          <div class="title-row">
            <span class="course-title">{{ item.courseTitle }}</span>
            <el-tag :type="item.auditStatus === '1' ? 'success' : item.auditStatus === '0' ? 'warning' : 'danger'" size="mini" effect="dark">
              <dict-tag :options="dict.type.sys_review" :value="item.auditStatus"/>
            </el-tag>
          </div>
          <div class="tag-row">
            <el-tag size="mini" effect="plain"><dict-tag :options="dict.type.sys_coursetype" :value="item.courseType"/></el-tag>
            <el-tag size="mini" effect="plain" type="success"><dict-tag :options="dict.type.sys_subject" :value="item.courseSubject"/></el-tag>
            <el-tag size="mini" effect="plain" type="warning"><dict-tag :options="dict.type.sys_grade" :value="item.courseGrade"/></el-tag>
            <span class="chapter-tag" v-if="item.courseChapter">{{ item.courseChapter }}</span>
          </div>
          <div class="meta-row">
            <span><i class="el-icon-video-play"></i> {{ item.playCount || 0 }} 播放</span>
          </div>
        </div>
        <div class="card-right">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="课程标题" prop="courseTitle">
          <el-input v-model="form.courseTitle" placeholder="请输入课程标题" />
        </el-form-item>
        <el-form-item label="视频文件" prop="videoUrl">
          <file-upload v-model="form.videoUrl" :file-type="['mp4', 'avi', 'mov', 'webm']" :file-size="200"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="form.courseType" placeholder="请选择课程类型">
            <el-option v-for="dict in dict.type.sys_coursetype" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="上传封面" prop="coverImage">
          <image-upload v-model="form.coverImage"/>
        </el-form-item>
        <el-form-item label="课程学科" prop="courseSubject">
          <el-select v-model="form.courseSubject" placeholder="请选择课程学科">
            <el-option v-for="dict in dict.type.sys_subject" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="教材版本" prop="textbookEdition">
          <el-select v-model="form.textbookEdition" placeholder="请选择教材版本">
            <el-option v-for="dict in dict.type.sys_textbooked" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="courseGrade">
          <el-select v-model="form.courseGrade" placeholder="请选择课程年级">
            <el-option v-for="dict in dict.type.sys_grade" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程章节" prop="courseChapter">
          <el-input v-model="form.courseChapter" placeholder="如: 第一章 牛顿定律" />
        </el-form-item>
        <el-form-item label="采集器配置" prop="experimentFormConfig">
          <el-input v-model="form.experimentFormConfig" type="textarea" :rows="3" placeholder="动态采集表单 JSON Schema" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">提 审</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse, getCourse, delCourse, addCourse, updateCourse } from "@/api/creator/course"

export default {
  name: "CreatorCourse",
  dicts: ['sys_textbooked', 'sys_grade', 'sys_coursetype', 'sys_review', 'sys_subject'],
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true,
      total: 0, courseList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, courseTitle: null, courseType: null, auditStatus: null, courseSubject: null, textbookEdition: null, courseGrade: null },
      form: {},
      rules: {
        courseTitle: [{ required: true, message: "课程标题不能为空", trigger: "blur" }],
        videoUrl: [{ required: true, message: "视频文件不能为空", trigger: "blur" }],
        courseType: [{ required: true, message: "课程类型不能为空", trigger: "change" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listCourse(this.queryParams).then(response => { this.courseList = response.rows; this.total = response.total; this.loading = false }).catch(() => { this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { courseId: null, courseTitle: null, videoUrl: null, courseType: null, price: null, coverImage: null, courseSubject: null, textbookEdition: null, courseGrade: null, courseChapter: null, experimentFormConfig: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1; this.multiple = this.ids.length === 0
    },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.courseId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "上传新素材" },
    handleUpdate(row) {
      this.reset(); const courseId = row.courseId || this.ids
      getCourse(courseId).then(response => { this.form = response.data; this.open = true; this.title = "修改上传素材" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.courseId != null) { updateCourse(this.form).then(() => { this.$modal.msgSuccess("修改并重新提审成功"); this.open = false; this.getList() }) }
          else { addCourse(this.form).then(() => { this.$modal.msgSuccess("上传提审成功"); this.open = false; this.getList() }) }
        }
      })
    },
    handleDelete(row) {
      const courseIds = row.courseId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + courseIds + '"的素材？').then(function() { return delCourse(courseIds) }).then(() => { this.getList(); this.ids = []; this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    handleExport() { this.download('creator/course/export', { ...this.queryParams }, `creator_course_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style lang="scss" scoped>
.creator-page { padding: 25px 30px; background: #f4f7fc; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
  h2 { margin: 0 0 5px; font-size: 22px; font-weight: 700; color: #1e293b; i { color: #6366f1; margin-right: 8px; } }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions { display: flex; align-items: center; gap: 10px; .el-button { font-weight: 600; } }
}

.filter-card {
  background: #fff; border-radius: 16px; padding: 22px 24px 6px; margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03); border: 1px solid #edf2f9;
  ::v-deep .el-form-item { margin-bottom: 16px; }
  ::v-deep .el-input__inner { border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #6366f1; } }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.cards-container { min-height: 200px; }
.empty-state { text-align: center; padding: 80px 20px; i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; } p { color: #94a3b8; font-size: 15px; margin: 0; } }

.course-card {
  display: flex; align-items: center; background: #fff; border-radius: 14px; padding: 18px 22px; margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); border: 1px solid #edf2f9; transition: all 0.3s;
  &:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.06); transform: translateY(-1px); }
}

.card-left {
  display: flex; align-items: center; gap: 14px; margin-right: 20px; flex-shrink: 0;
  .cover-wrap {
    border-radius: 10px; overflow: hidden; box-shadow: 0 3px 8px rgba(0,0,0,0.1); width: 80px; height: 56px;
    ::v-deep img { object-fit: cover; border-radius: 10px; }
  }
}

.card-center {
  flex: 1; min-width: 0;
  .title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
  .course-title { font-size: 15px; font-weight: 700; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .tag-row {
    display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 6px;
    .el-tag { border-radius: 6px; font-size: 11px; }
    .chapter-tag { font-size: 11px; color: #64748b; background: #f1f5f9; padding: 2px 8px; border-radius: 6px; }
  }
  .meta-row { font-size: 12px; color: #94a3b8; i { margin-right: 3px; } }
}

.card-right { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; margin-left: 16px; .el-button { font-weight: 500; } }

::v-deep .custom-dialog .el-dialog { border-radius: 16px; overflow: hidden;
  .el-dialog__header { background: #f8fafc; border-bottom: 1px solid #edf2f9; padding: 18px 24px; }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner, ::v-deep .custom-dialog .el-textarea__inner {
  border-radius: 8px; border-color: #e2e8f0; &:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
}
</style>
