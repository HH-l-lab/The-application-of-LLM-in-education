import request from '@/utils/request'

// 查询学生实验过程记录列表
export function listRecord(query) {
  return request({
    url: '/record/record/list',
    method: 'get',
    params: query
  })
}

// 查询当前用户的实验记录列表
export function listMyRecord(query) {
  return request({
    url: '/record/record/user/list',
    method: 'get',
    params: query
  })
}

// 查询学生实验过程记录详细
export function getRecord(recordId) {
  return request({
    url: '/record/record/' + recordId,
    method: 'get'
  })
}

// 新增学生实验过程记录
export function addRecord(data) {
  return request({
    url: '/record/record',
    method: 'post',
    data: data
  })
}

// 修改学生实验过程记录
export function updateRecord(data) {
  return request({
    url: '/record/record',
    method: 'put',
    data: data
  })
}

// 删除学生实验过程记录
export function delRecord(recordId) {
  return request({
    url: '/record/record/' + recordId,
    method: 'delete'
  })
}
