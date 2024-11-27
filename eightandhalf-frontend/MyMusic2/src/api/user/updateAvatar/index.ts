import request from "@/utils/request.ts" // 这里请替换为你的 axios 实例的路径
/**
 * 更新用户头像
 * @param {FormData} formData 包含用户上传头像的文件
 * @returns {Promise} 返回请求的 Promise
 */

export function updateAvatar(formData: any) {
    return request({
      url: '/user/avatar', // 假设后端接口为 /user/avatar
      method: 'put',
      headers: {
        'Content-Type': 'multipart/form-data', // 必须指定为 form-data
      },
      data: formData,
    });
  }
