import request from "@/utils/request.ts"; // 替换为你的 axios 实例路径
import { getUsersInfo } from "@/api/user/getInfo"; // 根据实际路径导入 getUsersInfo 函数

// 动态项接口
interface PostItem {
  blogId: string;
  userId: string;
  username?: string;
  avatarUrl?: string;
  isSinger?: string;
  singerCategory?: string;
  blogContent: string;
  createdAt: string;
  [key: string]: any; // 允许包含其他动态返回的字段
}

// 用户项接口
interface UserItem {
  userId: string;
  userName: string;
  avatarUrl: string;
  isSinger?: string;
  singerCategory?: string;
}

// 获取用户动态接口
export async function getAllFollowPosts(limit = 1000): Promise<PostItem[]> {
  try {
    // Step 1: 获取动态列表
    const postsResponse = await request({
      method: "get",
      url: "/community/blog/all",
    });

    // 检查动态列表是否成功获取，并限制数据量
    let posts: PostItem[] = postsResponse?.data || [];
    if (posts.length > limit) {
      posts = posts.slice(0, limit); // 截取前 limit 条
    }

    // Step 2: 提取所有 userId（去重处理）
    const userIds = [...new Set(posts.map((post) => post.userId))];

    console.log("用户数组，爱来自getAllFollowPosts"+userIds)
    // Step 3: 根据 userIds 获取对应的用户信息列表
    const usersResponse = await getUsersInfo(userIds);
    console.log("用户数组，爱来自"+JSON.stringify(usersResponse))
    const usersList: UserItem[] = usersResponse|| [];

    // 将用户信息转为字典格式 { userId: userInfo }，方便快速查找
    const usersMap: { [key: string]: UserItem } = usersList.reduce((map, user) => {
      map[user.userId] = user as UserItem; // 使用类型断言
      return map;
    }, {} as { [key: string]: UserItem }); // 初始化 map 为该类型

    // Step 4: 拼接用户信息到每个动态
    const enrichedPosts: PostItem[] = posts.map((post) => {
      const userInfo = usersMap[post.userId] || {};
      return {
        ...post,
        username: userInfo.userName,
        avatarUrl: userInfo.avatarUrl,
      };
    });
    console.log("拼接成功");

    return enrichedPosts;
  } catch (error) {
    console.error("获取关注动态失败", error);
    throw error;
  }
}
