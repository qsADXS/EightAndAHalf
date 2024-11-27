import request from '@/utils/request';

export function addPlayVolume(music_id: number) {
    return request({
        url: '/music/visit/update',
        method: 'post',
        params: {
            music_id: music_id
        }
    })
}