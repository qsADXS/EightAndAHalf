import request from '@/utils/request';

export function getSongByIdAPI(id: number) {
    return request({
        url: '/music/song/info',
        method: 'get',
        params: {
            music_id: id
        }
    })

}