import request from '@/utils/request'

// 查询在线课程资源列表
export function listCourse(query) {
  return request({
    url: '/course/course/list',
    method: 'get',
    params: query
  })
}

// 查询在线课程资源详细
export function getCourse(courseId) {
  return request({
    url: '/course/course/' + courseId,
    method: 'get'
  })
}

// 新增在线课程资源
export function addCourse(data) {
  return request({
    url: '/course/course',
    method: 'post',
    data: data
  })
}

// 修改在线课程资源
export function updateCourse(data) {
  return request({
    url: '/course/course',
    method: 'put',
    data: data
  })
}

// 删除在线课程资源
export function delCourse(courseId) {
  return request({
    url: '/course/course/' + courseId,
    method: 'delete'
  })
}

// 播放量+1
export function playCourse(courseId) {
  return request({
    url: '/course/course/play/' + courseId,
    method: 'put'
  })
}
