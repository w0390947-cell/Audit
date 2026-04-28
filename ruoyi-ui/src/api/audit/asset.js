import request from '@/utils/request'

export function listAsset(query) {
  return request({
    url: '/audit/asset/list',
    method: 'get',
    params: query
  })
}

export function getAsset(assetId) {
  return request({
    url: '/audit/asset/' + assetId,
    method: 'get'
  })
}

export function listAssetStats() {
  return request({
    url: '/audit/asset/stats',
    method: 'get'
  })
}

export function listAssetReviewers() {
  return request({
    url: '/audit/asset/reviewers',
    method: 'get'
  })
}

export function assignAssetPermission(data) {
  return request({
    url: '/audit/asset/assignPermission',
    method: 'put',
    data
  })
}

export function batchDownloadAsset(data) {
  return request({
    url: '/audit/asset/batchDownload',
    method: 'post',
    data
  })
}

export function batchPackageAsset(data) {
  return request({
    url: '/audit/asset/batchPackage',
    method: 'post',
    data
  })
}

export function reuploadAsset(data) {
  return request({
    url: '/audit/asset/reupload',
    method: 'put',
    data
  })
}

export function delAsset(assetId) {
  return request({
    url: '/audit/asset/' + assetId,
    method: 'delete'
  })
}
