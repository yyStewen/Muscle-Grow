<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import {
  setmealCategoryList,
  getSupplementsByCategory,
  createSetmeal,
  getSetmealById,
  updateSetmeal
} from "@/api/setmeal";
import { supplementCategoryList } from "@/api/supplement";

const route = useRoute();
const router = useRouter();

const setmealId = computed(() => route.query.id);
const isEditMode = computed(() => Boolean(setmealId.value));

const ruleFormRef = ref();
const ruleForm = ref({
  id: '',
  name: '',
  categoryId: '',
  price: '',
  image: '',
  description: '',
  status: 0,
  setmealSupplements: []
});

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传套餐图片', trigger: 'change' }]
};

const categoryOptions = ref([]);
const supplementCategoryOptions = ref([]);
const dialogVisible = ref(false);
const dialogLoading = ref(false);
const dialogCategoryId = ref('');
const supplementOptions = ref([]);
const checkedSupplementIds = ref([]);
const selectedSupplementRows = ref([]);
const dialogTableRef = ref();
const submitLoading = ref(false);

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
    status: 0,
    setmealSupplements: []
  };
  checkedSupplementIds.value = [];
  selectedSupplementRows.value = [];
};

const fetchSetmealCategories = async () => {
  const res = await setmealCategoryList({ type: 2 });
  if (res.code === 1) {
    categoryOptions.value = res.data || [];
  }
};

const fetchSupplementCategories = async () => {
  const res = await supplementCategoryList({ type: 1 });
  if (res.code === 1) {
    supplementCategoryOptions.value = res.data || [];
  }
};

const syncDialogSelection = () => {
  if (!dialogTableRef.value) return;
  dialogTableRef.value.clearSelection();
  supplementOptions.value.forEach(item => {
    if (checkedSupplementIds.value.includes(item.id)) {
      dialogTableRef.value.toggleRowSelection(item, true);
    }
  });
  selectedSupplementRows.value = supplementOptions.value.filter(item =>
    checkedSupplementIds.value.includes(item.id)
  );
};

const fetchSupplements = async () => {
  if (!dialogCategoryId.value) {
    supplementOptions.value = [];
    return;
  }
  dialogLoading.value = true;
  try {
    const res = await getSupplementsByCategory(dialogCategoryId.value);
    if (res.code === 1) {
      supplementOptions.value = res.data || [];
      await nextTick();
      syncDialogSelection();
    } else {
      ElMessage.error(res.msg || '获取补剂失败');
    }
  } catch (err) {
    console.error(err);
    ElMessage.error('获取补剂失败');
  } finally {
    dialogLoading.value = false;
  }
};

const openAddSupplementDialog = () => {
  dialogVisible.value = true;
  checkedSupplementIds.value = ruleForm.value.setmealSupplements.map(item => item.supplementId);
  selectedSupplementRows.value = [];
  if (!dialogCategoryId.value && supplementCategoryOptions.value.length) {
    dialogCategoryId.value = supplementCategoryOptions.value[0].id;
  }
  fetchSupplements();
};

const addSelectedSupplements = () => {
  const map = new Map(ruleForm.value.setmealSupplements.map(item => [item.supplementId, item]));
  selectedSupplementRows.value.forEach(item => {
    if (!map.has(item.id)) {
      map.set(item.id, {
        supplementId: item.id,
        name: item.name,
        price: item.price,
        copies: 1
      });
    }
  });
  ruleForm.value.setmealSupplements = Array.from(map.values());
  dialogVisible.value = false;
};

const removeSupplement = (id) => {
  ruleForm.value.setmealSupplements = ruleForm.value.setmealSupplements.filter(
    item => item.supplementId !== id
  );
};

const handleImageSuccess = (res) => {
  if (res.code === 1) {
    ruleForm.value.image = res.data;
    if (ruleFormRef.value) {
      ruleFormRef.value.validateField('image').catch(() => {});
    }
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(res.msg || '图片上传失败');
  }
};

const validateSupplements = () => {
  if (!ruleForm.value.setmealSupplements.length) {
    ElMessage.error('请至少添加一个补剂');
    return false;
  }
  return true;
};

const formatPayload = () => ({
  ...ruleForm.value,
  setmealSupplements: ruleForm.value.setmealSupplements.map(item => ({
    id: item.id,
    setmealId: item.setmealId,
    supplementId: item.supplementId,
    name: item.name,
    price: item.price,
    copies: item.copies
  }))
});

const fetchSetmealDetail = async () => {
  if (!isEditMode.value) {
    resetForm();
    return;
  }
  try {
    const res = await getSetmealById(setmealId.value);
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
        setmealSupplements: (data.setmealSupplements || data.setmealDishes || []).map(item => ({
          id: item.id,
          setmealId: item.setmealId,
          supplementId: item.supplementId ?? item.dishId,
          name: item.name,
          price: item.price,
          copies: item.copies || 1
        }))
      };
      checkedSupplementIds.value = ruleForm.value.setmealSupplements.map(item => item.supplementId);
    } else {
      ElMessage.error(res.msg || '获取套餐详情失败');
    }
  } catch (err) {
    console.error(err);
    ElMessage.error('获取套餐详情失败');
  }
};

const submitForm = async (isContinue = false) => {
  if (!ruleFormRef.value) return;
  const valid = await ruleFormRef.value.validate().catch(() => false);
  if (!valid || !validateSupplements()) return;

  submitLoading.value = true;
  try {
    const payload = formatPayload();
    const requestApi = isEditMode.value ? updateSetmeal : createSetmeal;
    const res = await requestApi(payload);
    if (res.code === 1) {
      ElMessage.success(isEditMode.value ? '套餐修改成功' : '套餐新增成功');
      if (isContinue && !isEditMode.value) {
        ruleFormRef.value.resetFields();
        ruleForm.value.setmealSupplements = [];
        ruleForm.value.status = 0;
      } else {
        router.push('/setmeal');
      }
    } else {
      ElMessage.error(res.msg || '保存失败');
    }
  } catch (err) {
    console.error(err);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const goBack = () => {
  router.push('/setmeal');
};

onMounted(async () => {
  await Promise.all([fetchSetmealCategories(), fetchSupplementCategories()]);
  await fetchSetmealDetail();
});

watch(() => route.query.id, () => {
  fetchSetmealDetail();
});

const handleDialogSelectionChange = (rows) => {
  selectedSupplementRows.value = rows;
  checkedSupplementIds.value = rows.map(item => item.id);
};
</script>

<template>
  <div class="setmeal-add-page">
    <div class="page-card">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px">
        <el-form-item label="套餐名称：" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入套餐名称" maxlength="20" />
        </el-form-item>

        <el-form-item label="套餐分类：" prop="categoryId">
          <el-select v-model="ruleForm.categoryId" placeholder="请选择套餐分类" style="width: 300px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="套餐价格：" prop="price">
          <el-input
            v-model="ruleForm.price"
            placeholder="请输入套餐价格"
            type="number"
            min="0"
            style="width: 300px"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>

        <el-form-item label="套餐补剂：" required>
          <div class="supplement-box">
            <el-button type="primary" @click="openAddSupplementDialog">+ 添加补剂</el-button>
            <el-table
              v-if="ruleForm.setmealSupplements.length"
              :data="ruleForm.setmealSupplements"
              border
              style="margin-top: 15px"
            >
              <el-table-column prop="name" label="补剂名称" />
              <el-table-column label="原价" width="120">
                <template #default="{ row }">
                  ￥{{ Number(row.price || 0).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="份数" width="160">
                <template #default="{ row }">
                  <el-input-number v-model="row.copies" :min="1" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="danger" @click="removeSupplement(row.supplementId)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="empty-tip">尚未选择补剂</div>
          </div>
        </el-form-item>

        <el-form-item label="套餐图片：" prop="image">
          <el-upload
            class="avatar-uploader"
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
          >
            <img v-if="ruleForm.image" :src="ruleForm.image" class="image-preview" alt="套餐图片" />
            <div v-else class="image-placeholder">
              <el-icon><Plus /></el-icon>
              <p>上传图片</p>
            </div>
          </el-upload>
          <div class="upload-tip">
            <p>图片大小不超过 2M</p>
            <p>仅支持 PNG / JPG / JPEG</p>
          </div>
        </el-form-item>

        <el-form-item label="套餐描述：">
          <el-input
            v-model="ruleForm.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="请输入套餐描述，最长 200 字"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <div class="action-bar">
        <el-button type="primary" :loading="submitLoading" @click="submitForm()">
          保存
        </el-button>
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

    <el-dialog v-model="dialogVisible" title="添加补剂" width="640px">
      <div class="dialog-toolbar">
        <el-select
          v-model="dialogCategoryId"
          placeholder="请选择补剂分类"
          style="width: 240px"
          @change="fetchSupplements"
        >
          <el-option
            v-for="item in supplementCategoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
      <el-table
        ref="dialogTableRef"
        v-loading="dialogLoading"
        :data="supplementOptions"
        @selection-change="handleDialogSelectionChange"
        height="360px"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="补剂名称" />
        <el-table-column label="售价" width="120">
          <template #default="{ row }">
            ￥{{ Number(row.price || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addSelectedSupplements">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.setmeal-add-page {
  padding: 30px;
}

.page-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
}

.supplement-box {
  width: 100%;
}

.empty-tip {
  margin-top: 12px;
  color: #c0c4cc;
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
  margin-right: 20px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.image-preview {
  width: 148px;
  height: 148px;
  object-fit: cover;
  border-radius: 6px;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.image-placeholder p {
  margin-top: 4px;
  font-size: 12px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.dialog-toolbar {
  margin-bottom: 15px;
}
</style>
