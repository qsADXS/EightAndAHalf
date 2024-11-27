import request from '@/utils/request'

export function searchComplete(prefix: string) {
    return request({
        url: '/music/suggestion',
        method: 'GET',
        params: {
            prefix: prefix,
        }
    })
}