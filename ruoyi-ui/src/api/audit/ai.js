import request from '@/utils/request'

// 查询 AI 任务队列
export function listAiTask(query) {
  return request({
    url: '/audit/ai/list',
    method: 'get',
    params: query
  })
}

// 查询 AI 队列统计
export function getAiStats() {
  return request({
    url: '/audit/ai/stats',
    method: 'get'
  })
}

// 查询 AI 任务提交人
export function listAiSubmitters() {
  return request({
    url: '/audit/ai/submitters',
    method: 'get'
  })
}

// 查询 AI 任务详情
export function getAiTask(aiTaskId) {
  return request({
    url: '/audit/ai/' + aiTaskId,
    method: 'get'
  })
}

// 查询 AI 任务报告预览文件
export function getAiReportPreview(aiTaskId) {
  return request({
    url: '/audit/ai/' + aiTaskId + '/reportPreview',
    method: 'get'
  })
}

// 批量切换 AI 任务状态
export function changeAiTaskStatus(data) {
  return request({
    url: '/audit/ai/changeTaskStatus',
    method: 'put',
    data
  })
}

// 批量提升 AI 任务优先级
export function raiseAiPriority(data) {
  return request({
    url: '/audit/ai/raisePriority',
    method: 'put',
    data
  })
}

// 人工审核 AI 任务
export function reviewAiTask(data) {
  return request({
    url: '/audit/ai/reviewDecision',
    method: 'put',
    data
  })
}

// 删除 AI 任务
export function delAiTask(aiTaskIds) {
  return request({
    url: '/audit/ai/' + aiTaskIds,
    method: 'delete'
  })
}
