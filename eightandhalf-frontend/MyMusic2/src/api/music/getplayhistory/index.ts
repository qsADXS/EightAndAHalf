import request from '@/utils/request'

export function getPlayHistory() {
    return request({
        url: '/music/recent/info',
        method: 'get'
    })

}