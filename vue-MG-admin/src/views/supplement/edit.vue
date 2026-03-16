<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import {
  querySupplementById,
  supplementCategoryList,
  addSupplement,
  editSupplement
} from '@/api/supplement';

const route = useRoute();
const router = useRouter();

const supplementId = computed(() => route.query.id);
const isEditMode = computed(() => Boolean(supplementId.value));
const loading = ref(false);
const submitLoading = ref(false);
const categoryOptions = ref([]);
const ruleFormRef = ref();
const ruleForm = ref({
  id: '',
  name: '',
  categoryId: '',
  price: '',
  image: '',
  description: '',
  status: 1,
  details: []
});

const rules = {
  name: [{ required: true, message: '请输入补剂名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择补剂分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入补剂价格', trigger: 'blur' }]
};

const dishFlavorsData = ref([
  { name: '口味', value: ['原味', '巧克力', '香草', '草莓', '提拉米苏'] },
  { name: '甜度', value: ['无糖', '少糖', '半糖', '多糖'] },
  { name: '容量', value: ['500g', '1kg', '2kg'] },
  { name: '包装', value: ['罐装', '袋装', '礼盒'] }
]);

const uploadHeaders = ref({
  token: JSON.parse(localStorage.getItem('loginUser'))?.token || ''
});

const resetForm = () => {
  ruleForm.value = {
    id: '',
    name: '',
    categoryId: '',
    price: '',
    image: '',
    description: '',
    status: 1,
    details: []
  };
};

const getPredefinedValues = (specName) => {
  const spec = dishFlavorsData.value.find(item => item.name === specName);
  return spec ? spec.value : [];
};

const formatDetailValues = (details) => {
  return (details || []).map(detail => {
    let parsed = [];
    try {
      parsed = detail.value ? JSON.parse(detail.value) : [];
      if (!Array.isArray(parsed)) {
        parsed = detail.value.split(',').map((str) => str.trim()).filter(Boolean);
      }
    } catch (err) {
      parsed = detail.value ? detail.value.split(',').map((str) => str.trim()).filter(Boolean) : [];
    }
    return {
      id: detail.id,
      name: detail.name,
      valueArr: parsed
    };
  });
};

const formatSubmitDetails = () => {
  return ruleForm.value.details
    .filter(detail => detail.name)
    .map(detail => ({
      id: detail.id || undefined,
      name: detail.name,
      value: JSON.stringify(detail.valueArr || [])
    }));
};

const fetchCategories = async () => {
  const res = await supplementCategoryList({ type: 1 });
  if (res.code === 1) {
    categoryOptions.value = res.data || [];
  }
};

const fetchSupplementDetail = async () => {
  if (!supplementId.value) {
    resetForm();
    return;
  }
  const res = await querySupplementById(supplementId.value);
  if (res.code === 1 && res.data) {
    const data = res.data;
    ruleForm.value = {
      id: data.id,
      name: data.name,
      categoryId: data.categoryId,
      price: data.price,
      image: data.image,
      description: data.description,
      status: data.status,
      details: formatDetailValues(data.details)
    };
  } else {
    ElMessage.error(res.msg || '获取补剂详情失败');
  }
};

const init = async () => {
  loading.value = true;
  try {
    await fetchCategories();
    await fetchSupplementDetail();
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/supplement');
};

const addDetail = () => {
  ruleForm.value.details.push({ name: '', valueArr: [] });
};

const removeDetail = (index) => {
  ruleForm.value.details.splice(index, 1);
};

const handleImageSuccess = (res) => {
  if (res.code === 1) {
    ruleForm.value.image = res.data;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(res.msg || '图片上传失败');
  }
};

const submitForm = async (isContinue = false) => {
  if (!ruleFormRef.value) return;
  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      const payload = {
        ...ruleForm.value,
        details: formatSubmitDetails()
      };
      const api = isEditMode.value ? editSupplement : addSupplement;
      const res = await api(payload);
      if (res.code === 1) {
        ElMessage.success(isEditMode.value ? '补剂修改成功' : '补剂保存成功');
        if (isEditMode.value) {
          goBack();
        } else if (isContinue) {
          ruleForm.value = {
            ...ruleForm.value,
            id: '',
            name: '',
            price: '',
            image: '',
            description: '',
            status: 1,
            details: []
          };
        } else {
          goBack();
        }
      } else {
        ElMessage.error(res.msg || '操作失败');
      }
    } catch (err) {
      console.error(err);
      ElMessage.error('操作失败，请稍后重试');
    } finally {
      submitLoading.value = false;
    }
  });
};

onMounted(() => {
  init();
});

watch(
  () => route.query.id,
  () => {
    init();
  }
);
</script>

<template>
  <div class="supplement-edit-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>{{ supplementId ? '修改补剂' : '新增补剂' }}</h2>
        <p class="subtitle">根据原型补全基础信息，当前阶段仅支持数据回显。</p>
      </div>
      <el-button type="default" @click="goBack">返回列表</el-button>
    </div>

    <div class="form-wrapper">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px" class="supplement-form">
        <div class="row">
          <el-form-item label="补剂名称：">
            <el-input v-model="ruleForm.name" placeholder="请输入补剂名称" />
          </el-form-item>
          <el-form-item label="补剂分类：">
            <el-select v-model="ruleForm.categoryId" placeholder="请选择补剂分类">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="补剂价格：">
            <el-input v-model="ruleForm.price" placeholder="请输入价格" type="number">
              <template #append>元</template>
            </el-input>
          </el-form-item>
        </div>

        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>补剂规格配置</span>
              <el-button type="primary" text @click="addDetail">+ 添加规格</el-button>
            </div>
          </template>

          <p class="tip-text">口味与标签仅为原型展示，接口联调后再完善保存逻辑。</p>

          <div v-if="ruleForm.details.length" class="detail-list">
            <div v-for="(item, index) in ruleForm.details" :key="item.id || index" class="detail-line">
              <el-select v-model="item.name" placeholder="规格名称（例如：口味）" class="spec-name" allow-create filterable>
                <el-option v-for="flavor in dishFlavorsData" :key="flavor.name" :label="flavor.name" :value="flavor.name" />
              </el-select>
              <el-select
                v-model="item.valueArr"
                multiple
                allow-create
                filterable
                placeholder="输入后回车可新增标签"
                class="spec-values"
              >
                <el-option
                  v-for="val in getPredefinedValues(item.name)"
                  :key="val"
                  :label="val"
                  :value="val"
                />
              </el-select>
              <el-button type="danger" text @click="removeDetail(index)">删除</el-button>
            </div>
          </div>
          <div v-else class="empty-details">
            暂无规格信息
          </div>
        </el-card>

        <el-form-item label="补剂图片：">
          <div class="image-field">
            <el-upload
              class="avatar-uploader"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImageSuccess"
            >
              <img v-if="ruleForm.image" :src="ruleForm.image" class="image-preview" alt="补剂图片" />
              <div v-else class="image-placeholder">
                <el-icon><Plus /></el-icon>
                <p>上传图片</p>
              </div>
            </el-upload>
            <div class="image-hint">
              <p>图片大小不超过 2M，仅支持 PNG / JPG / JPEG</p>
              <p>建议上传 200×200 或 300×300 像素图片</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="补剂描述：">
          <el-input
            v-model="ruleForm.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="补剂描述，最长 200 字"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <div class="action-bar">
        <el-button type="primary" :loading="submitLoading" @click="submitForm()">保存</el-button>
        <el-button
          v-if="!isEditMode"
          type="success"
          plain
          :loading="submitLoading"
          @click="submitForm(true)"
        >
          保存并继续添加
        </el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.supplement-edit-page {
  padding: 30px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.form-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
}

.supplement-form .row {
  display: flex;
  flex-wrap: wrap;
}

.supplement-form .row .el-form-item {
  width: 33.33%;
  padding-right: 20px;
}

.detail-card {
  margin: 20px 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.tip-text {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.detail-list {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  background: #fdfdfd;
}

.detail-line {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 12px;
}

.detail-line:last-child {
  margin-bottom: 0;
}

.spec-name {
  width: 200px;
}

.spec-values {
  flex: 1;
}

.empty-details {
  color: #c0c4cc;
  text-align: center;
  padding: 30px 0;
}

.image-field {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.image-preview,
.image-placeholder {
  width: 148px;
  height: 148px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.image-preview {
  background: #fff;
  object-fit: cover;
}

.image-placeholder p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.image-hint {
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

@media (max-width: 1024px) {
  .supplement-form .row .el-form-item {
    width: 100%;
    padding-right: 0;
  }

  .image-field {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
