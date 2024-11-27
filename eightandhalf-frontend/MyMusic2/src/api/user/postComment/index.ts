import request from "@/utils/request.ts";

interface CommentParams {
    content: string;    //内容
    type: string;       //music comment blog
    id: string;         // commentId和
  }
export function Comment({ content, type, id }: CommentParams) {
    const formData = new FormData();
    formData.append("content", content);
    formData.append("type", type);
    formData.append("id",id)
    return request({
      method: "post",
      url: "/community/comment/action",
      headers: {
        "Content-Type": "multipart/form-data",
      },
      data: formData,
    });
  }
  