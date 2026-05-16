package com.henu.order.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 课程交易订单对象 biz_course_order
 * 
 * @author henu
 * @date 2026-03-02
 */
public class BizCourseOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单流水号 */
    @Excel(name = "订单流水号")
    private String orderNo;

    /** 购买者(学生)ID */
    @Excel(name = "购买者(学生)ID")
    private Long buyerId;

    /** 购买的课程ID */
    @Excel(name = "购买的课程ID")
    private Long courseId;

    /** 支付金额 */
    @Excel(name = "支付金额")
    private BigDecimal amount;

    /** 支付状态（0待支付 1已支付 2已取消） */
    @Excel(name = "支付状态", readConverterExp = "0=待支付,1=已支付,2=已取消")
    private String payStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date payTime;

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setBuyerId(Long buyerId) 
    {
        this.buyerId = buyerId;
    }

    public Long getBuyerId() 
    {
        return buyerId;
    }

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setAmount(BigDecimal amount) 
    {
        this.amount = amount;
    }

    public BigDecimal getAmount() 
    {
        return amount;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    public void setPayTime(Date payTime) 
    {
        this.payTime = payTime;
    }

    public Date getPayTime() 
    {
        return payTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("buyerId", getBuyerId())
            .append("courseId", getCourseId())
            .append("amount", getAmount())
            .append("payStatus", getPayStatus())
            .append("createTime", getCreateTime())
            .append("payTime", getPayTime())
            .toString();
    }
}
