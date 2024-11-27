package com.eh.music.controller;

import com.eh.common.util.FileUtil;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.common.vo.PageBean;
import com.eh.common.vo.Result;
import com.eh.music.constant.Constants;
import com.eh.music.model.dto.*;
import com.eh.music.model.entity.MusicDoc;
import com.eh.music.model.entity.MusicInfo;
import com.eh.music.model.entity.UserPlaylist;
import com.eh.music.model.request.*;
import com.eh.music.service.MusicService;
import com.eh.music.utils.PersistentMultipartFileUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/music")
public class MusicController {
    @Autowired
    private MusicService musicService;

    @PostMapping("/upload")
    public Result uploadMusic(@ModelAttribute UploadMusicRequest uploadMusicRequest) throws IOException {
        if (uploadMusicRequest.getMusicFile() == null || uploadMusicRequest.getCoverFile() == null) {
            return Result.error("上传文件为空");
        }
        if (!FileUtil.getExtension(uploadMusicRequest.getMusicFile().getOriginalFilename()).equals("mp3")) {
            return Result.error("上传歌曲文件错误");
        }
        //获取当前用户id 为上传id
        Long loginUserId = ThreadLocalUtil.get();

        // 定义持久化存储目录
        String projectRootPath = System.getProperty("user.dir");
        Path musicDir = Paths.get(projectRootPath, "uploads/music");
        Path coverDir = Paths.get(projectRootPath, "uploads/cover");
        // 确保目录存在
        if (!Files.exists(musicDir)) {
            Files.createDirectories(musicDir);
        }
        if (!Files.exists(coverDir)) {
            Files.createDirectories(coverDir);
        }
        MultipartFile musicFile = PersistentMultipartFileUtil.convertToPersistentMultipartFile(uploadMusicRequest.getMusicFile(), musicDir.toString());
        MultipartFile coverFile = PersistentMultipartFileUtil.convertToPersistentMultipartFile(uploadMusicRequest.getCoverFile(), coverDir.toString());
        uploadMusicRequest.setMusicFile(musicFile);
        uploadMusicRequest.setCoverFile(coverFile);
        //保存至music表中
        musicService.saveUploadMusic(loginUserId, uploadMusicRequest);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<UpLoadMusicDTO> getUploadMusicList(@RequestParam("user_id") Long userId, @ModelAttribute ForPageRequest forPageRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("beginNo",(forPageRequest.getPageNo() - 1) * forPageRequest.getPageSize());
        map.put("pageSize",forPageRequest.getPageSize());
        List<MusicInfo> musicInfoList = musicService.queryUploadMusicForPage(map);
        if (musicInfoList == null) {
            return Result.error("查询投稿音乐列表失败");
        }
        UpLoadMusicDTO upLoadMusicDTO = new UpLoadMusicDTO();
        upLoadMusicDTO.setMusicList(musicInfoList);
        int total = musicService.queryCountOfUploadMusic(userId);
        upLoadMusicDTO.setTotal(total);
        return Result.success(upLoadMusicDTO);
    }

    @GetMapping("/category/list")
    public Result<MusicCategoryDTO> getMusicCategory() {
        MusicCategoryDTO musicCategoryDTO = musicService.queryAllMusicCategory();
        return Result.success(musicCategoryDTO);
    }

    @PostMapping("/song/collect")
    public Result insertMusicToPlaylist(@ModelAttribute SubscribeMusicRequest subscribeMusicRequest) {
        Long loginUserId =  ThreadLocalUtil.get();
        //查询歌曲是否已经在歌单中
        int check = musicService.queryMusicExistPlaylist(subscribeMusicRequest);
        if (check > 0) {
            return Result.error("音乐已存在此歌单中");
        }
        musicService.insertMusic(loginUserId, subscribeMusicRequest);
        return Result.success();
    }

    @DeleteMapping("/song/delete")
    public Result deleteMusicFromPlaylist(@ModelAttribute SubscribeMusicRequest subscribeMusicRequest) {
        Long loginUserId =  ThreadLocalUtil.get();
        musicService.removeMusicFromPlaylist(loginUserId, subscribeMusicRequest);
        return Result.success();
    }


    @GetMapping("/playlist/list")
    public Result<UserPlaylistDTO> playlistList(@RequestParam("user_id") Long userId, @RequestParam("relationship_type") Long relationshipType) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("relationshipType", relationshipType);
        Long loginUserId =  ThreadLocalUtil.get();
        if (loginUserId != null && userId != null && !loginUserId.equals(userId)) {
            map.put("isPublic", Constants.PLAYLIST_PUBLIC);
        }
        UserPlaylistDTO userPlaylistDTO = musicService.queryUserPlaylistList(map);
        if (userPlaylistDTO.getUserPlaylistList() == null) {
            return Result.error("查询用户歌单列表失败");
        }
        return Result.success(userPlaylistDTO);
    }

    @GetMapping("/album/list")
    public Result<UserAlbumDTO> albumList(@RequestParam("user_id") Long userId, @RequestParam("relationship_type") Long relationshipType) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("relationshipType", relationshipType);
        Long loginUserId =  ThreadLocalUtil.get();
        if (loginUserId != null && userId != null && !loginUserId.equals(userId)) {
            map.put("isPublic", Constants.PLAYLIST_PUBLIC);
        }
        UserAlbumDTO userAlbumDTO = musicService.queryUserAlbumList(map);
        if (userAlbumDTO.getUserAlbumList() == null) {
            return Result.error("查询用户歌单列表失败");
        }
        return Result.success(userAlbumDTO);
    }
    @GetMapping("/playlist/info")
    public Result<UserPlaylistInfoDTO> playlistInfo(@RequestParam("playlist_id") Long playlistId) {
        UserPlaylistInfoDTO userPlaylistInfoDTO = musicService.queryPlaylistInfo(playlistId);
        if (userPlaylistInfoDTO.getMusicList() == null) {
            return Result.error("查询歌单歌曲内容失败");
        }
        return Result.success(userPlaylistInfoDTO);
    }

    @PostMapping("/playlist/create")
    public Result createPlaylist(@ModelAttribute CreatePlaylistRequest createPlaylistRequest) {
        if (createPlaylistRequest.getPlaylistCoverFile() == null) {
            return Result.error("上传文件为空");
        }
        Long loginUserId = ThreadLocalUtil.get();
        int count = musicService.createPlaylist(loginUserId, createPlaylistRequest);
        if (count != 2) {
            return Result.error("创建歌单失败");
        }
        return Result.success();
    }
    @DeleteMapping("/playlist/delete")
    public Result deletePlaylist(@RequestParam("playlist_id") Long playlistId) {
        //删除歌单
        Long loginUserId = ThreadLocalUtil.get();
        try {
            musicService.deletePlaylistByPlaylistId(loginUserId, playlistId);
        } catch (Exception e) {
            return Result.error("删除歌单失败");
        }
        return Result.success();
    }

    @PostMapping("/playlist/collect")
    public Result collectPlaylist(@ModelAttribute CollectPlaylistRequest collectPlaylistRequest) {
        //
        Long loginUserId = ThreadLocalUtil.get();
        if (collectPlaylistRequest.getType() == 1) { //收藏操作
            int check = musicService.queryExistUserPlaylist(loginUserId, collectPlaylistRequest);
            if (check > 0) {
                return Result.error("用户已收藏该歌单");
            }
            //添加
            musicService.subscribePlaylist(loginUserId, collectPlaylistRequest);
        } else if (collectPlaylistRequest.getType() == 0) { //取消收藏
            int check = musicService.queryExistUserPlaylist(loginUserId, collectPlaylistRequest);
            if (check == 0) {
                return Result.error("用户没有收藏该歌单，无需再取消收藏");
            }
            //删除
            musicService.cancelSubscribePlaylist(loginUserId, collectPlaylistRequest);
        } else {
            return Result.error("操作参数有误");
        }
        return Result.success();
    }

    @PostMapping("/public/action")
    public Result publicAction(@RequestParam("type") Integer publicType) {
        Long loginUserId = ThreadLocalUtil.get();
        if (publicType == 1) {
            musicService.setUserPlaylistPublicType(loginUserId, Constants.PLAYLIST_PUBLIC);
        } else if (publicType == 0){
            musicService.setUserPlaylistPublicType(loginUserId, Constants.PLAYLIST_PRIVATE);
        } else {
            return Result.error("操作参数有误");
        }
        return Result.success();
    }
    @GetMapping("/popular/rank")
    public Result<PopularRankDTO> popularList(@RequestParam("num") Integer num) {
        PopularRankDTO popularRankDTO = musicService.queryPopularMusicList(num);
        return Result.success(popularRankDTO);
    }

    @PostMapping("/song/subscribe")
    public Result subscribeSong(@RequestParam("music_id") Long musicId) {
        Long loginUserId = ThreadLocalUtil.get();
        Long playlistId = musicService.queryMySubscribePlaylistId(loginUserId);
        SubscribeMusicRequest subscribeMusicRequest = new SubscribeMusicRequest();
        System.out.println("收藏歌单id:" + playlistId);
        subscribeMusicRequest.setPlaylistId(playlistId);
        subscribeMusicRequest.setMusicId(musicId);
        int check = musicService.queryMusicExistPlaylist(subscribeMusicRequest);
        if (check > 0) {
            return Result.error("音乐已存在此歌单中");
        }
        musicService.subscribeSong(loginUserId, musicId);
        return Result.success();
    }

    @PostMapping("/song/recent/action")
    public Result setRecentListen(@RequestParam("music_id") Long musicId) {
        Long loginUserId = ThreadLocalUtil.get();
        musicService.setRecentListen(loginUserId, musicId);
        return Result.success();
    }

    @GetMapping("/recent/info")
    public Result<UserPlaylistInfoDTO> getRecentInfo() {
        UserPlaylistInfoDTO userPlaylistInfoDTO = musicService.queryRecentPlaylistInfo();
        return Result.success(userPlaylistInfoDTO);
    }

    @GetMapping("/recommend/list")
    public Result<UserPlaylistInfoDTO> getRecommendList() {
        Long loginUserId = ThreadLocalUtil.get();
        UserPlaylistInfoDTO userPlaylistInfoDTO = musicService.queryRecommendListByIdList(loginUserId);
        return Result.success(userPlaylistInfoDTO);
    }

    @PostMapping("/search")
    @ApiOperation("搜索音乐")
    public Result<PageBean<MusicDoc>> search(@ModelAttribute RequestParams requestParams) {
        return Result.success(musicService.search(requestParams));
    }

    @GetMapping("/suggestion")
    @ApiOperation("搜索框补全")
    public Result<List<String>> getSuggestions(@RequestParam String prefix){
        return Result.success(musicService.getSuggestions(prefix));
    }

    @PostMapping("/comment/update")
    public Result updateCommentCount(@RequestParam Long musicId) {
        int count = musicService.updateCommentCount(musicId);
        if (count == 0) {
            return Result.error("更新失败");
        }
        return Result.success();
    }

    @PostMapping("/comment/decr")
    public Result decrCommentCount(@RequestParam Long musicId) {
        int count = musicService.decrCommentCount(musicId);
        if (count == 0) {
            return Result.error("更新失败");
        }
        return Result.success();
    }

    @PostMapping("/visit/update")
    public Result updateSongVisitCount(@RequestParam("music_id") Long musicId) {
        int count = musicService.updateVisitCount(musicId);
        return Result.success();
    }

    @GetMapping("/subscribe/info")
    public Result<UserPlaylistInfoDTO> getSubscribeList() {
        UserPlaylistInfoDTO userPlaylistInfoDTO = musicService.querySubscribePlaylistInfo();
        return Result.success(userPlaylistInfoDTO);
    }

    @GetMapping("/home/list")
    public Result<UserPlaylistDTO> getHomePagePlaylist(@RequestParam("limit") Integer limit) {
        UserPlaylistDTO userPlaylistDTO = musicService.queryHomePagePlaylist(limit);
        return Result.success(userPlaylistDTO);
    }

    @GetMapping("/song/info")
    public Result<MusicInfo> getMusicById(@RequestParam("music_id") Long musicId) {
        MusicInfo musicInfo = musicService.getMusicInfoById(musicId);
        return Result.success(musicInfo);
    }

    @PostMapping("/bulk")
    @ApiOperation("批量上传")
    public Result bulk() throws IOException {
        musicService.bulkMusic();
        return Result.success();
    }
}
