<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getSupplementPage,
  deleteSupplement,
  supplementStatusByStatus,
  supplementCategoryList,
  addSupplement,
  editSupplement
} from '@/api/supplement';
import { Plus } from '@element-plus/icons-vue';

const router = useRouter();

// --- 查询相关的状态 ---
const name = ref('');
const categoryId = ref('');
const status = ref('');

const saleStatus = [
  { label: '启售', value: '1' },
  { label: '停售', value: '0' }
];

const dishCategoryList = ref([]);

const tableData = ref([]);
const page = ref(1);
const pageSize = ref(10);
const counts = ref(0);
const loading = ref(false);

// --- 初始化与获取数据 ---
const init = async () => {
  loading.value = true;
  try {
    // 拉取分类列表 (补剂分类 params: { type: 1 })
    const cateRes = await supplementCategoryList({ type: 1 });
    if (cateRes.code === 1) {
      dishCategoryList.value = cateRes.data || [];
    }

    // 获取分页
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      name: name.value || undefined,
      categoryId: categoryId.value || undefined,
      status: status.value || undefined
    };
    const res = await getSupplementPage(params);
    if (res.code === 1) {
      tableData.value = res.data.records;
      counts.value = res.data.total;
    } else {
      ElMessage.error(res.msg || '获取补剂列表失败');
    }
  } catch (err) {
    console.error('加载补剂信息异常', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  init();
});

// 分页控制
const handleSizeChange = (val) => {
  pageSize.value = val;
  init();
};

const handleCurrentChange = (val) => {
  page.value = val;
  init();
};

// --- 表格操作 ---
const selectedIds = ref([]);
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id);
};

// 单删/批删
const deleteHandle = (type, id) => {
  const ids = type === '批量删除' ? selectedIds.value.join(',') : id;
  if (!ids) {
    ElMessage.warning('请选择要删除的补剂');
    return;
  }
  ElMessageBox.confirm('确认删除该补剂吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteSupplement(ids);
      if (res.code === 1) {
        ElMessage.success('删除成功');
        init();
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch (e) {
      console.error(e);
    }
  }).catch(() => {});
};

// 状态更改
const statusHandle = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1;
  const targetText = targetStatus === 0 ? '停售' : '启售';
  ElMessageBox.confirm(`确认${targetText}该补剂吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await supplementStatusByStatus({ id: row.id, status: targetStatus });
      if (res.code === 1) {
        ElMessage.success('修改状态成功');
        init();
      } else {
        ElMessage.error(res.msg || '修改失败');
      }
    } catch (e) {
      console.error(e);
    }
  }).catch(() => {});
};

// --- 弹窗与表单功能 ---
const dialogVisible = ref(false);
const dialogTitle = ref('新建补剂');
const ruleFormRef = ref();

const ruleForm = ref({
  id: '',
  name: '',
  categoryId: '',
  price: '',
  image: '',
  description: '',
  status: 1, // 默认启售
  details: []
});

const rules = {
  name: [{ required: true, message: '请输入补剂名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择补剂分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入补剂价格', trigger: 'blur' }]
};

// 预设补剂规格信息
const dishFlavorsData = ref([
  { name: '口味', value: ['香草', '巧克力', '草莓', '抹茶', '原味'] },
  { name: '容量', value: ['1磅', '2磅', '5磅', '10磅', '500g', '1kg'] },
  { name: '形态', value: ['粉剂', '胶囊', '液体', '片剂'] },
  { name: '包装', value: ['桶装', '袋装', '盒装'] }
]);

const getPredefinedValues = (specName) => {
  const spec = dishFlavorsData.value.find(item => item.name === specName);
  return spec ? spec.value : [];
};

// 打开新增
const openAddDialog = () => {
  dialogTitle.value = '新建补剂';
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
  dialogVisible.value = true;
};

// 打开修改 (由于可能还需要后端的按id查询接口，暂时做基本回显)
const openEditDialog = (row) => {
  router.push({ path: '/supplement/edit', query: { id: row.id } });
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
  }
};

// 规格操作
const addDetail = () => {
  ruleForm.value.details.push({ name: '', valueArr: [] });
};

const removeDetail = (index) => {
  ruleForm.value.details.splice(index, 1);
};

// 提交表单
const submitForm = async (formEl, isContinue = false) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (valid) {
      // 组装提交的规格数组 (将数组序列化成字符串，配合后端接收)
      const submitDetails = ruleForm.value.details.map(d => ({
        id: d.id || undefined, // 修改时带回细节id
        name: d.name,
        value: JSON.stringify(d.valueArr)
      }));

      const payload = { ...ruleForm.value, details: submitDetails };

      try {
        let res;
        if (payload.id) {
          res = await editSupplement(payload);
        } else {
          res = await addSupplement(payload);
        }

        if (res.code === 1) {
          ElMessage.success('操作成功');
          init();
          if (isContinue) {
             // 继续添加，清空表单但可以保留分类等状态
             ruleForm.value = {
               ...ruleForm.value,
               id: '',
               name: '',
               price: '',
               details: []
             };
          } else {
             handleCloseDialog();
          }
        } else {
          ElMessage.error(res.msg || '操作失败');
        }
      } catch (e) {
        console.error(e);
      }
    }
  });
};

// 上传图片相关
const uploadHeaders = ref({
  token: JSON.parse(localStorage.getItem('loginUser'))?.token || ''
});

const handleImageSuccess = (res) => {
  if (res.code === 1) {
    ruleForm.value.image = res.data;
  } else {
    ElMessage.error(res.msg || '图片上传失败');
  }
};
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <h2>补剂管理</h2>
      <br>
      <div class="tableBar" style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; align-items: center;">
          <label style="margin-right: 10px">补剂名称：</label>
          <el-input v-model="name" placeholder="请填写补剂名称" style="width: 200px" clearable @keyup.enter="init" />

          <label style="margin-left: 20px; margin-right: 10px;">补剂分类：</label>
          <el-select v-model="categoryId" placeholder="请选择" style="width: 150px" clearable @change="init">
            <el-option v-for="item in dishCategoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>

          <label style="margin-left: 20px; margin-right: 10px;">售卖状态：</label>
          <el-select v-model="status" placeholder="请选择" style="width: 120px" clearable @change="init">
            <el-option v-for="item in saleStatus" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>

          <el-button type="warning" style="margin-left: 20px;" @click="init">查 询</el-button>
        </div>

        <div class="tableLab">
          <el-button type="danger" @click="deleteHandle('批量删除')">批量删除</el-button>
          <el-button type="primary" style="margin-left: 15px" @click="openAddDialog">
            + 新建补剂
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="tableBox" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="补剂名称" />
        <el-table-column prop="image" label="图片">
          <template #default="{ row }">
            <el-image style="width: 60px; height: 60px; border-radius: 4px;" :src="row.image" :preview-src-list="[row.image]" preview-teleported>
              <template #error>
                <div class="image-slot" style="display: flex; align-items: center; justify-content: center; height: 100%; background: #f5f7fa; color: #a8a8a8">
                  无图片
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="补剂分类" />
        <el-table-column label="售价">
          <template #default="scope">
            <span style="font-weight: bold; color: #ff6000;">￥{{ Number(scope.row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="200" />
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              修改
            </el-button>
            <el-button type="danger" size="small" @click="deleteHandle('单删', scope.row.id)">
              删除
            </el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="statusHandle(scope.row)">
              {{ scope.row.status === 1 ? '停售' : '启售' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination 
          v-if="counts > 0"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="counts"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" 
        />
      </div>
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="65%" :before-close="handleCloseDialog">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px">
        
        <div style="display: flex; flex-wrap: wrap;">
          <el-form-item label="补剂名称:" prop="name" style="width: 48%;">
            <el-input v-model="ruleForm.name" placeholder="请填写补剂名称" maxlength="20" />
          </el-form-item>
          
          <el-form-item label="补剂分类:" prop="categoryId" style="width: 48%; margin-left: 2%;">
            <el-select v-model="ruleForm.categoryId" placeholder="请选择补剂分类" style="width: 100%">
              <el-option v-for="item in dishCategoryList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="补剂价格:" prop="price" style="width: 48%;">
            <el-input v-model="ruleForm.price" placeholder="请设置补剂价格" type="number" />
          </el-form-item>
        </div>

        <el-form-item label="补剂规格信息:">
          <div style="width: 100%">
            <el-button type="warning" @click="addDetail">+ 添加补剂规格</el-button>
            
            <div v-if="ruleForm.details.length" style="margin-top: 15px; border: 1px solid #eee; padding: 15px; border-radius: 4px;">
              <div v-for="(item, index) in ruleForm.details" :key="index" style="display: flex; margin-bottom: 10px;">
                <!-- 预设参数名下拉/自定义输入 -->
                <el-select v-model="item.name" placeholder="请选择或输入规格名称 (如:口味)" allow-create filterable style="width: 200px">
                  <el-option v-for="flavor in dishFlavorsData" :key="flavor.name" :label="flavor.name" :value="flavor.name" />
                </el-select>
                
                <!-- 对应参数标签输入框 (多选) -->
                <el-select v-model="item.valueArr" multiple allow-create filterable placeholder="请输入选项标签后敲击回车，如 '香草'" style="flex: 1; margin-left: 15px;" no-data-text="敲击回车完成输入">
                  <!-- 如果选择了预设的规格名，则提供预设的tags提示选项 -->
                  <el-option v-if="getPredefinedValues(item.name).length" v-for="val in getPredefinedValues(item.name)" :key="val" :label="val" :value="val" />
                </el-select>

                <el-button type="danger" link style="margin-left: 15px;" @click="removeDetail(index)">删除</el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="补剂图片:" prop="image">
          
          <el-upload
            class="avatar-uploader"
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            style="border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; width: 148px; height: 148px; display: flex; align-items: center; justify-content: center;"
          >
            <img v-if="ruleForm.image" :src="ruleForm.image" style="width: 148px; height: 148px; object-fit: cover;" />
            <el-icon v-else style="font-size: 28px; color: #8c939d;"><Plus /></el-icon>
            
            <!-- 如果你的后端暂时还不支持直接上传，为了你能先完成功能流转，你也可以开放下方直接填入图片链接的输入框 -->
          </el-upload>
          <div style="margin-left: 20px; font-size: 12px; color: #999; display: flex; flex-direction: column; justify-content: center;">
            <p>1. 图片大小不超过2M</p>
            <p>2. 仅支持 PNG, JPEG, JPG</p>
          </div>
        </el-form-item>

        <el-form-item label="补剂描述:" prop="description">
          <el-input v-model="ruleForm.description" type="textarea" :rows="3" placeholder="补剂描述，最长200字" maxlength="200" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取 消</el-button>
          <el-button type="primary" @click="submitForm(ruleFormRef)">保 存</el-button>
          <el-button v-if="!ruleForm.id" type="warning" @click="submitForm(ruleFormRef, true)">保存并继续添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 30px;
}
.dashboard-container .container {
  background-color: #fff;
  padding: 30px;
  border-radius: 4px;
}
.tableBox {
  margin-top: 20px;
}
.avatar-uploader:hover {
  border-color: #409EFF !important;
}
</style>
