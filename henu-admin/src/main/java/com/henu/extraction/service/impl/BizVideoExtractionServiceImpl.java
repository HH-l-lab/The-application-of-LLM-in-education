package com.henu.extraction.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.extraction.mapper.BizVideoExtractionMapper;
import com.henu.extraction.domain.BizVideoExtraction;
import com.henu.extraction.service.IBizVideoExtractionService;

/**
 * 视频AI知识提取结果Service业务层处理
 * 
 * @author henu
 * @date 2026-03-02
 */
@Service
public class BizVideoExtractionServiceImpl implements IBizVideoExtractionService 
{
    @Autowired
    private BizVideoExtractionMapper bizVideoExtractionMapper;

    /**
     * 查询视频AI知识提取结果
     * 
     * @param extractionId 视频AI知识提取结果主键
     * @return 视频AI知识提取结果
     */
    @Override
    public BizVideoExtraction selectBizVideoExtractionByExtractionId(Long extractionId)
    {
        return bizVideoExtractionMapper.selectBizVideoExtractionByExtractionId(extractionId);
    }

    /**
     * 查询视频AI知识提取结果列表
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 视频AI知识提取结果
     */
    @Override
    public List<BizVideoExtraction> selectBizVideoExtractionList(BizVideoExtraction bizVideoExtraction)
    {
        return bizVideoExtractionMapper.selectBizVideoExtractionList(bizVideoExtraction);
    }

    /**
     * 新增视频AI知识提取结果
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 结果
     */
    @Override
    public int insertBizVideoExtraction(BizVideoExtraction bizVideoExtraction)
    {
        bizVideoExtraction.setCreateTime(DateUtils.getNowDate());
        return bizVideoExtractionMapper.insertBizVideoExtraction(bizVideoExtraction);
    }

    /**
     * 修改视频AI知识提取结果
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 结果
     */
    @Override
    public int updateBizVideoExtraction(BizVideoExtraction bizVideoExtraction)
    {
        return bizVideoExtractionMapper.updateBizVideoExtraction(bizVideoExtraction);
    }

    /**
     * 批量删除视频AI知识提取结果
     * 
     * @param extractionIds 需要删除的视频AI知识提取结果主键
     * @return 结果
     */
    @Override
    public int deleteBizVideoExtractionByExtractionIds(Long[] extractionIds)
    {
        return bizVideoExtractionMapper.deleteBizVideoExtractionByExtractionIds(extractionIds);
    }

    /**
     * 删除视频AI知识提取结果信息
     * 
     * @param extractionId 视频AI知识提取结果主键
     * @return 结果
     */
    @Override
    public int deleteBizVideoExtractionByExtractionId(Long extractionId)
    {
        return bizVideoExtractionMapper.deleteBizVideoExtractionByExtractionId(extractionId);
    }
}
