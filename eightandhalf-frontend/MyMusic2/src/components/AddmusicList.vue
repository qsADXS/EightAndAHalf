<template>
  <div class="UpdateAvatar">
    <!-- 弹出框 -->
    <el-dialog
      title="创建歌单"
      v-model="dialogVisible"
      width="30%"
      :custom-class="'avatar-dialog'"
    >
      <div class="dialog-content">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <!-- 歌单名称 -->
          <el-form-item label="歌单名称" prop="playlistName">
            <el-input v-model="form.playlistName" placeholder="请输入歌单名称" />
          </el-form-item>

          <!-- 封面上传 -->
          <el-form-item label="封面图片" prop="playlistCoverFile">
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
                <div v-if="!previewImage" class="upload-text">点击上传封面</div>
              </div>
            </el-upload>
          </el-form-item>

          <!-- 描述 -->
          <el-form-item label="描述" prop="description">
            <el-input
              type="textarea"
              v-model="form.description"
              placeholder="请输入描述"
              rows="3"
            />
          </el-form-item>

          <!-- 是否公开 -->
          <el-form-item label="是否公开" prop="isPublic">
            <el-radio-group v-model="form.isPublic">
              <el-radio :label="1">公开</el-radio>
              <el-radio :label="0">私密</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 歌单类型 -->
          <!-- <el-form-item label="歌单类型" prop="playlist_type">
            <el-select v-model="form.playlist_type" placeholder="请选择歌单类型">
              <el-option label="流行" value="pop" />
              <el-option label="古典" value="classical" />
              <el-option label="爵士" value="jazz" />
            </el-select>
          </el-form-item> -->
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createPlaylist" :disabled="!selectedFile">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { createMusicList } from "@/api/music/createmusiclist"; // 使用提供的创建歌单API
import { ElMessage } from "element-plus";

const dialogVisible = ref(false); // 控制弹窗的显示
const selectedFile = ref(null); // 用户选中的文件
const fileList = ref([]); // 文件列表
const previewImage = ref(null); // 用于预览的图片

// 表单数据和验证规则
const form = reactive({
  playlistName: "",
  playlistCoverFile: null,
  description: "",
  isPublic: 1,
  playlist_type: "0",
});

const rules = {
  playlistName: [{ required: true, message: "请输入歌单名称", trigger: "blur" }],
  playlistCoverFile: [{ required: true, message: "请上传封面图片", trigger: "change" }],
  description: [{ required: true, message: "请输入描述", trigger: "blur" }],
  isPublic: [{ required: true, message: "请选择是否公开", trigger: "change" }],
  // playlist_type: [{ required: true, message: "请选择歌单类型", trigger: "change" }],
};

// 打开弹窗
const openUpdateDialog = () => {
  dialogVisible.value = true;
};

// 处理文件选择
const handleFileSelect = (file) => {
  selectedFile.value = file.raw;
  previewImage.value = URL.createObjectURL(file.raw);
  form.playlistCoverFile = file.raw; // 将文件赋值给表单
};

// 处理文件移除
const handleFileRemove = () => {
  selectedFile.value = null;
  previewImage.value = null;
  form.playlistCoverFile = null;
};

// 创建歌单
const createPlaylist = async () => {
  const formData = new FormData();
  formData.append("playlistName", form.playlistName);
  formData.append("playlistCoverFile", form.playlistCoverFile);
  formData.append("description", form.description);
  formData.append("isPublic", form.isPublic);
  formData.append("playlist_type", form.playlist_type);

  try {
    await createMusicList(
      form.playlistName,
      form.playlistCoverFile,
      form.description,
      form.isPublic,
      form.playlist_type
    );
    ElMessage.success("歌单创建成功！");
    window.location.reload();
    dialogVisible.value = false;
    resetForm(); // 重置表单
  } catch (error) {
    ElMessage.error("创建歌单失败，请重试！");
  }
};

// 重置表单
const resetForm = () => {
  form.playlistName = "";
  form.playlistCoverFile = null;
  form.description = "";
  form.isPublic = 1;
  form.playlist_type = "";
  selectedFile.value = null;
  previewImage.value = null;
};

// 动态样式：封面预览
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

// 对外暴露方法
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

.dialog-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}
</style>
