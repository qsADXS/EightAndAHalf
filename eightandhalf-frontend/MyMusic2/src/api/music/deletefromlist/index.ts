import request from '@/utils/request'

export function deleteFtomList(musicId: string, playlistId: string) {
    return request({
        url: '/music/song/delete',
        method: 'DELETE',
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        data: {
            musicId: musicId,
            playlistId: playlistId
        }
    })
}