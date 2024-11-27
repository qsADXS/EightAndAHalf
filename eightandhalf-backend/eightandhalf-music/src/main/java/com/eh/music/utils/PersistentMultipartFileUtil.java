package com.eh.music.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class PersistentMultipartFileUtil {

    /**
     * 将 MultipartFile 保存到指定持久化存储位置，并返回持久化后的 MockMultipartFile。
     * @param multipartFile 原始 MultipartFile
     * @param directoryPath 保存文件的持久化目录
     * @return 持久化后的 MockMultipartFile
     * @throws IOException 如果文件保存或读取过程中出现异常
     */
    public static MockMultipartFile convertToPersistentMultipartFile(MultipartFile multipartFile, String directoryPath) throws IOException {
        // 确保持久化目录存在
        Path dirPath = Path.of(directoryPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 将 MultipartFile 保存到持久化位置
        File persistentFile = saveToFile(multipartFile, directoryPath);

        // 将保存后的文件转换为 MockMultipartFile
        return convertFileToMockMultipartFile(persistentFile);
    }

    /**
     * 将 MultipartFile 的内容保存到指定目录。
     * @param multipartFile 要保存的文件
     * @param directoryPath 保存文件的目录
     * @return 保存后的 File 对象
     * @throws IOException 如果文件保存过程中出现异常
     */
    private static File saveToFile(MultipartFile multipartFile, String directoryPath) throws IOException {
        // 创建目标文件
        File persistentFile = new File(directoryPath, multipartFile.getOriginalFilename());

        // 将 MultipartFile 写入目标文件
        try (FileOutputStream outputStream = new FileOutputStream(persistentFile)) {
            outputStream.write(multipartFile.getBytes());
        }
        return persistentFile;
    }

    /**
     * 将持久化存储的 File 转换为 MockMultipartFile。
     * @param file 已保存的文件
     * @return MockMultipartFile 对象
     * @throws IOException 如果文件读取过程中出现异常
     */
    private static MockMultipartFile convertFileToMockMultipartFile(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            // 自动获取 MIME 类型
            String mimeType = Files.probeContentType(file.toPath());

            return new MockMultipartFile(
                    "file",                      // 参数名
                    file.getName(),              // 文件名
                    mimeType != null ? mimeType : "application/octet-stream",  // MIME 类型
                    inputStream                  // 文件输入流
            );
        }
    }

    public static boolean deleteDirectoryAndFiles(Path dir) throws IOException {
        // 检查目录是否存在
        if (Files.exists(dir)) {
            // 遍历目录及其子目录
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())  // 反向排序，先删除文件再删除目录
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete: " + file);
                        }
                    });

            // 删除空目录
            return Files.deleteIfExists(dir);
        }
        return false;
    }
}