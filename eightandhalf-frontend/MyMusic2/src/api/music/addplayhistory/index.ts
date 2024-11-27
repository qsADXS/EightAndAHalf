import request from '@/utils/request';

export function addPlayHistory(music_id: number) {
    return request({
        url: '/music/song/recent/action',
        method: 'post',
        params: {
            music_id: music_id
        }
    })
}