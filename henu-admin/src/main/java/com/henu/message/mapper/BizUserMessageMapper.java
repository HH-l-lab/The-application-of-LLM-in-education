package com.henu.message.mapper;

import java.util.List;
import com.henu.message.domain.BizUserMessage;

/**
 * 站内系统消息Mapper接口
 * 
 * @author henu
 * @date 2026-03-09
 */
public interface BizUserMessageMapper 
{
    /**
     * 查询站内系统消息
     * 
     * @param msgId 站内系统消息主键
     * @return 站内系统消息
     */
    public BizUserMessage selectBizUserMessageByMsgId(Long msgId);

    /**
     * 查询站内系统消息列表
     * 
     * @param bizUserMessage 站内系统消息
     * @return 站内系统消息集合
     */
    public List<BizUserMessage> selectBizUserMessageList(BizUserMessage bizUserMessage);

    /**
     * 新增站内系统消息
     * 
     * @param bizUserMessage 站内系统消息
     * @return 结果
     */
    public int insertBizUserMessage(BizUserMessage bizUserMessage);

    /**
     * 修改站内系统消息
     * 
     * @param bizUserMessage 站内系统消息
     * @return 结果
     */
    public int updateBizUserMessage(BizUserMessage bizUserMessage);

    /**
     * 删除站内系统消息
     * 
     * @param msgId 站内系统消息主键
     * @return 结果
     */
    public int deleteBizUserMessageByMsgId(Long msgId);

    /**
     * 批量删除站内系统消息
     * 
     * @param msgIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizUserMessageByMsgIds(Long[] msgIds);
}
