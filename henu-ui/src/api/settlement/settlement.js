import request from '@/utils/request'

// 查询创作者收益结算流水列表
export function listSettlement(query) {
  return request({
    url: '/settlement/settlement/list',
    method: 'get',
    params: query
  })
}

// 查询创作者收益结算流水详细
export function getSettlement(settlementId) {
  return request({
    url: '/settlement/settlement/' + settlementId,
    method: 'get'
  })
}

// 新增创作者收益结算流水
export function addSettlement(data) {
  return request({
    url: '/settlement/settlement',
    method: 'post',
    data: data
  })
}

// 修改创作者收益结算流水
export function updateSettlement(data) {
  return request({
    url: '/settlement/settlement',
    method: 'put',
    data: data
  })
}

// 删除创作者收益结算流水
export function delSettlement(settlementId) {
  return request({
    url: '/settlement/settlement/' + settlementId,
    method: 'delete'
  })
}

// 查询当前创作者的结算流水列表
export function listMySettlement(query) {
  return request({
    url: '/settlement/settlement/user/list',
    method: 'get',
    params: query
  })
}

// 一键结算
export function settleAll() {
  return request({
    url: '/settlement/settlement/user/settle',
    method: 'post'
  })
}
