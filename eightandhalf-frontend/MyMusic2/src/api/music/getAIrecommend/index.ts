import request from '@/utils/request'

export function getAIrecommend() {
    return request({
        url: '/music/recommend/list',
        method: 'GET',
    })
}