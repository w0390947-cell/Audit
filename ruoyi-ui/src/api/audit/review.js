import request from '@/utils/request'

// 查询审核列表
export function listReview(query) {
  return request({
    url: '/audit/review/list',
    method: 'get',
    params: query
  })
}

// 查询审核详情
export function getReview(taskId, versionId) {
  return request({
    url: '/audit/review/' + taskId,
    method: 'get',
    params: { versionId }
  })
}

// 查询版本列表
export function listReviewVersions(taskId) {
  return request({
    url: '/audit/review/version/list/' + taskId,
    method: 'get'
  })
}

// 查询经办人列表
export function listReviewOperators() {
  return request({
    url: '/audit/review/operators',
    method: 'get'
  })
}

// 新增审核任务
export function addReview(data) {
  return request({
    url: '/audit/review',
    method: 'post',
    data
  })
}

// 修改审核任务
export function updateReview(data) {
  return request({
    url: '/audit/review',
    method: 'put',
    data
  })
}

// 暂停/恢复审核任务
export function changeReviewProcessFlag(data) {
  return request({
    url: '/audit/review/changeProcessFlag',
    method: 'put',
    data
  })
}

// 删除审核任务
export function delReview(taskId) {
  return request({
    url: '/audit/review/' + taskId,
    method: 'delete'
  })
}
