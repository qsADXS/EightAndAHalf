import request from "@/utils/request"; // 使用封装好的axios实例

/**
 * 获取用户动态列表
 * @param userId 用户ID
 * @param pageNum 当前页码
 * @param pageSize 每页大小
 * @returns Promise<any>
 */
export function getUserPosts(userId: string, pageNum = 1, pageSize = 10): Promise<any> {
  return request({
    url: "/community/blog/list",
    method: "get",
    params: {
      user_id: userId,
      page_num: pageNum,
      page_size: pageSize,
    },
  });
}


  


  