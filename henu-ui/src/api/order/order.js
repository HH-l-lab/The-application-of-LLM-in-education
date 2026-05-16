import request from '@/utils/request'

// 查询课程交易订单列表
export function listOrder(query) {
  return request({
    url: '/order/order/list',
    method: 'get',
    params: query
  })
}

// 查询当前用户的订单列表
export function listMyOrder(query) {
  return request({
    url: '/order/order/user/list',
    method: 'get',
    params: query
  })
}

// 查询课程交易订单详细
export function getOrder(orderId) {
  return request({
    url: '/order/order/' + orderId,
    method: 'get'
  })
}

// 新增课程交易订单
export function addOrder(data) {
  return request({
    url: '/order/order',
    method: 'post',
    data: data
  })
}

// 修改课程交易订单
export function updateOrder(data) {
  return request({
    url: '/order/order',
    method: 'put',
    data: data
  })
}

// 删除课程交易订单
export function delOrder(orderId) {
  return request({
    url: '/order/order/' + orderId,
    method: 'delete'
  })
}
