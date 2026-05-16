package com.henu.extraction.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 视频AI知识提取结果对象 biz_video_extraction
 * 
 * @author henu
 * @date 2026-03-02
 */
public class BizVideoExtraction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long extractionId;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long courseId;

    /** ASR语音转写全文 */
    @Excel(name = "ASR语音转写全文")
    private String asrText;

    /** 大模型提炼的课程总结及考点 */
    @Excel(name = "大模型提炼的课程总结及考点")
    private String aiSummary;

    /** 提取状态（0提取中 1成功 2失败） */
    @Excel(name = "提取状态", readConverterExp = "0=提取中,1=成功,2=失败")
    private String extractionStatus;

    public void setExtractionId(Long extractionId) 
    {
        this.extractionId = extractionId;
    }

    public Long getExtractionId() 
    {
        return extractionId;
    }

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setAsrText(String asrText) 
    {
        this.asrText = asrText;
    }

    public String getAsrText() 
    {
        return asrText;
    }

    public void setAiSummary(String aiSummary) 
    {
        this.aiSummary = aiSummary;
    }

    public String getAiSummary() 
    {
        return aiSummary;
    }

    public void setExtractionStatus(String extractionStatus) 
    {
        this.extractionStatus = extractionStatus;
    }

    public String getExtractionStatus() 
    {
        return extractionStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("extractionId", getExtractionId())
            .append("courseId", getCourseId())
            .append("asrText", getAsrText())
            .append("aiSummary", getAiSummary())
            .append("extractionStatus", getExtractionStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
