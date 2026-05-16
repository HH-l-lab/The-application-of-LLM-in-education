package com.henu.settlement.service;

import java.util.List;
import com.henu.settlement.domain.BizCreatorSettlement;

/**
 * 创作者收益结算流水Service接口
 * 
 * @author henu
 * @date 2026-03-02
 */
public interface IBizCreatorSettlementService 
{
    /**
     * 查询创作者收益结算流水
     * 
     * @param settlementId 创作者收益结算流水主键
     * @return 创作者收益结算流水
     */
    public BizCreatorSettlement selectBizCreatorSettlementBySettlementId(Long settlementId);

    /**
     * 查询创作者收益结算流水列表
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 创作者收益结算流水集合
     */
    public List<BizCreatorSettlement> selectBizCreatorSettlementList(BizCreatorSettlement bizCreatorSettlement);

    /**
     * 新增创作者收益结算流水
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 结果
     */
    public int insertBizCreatorSettlement(BizCreatorSettlement bizCreatorSettlement);

    /**
     * 修改创作者收益结算流水
     * 
     * @param bizCreatorSettlement 创作者收益结算流水
     * @return 结果
     */
    public int updateBizCreatorSettlement(BizCreatorSettlement bizCreatorSettlement);

    /**
     * 批量删除创作者收益结算流水
     * 
     * @param settlementIds 需要删除的创作者收益结算流水主键集合
     * @return 结果
     */
    public int deleteBizCreatorSettlementBySettlementIds(Long[] settlementIds);

    /**
     * 删除创作者收益结算流水信息
     * 
     * @param settlementId 创作者收益结算流水主键
     * @return 结果
     */
    public int deleteBizCreatorSettlementBySettlementId(Long settlementId);
}
