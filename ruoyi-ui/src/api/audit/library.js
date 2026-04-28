import request from '@/utils/request'

export function listLibraryFolders(query) {
  return request({
    url: '/audit/library/folder/list',
    method: 'get',
    params: query
  })
}

export function listLibraryFolderOptions() {
  return request({
    url: '/audit/library/folder/options',
    method: 'get'
  })
}

export function addLibraryFolder(data) {
  return request({
    url: '/audit/library/folder',
    method: 'post',
    data
  })
}

export function updateLibraryFolder(data) {
  return request({
    url: '/audit/library/folder',
    method: 'put',
    data
  })
}

export function delLibraryFolder(folderId) {
  return request({
    url: '/audit/library/folder/' + folderId,
    method: 'delete'
  })
}

export function listCommonResource(query) {
  return request({
    url: '/audit/library/common/list',
    method: 'get',
    params: query
  })
}

export function getCommonResource(resourceId) {
  return request({
    url: '/audit/library/common/' + resourceId,
    method: 'get'
  })
}

export function addCommonResource(data) {
  return request({
    url: '/audit/library/common',
    method: 'post',
    data
  })
}

export function updateCommonResource(data) {
  return request({
    url: '/audit/library/common',
    method: 'put',
    data
  })
}

export function assignCommonResourceFolder(data) {
  return request({
    url: '/audit/library/common/assignFolder',
    method: 'put',
    data
  })
}

export function delCommonResource(resourceId) {
  return request({
    url: '/audit/library/common/' + resourceId,
    method: 'delete'
  })
}

export function listTaskResource(query) {
  return request({
    url: '/audit/library/task/list',
    method: 'get',
    params: query
  })
}

export function reuploadTaskResource(data) {
  return request({
    url: '/audit/library/task/reupload',
    method: 'put',
    data
  })
}

export function delTaskResource(resourceId) {
  return request({
    url: '/audit/library/task/' + resourceId,
    method: 'delete'
  })
}
