import request from '@/utils/request'

export function colletSong(musicId: string) {
    console.log(musicId)
    return request({
        url: '/music/song/subscribe',
        method: 'POST',
        params: {
            music_id: musicId,
        }
    })

}