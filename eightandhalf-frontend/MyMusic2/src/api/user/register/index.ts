import request from "@/utils/request.ts" // 这里请替换为你的 axios 实例的路径


interface RegisterParams {
  username: string;
  password: string;
  email: string; 
}



export function Register({ username, password, email}:RegisterParams) {
  const formData = new FormData();
  formData.append("username", username);
  formData.append("password", password);
  formData.append("email", email);
  
  return request({
    method: "post",
    url: "/user/register",
    headers: {
      "Content-Type": "multipart/form-data",
    },
    data: formData,
  });
}