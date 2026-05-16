import request from '@/utils/request'

// 查询站内系统消息列表
export function listMessage(query) {
  return request({
    url: '/message/message/list',
    method: 'get',
    params: query
  })
}

// 查询站内系统消息详细
export function getMessage(msgId) {
  return request({
    url: '/message/message/' + msgId,
    method: 'get'
  })
}

// 新增站内系统消息
export function addMessage(data) {
  return request({
    url: '/message/message',
    method: 'post',
    data: data
  })
}

// 修改站内系统消息
export function updateMessage(data) {
  return request({
    url: '/message/message',
    method: 'put',
    data: data
  })
}

// 删除站内系统消息
export function delMessage(msgId) {
  return request({
    url: '/message/message/' + msgId,
    method: 'delete'
  })
}

// 查询当前用户的消息列表
export function listMyMessage(query) {
  return request({
    url: '/message/message/user/list',
    method: 'get',
    params: query
  })
}

// 标记消息为已读
export function markMessageRead(msgId) {
  return request({
    url: '/message/message/user/read/' + msgId,
    method: 'post'
  })
}
