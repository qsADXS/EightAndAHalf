// 获取评论列表
import request from "@/utils/request.ts";
export async function getCommentList(page_num:string,page_size:string, type:string, id: string) {
  
    return await request({
      method: "get",
      url: "/community/comment/list",
      params: {
        page_num:page_num,
        page_size:page_size,
        type:type,
        id:id,
      },
    });

}
