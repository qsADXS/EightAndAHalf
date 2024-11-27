import request from "@/utils/request.ts"; // 替换为你的 axios 实例路径
/**
 * 获取关注列表
 */

export interface FanItem {
    userId: string;
    userName: string;
    avatarUrl: string;
    isSinger?:string;
    singerCategory?:string;
  }
  
  export interface FollowListResponse {
    items: FanItem[];
    total: number;
  }
  
  // 获取关注列表
  export async function getFanList(userId: string, pageNum: number, pageSize: number) {
  
      return await request({
        method: "get",
        url: "/community/follower/list",
        params: {
          user_id: userId,
          page_num: pageNum,
          page_size: pageSize,
        },
      });
  
  }