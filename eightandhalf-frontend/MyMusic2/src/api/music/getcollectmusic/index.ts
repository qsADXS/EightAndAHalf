import request from '@/utils/request'

export function getCollectMusic() {
    return request({
        url: '/music/subscribe/info',
        method: 'get'
    })

}