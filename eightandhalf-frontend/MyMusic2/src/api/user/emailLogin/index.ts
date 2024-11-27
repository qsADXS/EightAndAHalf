import request from "@/utils/request.ts" // 这里请替换为你的 axios 实例的路径

interface LoginParams {
    email: string;
    code: string;
}

export async function getEmailCode(email:string) {
    const formData = new FormData();
    formData.append("email", email);
    return request({
        method:"post",
        url:"/user/login/email/code",
        data: formData,
    })
}

export function emailLogin({ email, code }: LoginParams) {
    const formData = new FormData();
    formData.append("email", email);
    formData.append("code", code);

    return request({
        method: "post",
        url: "/user/login/email",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data: formData,
    });
}


