package com.henu.message.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.message.mapper.BizUserMessageMapper;
import com.henu.message.domain.BizUserMessage;
import com.henu.message.service.IBizUserMessageService;

/**
 * 站内系统消息Service业务层处理
 * 
 * @author henu
 * @date 2026-03-09
 */
@Service
public class BizUserMessageServiceImpl implements IBizUserMessageService 
{
    @Autowired
    private BizUserMessageMapper bizUserMessageMapper;

    /**
     * 查询站内系统消息
     * 
     * @param msgId 站内系统消息主键
     * @return 站内系统消息
     */
    @Override
    public BizUserMessage selectBizUserMessageByMsgId(Long msgId)
    {
        return bizUserMessageMapper.selectBizUserMessageByMsgId(msgId);
    }

    /**
     * 查询站内系统消息列表
     * 
     * @param bizUserMessage 站内系统消息
     * @return 站内系统消息
     */
    @Override
    public List<BizUserMessage> selectBizUserMessageList(BizUserMessage bizUserMessage)
    {
        return bizUserMessageMapper.selectBizUserMessageList(bizUserMessage);
    }

    /**
     * 新增站内系统消息
     * 
     * @param bizUserMessage 站内系统消息
     * @return 结果
     */
    @Override
    public int insertBizUserMessage(BizUserMessage bizUserMessage)
    {
        bizUserMessage.setCreateTime(DateUtils.getNowDate());
        return bizUserMessageMapper.insertBizUserMessage(bizUserMessage);
    }

    /**
     * 修改站内系统消息
     * 
     * @param bizUserMessage 站内系统消息
     * @return 结果
     */
    @Override
    public int updateBizUserMessage(BizUserMessage bizUserMessage)
    {
        bizUserMessage.setUpdateTime(DateUtils.getNowDate());
        return bizUserMessageMapper.updateBizUserMessage(bizUserMessage);
    }

    /**
     * 批量删除站内系统消息
     * 
     * @param msgIds 需要删除的站内系统消息主键
     * @return 结果
     */
    @Override
    public int deleteBizUserMessageByMsgIds(Long[] msgIds)
    {
        return bizUserMessageMapper.deleteBizUserMessageByMsgIds(msgIds);
    }

    /**
     * 删除站内系统消息信息
     * 
     * @param msgId 站内系统消息主键
     * @return 结果
     */
    @Override
    public int deleteBizUserMessageByMsgId(Long msgId)
    {
        return bizUserMessageMapper.deleteBizUserMessageByMsgId(msgId);
    }
}
