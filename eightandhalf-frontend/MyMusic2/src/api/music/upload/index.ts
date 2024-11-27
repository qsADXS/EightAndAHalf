import request from '@/utils/request';

// 登录
export function uploadMusicAPI(data: FormData) {
    return request({
        url: '/music/upload',
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}