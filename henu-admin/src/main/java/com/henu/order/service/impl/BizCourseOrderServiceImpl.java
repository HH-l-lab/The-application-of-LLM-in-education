package com.henu.order.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.order.mapper.BizCourseOrderMapper;
import com.henu.order.domain.BizCourseOrder;
import com.henu.order.service.IBizCourseOrderService;

/**
 * 课程交易订单Service业务层处理
 * 
 * @author henu
 * @date 2026-03-02
 */
@Service
public class BizCourseOrderServiceImpl implements IBizCourseOrderService 
{
    @Autowired
    private BizCourseOrderMapper bizCourseOrderMapper;

    /**
     * 查询课程交易订单
     * 
     * @param orderId 课程交易订单主键
     * @return 课程交易订单
     */
    @Override
    public BizCourseOrder selectBizCourseOrderByOrderId(Long orderId)
    {
        return bizCourseOrderMapper.selectBizCourseOrderByOrderId(orderId);
    }

    /**
     * 查询课程交易订单列表
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 课程交易订单
     */
    @Override
    public List<BizCourseOrder> selectBizCourseOrderList(BizCourseOrder bizCourseOrder)
    {
        return bizCourseOrderMapper.selectBizCourseOrderList(bizCourseOrder);
    }

    /**
     * 新增课程交易订单
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 结果
     */
    @Override
    public int insertBizCourseOrder(BizCourseOrder bizCourseOrder)
    {
        bizCourseOrder.setCreateTime(DateUtils.getNowDate());
        return bizCourseOrderMapper.insertBizCourseOrder(bizCourseOrder);
    }

    /**
     * 修改课程交易订单
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 结果
     */
    @Override
    public int updateBizCourseOrder(BizCourseOrder bizCourseOrder)
    {
        return bizCourseOrderMapper.updateBizCourseOrder(bizCourseOrder);
    }

    /**
     * 批量删除课程交易订单
     * 
     * @param orderIds 需要删除的课程交易订单主键
     * @return 结果
     */
    @Override
    public int deleteBizCourseOrderByOrderIds(Long[] orderIds)
    {
        return bizCourseOrderMapper.deleteBizCourseOrderByOrderIds(orderIds);
    }

    /**
     * 删除课程交易订单信息
     * 
     * @param orderId 课程交易订单主键
     * @return 结果
     */
    @Override
    public int deleteBizCourseOrderByOrderId(Long orderId)
    {
        return bizCourseOrderMapper.deleteBizCourseOrderByOrderId(orderId);
    }
}
