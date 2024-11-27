import request from "@/utils/request.ts";

export function searchMusicAPI(key: string) {
    return request({
        method: "post",
        url: "/music/search",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data: {
            key: key,
        },
    });
}
