package com.eh.community.mapper;

import com.eh.community.model.entity.Blog;
import com.eh.community.model.entity.Comment;
import com.eh.community.model.entity.Like;
import com.eh.community.model.entity.Relation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CommunityMapper {
    void createBlog(Blog blog);

    Blog[] getBlogsByUid(Long user_id);

    void deleteBlogByBlogId(Long blog_id);

    Blog getBlogsByUidAndBlogId(Long user_id, Long blog_id);

    void createRelation(Relation relation);

    void deleteRelation(Relation relation);

    Relation[] getFollowersByUid(Long user_id, int page_size, int page_num);

    Relation[] getFollowingListByUid(Long follower_user_id);

    Blog getBlogsByBlogId(Long blog_id);

    Relation[] getFollowingsByUid(@Param("userIds")List<Long> userIds);

    Relation[] getFollowersListByUid(Long user_id);

    Relation getFollowByUidAndFid(Relation relation);

    void createComment(Comment comment);

    Comment getCommentByCid(Long commentId);

    void deleteComment(Long commentId);

    int getFollowersCountByUid(Long user_id);

    int getFollowingCountByUid(Long user_id);

    void createLike(Like like);

    void deleteLikeInComment(Like like);

    void deleteLikeInBlog(Like like);

    Like getUserLikeByCid(Like like);

    Like getUserLikeByBid(Like like);

    Comment[] getCommentsByCid(Long parentId);

    Comment[] getCommentsByMusicId(Long musicId);

    Comment[] getCommentsByBlogId(Long blogId);

    void incrCommentLikeCount(Long commentId);

    void decrCommentLikeCount(Long commentId);

    void incrBlogLikeCount(Long blogId);

    void decrBlogLikeCount(Long blogId);

    void deleteAllLikeInComment(Long commentId);

    void incrParentCommentCount(Long commentId);

    void decrParentCommentCount(Long commentId);

    void incrBlogCommentCount(Long blogId);

    void decrBlogCommentCount(Long blogId);

    void deleteAllLikeInBlog(Long blogId);
}
