package com.henu.record.mapper;

import java.util.List;
import com.henu.record.domain.BizExperimentRecord;

/**
 * 学生实验过程记录Mapper接口
 * 
 * @author henu
 * @date 2026-03-02
 */
public interface BizExperimentRecordMapper 
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
     * 删除学生实验过程记录
     * 
     * @param recordId 学生实验过程记录主键
     * @return 结果
     */
    public int deleteBizExperimentRecordByRecordId(Long recordId);

    /**
     * 批量删除学生实验过程记录
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizExperimentRecordByRecordIds(Long[] recordIds);
}
