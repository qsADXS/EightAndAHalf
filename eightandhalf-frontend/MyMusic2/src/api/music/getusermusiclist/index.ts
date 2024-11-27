import request from '@/utils/request'

export function getUserMusicList(id: string, relationship_type: number) {
    return request({
        url: '/music/playlist/list',
        method: 'GET',
        params: {
            user_id: id,
            relationship_type: relationship_type,
        }
    })
}
