import request from '@/utils/request.ts'

export function getRankListAPI(num: number) {
    return request({
        method: "GET",
        url: "music/popular/rank",
        params: {
            num: num,
        }
    });

}