import request from "@/utils/request.ts"; // 替换为你的 axios 实例路径

/**
 * 点赞博客
 * @param {string} blogId - 博客的 ID
 * @returns {Promise} 请求的响应结果
 */
export async function likeBlog(blogId:string) {
  return await request({
    method: "post",
    url: "/community/like/action",
    headers: {
        "Content-Type": "multipart/form-data", // 必须为表单上传
      },
    params: {
      action_type: "create", // 点赞动作
      target_type: "blog",
      id: blogId,
    },
  });
}

/**
 * 取消点赞博客
 * @param {string} blogId - 博客的 ID
 * @returns {Promise} 请求的响应结果
 */
export async function unlikeBlog(blogId:string) {
  return await request({
    method: "post",
    url: "/community/like/action",
    headers: {
        "Content-Type": "multipart/form-data", // 必须为表单上传
      },
    params: {
      action_type: "delete", // 取消点赞动作
      target_type: "blog",
      id: blogId,
    },
  });
}

export async function likeComment(blogId:string) {
  return await request({
    method: "post",
    url: "/community/like/action",
    headers: {
        "Content-Type": "multipart/form-data", // 必须为表单上传
      },
    params: {
      action_type: "create", // 点赞动作
      target_type: "comment",
      id: blogId,
    },
  });
}

export async function unlikeComment(blogId:string) {
  return await request({
    method: "post",
    url: "/community/like/action",
    headers: {
        "Content-Type": "multipart/form-data", // 必须为表单上传
      },
    params: {
      action_type: "delete", // 点赞动作
      target_type: "comment",
      id: blogId,
    },
  });
}
