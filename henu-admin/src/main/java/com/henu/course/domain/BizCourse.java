package com.henu.course.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 在线课程资源对象 biz_course
 * 
 * @author henu
 * @date 2026-03-06
 */
public class BizCourse extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 课程ID */
    private Long courseId;

    /** 课程标题 */
    @Excel(name = "课程标题")
    private String courseTitle;

    /** 上传者ID */
    @Excel(name = "上传者ID")
    private Long creatorId;

    /** 流媒体URL */
    private String videoUrl;

    /** 课程类型 */
    @Excel(name = "课程类型")
    private String courseType;

    /** 价格 */
    @Excel(name = "价格")
    private BigDecimal price;

    /** 封面图片URL */
    @Excel(name = "封面图片URL")
    private String coverImage;

    /** 审核状态 */
    @Excel(name = "审核状态")
    private String auditStatus;

    /** 播放次数 */
    @Excel(name = "播放次数")
    private Long playCount;

    /** 删除标志 */
    private String delFlag;

    /** 课程学科 */
    @Excel(name = "课程学科")
    private String courseSubject;

    /** 教材类型 */
    @Excel(name = "教材类型")
    private String textbookEdition;

    /** 课程年级 */
    @Excel(name = "课程年级")
    private String courseGrade;

    /** 课程章节 */
    @Excel(name = "课程章节")
    private String courseChapter;

    /** 实验任务配置JSON */
    @Excel(name = "实验任务配置JSON")
    private String experimentFormConfig;

    /** 审核驳回理由 */
    @Excel(name = "审核驳回理由")
    private String auditMessage;

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public void setTextbookEdition(String textbookEdition) {
        this.textbookEdition = textbookEdition;
    }

    public String getTextbookEdition() {
        return textbookEdition;
    }

    public void setCourseGrade(String courseGrade) {
        this.courseGrade = courseGrade;
    }

    public String getCourseGrade() {
        return courseGrade;
    }

    public void setCourseChapter(String courseChapter) {
        this.courseChapter = courseChapter;
    }

    public String getCourseChapter() {
        return courseChapter;
    }

    public void setExperimentFormConfig(String experimentFormConfig) {
        this.experimentFormConfig = experimentFormConfig;
    }

    public String getExperimentFormConfig() {
        return experimentFormConfig;
    }

    public void setAuditMessage(String auditMessage) {
        this.auditMessage = auditMessage;
    }

    public String getAuditMessage() {
        return auditMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("courseId", getCourseId())
                .append("courseTitle", getCourseTitle())
                .append("creatorId", getCreatorId())
                .append("videoUrl", getVideoUrl())
                .append("courseType", getCourseType())
                .append("price", getPrice())
                .append("coverImage", getCoverImage())
                .append("auditStatus", getAuditStatus())
                .append("playCount", getPlayCount())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("courseSubject", getCourseSubject())
                .append("textbookEdition", getTextbookEdition())
                .append("courseGrade", getCourseGrade())
                .append("courseChapter", getCourseChapter())
                .append("experimentFormConfig", getExperimentFormConfig())
                .toString();
    }
}
