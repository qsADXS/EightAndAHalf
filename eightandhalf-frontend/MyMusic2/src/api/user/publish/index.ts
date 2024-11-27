import request from "@/utils/request"; // 你提供的axios实例

/**
 * 发布动态
 * @param data 表单数据，包括 title, content, file, music_id
 * @returns Promise<any>
 */
export function publishPost(data: FormData): Promise<any> {
  return request({
    url: "/community/blog/publish",
    method: "post",
    headers: {
      "Content-Type": "multipart/form-data", // 必须为表单上传
    },
    data, // 使用 FormData 格式上传
  });
}


