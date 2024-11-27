import request from "@/utils/request.ts" // 这里请替换为你的 axios 实例的路径

interface bindParams {
    code: number;
    secret: string;
}

export async function getQrcode() {
    const formData = new FormData();
    return request({
        method:"get",
        url:"/user/mfa/qrcode",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data: formData,
    })
}

export async function bindcode({ code, secret }: LoginParams) {
    const formData = new FormData();
    formData.append("code", code);
    formData.append("secret", secret);
    console.log(code, secret)
    return request({
        method: "post",
        url: "/user/mfa/bind",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data: formData,
    });
}


