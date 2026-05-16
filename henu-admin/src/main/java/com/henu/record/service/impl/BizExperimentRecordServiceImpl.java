package com.henu.record.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.record.mapper.BizExperimentRecordMapper;
import com.henu.record.domain.BizExperimentRecord;
import com.henu.record.service.IBizExperimentRecordService;

/**
 * 学生实验过程记录Service业务层处理
 * 
 * @author henu
 * @date 2026-03-02
 */
@Service
public class BizExperimentRecordServiceImpl implements IBizExperimentRecordService 
{
    @Autowired
    private BizExperimentRecordMapper bizExperimentRecordMapper;

    /**
     * 查询学生实验过程记录
     * 
     * @param recordId 学生实验过程记录主键
     * @return 学生实验过程记录
     */
    @Override
    public BizExperimentRecord selectBizExperimentRecordByRecordId(Long recordId)
    {
        return bizExperimentRecordMapper.selectBizExperimentRecordByRecordId(recordId);
    }

    /**
     * 查询学生实验过程记录列表
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 学生实验过程记录
     */
    @Override
    public List<BizExperimentRecord> selectBizExperimentRecordList(BizExperimentRecord bizExperimentRecord)
    {
        return bizExperimentRecordMapper.selectBizExperimentRecordList(bizExperimentRecord);
    }

    /**
     * 新增学生实验过程记录
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 结果
     */
    @Override
    public int insertBizExperimentRecord(BizExperimentRecord bizExperimentRecord)
    {
        bizExperimentRecord.setCreateTime(DateUtils.getNowDate());
        return bizExperimentRecordMapper.insertBizExperimentRecord(bizExperimentRecord);
    }

    /**
     * 修改学生实验过程记录
     * 
     * @param bizExperimentRecord 学生实验过程记录
     * @return 结果
     */
    @Override
    public int updateBizExperimentRecord(BizExperimentRecord bizExperimentRecord)
    {
        bizExperimentRecord.setUpdateTime(DateUtils.getNowDate());
        return bizExperimentRecordMapper.updateBizExperimentRecord(bizExperimentRecord);
    }

    /**
     * 批量删除学生实验过程记录
     * 
     * @param recordIds 需要删除的学生实验过程记录主键
     * @return 结果
     */
    @Override
    public int deleteBizExperimentRecordByRecordIds(Long[] recordIds)
    {
        return bizExperimentRecordMapper.deleteBizExperimentRecordByRecordIds(recordIds);
    }

    /**
     * 删除学生实验过程记录信息
     * 
     * @param recordId 学生实验过程记录主键
     * @return 结果
     */
    @Override
    public int deleteBizExperimentRecordByRecordId(Long recordId)
    {
        return bizExperimentRecordMapper.deleteBizExperimentRecordByRecordId(recordId);
    }
}
