package com.eh.community.controller;

import com.eh.common.config.FileConstant;
import com.eh.common.exception.CustomException;
import com.eh.common.util.TencentYunCOSUtil;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.common.vo.Result;
import com.eh.common.vo.ResultWIthTotal;
import com.eh.community.model.dto.BlogDTO;
import com.eh.community.model.dto.CommentDTO;
import com.eh.community.model.dto.RelationUserDTO;
import com.eh.community.model.request.*;
import com.eh.community.pack.ListData;
import com.eh.community.service.CommunityService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
@Slf4j
public class communityController{

    @Autowired
    private CommunityService communityService;

    @PostMapping("/blog/publish")
    @ApiOperation("发布博客")
    public Result PublishBlog(@ModelAttribute PublishBlogRequest request) {
        String url = new String("");
        if (request.getFile() != null && request.getFile().getOriginalFilename() != null && request.getFile().getOriginalFilename().length() > 0) {
            try {
                url = TencentYunCOSUtil.uploadBlogFile(request.getFile());
            } catch (CustomException e) {
                throw new RuntimeException(e);
            }
        }

        communityService.publishBlog(request, url, ThreadLocalUtil.get());
        return Result.success();
    }

    @DeleteMapping("/blog/delete")
    @ApiOperation("删除博客")
    public Result DeleteBlog(@ModelAttribute DeleteBlogRequest request)  {
        try {
            communityService.deleteBlog(request.getBlog_id(), ThreadLocalUtil.get());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

        return Result.success();
    }

    @GetMapping("/blog/list")
    @ApiOperation("查看用户的博客列表")
    public Result<List<BlogDTO>> ListBlogs(@RequestParam("user_id") Long user_id) {
        return Result.success(communityService.ListBlogs(user_id));
    }


    @GetMapping("/blog/all")
    @ApiOperation("查看所有关注的人的动态")
    public Result<List<BlogDTO>> AllListBlogs() {
        return Result.success(communityService.ListAllBlogs(ThreadLocalUtil.get()));
    }

    @PostMapping("/relation/action")
    @ApiOperation("关注操作")
    public Result ActionRelation(@ModelAttribute ActionRelationRequest request)  {
        Long id = ThreadLocalUtil.get();

        if (id.equals(Long.valueOf(request.getTarget_user_id()))) {
            return Result.error("illegal operation");
        }

        try {
            if (request.getAction_type().equals(FileConstant.CREATE)) {
                communityService.CreateRelation(id, Long.valueOf(request.getTarget_user_id()));
                return Result.success();
            } else if (request.getAction_type().equals(FileConstant.DELETE)) {
                communityService.DeleteRelation(id, Long.valueOf(request.getTarget_user_id()));
                return Result.success();
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }


        return Result.error("invalid relation action type");
    }

    @GetMapping("/following/list")
    @ApiOperation("关注列表")
    public Result<List<RelationUserDTO>> ListFollowing(@RequestParam("user_id") String user_id, @RequestParam("page_num") int page_num, @RequestParam("page_size") int page_size) {
        ListData<RelationUserDTO> followings = communityService.ListFollowing(Long.valueOf(user_id), page_size, page_num);
       return ResultWIthTotal.successWithTotal(followings.getData(), followings.getTotal());
    }

    @GetMapping("/follower/list")
    @ApiOperation("粉丝列表")
    public  Result<RelationUserDTO> ListFollowers(@RequestParam("user_id") String user_id, @RequestParam("page_num") int page_num, @RequestParam("page_size") int page_size) {
        ListData<RelationUserDTO> followers = communityService.ListFollowers(Long.valueOf(user_id), page_size, page_num);
        return ResultWIthTotal.successWithTotal(followers.getData(), followers.getTotal());
    }

    @PostMapping("/blog/repost")
    @ApiOperation("转发博客")
    public Result RepostBlog(@ModelAttribute RepostBlogRequest request) {
        try {
            communityService.RepostBlog(Long.valueOf(request.getBlog_id()), ThreadLocalUtil.get());

        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    @GetMapping("/friends/list")
    @ApiOperation("好友列表")
    public Result<List<RelationUserDTO>> ListFriends() {
        return Result.success(communityService.ListFriends(ThreadLocalUtil.get()));
    }

    @GetMapping("/friends/refresh")
    @ApiOperation("刷新好友列表")
    public Result<List<RelationUserDTO>> RefreshFriends() {
        return Result.success(communityService.RefreshFriends(ThreadLocalUtil.get()));
    }
    @PostMapping("/comment/action")
    @ApiOperation("发表评论")
    public Result ActionComment(@ModelAttribute ActionCommentRequest request) {
        if (request.getType().equals(FileConstant.COMMENT)) {
            communityService.CreateCommentInComment(ThreadLocalUtil.get(), request.getContent(), Long.valueOf(request.getId()));
        } else if (request.getType().equals(FileConstant.MUSIC)) {
            communityService.CreateCommentInMusic(ThreadLocalUtil.get(), Long.valueOf(request.getId()), request.getContent());
        } else if (request.getType().equals(FileConstant.BLOG)) {
            communityService.CreateCommentInBlog(ThreadLocalUtil.get(), request.getContent(), Long.valueOf(request.getId()));
        } else {
            return Result.error("invalid action type");
        }
        return Result.success();
    }

    @DeleteMapping("comment/delete")
    @ApiOperation("删除评论")
    public Result DeleteComment(@RequestParam String id) {
        try {
            communityService.DeleteComment(ThreadLocalUtil.get(), Long.valueOf(id));
        } catch (CustomException e) {
            return Result.error("Controller.DeleteComment error:", e);
        }
        return Result.success();
    }

    @PostMapping("/like/action")
    @ApiOperation("点赞/取消点赞" )
    public Result ActionLike(@ModelAttribute ActionLikeRequest request) {
        Long userId = ThreadLocalUtil.get();
        Long id = Long.valueOf(request.getId());

        try {
            if (request.getAction_type().equals(FileConstant.CREATE)) {
                if (request.getTarget_type().equals(FileConstant.COMMENT)) {
                    communityService.CreateLikeInComment(userId, id);
                } else if (request.getTarget_type().equals(FileConstant.BLOG)) {
                    communityService.CreateLikeInBlog(userId, id);
                } else {
                    return Result.error("invalid action type");
                }
            } else if (request.getAction_type().equals(FileConstant.DELETE)) {
                if (request.getTarget_type().equals(FileConstant.COMMENT)) {
                    communityService.DeleteLikeInComment(userId, id);
                } else if (request.getTarget_type().equals(FileConstant.BLOG)) {
                    communityService.DeleteLikeInBlog(userId, id);
                } else {
                    return Result.error("invalid action type");
                }
            }  else {
                return Result.error("invalid action type");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }


        return Result.success();
    }


    @GetMapping("/comment/list")
    @ApiOperation("评论列表")
    public  Result<List<CommentDTO>> ListComment(@RequestParam String type, @RequestParam String id, @RequestParam int page_num, @RequestParam int page_size) {

        if (type.equals(FileConstant.COMMENT)) {
            return Result.success(communityService.ListSubComments(Long.valueOf(id), page_size, page_num));
        } else if(type.equals(FileConstant.MUSIC)) {
          return   Result.success( communityService.ListMusicComment(Long.valueOf(id), page_size, page_num));
        } else if (type.equals(FileConstant.BLOG)) {
            return Result.success( communityService.ListBlogComments(Long.valueOf(id), page_size, page_num));
        } else {
            return Result.error("invalid action type");
        }
    }

    }
