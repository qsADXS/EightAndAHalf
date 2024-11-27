<template>
    <el-dialog title="发布动态" v-model="visible" width="400px">
      <el-form :model="formData" ref="publishForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="formData.content"
            placeholder="请输入动态内容"
          />
        </el-form-item>
  
        <!-- 图片上传和预览 -->
        <el-form-item label="图片" prop="file">
          <el-upload
            class="custom-upload"
            action="#"
            :file-list="fileList"
            :show-file-list="false"
            :on-change="handleFileSelect"
            :on-remove="handleFileRemove"
            :auto-upload="false"
          >
            <!-- 自定义上传按钮 -->
            <div class="custom-upload-button" :style="previewImageStyle">
              <i v-if="!previewImage" class="el-icon-plus"></i>
              <div v-if="!previewImage" class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :style="{backgroundColor:'rgb(34,144,148)'}">发布</el-button>
      </template>
    </el-dialog>
  </template>
  
  <script setup>
  import { ref, computed, defineExpose } from "vue";
  import { publishPost } from "@/api/user/publish/index";
  import { ElMessage } from "element-plus";
  
  const visible = ref(false);
  const formData = ref({
    title: "",
    content: "",
    file: null,
  });
  
  const selectedFile = ref(null);
  const fileList = ref([]);
  const previewImage = ref(null);
  
  // 处理图片选择
  const handleFileSelect = (file) => {
    selectedFile.value = file.raw;
    previewImage.value = URL.createObjectURL(file.raw); // 设置预览图片的URL
  };
  
  // 处理图片移除
  const handleFileRemove = () => {
    selectedFile.value = null;
    previewImage.value = null;
  };
  
  // 发布动态
  const handlePublish = () => {
    const form = new FormData();
    form.append("title", formData.value.title);
    form.append("content", formData.value.content);
    if (selectedFile.value) {
      form.append("file", selectedFile.value); // 添加图片文件
    }
    publishPost(form)
      .then(() => {
        ElMessage.success("动态发布成功！");
        visible.value = false;
        resetForm();
      })
      .catch(() => {
        ElMessage.error("动态发布失败，请重试！");
      });
  };
  
  // 重置表单数据
  const resetForm = () => {
    formData.value = { title: "", content: "", file: null };
    selectedFile.value = null;
    previewImage.value = null;
    fileList.value = [];
  };
  
  // 自定义预览图片样式
  const previewImageStyle = computed(() => {
    if (previewImage.value) {
      return {
        backgroundImage: `url(${previewImage.value})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      };
    }
    return {};
  });
  
  // 暴露 show 方法给父组件调用
  defineExpose({
    show: () => (visible.value = true),
  });
  </script>
  
  <style scoped>
  .custom-upload {
    margin-bottom: 20px;
  }
  
  .custom-upload-button {
    width: 120px;
    height: 120px;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #8c939d;
    cursor: pointer;
    overflow: hidden;
    background-color: #fff;
    background-repeat: no-repeat;
  }
  
  .custom-upload-button:hover {
    border-color: #409eff;
  }
  
  .custom-upload-button i {
    font-size: 32px;
  }
  
  .upload-text {
    margin-top: 8px;
    font-size: 14px;
  }
  </style>
  