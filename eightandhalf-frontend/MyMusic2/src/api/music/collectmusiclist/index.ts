import request from '@/utils/request'

export function collectMusicList(playlistId: number, type: number) {
    return request({
        url: 'music/playlist/collect',
        method: 'POST',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data: {
            playlistId,
            type
        }
    })

}