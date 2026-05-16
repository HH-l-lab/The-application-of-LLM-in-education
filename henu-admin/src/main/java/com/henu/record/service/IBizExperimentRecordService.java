package com.henu.record.service;

import java.util.List;
import com.henu.record.domain.BizExperimentRecord;

/**
 * 学生实验过程记录Service接口
 * 
 * @author henu
 * @date 2026-03-02
 */
public interface IBizExperimentRecordService 
{
    /**
     * 查询学生实验过程记录
     * 
     * @param recordId 学生实验过程记录主键
     * @return 学生实验过程记录
     */
    public BizExperimentRecord selectBizExperimentRecordByRecordId(Long recordId);

    /**
     * 查询学生实验过程记录列表
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 学生实验过程记录集合
     */
    public List<BizExperimentRecord> selectBizExperimentRecordList(BizExperimentRecord bizExperimentRecord);

    /**
     * 新增学生实验过程记录
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 结果
     */
    public int insertBizExperimentRecord(BizExperimentRecord bizExperimentRecord);

    /**
     * 修改学生实验过程记录
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 结果
     */
    public int updateBizExperimentRecord(BizExperimentRecord bizExperimentRecord);

    /**
     * 批量删除学生实验过程记录
     * 
     * @param recordIds 需要删除的学生实验过程记录主键集合
     * @return 结果
     */
    public int deleteBizExperimentRecordByRecordIds(Long[] recordIds);

    /**
     * 删除学生实验过程记录信息
     * 
     * @param recordId 学生实验过程记录主键
     * @return 结果
     */
    public int deleteBizExperimentRecordByRecordId(Long recordId);
}
