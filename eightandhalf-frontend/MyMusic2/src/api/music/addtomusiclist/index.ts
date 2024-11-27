import request from '@/utils/request'

export function addToMusicList(musicId: string, playlistId: string) {
    return request({
        url: '/music/song/collect',
        method: 'POST',
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data: {
            musicId: musicId,
            playlistId: playlistId
        },

    })
}