import {defineStore} from "pinia"
import { getUserInfo} from '@/api/user/getInfo'; // 假设您的接口文件路径
import defaultAvatar from "@/assets/avator/xingyue.jpg"; // 静态资源导入
import { getFanList } from "@/api/user/getFans";
import { getFollowList } from "@/api/user/getFollowers";

export const useUserInfoStore = defineStore('userInfo', {
  state: () => ({
    userInfo:{
        userId:"",
        userName:"",
        avatarUrl:"",
    },
    totalFans:0,
    totalFollowers:0,
  }),
  persist:true,
  getters: {
   
    getUserId: (state) => state.userInfo.userId || null,
    getUserName: (state) => state.userInfo.userName || "未登录",
    getUserAvatar: (state) => state.userInfo.avatarUrl || defaultAvatar,
    getUserTotalFans: (state) => state.totalFans || "",
    getUserTotalFollowers: (state) => state.totalFollowers ||"",
    
    
  },
  actions: {
  
    // 获取并设置用户信息
    async fetchUserInfo() {
      try {
        const response = await getUserInfo();
        this.userInfo = response.data;
        const FansResponse=await getFanList(this.userInfo.userId,0,1000);
        const FollowerResponse= await getFollowList(this.userInfo.userId,0,1000);
        this.totalFans=FansResponse.total;
        this.totalFollowers=FollowerResponse.total;
      } catch (error) {
        console.error('获取用户信息失败', error);
        throw error;
      }
    },

    logout(){
      this.userInfo={
        userId:"",
        userName:"",
        avatarUrl:"",
      }
      this.totalFans=0;
      this.totalFollowers=0;
      
    },
    
  },
 
  
});
