import request from '@/utils/request'

// 查询视频AI知识提取结果列表
export function listExtraction(query) {
  return request({
    url: '/extraction/extraction/list',
    method: 'get',
    params: query
  })
}

// 查询视频AI知识提取结果详细
export function getExtraction(extractionId) {
  return request({
    url: '/extraction/extraction/' + extractionId,
    method: 'get'
  })
}

// 新增视频AI知识提取结果
export function addExtraction(data) {
  return request({
    url: '/extraction/extraction',
    method: 'post',
    data: data
  })
}

// 修改视频AI知识提取结果
export function updateExtraction(data) {
  return request({
    url: '/extraction/extraction',
    method: 'put',
    data: data
  })
}

// 删除视频AI知识提取结果
export function delExtraction(extractionId) {
  return request({
    url: '/extraction/extraction/' + extractionId,
    method: 'delete'
  })
}
