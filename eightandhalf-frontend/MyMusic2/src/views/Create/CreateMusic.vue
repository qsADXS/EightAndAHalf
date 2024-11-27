<template>
  <div class="upload-music-container">
    <h2>上传音乐</h2>
    <el-form
      :model="form"
      :rules="rules"
      ref="uploadForm"
      label-width="100px"
      class="upload-form"
    >
      <!-- 音乐文件 -->
      <el-form-item label="音乐文件" prop="musicFile">
        <el-upload
          class="upload-demo"
          action=""
          :file-list="musicFileList"
          :auto-upload="false"
          accept=".mp3,.wav"
          :limit="1"
          :on-change="handleMusicFileChange"
        >
          <el-button type="primary">选择文件</el-button>
          <div slot="tip" class="el-upload__tip">仅支持 .mp3, .wav 格式</div>
        </el-upload>
      </el-form-item>

      <!-- 封面文件 -->
      <el-form-item label="封面文件" prop="coverFile">
        <el-upload
          class="upload-demo"
          action=""
          :file-list="coverFileList"
          :auto-upload="false"
          accept=".jpg,.jpeg,.png"
          :limit="1"
          :on-change="handleCoverFileChange"
        >
          <el-button type="primary">选择文件</el-button>
          <div slot="tip" class="el-upload__tip">仅支持 .jpg, .jpeg, .png 格式</div>
        </el-upload>
      </el-form-item>

      <!-- 音乐名称 -->
      <el-form-item label="音乐名称" prop="musicName">
        <el-input v-model="form.musicName" placeholder="请输入音乐名称" />
      </el-form-item>

      <!-- 歌手 -->
      <el-form-item label="歌手" prop="singer">
        <el-input v-model="form.singer" placeholder="请输入歌手名称" />
      </el-form-item>

      <!-- 描述 -->
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入音乐描述"
        />
      </el-form-item>

      <!-- 音乐分类 -->
      <el-form-item label="分类" prop="musicCategory">
        <el-select v-model="form.musicCategory" placeholder="请选择分类">
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.name"
          />
        </el-select>
      </el-form-item>

      <!-- 所属专辑 -->
      <el-form-item label="所属专辑" prop="album">
        <el-input v-model="form.album" placeholder="请输入所属专辑名称" />
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadMusicAPI } from '@/api/music/upload';

export default {
  name: 'UploadMusic',
  setup() {
    const form = ref({
      musicFile: null,
      coverFile: null,
      musicName: '',
      singer: '',
      description: '',
      musicCategory: '',
      album: '',
    });

    const musicFileList = ref([]);
    const coverFileList = ref([]);
    const categories = ref([
      { id: '1', name: 'Pop' },
      { id: '2', name: 'Rock' },
      { id: '3', name: 'Jazz' },
      { id: '4', name: 'Blues' },
      { id: '5', name: 'Hip-Hop' },
      { id: '6', name: 'R&B' },
      { id: '7', name: 'Country' },
      { id: '8', name: 'Electronic' },
      { id: '9', name: 'Classical' },
      { id: '10', name: 'Reggae' },
      { id: '11', name: 'Folk' },
      { id: '12', name: 'Funk' },
      { id: '13', name: 'Metal' },
      { id: '14', name: 'World Music' },
      { id: '15', name: 'New Age' },
    ]);

    const rules = {
      musicFile: [{ required: true, message: '请选择音乐文件', trigger: 'change' }],
      coverFile: [{ required: true, message: '请选择封面文件', trigger: 'change' }],
      musicName: [{ required: true, message: '请输入音乐名称', trigger: 'blur' }],
      musicCategory: [{ required: true, message: '请选择音乐分类', trigger: 'change' }],
    };

    const handleMusicFileChange = (file) => {
      form.value.musicFile = file.raw;
      form.value.musicName = getFileNamePrefix(file.name);
    };

    const getFileNamePrefix = (fileName) => {
      // 使用正则表达式获取文件名中最后一个点之前的部分
      const match = fileName.match(/(.*)\.[^.]+$/);
      return match ? match[1] : fileName; // 如果匹配成功，返回前缀名；否则返回原文件名
    }

    const handleCoverFileChange = (file) => {
      form.value.coverFile = file.raw;
    };

    const resetForm = () => {
      form.value = {
        musicFile: null,
        coverFile: null,
        musicName: '',
        singer: '',
        description: '',
        musicCategory: '',
        album: '',
      };
      musicFileList.value = [];
      coverFileList.value = [];
    };

    // 确保在 setup 中定义 uploadForm，并在模板中引用
    const uploadForm = ref(null);

    const handleSubmit = async () => {
      if (!uploadForm.value) return; // 确保 uploadForm 已被赋值
      uploadForm.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.error('请填写完整信息');
          return;
        }

        const formData = new FormData();
        formData.append('musicFile', form.value.musicFile);
        formData.append('coverFile', form.value.coverFile);
        formData.append('musicName', form.value.musicName);
        formData.append('singer', form.value.singer || '');
        formData.append('description', form.value.description || '');
        formData.append('musicCategory', form.value.musicCategory);
        formData.append('album', form.value.album || '');

        try {
          const response = await uploadMusicAPI(formData);
          if (response.base.code === 10000) {
            ElMessage.success('上传成功');
            resetForm();
          } else {
            ElMessage.error(`上传失败: ${response.message}`);
          }
        } catch (error) {
          ElMessage.error('上传失败，请检查网络连接');
        }
      });
    };

    return {
      form,
      musicFileList,
      coverFileList,
      categories,
      rules,
      handleMusicFileChange,
      handleCoverFileChange,
      resetForm,
      handleSubmit,
      uploadForm, // 确保返回这个 ref
    };
  },
};
</script>



<style scoped>
.upload-music-container {
  max-width: 600px;
  margin: 50px auto;
  padding: 20px;
  background: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.upload-form {
  display: flex;
  flex-direction: column;
}
</style>
