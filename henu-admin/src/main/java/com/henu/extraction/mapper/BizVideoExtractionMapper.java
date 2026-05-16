package com.henu.extraction.mapper;

import java.util.List;
import com.henu.extraction.domain.BizVideoExtraction;

/**
 * 视频AI知识提取结果Mapper接口
 * 
 * @author henu
 * @date 2026-03-02
 */
public interface BizVideoExtractionMapper 
{
    /**
     * 查询视频AI知识提取结果
     * 
     * @param extractionId 视频AI知识提取结果主键
     * @return 视频AI知识提取结果
     */
    public BizVideoExtraction selectBizVideoExtractionByExtractionId(Long extractionId);

    /**
     * 查询视频AI知识提取结果列表
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 视频AI知识提取结果集合
     */
    public List<BizVideoExtraction> selectBizVideoExtractionList(BizVideoExtraction bizVideoExtraction);

    /**
     * 新增视频AI知识提取结果
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 结果
     */
    public int insertBizVideoExtraction(BizVideoExtraction bizVideoExtraction);

    /**
     * 修改视频AI知识提取结果
     * 
     * @param bizVideoExtraction 视频AI知识提取结果
     * @return 结果
     */
    public int updateBizVideoExtraction(BizVideoExtraction bizVideoExtraction);

    /**
     * 删除视频AI知识提取结果
     * 
     * @param extractionId 视频AI知识提取结果主键
     * @return 结果
     */
    public int deleteBizVideoExtractionByExtractionId(Long extractionId);

    /**
     * 批量删除视频AI知识提取结果
     * 
     * @param extractionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizVideoExtractionByExtractionIds(Long[] extractionIds);
}
