import request from "@/utils/request.ts"; // 替换为你的 axios 实例路径
/**
 * 获取关注列表
 */

export interface FriendItem {
    userId: string;
    userName: string;
    avatarUrl: string;
    isSinger?:string;
    singerCategory?:string;
  }
  
  export interface FollowListResponse {
    items: FriendItem[];
    total: number;
  }
  
  // 获取关注列表
  export async function getFriendList(userId: string, pageNum: number, pageSize: number) {
      await refreshFrientList();
      return await request({
        method: "get",
        url: "/community/friends/list",
        params: {
          user_id: userId,
          page_num: pageNum,
          page_size: pageSize,
        },
      });
  
  }

  export async function refreshFrientList(){
    return await request({
      method: "get",
      url: "/community/friends/list",
    });
  }