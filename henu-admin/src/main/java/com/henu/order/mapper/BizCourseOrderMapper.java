package com.henu.order.mapper;

import java.util.List;
import com.henu.order.domain.BizCourseOrder;

/**
 * 课程交易订单Mapper接口
 * 
 * @author henu
 * @date 2026-03-02
 */
public interface BizCourseOrderMapper 
{
    /**
     * 查询课程交易订单
     * 
     * @param orderId 课程交易订单主键
     * @return 课程交易订单
     */
    public BizCourseOrder selectBizCourseOrderByOrderId(Long orderId);

    /**
     * 查询课程交易订单列表
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 课程交易订单集合
     */
    public List<BizCourseOrder> selectBizCourseOrderList(BizCourseOrder bizCourseOrder);

    /**
     * 新增课程交易订单
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 结果
     */
    public int insertBizCourseOrder(BizCourseOrder bizCourseOrder);

    /**
     * 修改课程交易订单
     * 
     * @param bizCourseOrder 课程交易订单
     * @return 结果
     */
    public int updateBizCourseOrder(BizCourseOrder bizCourseOrder);

    /**
     * 删除课程交易订单
     * 
     * @param orderId 课程交易订单主键
     * @return 结果
     */
    public int deleteBizCourseOrderByOrderId(Long orderId);

    /**
     * 批量删除课程交易订单
     * 
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizCourseOrderByOrderIds(Long[] orderIds);
}
