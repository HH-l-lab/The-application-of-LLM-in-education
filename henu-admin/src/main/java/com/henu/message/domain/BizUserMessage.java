package com.henu.message.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 站内系统消息对象 biz_user_message
 * 
 * @author henu
 * @date 2026-03-09
 */
public class BizUserMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long msgId;

    /** 接收方用户ID */
    @Excel(name = "接收方用户ID")
    private Long userId;

    /** 消息标题 */
    @Excel(name = "消息标题")
    private String title;

    /** 消息详细内容 */
    @Excel(name = "消息详细内容")
    private String content;

    /** 消息类型（0系统通知 1审核提醒 2交易凭证） */
    @Excel(name = "消息类型", readConverterExp = "0=系统通知,1=审核提醒,2=交易凭证")
    private String type;

    /** 阅读状态（0未读 1已读） */
    @Excel(name = "阅读状态", readConverterExp = "0=未读,1=已读")
    private String isRead;

    /** 关联的业务ID(如课程ID或订单ID) */
    @Excel(name = "关联的业务ID(如课程ID或订单ID)")
    private Long relatedId;

    public void setMsgId(Long msgId) 
    {
        this.msgId = msgId;
    }

    public Long getMsgId() 
    {
        return msgId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }

    public void setIsRead(String isRead) 
    {
        this.isRead = isRead;
    }

    public String getIsRead() 
    {
        return isRead;
    }

    public void setRelatedId(Long relatedId) 
    {
        this.relatedId = relatedId;
    }

    public Long getRelatedId() 
    {
        return relatedId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("msgId", getMsgId())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("type", getType())
            .append("isRead", getIsRead())
            .append("relatedId", getRelatedId())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
