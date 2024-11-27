package com.eh.common.util;

import com.eh.common.config.CosManager;
import com.eh.common.config.FileConstant;
import com.eh.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
public class TencentYunCOSUtil {


    private static CosManager cosManager;


    @Autowired
    public TencentYunCOSUtil(CosManager cosManager) {
        TencentYunCOSUtil.cosManager = cosManager;
    }

    public static String upload(MultipartFile avatar) {
        // 文件目录
        String filename = avatar.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            //上传文件
            file = File.createTempFile(filepath, null);
            avatar.transferTo(file);
            cosManager.putObject(filepath, file);
            //返回可访问地址
            return FileConstant.COS_HOST+filepath;
        } catch (Exception e) {
            throw new RuntimeException("file upload error, filepath = " + filepath + ", error = " +e);
        } finally {
            if (file != null) {
                //删除临时文件
                boolean delete = file.delete();
                if (!delete) System.out.println("file delete error, filepath = [l" + filepath);
            }
        }
    }

    public static String uploadBlogFile(MultipartFile blogFile) throws CustomException {

        // 文件目录
        String filename = blogFile.getOriginalFilename();

        if ( !FileUtil.isIllegalExtension(FileUtil.getExtension(filename))) {
            throw new CustomException("invalid file type");
        }

        String filepath = String.format("/test/%s", filename); //这里先test，以后改回blog
        File file = null;
        try {
            //上传文件
            file = File.createTempFile(filepath, null);
            blogFile.transferTo(file);
            cosManager.putObject(filepath, file);
            //返回可访问地址
            return FileConstant.COS_HOST+filepath;
        } catch (Exception e) {
            throw new RuntimeException("file upload error, filepath = " + filepath + ", error = " +e);
        } finally {
            if (file != null) {
                //删除临时文件
                boolean delete = file.delete();
                if (!delete) System.out.println("file delete error, filepath = [l" + filepath);
            }
        }
    }

}
