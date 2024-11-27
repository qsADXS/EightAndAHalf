import request from "@/utils/request.ts"; // 确保路径正确

/**
 * 删除动态
 * @param {string} blogId - 要删除的动态的ID
 * @returns {Promise} - 返回删除请求的结果
 */
export async function DeletePost(blogId:string){
   const formData = new FormData();
    formData.append("blog_id",blogId);
  return request({
    method: "delete",
    url: "/community/blog/delete",
    data:formData
  });
}
