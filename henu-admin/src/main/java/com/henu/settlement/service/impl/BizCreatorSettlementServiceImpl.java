package com.henu.settlement.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.settlement.mapper.BizCreatorSettlementMapper;
import com.henu.settlement.domain.BizCreatorSettlement;
import com.henu.settlement.service.IBizCreatorSettlementService;

/**
 * 创作者收益结算流水Service业务层处理
 * 
 * @author henu
 * @date 2026-03-02
 */
@Service
public class BizCreatorSettlementServiceImpl implements IBizCreatorSettlementService 
{
    @Autowired
    private BizCreatorSettlementMapper bizCreatorSettlementMapper;

    /**
     * 查询创作者收益结算流水
     * 
     * @param settlementId 创作者收益结算流水主键
     * @return 创作者收益结算流水
     */
    @Override
    public BizCreatorSettlement selectBizCreatorSettlementBySettlementId(Long settlementId)
    {
        return bizCreatorSettlementMapper.selectBizCreatorSettlementBySettlementId(settlementId);
    }

    /**
     * 查询创作者收益结算流水列表
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 创作者收益结算流水
     */
    @Override
    public List<BizCreatorSettlement> selectBizCreatorSettlementList(BizCreatorSettlement bizCreatorSettlement)
    {
        return bizCreatorSettlementMapper.selectBizCreatorSettlementList(bizCreatorSettlement);
    }

    /**
     * 新增创作者收益结算流水
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 结果
     */
    @Override
    public int insertBizCreatorSettlement(BizCreatorSettlement bizCreatorSettlement)
    {
        bizCreatorSettlement.setCreateTime(DateUtils.getNowDate());
        return bizCreatorSettlementMapper.insertBizCreatorSettlement(bizCreatorSettlement);
    }

    /**
     * 修改创作者收益结算流水
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 结果
     */
    @Override
    public int updateBizCreatorSettlement(BizCreatorSettlement bizCreatorSettlement)
    {
        return bizCreatorSettlementMapper.updateBizCreatorSettlement(bizCreatorSettlement);
    }

    /**
     * 批量删除创作者收益结算流水
     * 
     * @param settlementIds 需要删除的创作者收益结算流水主键
     * @return 结果
     */
    @Override
    public int deleteBizCreatorSettlementBySettlementIds(Long[] settlementIds)
    {
        return bizCreatorSettlementMapper.deleteBizCreatorSettlementBySettlementIds(settlementIds);
    }

    /**
     * 删除创作者收益结算流水信息
     * 
     * @param settlementId 创作者收益结算流水主键
     * @return 结果
     */
    @Override
    public int deleteBizCreatorSettlementBySettlementId(Long settlementId)
    {
        return bizCreatorSettlementMapper.deleteBizCreatorSettlementBySettlementId(settlementId);
    }
}
