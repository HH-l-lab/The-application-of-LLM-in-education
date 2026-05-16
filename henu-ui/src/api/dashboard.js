import request from '@/utils/request'

// 首页统计数据
export function getDashboardStats() {
    return request({
        url: '/dashboard/stats',
        method: 'get'
    })
}

// 创作者看板统计
export function getCreatorStats() {
    return request({
        url: '/dashboard/creator',
        method: 'get'
    })
}

// 首页公告列表
export function getDashboardNotices() {
    return request({
        url: '/dashboard/notices',
        method: 'get'
    })
}

// 学情诊断数据
export function getDiagnosticStats() {
    return request({
        url: '/dashboard/diagnostic',
        method: 'get'
    })
}
