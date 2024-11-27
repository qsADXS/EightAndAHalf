import request from '@/utils/request'

export function getContentofList(id: number) {
    return request({
        url: '/music/playlist/info',
        method: 'GET',
        params: {
            playlist_id: id,
        }

    })
}