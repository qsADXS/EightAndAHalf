import request from "@/utils/request.ts"; // 替换为你的 axios 实例路径


// 定义用户信息接口，修正结构
export interface UserInfo {
  userId: string;
  userName: string;
  avatarUrl: string; // 确保使用 avatar 统一字段名称
  roles?: string[]; // 用户角色
}

// 获取自身用户信息
export async function getUserInfo() {
  return request({
    method: "get",
    url: "/user/info",
  });
}

export async function getUsersInfo(ids: string[]) {
  const queryParams = ids.map(id => `ids=${encodeURIComponent(id)}`).join('&');
  console.log("queryParams:", queryParams);
  return request({
    method: "get",
    url: `/user/listAll?${queryParams}`,  // 手动构造查询字符串
  });
}

