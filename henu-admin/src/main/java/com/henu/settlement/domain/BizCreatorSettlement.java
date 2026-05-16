package com.henu.settlement.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.henu.common.annotation.Excel;
import com.henu.common.core.domain.BaseEntity;

/**
 * 创作者收益结算流水对象 biz_creator_settlement
 * 
 * @author henu
 * @date 2026-03-02
 */
public class BizCreatorSettlement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结算流水ID */
    private Long settlementId;

    /** 创作者ID */
    @Excel(name = "创作者ID")
    private Long creatorId;

    /** 来源订单ID */
    @Excel(name = "来源订单ID")
    private Long orderId;

    /** 分润金额 */
    @Excel(name = "分润金额")
    private BigDecimal profitAmount;

    /** 结算状态（0待结算 1已结算） */
    @Excel(name = "结算状态", readConverterExp = "0=待结算,1=已结算")
    private String settlementStatus;

    /** 实际结算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际结算时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date settlementTime;

    public void setSettlementId(Long settlementId) 
    {
        this.settlementId = settlementId;
    }

    public Long getSettlementId() 
    {
        return settlementId;
    }

    public void setCreatorId(Long creatorId) 
    {
        this.creatorId = creatorId;
    }

    public Long getCreatorId() 
    {
        return creatorId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setProfitAmount(BigDecimal profitAmount) 
    {
        this.profitAmount = profitAmount;
    }

    public BigDecimal getProfitAmount() 
    {
        return profitAmount;
    }

    public void setSettlementStatus(String settlementStatus) 
    {
        this.settlementStatus = settlementStatus;
    }

    public String getSettlementStatus() 
    {
        return settlementStatus;
    }

    public void setSettlementTime(Date settlementTime) 
    {
        this.settlementTime = settlementTime;
    }

    public Date getSettlementTime() 
    {
        return settlementTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("settlementId", getSettlementId())
            .append("creatorId", getCreatorId())
            .append("orderId", getOrderId())
            .append("profitAmount", getProfitAmount())
            .append("settlementStatus", getSettlementStatus())
            .append("createTime", getCreateTime())
            .append("settlementTime", getSettlementTime())
            .toString();
    }
}
