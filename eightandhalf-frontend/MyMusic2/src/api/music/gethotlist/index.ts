import request from '@/utils/request'

export function getRecommendMusicAPI() {
    return request({
        url: '/music/home/list',
        method: 'GET',
        params: {
            limit: 20,
        }

    })
}