import request from "@/utils/request"
/**
 * 
 * @param blogId 
 * @returns {response.data}
 * 
 */
export function transmit(blogId:string) {
    const formData = new FormData();
    formData.append("blog_id", blogId);
    return request({
      method: "post",
      url: "/community/blog/repost",
      headers: {
        "Content-Type": "multipart/form-data",
      },
      data: formData,
    });
  }