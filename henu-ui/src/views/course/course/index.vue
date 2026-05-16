<template>
  <div class="app-container course-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-video-camera-solid"></i> 在线课程资源中心</h2>
        <p>课程管理 · 视频审核 · 智能检索</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" round @click="handleAdd" v-hasPermi="['course:course:add']">新增课程</el-button>
        <el-button type="danger" icon="el-icon-delete" size="small" round plain @click="handleDelete" v-hasPermi="['course:course:remove']" :disabled="multiple">批量删除 ({{ ids.length }})</el-button>
        <el-button type="warning" icon="el-icon-download" size="small" round plain @click="handleExport" v-hasPermi="['course:course:export']">导出</el-button>
        <el-button :icon="showSearch ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="small" round @click="showSearch = !showSearch">{{ showSearch ? '收起筛选' : '展开筛选' }}</el-button>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <transition name="el-zoom-in-top">
      <div class="filter-card" v-show="showSearch">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
          <el-form-item label="课程标题" prop="courseTitle">
            <el-input v-model="queryParams.courseTitle" placeholder="请输入课程标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="上传者ID" prop="creatorId">
            <el-input v-model="queryParams.creatorId" placeholder="请输入上传者ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="课程类型" prop="courseType">
            <el-select v-model="queryParams.courseType" placeholder="全部类型" clearable>
              <el-option v-for="dict in dict.type.sys_coursetype" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态" prop="auditStatus">
            <el-select v-model="queryParams.auditStatus" placeholder="全部状态" clearable>
              <el-option v-for="dict in dict.type.sys_review" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程学科" prop="courseSubject">
            <el-select v-model="queryParams.courseSubject" placeholder="全部学科" clearable>
              <el-option v-for="dict in dict.type.sys_subject" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="教材类型" prop="textbookEdition">
            <el-select v-model="queryParams.textbookEdition" placeholder="全部教材" clearable>
              <el-option v-for="dict in dict.type.sys_textbooked" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程年级" prop="courseGrade">
            <el-select v-model="queryParams.courseGrade" placeholder="全部年级" clearable>
              <el-option v-for="dict in dict.type.sys_grade" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程章节" prop="courseChapter">
            <el-input v-model="queryParams.courseChapter" placeholder="请输入章节" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input v-model="queryParams.price" placeholder="价格" clearable style="width: 100px;" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <!-- 课程卡片列表 -->
    <div v-loading="loading" class="cards-container">
      <div v-if="courseList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-video-camera"></i>
        <p>暂无课程数据</p>
      </div>

      <div class="course-card" v-for="item in courseList" :key="item.courseId">
        <div class="card-left">
          <el-checkbox :value="ids.includes(item.courseId)" @change="toggleSelect(item.courseId)"></el-checkbox>
          <div class="cover-wrap">
            <image-preview :src="item.coverImage" :width="80" :height="56" />
          </div>
        </div>

        <div class="card-center">
          <div class="course-title-row">
            <span class="course-title">{{ item.courseTitle }}</span>
            <el-tag :type="item.auditStatus === '1' ? 'success' : item.auditStatus === '0' ? 'warning' : 'danger'" size="mini" effect="dark">
              <dict-tag :options="dict.type.sys_review" :value="item.auditStatus"/>
            </el-tag>
          </div>
          <div class="course-tags">
            <el-tag size="mini" effect="plain"><dict-tag :options="dict.type.sys_coursetype" :value="item.courseType"/></el-tag>
            <el-tag size="mini" effect="plain" type="success"><dict-tag :options="dict.type.sys_subject" :value="item.courseSubject"/></el-tag>
            <el-tag size="mini" effect="plain" type="warning"><dict-tag :options="dict.type.sys_grade" :value="item.courseGrade"/></el-tag>
            <el-tag size="mini" effect="plain" type="info"><dict-tag :options="dict.type.sys_textbooked" :value="item.textbookEdition"/></el-tag>
            <span class="chapter-tag" v-if="item.courseChapter">{{ item.courseChapter }}</span>
          </div>
          <div class="course-meta">
            <span><i class="el-icon-user"></i> 上传者 {{ item.creatorId }}</span>
            <span><i class="el-icon-video-play"></i> {{ item.playCount || 0 }} 次播放</span>
            <span class="price-tag">¥{{ item.price || '0.00' }}</span>
          </div>
        </div>

        <div class="card-right">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['course:course:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#ef4444;" @click="handleDelete(item)" v-hasPermi="['course:course:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body class="custom-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程标题" prop="courseTitle">
          <el-input v-model="form.courseTitle" placeholder="请输入课程标题" />
        </el-form-item>
        <el-form-item label="上传者ID" prop="creatorId">
          <el-input v-model="form.creatorId" placeholder="请输入上传者ID" />
        </el-form-item>
        <el-form-item label="流媒体URL" prop="videoUrl">
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
        <el-form-item label="封面图片" prop="coverImage">
          <image-upload v-model="form.coverImage"/>
        </el-form-item>
        <el-form-item label="审核状态" prop="auditStatus">
          <el-select v-model="form.auditStatus" placeholder="请选择审核状态">
            <el-option v-for="dict in dict.type.sys_review" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="播放次数" prop="playCount">
          <el-input v-model="form.playCount" placeholder="请输入播放次数" />
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志" />
        </el-form-item>
        <el-form-item label="课程学科" prop="courseSubject">
          <el-select v-model="form.courseSubject" placeholder="请选择课程学科">
            <el-option v-for="dict in dict.type.sys_subject" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="教材类型" prop="textbookEdition">
          <el-select v-model="form.textbookEdition" placeholder="请选择教材类型">
            <el-option v-for="dict in dict.type.sys_textbooked" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程年级" prop="courseGrade">
          <el-select v-model="form.courseGrade" placeholder="请选择课程年级">
            <el-option v-for="dict in dict.type.sys_grade" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程章节" prop="courseChapter">
          <el-input v-model="form.courseChapter" placeholder="请输入课程章节" />
        </el-form-item>
        <el-form-item label="采集器配置" prop="experimentFormConfig">
          <el-input v-model="form.experimentFormConfig" type="textarea" :rows="3" placeholder="请输入JSON配置" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse, getCourse, delCourse, addCourse, updateCourse } from "@/api/course/course"

export default {
  name: "Course",
  dicts: ['sys_textbooked', 'sys_grade', 'sys_coursetype', 'sys_review', 'sys_subject'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      courseList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseTitle: null,
        creatorId: null,
        courseType: null,
        price: null,
        coverImage: null,
        auditStatus: null,
        playCount: null,
        courseSubject: null,
        textbookEdition: null,
        courseGrade: null,
        courseChapter: null,
      },
      form: {},
      rules: {
        courseTitle: [
          { required: true, message: "课程标题不能为空", trigger: "blur" }
        ],
        creatorId: [
          { required: true, message: "上传者ID不能为空", trigger: "blur" }
        ],
        videoUrl: [
          { required: true, message: "流媒体URL不能为空", trigger: "blur" }
        ],
        courseType: [
          { required: true, message: "课程类型不能为空", trigger: "change" }
        ],
        price: [
          { required: true, message: "价格不能为空", trigger: "blur" }
        ],
        coverImage: [
          { required: true, message: "封面图片不能为空", trigger: "blur" }
        ],
        auditStatus: [
          { required: true, message: "审核状态不能为空", trigger: "change" }
        ],
        courseSubject: [
          { required: true, message: "课程学科不能为空", trigger: "change" }
        ],
        textbookEdition: [
          { required: true, message: "教材类型不能为空", trigger: "change" }
        ],
        courseGrade: [
          { required: true, message: "课程年级不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        courseId: null, courseTitle: null, creatorId: null, videoUrl: null,
        courseType: null, price: null, coverImage: null, auditStatus: null,
        playCount: null, delFlag: null, createBy: null, createTime: null,
        updateBy: null, updateTime: null, courseSubject: null,
        textbookEdition: null, courseGrade: null, courseChapter: null,
        experimentFormConfig: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    toggleSelect(id) {
      const idx = this.ids.indexOf(id)
      if (idx > -1) { this.ids.splice(idx, 1) } else { this.ids.push(id) }
      this.single = this.ids.length !== 1
      this.multiple = this.ids.length === 0
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.courseId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增课程资源"
    },
    handleUpdate(row) {
      this.reset()
      const courseId = row.courseId || this.ids
      getCourse(courseId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改课程资源"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.courseId != null) {
            updateCourse(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCourse(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const courseIds = row.courseId || this.ids
      this.$modal.confirm('是否确认删除编号为"' + courseIds + '"的课程数据？').then(function() {
        return delCourse(courseIds)
      }).then(() => {
        this.getList()
        this.ids = []
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('course/course/export', {
        ...this.queryParams
      }, `course_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.course-page {
  padding: 25px 30px;
  background: #f4f7fc;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  h2 {
    margin: 0 0 5px;
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    i { color: #f59e0b; margin-right: 8px; }
  }
  p { margin: 0; color: #94a3b8; font-size: 13px; }
  .header-actions {
    display: flex; align-items: center; gap: 10px;
    .el-button { font-weight: 600; }
  }
}

/* 搜索筛选卡片 */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px 6px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  border: 1px solid #edf2f9;

  ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
  ::v-deep .el-input__inner,
  ::v-deep .el-select .el-input__inner {
    border-radius: 8px;
    border-color: #e2e8f0;
    &:focus { border-color: #f59e0b; }
  }
  ::v-deep .el-button { border-radius: 8px; font-weight: 600; }
}

.cards-container { min-height: 200px; }

.empty-state {
  text-align: center; padding: 80px 20px;
  i { font-size: 50px; color: #cbd5e1; display: block; margin-bottom: 12px; }
  p { color: #94a3b8; font-size: 15px; margin: 0; }
}

/* 课程卡片 */
.course-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  padding: 18px 22px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  border: 1px solid #edf2f9;
  transition: box-shadow 0.3s, transform 0.15s;

  &:hover {
    box-shadow: 0 8px 25px rgba(0,0,0,0.06);
    transform: translateY(-1px);
  }
}

.card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-right: 20px;
  flex-shrink: 0;

  .cover-wrap {
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 6px rgba(0,0,0,0.08);
    width: 80px; height: 56px;

    ::v-deep img { object-fit: cover; border-radius: 8px; }
  }
}

.card-center {
  flex: 1;
  min-width: 0;

  .course-title-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
  }

  .course-title {
    font-size: 15px;
    font-weight: 700;
    color: #1e293b;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .course-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;

    .el-tag { border-radius: 6px; font-size: 11px; }
    .chapter-tag {
      font-size: 11px;
      color: #64748b;
      background: #f1f5f9;
      padding: 2px 8px;
      border-radius: 6px;
    }
  }

  .course-meta {
    display: flex;
    align-items: center;
    gap: 18px;
    font-size: 12px;
    color: #94a3b8;

    i { margin-right: 3px; }

    .price-tag {
      color: #ef4444;
      font-weight: 700;
      font-size: 14px;
    }
  }
}

.card-right {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 16px;
  .el-button { font-weight: 500; }
}

/* 对话框美化 */
::v-deep .custom-dialog .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #edf2f9;
    padding: 18px 24px;
  }
  .el-dialog__body { padding: 24px; }
}
::v-deep .custom-dialog .el-input__inner,
::v-deep .custom-dialog .el-textarea__inner {
  border-radius: 8px;
  border-color: #e2e8f0;
  &:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1); }
}
</style>
