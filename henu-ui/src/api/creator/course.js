import request from '@/utils/request'

// 查询在线课程资源列表
export function listCourse(query) {
    return request({
        url: '/creator/course/list',
        method: 'get',
        params: query
    })
}

// 查询在线课程资源详细
export function getCourse(courseId) {
    return request({
        url: '/creator/course/' + courseId,
        method: 'get'
    })
}

// 新增在线课程资源
export function addCourse(data) {
    return request({
        url: '/creator/course',
        method: 'post',
        data: data
    })
}

// 修改在线课程资源
export function updateCourse(data) {
    return request({
        url: '/creator/course',
        method: 'put',
        data: data
    })
}

// 删除在线课程资源
export function delCourse(courseId) {
    return request({
        url: '/creator/course/' + courseId,
        method: 'delete'
    })
}
