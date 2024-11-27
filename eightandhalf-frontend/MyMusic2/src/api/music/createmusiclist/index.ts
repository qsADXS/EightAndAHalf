import request from '@/utils/request'

export function createMusicList(playlistName: string, playlistCoverFile: File, description: string, isPublic: number, playlist_type: string) {
    return request({
        url: '/music/playlist/create',
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data: {
            playlistName: playlistName,
            playlistCoverFile: playlistCoverFile,
            description: description,
            isPublic: isPublic,
            playlist_type: playlist_type
        }
    })

}