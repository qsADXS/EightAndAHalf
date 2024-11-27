// auth.ts
import {getRefresh} from "@/api/user/login/index"
let promise: Promise<any> | null;

const accessTokenKey = 'Access-Token';
const refreshTokenKey = 'Refresh-Token';

/**
 * 设置 accessToken
 */
export const setAccessToken = (token: string) => {
  localStorage.setItem(accessTokenKey, token);
};

/**
 * 设置 refreshToken
 */
export const setRefreshToken = (token: string) => {
  localStorage.setItem(refreshTokenKey, token);
};

/**
 * 获取 accessToken
 */
export const getAccessToken = () => {
  return localStorage.getItem(accessTokenKey);
};

/**
 * 获取 refreshToken
 */
export const getRefreshToken = () => {
  return localStorage.getItem(refreshTokenKey);
};

/**
 * 删除 accessToken 和 refreshToken
 */
export const removeTokens = () => {
  localStorage.removeItem(accessTokenKey);
  localStorage.removeItem(refreshTokenKey);
};

export function refreshToken(){
	if(promise){
		return promise;
	}
	
	promise=new Promise(async(reslove)=>{
		const res=await getRefresh();
		localStorage.setItem("Access-Token",res.data.accessToken);
		localStorage.setItem("Refresh-Token",res.data.refreshToken);
		console.log("重新设置Access-Token");
		reslove(res);
	})
	
	promise.finally(()=>{
		promise=null;
	})
	
	return promise;
}

