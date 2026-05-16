package com.henu.message.service;

import java.util.List;
import com.henu.message.domain.BizUserMessage;

/**
 * 站内系统消息Service接口
 * 
 * @author henu
 * @date 2026-03-09
 */
public interface IBizUserMessageService 
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
     * 批量删除站内系统消息
     * 
     * @param msgIds 需要删除的站内系统消息主键集合
     * @return 结果
     */
    public int deleteBizUserMessageByMsgIds(Long[] msgIds);

    /**
     * 删除站内系统消息信息
     * 
     * @param msgId 站内系统消息主键
     * @return 结果
     */
    public int deleteBizUserMessageByMsgId(Long msgId);
}
