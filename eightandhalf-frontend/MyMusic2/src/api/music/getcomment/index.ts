import request from '@/utils/request'

export function getComment(id: string, type: string, page_size: string, page_num: string) {
    return request({
        url: '/community/comment/list',
        method: 'GET',
        params: {
            id: id,
            type: type,
            page_size: page_size,
            page_num: page_num,
        }
    })

}