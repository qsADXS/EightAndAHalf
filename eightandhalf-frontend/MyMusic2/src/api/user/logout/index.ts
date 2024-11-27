import {removeTokens} from "@/utils/auth"
import { useUserInfoStore } from "@/store/userStore";



export function Mylogout(){
    removeTokens();
    const userInfoStore=useUserInfoStore();
    userInfoStore.logout();
     // 刷新页面
    window.location.reload();
}