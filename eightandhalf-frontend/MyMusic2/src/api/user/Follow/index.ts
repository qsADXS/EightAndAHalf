import request from "@/utils/request"


export function follow(targetUserId:string){
    const data=new FormData();
    data.append("target_user_id",targetUserId);
    data.append("action_type","create");
    return request({
        method:"post",
        url:"/community/relation/action",
        headers:{
            "Content-Type":"multipart/form-data",
        },
        data:data,
    })

}

export function unfollow(targetUserId:string){
    const data=new FormData();
    data.append("target_user_id",targetUserId);
    data.append("action_type","delete");
    return request({
        method:"post",
        url:"/community/relation/action",
        headers:{
            "Content-Type":"multipart/form-data",
        },
        data:data,
    })

}