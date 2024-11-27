<template>
  <div class="UpdateAvatar">
    <!-- 弹出框 -->
    <el-dialog
      title="更改头像"
      v-model="dialogVisible"
      width="30%"
      :custom-class="'avatar-dialog'"
    >
      <div class="dialog-content">
        <el-upload
          class="avatar-uploader"
          action=""
          :file-list="fileList"
          :show-file-list="false"
          :on-change="handleFileSelect"
          :on-remove="handleFileRemove"
          :auto-upload="false"
        >
          <!-- 自定义上传按钮 -->
          <div class="custom-upload-button" :style="previewImageStyle">
            <i v-if="!previewImage" class="el-icon-plus"></i>
            <div v-if="!previewImage" class="upload-text">点击上传头像</div>
          </div>
        </el-upload>
        <!-- 移除原来的预览图片 -->
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="uploadAvatar" :disabled="!selectedFile">
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { updateAvatar } from '@/api/user/updateAvatar/index'; // 更新为真实路径
import { ElMessage } from 'element-plus';
import { useUserInfoStore } from '@/store/userStore';

const dialogVisible = ref(false);
const selectedFile = ref(null);
const fileList = ref([]);
const previewImage = ref(null);
const userInfoStore = useUserInfoStore();

const openUpdateDialog = () => {
  dialogVisible.value = true;
};

// 判断文件类型是否为图片
const isImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  return validTypes.includes(file.type);
};

const handleFileSelect = (file, fileList) => {
  if (!isImageFile(file.raw)) {
    ElMessage.error('上传的文件必须是图片（jpg, png, gif, webp）类型');
    return;
  }
  selectedFile.value = file.raw;
  previewImage.value = URL.createObjectURL(file.raw);
};

const handleFileRemove = () => {
  selectedFile.value = null;
  previewImage.value = null;
};

const uploadAvatar = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择头像文件');
    return;
  }

  const formData = new FormData();
  formData.append('avatar', selectedFile.value);

  try {
    await updateAvatar(formData);
    ElMessage.success('头像上传成功！');
    dialogVisible.value = false;
    // 重置状态
    userInfoStore.fetchUserInfo();
    selectedFile.value = null;
    previewImage.value = null;
  } catch (error) {
    ElMessage.error('头像上传失败，请重试！');
  }
};

const previewImageStyle = computed(() => {
  if (previewImage.value) {
    return {
      backgroundImage: `url(${previewImage.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    };
  }
  return {};
});

// 使用 defineExpose 对外暴露 openDialog 方法
defineExpose({
  openUpdateDialog,
});
</script>

<style scoped>
.UpdateAvatar {
  cursor: default;
}
.avatar-uploader {
  margin-bottom: 20px;
}

.custom-upload-button {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
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

.avatar-dialog .el-dialog__body {
  background: none;
  position: relative;
}

.avatar-dialog .el-dialog__body::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: url('/path/to/your/background-image.jpg') no-repeat center center;
  background-size: cover;
  filter: blur(5px);
  opacity: 0.5;
}

.dialog-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}
</style>
