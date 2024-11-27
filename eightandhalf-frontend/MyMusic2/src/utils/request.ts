import axios, { AxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import {getAccessToken, refreshToken, setAccessToken, setRefreshToken} from "@/utils/auth"
import { getRefreshToken } from "./auth";

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 50000,
  withCredentials: true,
  
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {

    // 添加其他的参数或 header
    config.params = {
      ...config.params,
      timestamp: Date.now(), // 防止缓存
	  
    };
	 config.headers["Access-Token"] = `${getAccessToken()}`;
	 config.headers["Refresh-Token"]= `${getRefreshToken()}`;
   console.log("以为你自动添加了token:"+getAccessToken());

    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
  async (response) => {
    const { data } = response;
    /**
     * response{
     *  base
     *  data
     * }
     */
	// 检查是否包含 accessToken 和 refreshToken，如果有则存储
	if (data.data && data.data.accessToken && data.data.refreshToken) {
	  setRefreshToken(data.data.refreshToken);
	  setAccessToken(data.data.accessToken);
    console.log("检测到登录，设置token成功");
	}
  //console.log("爱来自拦截器："+JSON.stringify(response));
	if(response.status===401||(data.base&&(data.base.code===401))){
    console.log("会话已过期");
		const sucess=await refreshToken();
		if(sucess){
			response.config.headers["Access-Token"]=`${getAccessToken()}`;
			const result=await service.request(response.config);
			return result;
		}
	}
   
    return data;
  },
  async (error) => {
    const {response}=error;
    if(response&&response.status===401){
      console.log("拦截器捕获到错误401，会话过期");
      const sucess=await refreshToken();
		if(sucess){
			response.config.headers["Access-Token"]=`${getAccessToken()}`;
			const result=await service.request(response.config);
			return result;
		}
    }
    ElMessage.error(error.message || "网络错误");
    return Promise.reject(error);
  }
);

function requset(options: AxiosRequestConfig<any>){
	options.method=options.method||"get";
	if(options.method.toLowerCase()==="get"){
		options.params=options.data;
	}
	return service(options);
}

export default service;
