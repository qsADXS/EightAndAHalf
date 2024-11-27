import request from "@/utils/request.ts" // 这里请替换为你的 axios 实例的路径
import {getRefreshToken} from "@/utils/auth"

interface LoginParams {
  username: string;
  password: string;
}

export async function getRefresh() {
  return request({
	  method:"get",
	  url:"/user/refresh",
	  params:{
		  refreshToken:getRefreshToken(),
	  }
  })
}

export function login({ username, password}: LoginParams) {
  const formData = new FormData();
  formData.append("username", username);
  formData.append("password", password);
  formData.append("code", password); // 仅当 code 存在时才添加

  return request({
    method: "post",
    url: "/user/login",
    headers: {
      "Content-Type": "multipart/form-data",
    },
    data: formData,
  });
}


