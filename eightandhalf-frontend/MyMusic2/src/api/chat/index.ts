// api/chat.ts
import request from "@/utils/request";

export interface Message {
  messageId: string;
  sender: string;
  receiver: string;
  content: string;
  createdAt: string;
}

export const getChatHistory = (userId: string) => {
  const formatChinaDate = () => {
    const now = new Date();
    const offset = 8 * 60 * 60 * 1000; // 中国时区相对于 UTC 的偏移量
    const chinaTime = new Date(now.getTime() + offset);
    const year = chinaTime.getUTCFullYear();
    const month = (chinaTime.getUTCMonth() + 1).toString().padStart(2, "0"); // 月份需要加1并补0
    const day = chinaTime.getUTCDate().toString().padStart(2, "0"); // 补0
    return `${year}-${month}-${day}`;
  };

  return request({
    url: `/chat/history`,
    method: "get",
    params: {
      userId,
      date: formatChinaDate(), // 使用中国时间并格式化为 yyyy-mm-dd
      pageNum: 1,
      pageSize: 50,
    },
  });
};


// 发送图片给指定用户
export const sendImageToUser = (toUserName: string, pictureFile: File) => {
    const formData = new FormData();
    formData.append("picture", pictureFile);
    formData.append("to", toUserName);
  
    return request({
      url: `/chat/image`,
      method: "post",
      headers: {
        "Content-Type": "multipart/form-data",
      },
      data: formData,
    });
  };