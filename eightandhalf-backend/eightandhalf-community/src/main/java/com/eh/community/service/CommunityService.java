package com.eh.community.service;

import com.eh.common.exception.CustomException;
import com.eh.community.model.dto.BlogDTO;
import com.eh.community.model.dto.CommentDTO;
import com.eh.community.model.dto.RelationUserDTO;
import com.eh.community.model.request.PublishBlogRequest;
import com.eh.community.pack.ListData;

import java.util.List;

public interface CommunityService {

    List<BlogDTO> ListAllBlogs(Long user_id);

    void publishBlog(PublishBlogRequest request,String url, Long user_id);

    void deleteBlog(Long blog_id, Long user_id) throws CustomException;

    List<BlogDTO> ListBlogs(Long user_id);

    void CreateRelation(Long follower_user_id, Long user_id) throws CustomException;

    void DeleteRelation(Long follower_user_id, Long user_id) throws CustomException;

    ListData<RelationUserDTO> ListFollowers(Long user_id, int limit, int offset);

    ListData<RelationUserDTO> ListFollowing(Long user_id, int limit, int offset);

    void RepostBlog(Long blog_id, Long user_id) throws CustomException;

    List<RelationUserDTO> ListFriends(Long user_id);

    List<RelationUserDTO> RefreshFriends(Long user_id);

    void CreateLikeInComment(Long user_id, Long comment_id) throws CustomException;

    void CreateLikeInBlog(Long user_id, Long blog_id) throws CustomException;

    void DeleteLikeInComment(Long user_id, Long comment_id) throws CustomException;

    void DeleteLikeInBlog(Long user_id, Long blog_id) throws CustomException;

    void CreateCommentInComment(Long user_id, String content, Long parent_id);

    void CreateCommentInMusic(Long user_id, Long id, String content);

    void CreateCommentInBlog(Long user_id, String content, Long blog_id);

    void DeleteComment(Long user_id, Long comment_id) throws CustomException;

    List<CommentDTO> ListMusicComment(Long music_id, int page_size, int page_num);

    List<CommentDTO> ListSubComments(Long parent_id, int page_size, int page_num);


    List<CommentDTO> ListBlogComments(Long blog_id, int page_size, int page_num);
}
