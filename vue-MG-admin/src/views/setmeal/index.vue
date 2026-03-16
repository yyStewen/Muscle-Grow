<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getSetmealPage,
  setmealCategoryList,
  deleteSetmeal,
  setmealStatusByStatus
} from '@/api/setmeal';

const router = useRouter();

const name = ref('');
const categoryId = ref('');
const status = ref('');

const saleStatus = [
  { label: '启售', value: 1 },
  { label: '停售', value: 0 }
];

const categoryOptions = ref([]);
const tableData = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const counts = ref(0);

const selectedIds = ref([]);
const selectedRows = ref([]);

const fetchCategories = async () => {
  const res = await setmealCategoryList({ type: 2 });
  if (res.code === 1) {
    categoryOptions.value = res.data || [];
  }
};

const fetchSetmealList = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      name: name.value || undefined,
      categoryId: categoryId.value || undefined,
      status: status.value === '' ? undefined : status.value
    };
    const res = await getSetmealPage(params);
    if (res.code === 1) {
      tableData.value = res.data.records || [];
      counts.value = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || '获取套餐列表失败');
    }
  } catch (err) {
    console.error(err);
    ElMessage.error('请求套餐列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchSetmealList();
};

const handleReset = () => {
  name.value = '';
  categoryId.value = '';
  status.value = '';
  page.value = 1;
  fetchSetmealList();
};

const handleSelectionChange = (rows) => {
  selectedRows.value = rows;
  selectedIds.value = rows.map(item => item.id);
};

const handleCurrentChange = (val) => {
  page.value = val;
  fetchSetmealList();
};

const goToAdd = () => {
  router.push('/setmeal/add');
};

const goToEdit = (id) => {
  router.push({ path: '/setmeal/add', query: { id } });
};

const handleStatus = (row) => {
  const targetStatus = row.status === 1 ? 0 : 1;
  const actionText = targetStatus === 1 ? '启售' : '停售';
  ElMessageBox.confirm(`确认${actionText}该套餐吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await setmealStatusByStatus({ id: row.id, status: targetStatus });
      if (res.code === 1) {
        ElMessage.success(`${actionText}成功`);
        fetchSetmealList();
      } else {
        ElMessage.error(res.msg || `${actionText}失败`);
      }
    } catch (err) {
      console.error(err);
    }
  }).catch(() => {});
};

const handleDelete = (ids, rows = []) => {
  if (!ids || !ids.length) {
    ElMessage.warning('请先选择要删除的套餐');
    return;
  }
  const targetRows = rows.length ? rows : tableData.value.filter(item => ids.includes(item.id));
  if (targetRows.some(item => item.status === 1)) {
    ElMessage.warning('启售中的套餐不可删除');
    return;
  }
  ElMessageBox.confirm('确认删除选中的套餐吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteSetmeal(ids.join(','));
      if (res.code === 1) {
        ElMessage.success('删除成功');
        selectedRows.value = [];
        selectedIds.value = [];
        fetchSetmealList();
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch (err) {
      console.error(err);
    }
  }).catch(() => {});
};

onMounted(async () => {
  await fetchCategories();
  fetchSetmealList();
});
</script>

<template>
  <div class="setmeal-page">
    <div class="toolbar">
      <div class="filter-group">
        <label>套餐名称：</label>
        <el-input
          v-model="name"
          placeholder="请输入套餐名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />

        <label>套餐分类：</label>
        <el-select
          v-model="categoryId"
          placeholder="请选择套餐分类"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <label>售卖状态：</label>
        <el-select
          v-model="status"
          placeholder="请选择"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in saleStatus"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" style="margin-left: 16px" @click="handleSearch">搜索</el-button>
        <el-button style="margin-left: 8px" @click="handleReset">重置</el-button>
      </div>

      <div class="action-group">
        <el-button
          type="danger"
          plain
          :disabled="!selectedIds.length"
          @click="handleDelete(selectedIds, selectedRows)"
        >
          批量删除
        </el-button>
        <el-button type="primary" @click="goToAdd">+ 新建套餐</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table
        :data="tableData"
        stripe
        class="setmeal-table"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="套餐名称" min-width="160" />
      <el-table-column label="套餐图片" width="140">
        <template #default="{ row }">
          <el-image
            :src="row.image"
            :preview-src-list="row.image ? [row.image] : []"
            preview-teleported
            fit="cover"
            style="width: 80px; height: 60px; border-radius: 4px;"
          >
            <template #error>
              <div class="image-slot">暂无图片</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="套餐分类" />
      <el-table-column label="套餐价" width="120">
        <template #default="{ row }">
          ￥{{ Number(row.price || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="售卖状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启售' : '停售' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" min-width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="goToEdit(row.id)">修改</el-button>
          <el-button
            link
            type="danger"
            :disabled="row.status === 1"
            @click="handleDelete([row.id], [row])"
          >
            删除
          </el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleStatus(row)"
          >
            {{ row.status === 1 ? '停售' : '启售' }}
          </el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrap" v-if="counts">
      <span>共 {{ counts }} 项</span>
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="pageSize"
        :total="counts"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.setmeal-page {
  padding: 30px;
}

.toolbar {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  color: #606266;
}

.filter-group label {
  margin-left: 8px;
}

.action-group {
  display: flex;
  gap: 12px;
}

.table-card {
  background: #fff;
  border-radius: 6px;
  padding: 0 20px 20px;
}

.setmeal-table {
  background: transparent;
}

.image-slot {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 12px;
}

.setmeal-table :deep(.el-button.is-link) {
  pointer-events: auto;
}

.setmeal-table :deep(.el-image) {
  cursor: pointer;
}

.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 20px;
}
</style>
