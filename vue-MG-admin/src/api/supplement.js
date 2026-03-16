import request from '@/utils/request'
/**
 *
 * 补剂管理相关接口
 *
 **/
// 查询列表接口
export const getSupplementPage = (params) => {
    return request({
        url: '/supplement/page',
        method: 'get',
        params
    })
}

// 删除接口
export const deleteSupplement = (ids) => {
    return request({
        url: '/supplement',
        method: 'delete',
        params: { ids }
    })
}

// 修改接口
export const editSupplement = (params) => {
    return request({
        url: '/supplement',
        method: 'put',
        data: { ...params }
    })
}

// 新增接口
export const addSupplement = (params) => {
    return request({
        url: '/supplement',
        method: 'post',
        data: { ...params }
    })
}

// 根据id查询
export const querySupplementById = (id) => {
    return request({
        url: `/supplement/${id}`,
        method: 'get'
    })
}

// 获取补剂分类列表
export const getCategoryList = (params) => {
    return request({
        url: '/category/list',
        method: 'get',
        params
    })
}

// 查询补剂列表的接口
export const querySupplementList = (params) => {
    return request({
        url: '/supplement/list',
        method: 'get',
        params
    })
}

// 文件下载预览
export const commonDownload = (params) => {
    return request({
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        url: '/common/download',
        method: 'get',
        params
    })
}

// 起售停售---批量起售停售接口
export const supplementStatusByStatus = (params) => {
    return request({
        url: `/supplement/status/${params.status}`,
        method: 'post',
        params: { id: params.id }
    })
}

// 补剂分类数据查询
export const supplementCategoryList = (params) => {
    return request({
        url: `/category/list`,
        method: 'get',
        params: { ...params }
    })
}
