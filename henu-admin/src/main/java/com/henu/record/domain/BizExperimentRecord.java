package com.henu.record.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 学生实验过程记录对象 biz_experiment_record
 * 
 * @author henu
 * @date 2026-03-02
 */
public class BizExperimentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 学生ID(sys_user) */
    @Excel(name = "学生ID(sys_user)")
    private Long studentId;

    /** 课程(实验)ID */
    @Excel(name = "课程(实验)ID")
    private Long courseId;

    /** 学生填写的JSON实验数据 */
    @Excel(name = "学生填写的JSON实验数据")
    private String experimentData;

    /** 大模型的预检分析与建议(诊断结果) */
    @Excel(name = "大模型的预检分析与建议(诊断结果)")
    private String aiAnalysis;

    /** 生成的Word实验报告下载地址 */
    @Excel(name = "生成的Word实验报告下载地址")
    private String reportUrl;

    /** 实验自动评分(0-100) */
    @Excel(name = "实验自动评分(0-100)")
    private Long score;

    /** 实验状态（0进行中 1已完成） */
    @Excel(name = "实验状态", readConverterExp = "0=进行中,1=已完成")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setExperimentData(String experimentData) 
    {
        this.experimentData = experimentData;
    }

    public String getExperimentData() 
    {
        return experimentData;
    }

    public void setAiAnalysis(String aiAnalysis) 
    {
        this.aiAnalysis = aiAnalysis;
    }

    public String getAiAnalysis() 
    {
        return aiAnalysis;
    }

    public void setReportUrl(String reportUrl) 
    {
        this.reportUrl = reportUrl;
    }

    public String getReportUrl() 
    {
        return reportUrl;
    }

    public void setScore(Long score) 
    {
        this.score = score;
    }

    public Long getScore() 
    {
        return score;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("studentId", getStudentId())
            .append("courseId", getCourseId())
            .append("experimentData", getExperimentData())
            .append("aiAnalysis", getAiAnalysis())
            .append("reportUrl", getReportUrl())
            .append("score", getScore())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
