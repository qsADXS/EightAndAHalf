import request from '@/utils/request';

export function addComment(content: string, type: string, id: string) {
    return request({
        url: '/community/comment/action',
        method: 'post',
        params: {
            content: content,
            type: type,
            id: id
        }
    })


}