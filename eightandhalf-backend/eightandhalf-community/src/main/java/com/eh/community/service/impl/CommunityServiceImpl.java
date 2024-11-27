package com.eh.community.service.impl;

import com.eh.api.client.MusicClient;
import com.eh.api.client.UserClient;
import com.eh.common.dto.UserDTO;
import com.eh.common.exception.CustomException;
import com.eh.common.util.SnowFlakeUtil;
import com.eh.community.cache.RedisService;
import com.eh.community.listener.CommunityListener;
import com.eh.community.mapper.CommunityMapper;
import com.eh.community.model.dto.BlogDTO;
import com.eh.community.model.dto.CommentDTO;
import com.eh.community.model.dto.RelationUserDTO;
import com.eh.community.model.entity.Blog;
import com.eh.community.model.entity.Comment;
import com.eh.community.model.entity.Like;
import com.eh.community.model.entity.Relation;
import com.eh.community.model.request.PublishBlogRequest;
import com.eh.community.pack.ListData;
import com.eh.community.service.CommunityService;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static com.eh.community.pack.ListData.SubList;

@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private MusicClient musicClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisService redisService;

    @Override
    public List<BlogDTO> ListAllBlogs(Long user_id) {

        Relation[] relation = communityMapper.getFollowingListByUid(user_id);
        List<BlogDTO> blogs = new ArrayList<>();

        for (Relation r : relation) {
            List<BlogDTO> dto = ListBlogs(r.getUserId());
            blogs.addAll(dto);
        }

        Collections.sort(blogs, new Comparator<BlogDTO>() {
            @Override
            public int compare(BlogDTO o1, BlogDTO o2) {
                return o2.getBlogId().compareTo(o1.getBlogId());
            };
        });

        return blogs;
    }

    @Override
    public void publishBlog(PublishBlogRequest request, String url, Long user_id) {
        String key = redisService.getBlogKey(String.valueOf(user_id));

        Blog blog = new Blog();
        blog.setUrl(url);
        blog.setUserId(user_id);
        blog.setTitle(request.getTitle());
        blog.setBlogContent(request.getContent());
        if (request.getMusic_id() != null && request.getMusic_id().length() > 0) {
            blog.setMusicId(Long.valueOf(request.getMusic_id()));
        }

        blog.setBlogId(SnowFlakeUtil.getSnowFlakeId());
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());

        threadPoolExecutor.execute(() -> {
            if (redisService.IsExists(key)) {
                redisService.removeInfo(key);
            }
        });
        communityMapper.createBlog(blog);

    }

    @Override
    public void deleteBlog(Long blog_id, Long user_id) throws CustomException {
        String key = redisService.getBlogKey(String.valueOf(user_id));
        Blog blog = communityMapper.getBlogsByUidAndBlogId(user_id, blog_id);
        if (blog != null) {

            threadPoolExecutor.execute(() -> {
                if (redisService.IsExists(key)) {
                    redisService.removeInfo(key);
                }
            });

            communityMapper.deleteAllLikeInBlog(blog_id);
            communityMapper.deleteBlogByBlogId(blog_id);
        } else {
            throw new CustomException(CustomException.targetNotFound);
        }
    }

    @Override
    public List<BlogDTO> ListBlogs(Long user_id) {

        String key = redisService.getBlogKey(String.valueOf(user_id));

        if (redisService.IsExists(key)) {
            return (List<BlogDTO>) redisService.getData(key);
        }

        Blog[] blogs = communityMapper.getBlogsByUid(user_id);

        List<BlogDTO> blogDTOS = new ArrayList<>();
        for (Blog blog : blogs) {
            BlogDTO blogDTO = new BlogDTO();
            blogDTO.ConvertToBlogDTO(blog);
            if (redisService.IsExists((redisService.getLikeInBlogKey(String.valueOf(blog.getBlogId())))) ){
                blogDTO.setLikeCount(redisService.getLikeCount(redisService.getLikeInBlogKey(String.valueOf(blog.getBlogId()))));;

            }
            blogDTOS.add(blogDTO);
        }

        redisService.setData(key, blogDTOS, 2);
        return blogDTOS;
    }

    @Override
    public void CreateRelation(Long follower_user_id, Long user_id) throws CustomException{
        Relation relation = new Relation();
        relation.setUserId(user_id);
        relation.setFollowerUserId(Long.valueOf(follower_user_id));
        Relation res = communityMapper.getFollowByUidAndFid(relation);
        if (res == null) {
            threadPoolExecutor.execute(() -> {
                redisService.removeInfo(redisService.getFollowingKey(String.valueOf(follower_user_id)));
            });

            threadPoolExecutor.execute(() -> {
                redisService.removeInfo(redisService.getFollowerKey(String.valueOf(user_id)));
            });

            communityMapper.createRelation(relation);
        } else {
            throw new CustomException(CustomException.repeatedOperations);
        }
    }

    @Override
    public void DeleteRelation(Long follower_user_id, Long user_id) throws CustomException {
        Relation relation = new Relation();
        relation.setUserId(user_id);
        relation.setFollowerUserId(follower_user_id);
        Relation res = communityMapper.getFollowByUidAndFid(relation);
        if (res != null) {

            threadPoolExecutor.execute(() -> {
                redisService.removeInfo(redisService.getFollowingKey(String.valueOf(follower_user_id)));
            });

            threadPoolExecutor.execute(() -> {
                redisService.removeInfo(redisService.getFollowerKey(String.valueOf(user_id)));
            });

            communityMapper.deleteRelation(relation);
        } else {
            throw new CustomException(CustomException.repeatedOperations);
        }
    }

    @Override
    public ListData<RelationUserDTO> ListFollowers(Long user_id, int limit, int offset) {
        String key = redisService.getFollowerKey(String.valueOf(user_id));
        List<Long> uids = new ArrayList<>();
        ListData<RelationUserDTO> data = new ListData<>();

        if (redisService.IsExists(key)) {
            data = (ListData<RelationUserDTO>) redisService.getData(key);
            return new ListData<>(SubList(limit, offset, data.getData()), data.getTotal());
        }

        Relation[] relations = communityMapper.getFollowersListByUid(user_id);
        int count = communityMapper.getFollowersCountByUid(user_id);

        for (Relation relation : relations) {
            uids.add(relation.getFollowerUserId());
        }

        List<UserDTO> userInfo = userClient.getUsers(uids);
        List<RelationUserDTO> relationUserDTO = new ArrayList<>();

        for (int i = 0; i < userInfo.size(); i++) {
            RelationUserDTO dto = new RelationUserDTO();
            dto.ConvertToRelationUserDTO(userInfo.get(i));
            relationUserDTO.add(dto);
        }

        data.setData(relationUserDTO);
        data.setTotal(count);
        redisService.setData(key, data);

        return new ListData<>(SubList(limit, offset, data.getData()), count);
    }

    @Override
    public ListData<RelationUserDTO> ListFollowing(Long user_id, int limit, int offset) {
        String key = redisService.getFollowingKey(String.valueOf(user_id));
        List<Long> uids = new ArrayList<>();
        ListData<RelationUserDTO> data = new ListData<>();

        if (redisService.IsExists(key)) {
            data = (ListData<RelationUserDTO>) redisService.getData(key);
            return new ListData<>(SubList(limit, offset, data.getData()), data.getTotal());
        }

        Relation[] relations = communityMapper.getFollowingListByUid(user_id);
        int count = communityMapper.getFollowingCountByUid(user_id);

        for (Relation relation : relations) {
            uids.add(relation.getUserId());
        }

        List<UserDTO> userInfo = userClient.getUsers(uids);
        List<RelationUserDTO> relationUserDTO = new ArrayList<>();

        for (int i = 0; i < userInfo.size(); i++) {
            RelationUserDTO dto = new RelationUserDTO();
            dto.ConvertToRelationUserDTO(userInfo.get(i));
            relationUserDTO.add(dto);
        }

        data.setData(relationUserDTO);
        data.setTotal(count);
        redisService.setData(key, data);


        return new ListData<>(SubList(limit, offset, data.getData()), count);

    }

    @Override
    public void RepostBlog(Long blog_id, Long user_id) throws CustomException {
        String key = redisService.getBlogKey(String.valueOf(user_id));
        Blog blog = communityMapper.getBlogsByBlogId(blog_id);
        if (blog == null) {
            throw new CustomException(CustomException.targetNotFound);
        }
        Blog repostBlog = new Blog();

            repostBlog.setBlogId(SnowFlakeUtil.getSnowFlakeId());
            repostBlog.setUserId(user_id);
            repostBlog.setUrl(blog.getUrl());
            repostBlog.setTitle(blog.getTitle());
        if (blog.getMusicId() != null && blog.getMusicId() > 0) {
            blog.setMusicId(Long.valueOf(blog.getMusicId()));
        }

            repostBlog.setBlogContent(blog.getBlogContent());
            repostBlog.setCreatedAt(LocalDateTime.now());
            repostBlog.setUpdatedAt(LocalDateTime.now());
            threadPoolExecutor.execute(() -> {
                if (redisService.IsExists(key)) {
                    redisService.removeInfo(key);
                }
            });

        communityMapper.createBlog(repostBlog);

    }

    @Override
    public List<RelationUserDTO> ListFriends(Long user_id) {

        String key = redisService.getFriendKey(String.valueOf(user_id));
        List<RelationUserDTO> relations = new ArrayList<>();

        if (redisService.IsExists(key)) {
            relations = (List<RelationUserDTO>) redisService.getData(key);
            return relations;
        } else {
            Relation[] relationList = communityMapper.getFollowersListByUid(user_id);
            List<Long> uids = new ArrayList<>();
            List<RelationUserDTO> rus = new ArrayList<>();
            for (Relation relation : relationList) {
                uids.add(relation.getUserId());
            }

            if (uids.size() > 0) {
                Relation[] rs = communityMapper.getFollowingsByUid(uids);
                uids.clear();

                for (Relation relation : rs) {
                    uids.add(relation.getUserId());
                }

                List<UserDTO> userInfos = userClient.getUsers(uids);
                for (int i = 0; i < userInfos.size(); i++) {
                    RelationUserDTO ru = new RelationUserDTO();
                    ru.ConvertToRelationUserDTO(userInfos.get(i));
                    rus.add(ru);
                }

                redisService.setData(key, rus);
                return rus;
            }
            return relations;
        }

    }

    @Override
    public List<RelationUserDTO> RefreshFriends(Long user_id) {
        String key = redisService.getFriendKey(String.valueOf(user_id));
        List<RelationUserDTO> relations = new ArrayList<>();

            Relation[] relationList = communityMapper.getFollowersListByUid(user_id);
            List<Long> uids = new ArrayList<>();
            List<RelationUserDTO> rus = new ArrayList<>();
            for (Relation relation : relationList) {
                uids.add(relation.getUserId());
            }

            if (uids.size() > 0) {
                Relation[] rs = communityMapper.getFollowingsByUid(uids);
                uids.clear();

                for (Relation relation : rs) {
                    uids.add(relation.getUserId());
                }

                List<UserDTO> userInfos = userClient.getUsers(uids);
                for (int i = 0; i < userInfos.size(); i++) {
                    RelationUserDTO ru = new RelationUserDTO();
                    ru.ConvertToRelationUserDTO(userInfos.get(i));
                    rus.add(ru);
                }

                redisService.setData(key, rus);
                return rus;
            }
            return relations;

    }

    @Override
    public void CreateLikeInComment(Long user_id, Long comment_id) throws CustomException {
        String key = redisService.getLikeInCommentKey(String.valueOf(comment_id));
        Like like = new Like();
        like.setUserId(user_id);
        like.setCommentId(comment_id);

        Like res = communityMapper.getUserLikeByCid(like);
        if (res == null) {

            threadPoolExecutor.execute(() -> {
                communityMapper.createLike(like);
            });

            threadPoolExecutor.execute(() -> {
                if (redisService.IsExists(key)) {
                    redisService.incrData(key);
                } else {
                    Comment comment = communityMapper.getCommentByCid(comment_id);
                    redisService.setData(key, comment.getLikeCount() + 1);
                }
            });

            rabbitTemplate.convertAndSend("community.like3",
                    "community.create3", like);
//            communityMapper.incrCommentLikeCount(comment_id);

        } else {
            throw new CustomException(CustomException.repeatedOperations);
        }

    }

    @Override
    public void CreateLikeInBlog(Long user_id, Long blog_id) throws CustomException {
        String key = redisService.getLikeInBlogKey(String.valueOf(blog_id));

        Like like = new Like();
        like.setUserId(user_id);
        like.setBlogId(blog_id);

        Like res = communityMapper.getUserLikeByBid(like);
        if (res == null) {

            threadPoolExecutor.execute(() -> {
                communityMapper.createLike(like);
            });

            threadPoolExecutor.execute(() -> {

                if (redisService.IsExists(key)) {

                    redisService.incrData(key);
                } else {

                    Blog blog = communityMapper.getBlogsByBlogId(blog_id);
                    redisService.setData(key, blog.getLikeCount() + 1);
                }

            });


                rabbitTemplate.convertAndSend("community.like3",
                        "community.create3", like);
//            communityMapper.incrBlogLikeCount(blog_id);

        } else {
            throw new CustomException(CustomException.repeatedOperations);
        }
    }

    @Override
    public void DeleteLikeInComment(Long user_id, Long comment_id) throws CustomException {
        String key = redisService.getLikeInCommentKey(String.valueOf(comment_id));
        Like like = new Like();
        like.setUserId(user_id);
        like.setCommentId(comment_id);

        Like res = communityMapper.getUserLikeByCid(like);
        if (res != null) {

            threadPoolExecutor.execute(() -> {
                communityMapper.deleteLikeInComment(like);
            });
                if (redisService.IsExists(key)) {
                    redisService.decrData(key);
                } else {
                    Comment comment = communityMapper.getCommentByCid(comment_id);
                    redisService.setData(key, comment.getLikeCount()>0?comment.getLikeCount()-1:0);
                }
            rabbitTemplate.convertAndSend("community.like4",
                    "community.delete4", like);
//            communityMapper.decrCommentLikeCount(comment_id);

        } else {
            throw new CustomException(CustomException.targetNotFound);
        }
    }

    @Override
    public void DeleteLikeInBlog(Long user_id, Long blog_id) throws CustomException {
        String key = redisService.getLikeInBlogKey(String.valueOf(blog_id));
        Like like = new Like();
        like.setUserId(user_id);
        like.setBlogId(blog_id);

        Like res = communityMapper.getUserLikeByBid(like);
        if (res != null) {

            threadPoolExecutor.execute(() -> {
                communityMapper.deleteLikeInBlog(like);
            });

            if (redisService.IsExists(key)) {

                    redisService.decrData(key);
                } else {

                    Blog blog = communityMapper.getBlogsByBlogId(blog_id);
                    redisService.setData(key, blog.getLikeCount()>0?blog.getLikeCount()-1:0);
                }
//            System.out.println(like.getBlogId());
            rabbitTemplate.convertAndSend("community.like4",
                    "community.delete4", like);
        } else {
            throw new CustomException(CustomException.targetNotFound);
        }

    }

    @Override
    public void CreateCommentInComment(Long user_id, String content, Long parent_id) {
        String key = redisService.getCommentInParentCommentKey(String.valueOf(parent_id));
        String key1 = new String();
        Comment parentComment = communityMapper.getCommentByCid(parent_id);
        Comment comment = new Comment();
        if (parentComment.getBlogId() != null) {
            key1 = redisService.getCommentInBlogKey(String.valueOf(parentComment.getBlogId()));

        } else if (parentComment.getMusicId() != null) {
            key1 = redisService.getCommentInMusicKey(String.valueOf(parentComment.getMusicId()));
        }
        System.out.println(comment);
        comment.setBlogId(parentComment.getBlogId());
        comment.setMusicId(parentComment.getMusicId());
        comment.setUserId(user_id);
        comment.setCommentContent(content);
        comment.setParentId(parent_id);

        comment.setCreatedAt(LocalDateTime.now());
        comment.setCommentId(SnowFlakeUtil.getSnowFlakeId());

        threadPoolExecutor.execute(() -> {
            communityMapper.createComment(comment);
        });
        rabbitTemplate.convertAndSend("community.comment1", "community.create1", comment);
        rabbitTemplate.convertAndSend("community.topic", "community.delete", key);
        rabbitTemplate.convertAndSend("community.topic", "community.delete", key1);
//        musicClient.updateCommentCount(parentComment.getMusicId());
    }

    @Override
    public void CreateCommentInMusic(Long user_id, Long id, String content) {
        String key = redisService.getCommentInMusicKey(String.valueOf(id));

        Comment comment = new Comment();
        comment.setUserId(user_id);
        comment.setCommentContent(content);
        comment.setCommentId(SnowFlakeUtil.getSnowFlakeId());
        comment.setMusicId(id);
        comment.setCreatedAt(LocalDateTime.now());

        threadPoolExecutor.execute(() -> {
            communityMapper.createComment(comment);

        });
        threadPoolExecutor.execute(() -> {
            musicClient.updateCommentCount(comment.getMusicId()); // 由于网络问题，无法判断是mq的问题还是网络导致的，只能放弃把这个操作放在mq了
        });
        rabbitTemplate.convertAndSend("community.topic", "community.delete", key);


//        musicClient.updateCommentCount(id);
    }

    @Override
    public void CreateCommentInBlog(Long user_id, String content, Long blog_id) {
        String key = redisService.getCommentInBlogKey(String.valueOf(blog_id));

        Comment comment = new Comment();
        comment.setUserId(user_id);
        comment.setCommentContent(content);
        comment.setCommentId(SnowFlakeUtil.getSnowFlakeId());
        comment.setBlogId(blog_id);
        comment.setCreatedAt(LocalDateTime.now());

        try {
            communityMapper.createComment(comment);
        } catch (Exception e) {
            System.out.println(e);
        }


        rabbitTemplate.convertAndSend("community.comment1", "community.create1", comment);
        rabbitTemplate.convertAndSend("community.topic", "community.delete", key);

    }

    @Override
    public void DeleteComment(Long user_id, Long comment_id)  throws CustomException {
        Comment comment = communityMapper.getCommentByCid(comment_id);

        // comment的userid或管理员才可以删除
        if (comment.getUserId().equals(user_id)) {

//            communityMapper.deleteAllLikeInComment(comment_id);
//            communityMapper.deleteComment(comment_id);
            if (comment.getParentId() != 0) {
                String key = redisService.getCommentInParentCommentKey(String.valueOf(comment.getParentId()));
                rabbitTemplate.convertAndSend("community.topic", "community.delete", key);
//                communityMapper.decrCommentLikeCount(comment.getParentId());
            } else if (comment.getMusicId() != 0) {
                threadPoolExecutor.execute(() -> {
                    communityMapper.decrBlogLikeCount(comment.getMusicId());
                });
                String key = redisService.getCommentInMusicKey(String.valueOf(comment.getMusicId()));
                rabbitTemplate.convertAndSend("community.topic", "community.delete", key);
                return;
            } else if (comment.getBlogId() != 0) {
                String key = redisService.getCommentInBlogKey(String.valueOf(comment.getBlogId()));
                rabbitTemplate.convertAndSend("community.topic", "community.delete", key);
            }
            rabbitTemplate.convertAndSend("community.comment2", "community.delete2", comment);

            return;
        }

        throw new CustomException("illegal operation in deleteComment");

    }

    @Override
    public List<CommentDTO> ListSubComments(Long parent_id, int page_size, int page_num) {

        String key = redisService.getCommentInParentCommentKey(String.valueOf(parent_id));

        if (redisService.IsExists(key)) {
            List<CommentDTO> res = (List<CommentDTO>) redisService.getData(key);

            return SubList(page_size, page_num, res);
        }

        Comment[] cs = communityMapper.getCommentsByCid(parent_id);
        List<CommentDTO> dtos = new ArrayList<>();
        for (int i = 0; i < cs.length; i++) {
            CommentDTO dto = new CommentDTO();
            dto.ConvertToCommentDTO(cs[i]);
            if (redisService.IsExists(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId())))) {
                dto.setLikeCount(redisService.getLikeCount(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId()))));
            }
            dtos.add(dto);
        }

        Collections.sort(dtos, new Comparator<CommentDTO>() {
            @Override
            public int compare(CommentDTO o1, CommentDTO o2) {
                return o2.getLikeCount() - o1.getLikeCount();
            }
        });

        redisService.setData(key, dtos);
        return SubList(page_size, page_num, dtos);
    }

    @Override
    public List<CommentDTO> ListBlogComments(Long blog_id, int page_size, int page_num) {
        String key = redisService.getCommentInBlogKey(String.valueOf(blog_id));//GetListMusicCommentsKey(String.valueOf(music_id));

        if (redisService.IsExists(key)) {
            List<CommentDTO> res = (List<CommentDTO>) redisService.getData(key);

            return SubList(page_size, page_num, res);
        }

        Comment[] cs = communityMapper.getCommentsByBlogId(blog_id);
        List<CommentDTO> dtos = new ArrayList<>();
        for (int i = 0; i < cs.length; i++) {
            CommentDTO dto = new CommentDTO();
            dto.ConvertToCommentDTO(cs[i]);
            if (redisService.IsExists(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId())))) {
                dto.setLikeCount(redisService.getLikeCount(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId()))));
            }
            dtos.add(dto);
        }

        Collections.sort(dtos, new Comparator<CommentDTO>() {
            @Override
            public int compare(CommentDTO o1, CommentDTO o2) {
                return o2.getLikeCount() - o1.getLikeCount();
            }
        });

        redisService.setData(key, dtos);
        return SubList(page_size, page_num, dtos);
    }

    @Override
    public List<CommentDTO> ListMusicComment(Long music_id, int page_size, int page_num) {
        String key = redisService.getCommentInMusicKey(String.valueOf(music_id));//GetListMusicCommentsKey(String.valueOf(music_id));

        if (redisService.IsExists(key)) {
            List<CommentDTO> res = (List<CommentDTO>) redisService.getData(key);

            return SubList(page_size, page_num, res);
        }

        Comment[] cs = communityMapper.getCommentsByMusicId(music_id);
        List<CommentDTO> dtos = new ArrayList<>();
        for (int i = 0; i < cs.length; i++) {
            CommentDTO dto = new CommentDTO();
            dto.ConvertToCommentDTO(cs[i]);
            if (redisService.IsExists(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId())))) {
                dto.setLikeCount(redisService.getLikeCount(redisService.getLikeInCommentKey(String.valueOf(dto.getCommentId()))));
            }

            dtos.add(dto);
        }

        Collections.sort(dtos, new Comparator<CommentDTO>() {
            @Override
            public int compare(CommentDTO o1, CommentDTO o2) {
                return o2.getLikeCount() - o1.getLikeCount();
            }
        });

        redisService.setData(key, dtos);
        return SubList(page_size, page_num, dtos);
    }


}
