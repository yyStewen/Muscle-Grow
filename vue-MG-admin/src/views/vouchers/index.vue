<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import {
  addVoucher,
  editVoucher,
  getVoucherById,
  getVoucherPage,
  voucherStatusByStatus
} from '@/api/voucher';

const text = {
  addVoucher: '\u65b0\u589e\u4f18\u60e0\u5238',
  editVoucher: '\u4fee\u6539\u4f18\u60e0\u5238',
  titleFilterLabel: '\u4f18\u60e0\u5238\u6807\u9898\uff1a',
  titlePlaceholder: '\u8bf7\u8f93\u5165\u4f18\u60e0\u5238\u6807\u9898',
  statusFilterLabel: '\u72b6\u6001\uff1a',
  statusPlaceholder: '\u8bf7\u9009\u62e9\u72b6\u6001',
  rangeFilterLabel: '\u6709\u6548\u671f\uff1a',
  startPlaceholder: '\u5f00\u59cb\u65f6\u95f4',
  endPlaceholder: '\u7ed3\u675f\u65f6\u95f4',
  search: '\u641c\u7d22',
  reset: '\u91cd\u7f6e',
  titleColumn: '\u4f18\u60e0\u5238\u6807\u9898',
  payValueColumn: '\u652f\u4ed8\u91d1\u989d',
  actualValueColumn: '\u62b5\u6263\u91d1\u989d',
  stockColumn: '\u5e93\u5b58',
  dateColumn: '\u6709\u6548\u671f',
  statusColumn: '\u72b6\u6001',
  createTimeColumn: '\u521b\u5efa\u65f6\u95f4',
  actionColumn: '\u64cd\u4f5c',
  currency: '\uffe5',
  until: '\u81f3',
  unknownStatus: '\u672a\u77e5\u72b6\u6001',
  dialogTitleLabel: '\u4f18\u60e0\u5238\u6807\u9898',
  payValueLabel: '\u652f\u4ed8\u91d1\u989d',
  actualValueLabel: '\u62b5\u6263\u91d1\u989d',
  stockLabel: '\u5e93\u5b58',
  beginTimeLabel: '\u5f00\u59cb\u65f6\u95f4',
  endTimeLabel: '\u7ed3\u675f\u65f6\u95f4',
  launchStatusLabel: '\u6295\u653e\u72b6\u6001',
  launchEnabled: '\u542f\u7528\u6295\u653e',
  launchDisabled: '\u6682\u4e0d\u4e0a\u67b6',
  edit: '\u4fee\u6539',
  up: '\u4e0a\u67b6',
  down: '\u4e0b\u67b6',
  cancel: '\u53d6\u6d88',
  save: '\u4fdd\u5b58',
  confirm: '\u786e\u5b9a',
  statusOnSale: '\u6295\u653e\u4e2d',
  statusWaiting: '\u5f85\u5f00\u59cb',
  statusEnded: '\u5df2\u7ed3\u675f',
  statusOffShelf: '\u5df2\u4e0b\u67b6',
  statusActionTip: '\u63d0\u793a',
  fetchListFailed: '\u83b7\u53d6\u4f18\u60e0\u5238\u5217\u8868\u5931\u8d25',
  fetchDetailFailed: '\u83b7\u53d6\u4f18\u60e0\u5238\u8be6\u60c5\u5931\u8d25',
  saveSuccess: '\u65b0\u589e\u6210\u529f',
  updateSuccess: '\u4fee\u6539\u6210\u529f',
  actionFailed: '\u64cd\u4f5c\u5931\u8d25',
  endedCannotOperate: '\u5df2\u7ed3\u675f\u7684\u4f18\u60e0\u5238\u4e0d\u80fd\u518d\u64cd\u4f5c',
  confirmUpDownPrefix: '\u786e\u8ba4',
  confirmUpDownSuffix: '\u8be5\u4f18\u60e0\u5238\u5417\uff1f',
  actionSuccessSuffix: '\u6210\u529f',
  actionFailedSuffix: '\u5931\u8d25',
  titleRequired: '\u8bf7\u8f93\u5165\u4f18\u60e0\u5238\u6807\u9898',
  payValueRequired: '\u8bf7\u8f93\u5165\u652f\u4ed8\u91d1\u989d',
  payValueInvalid: '\u652f\u4ed8\u91d1\u989d\u4e0d\u80fd\u5c0f\u4e8e0',
  actualValueRequired: '\u8bf7\u8f93\u5165\u62b5\u6263\u91d1\u989d',
  actualValueInvalid: '\u62b5\u6263\u91d1\u989d\u5fc5\u987b\u5927\u4e8e0',
  actualValueCompareInvalid: '\u62b5\u6263\u91d1\u989d\u5fc5\u987b\u5927\u4e8e\u652f\u4ed8\u91d1\u989d',
  stockRequired: '\u8bf7\u8f93\u5165\u5e93\u5b58',
  stockInvalid: '\u5e93\u5b58\u5fc5\u987b\u4e3a\u6b63\u6574\u6570',
  beginTimeRequired: '\u8bf7\u9009\u62e9\u5f00\u59cb\u65f6\u95f4',
  timeRequired: '\u8bf7\u9009\u62e9\u5f00\u59cb\u65f6\u95f4\u548c\u7ed3\u675f\u65f6\u95f4',
  timeInvalid: '\u7ed3\u675f\u65f6\u95f4\u5fc5\u987b\u665a\u4e8e\u5f00\u59cb\u65f6\u95f4',
  launchStatusRequired: '\u8bf7\u9009\u62e9\u6295\u653e\u72b6\u6001'
};

const tableLoading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref(text.addVoucher);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref([]);
const formRef = ref();

const statusOptions = [
  { label: text.statusOnSale, value: 1 },
  { label: text.statusWaiting, value: 4 },
  { label: text.statusEnded, value: 2 },
  { label: text.statusOffShelf, value: 3 }
];

const formStatusOptions = [
  { label: text.launchEnabled, value: 1 },
  { label: text.launchDisabled, value: 3 }
];

// 列表筛选条件直接和后端分页 DTO 对齐，方便后续继续扩展筛选项。
const filters = reactive({
  title: '',
  status: '',
  dateRange: []
});

// 弹窗新增和编辑共用同一份表单结构，回显和重置都基于这里处理。
const createDefaultFormData = () => ({
  id: null,
  title: '',
  payValue: 0,
  actualValue: 1,
  stock: 1,
  beginTime: '',
  endTime: '',
  status: 1
});

const formData = reactive(createDefaultFormData());

// 表单校验集中处理业务规则，避免模板中出现分散的判断逻辑。
const validatePayValue = (_, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error(text.payValueRequired));
    return;
  }

  if (Number(value) < 0) {
    callback(new Error(text.payValueInvalid));
    return;
  }

  callback();
};

const validateActualValue = (_, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error(text.actualValueRequired));
    return;
  }

  if (Number(value) <= 0) {
    callback(new Error(text.actualValueInvalid));
    return;
  }

  if (Number(value) <= Number(formData.payValue || 0)) {
    callback(new Error(text.actualValueCompareInvalid));
    return;
  }

  callback();
};

const validateStock = (_, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error(text.stockRequired));
    return;
  }

  if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
    callback(new Error(text.stockInvalid));
    return;
  }

  callback();
};

const validateEndTime = (_, value, callback) => {
  if (!formData.beginTime || !value) {
    callback(new Error(text.timeRequired));
    return;
  }

  if (new Date(value).getTime() <= new Date(formData.beginTime).getTime()) {
    callback(new Error(text.timeInvalid));
    return;
  }

  callback();
};

const rules = {
  title: [{ required: true, message: text.titleRequired, trigger: 'blur' }],
  payValue: [{ validator: validatePayValue, trigger: 'change' }],
  actualValue: [{ validator: validateActualValue, trigger: 'change' }],
  stock: [{ validator: validateStock, trigger: 'change' }],
  beginTime: [{ required: true, message: text.beginTimeRequired, trigger: 'change' }],
  endTime: [{ validator: validateEndTime, trigger: 'change' }],
  status: [{ required: true, message: text.launchStatusRequired, trigger: 'change' }]
};

const getStatusTagType = (status) => {
  if (status === 1) {
    return 'success';
  }
  if (status === 4) {
    return 'primary';
  }
  if (status === 3) {
    return 'warning';
  }
  return 'info';
};

const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '--');
const formatAmount = (value) => Number(value || 0).toFixed(2);

// 查询参数保持和后端接口字段一致，减少中间映射成本。
const buildQueryParams = () => ({
  page: page.value,
  pageSize: pageSize.value,
  title: filters.title || undefined,
  status: filters.status === '' ? undefined : filters.status,
  beginTime: filters.dateRange?.[0] || undefined,
  endTime: filters.dateRange?.[1] || undefined
});

const fetchVoucherList = async () => {
  tableLoading.value = true;
  try {
    const res = await getVoucherPage(buildQueryParams());
    if (res.code === 1) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || text.fetchListFailed);
    }
  } catch (error) {
    console.error(error);
  } finally {
    tableLoading.value = false;
  }
};

const resetFormData = () => {
  Object.assign(formData, createDefaultFormData());
};

const handleSearch = () => {
  page.value = 1;
  fetchVoucherList();
};

const handleReset = () => {
  filters.title = '';
  filters.status = '';
  filters.dateRange = [];
  page.value = 1;
  fetchVoucherList();
};

const handleSizeChange = (value) => {
  pageSize.value = value;
  page.value = 1;
  fetchVoucherList();
};

const handleCurrentChange = (value) => {
  page.value = value;
  fetchVoucherList();
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
  resetFormData();
  formRef.value?.clearValidate();
};

const beforeDialogClose = (done) => {
  handleCloseDialog();
  done();
};

const openAddDialog = () => {
  dialogTitle.value = text.addVoucher;
  resetFormData();
  dialogVisible.value = true;
};

const openEditDialog = async (id) => {
  dialogTitle.value = text.editVoucher;
  resetFormData();
  dialogVisible.value = true;

  try {
    const res = await getVoucherById(id);
    if (res.code === 1) {
      Object.assign(formData, {
        id: res.data.id,
        title: res.data.title,
        payValue: Number(res.data.payValue || 0),
        actualValue: Number(res.data.actualValue || 0),
        stock: Number(res.data.stock || 1),
        beginTime: res.data.beginTime ? String(res.data.beginTime).replace('T', ' ') : '',
        endTime: res.data.endTime ? String(res.data.endTime).replace('T', ' ') : '',
        // 编辑表单只允许操作“投放中/已下架”，待开始和已结束都由展示态换算而来。
        status: [1, 3].includes(Number(res.data.status)) ? Number(res.data.status) : 1
      });
    } else {
      ElMessage.error(res.msg || text.fetchDetailFailed);
      handleCloseDialog();
    }
  } catch (error) {
    console.error(error);
    handleCloseDialog();
  }
};

const handleSubmit = async () => {
  if (!formRef.value) {
    return;
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    submitLoading.value = true;
    try {
      const payload = {
        ...formData
      };

      const request = payload.id ? editVoucher(payload) : addVoucher(payload);
      const res = await request;

      if (res.code === 1) {
        ElMessage.success(payload.id ? text.updateSuccess : text.saveSuccess);
        handleCloseDialog();
        fetchVoucherList();
      } else {
        ElMessage.error(res.msg || text.actionFailed);
      }
    } catch (error) {
      console.error(error);
    } finally {
      submitLoading.value = false;
    }
  });
};

const handleToggleStatus = (row) => {
  if (Number(row.displayStatus) === 2) {
    ElMessage.warning(text.endedCannotOperate);
    return;
  }

  // 上下架仍然以持久化状态为准，展示状态只负责页面显示。
  const targetStatus = Number(row.status) === 3 ? 1 : 3;
  const actionText = targetStatus === 1 ? text.up : text.down;

  ElMessageBox.confirm(
    `${text.confirmUpDownPrefix}${actionText}${text.confirmUpDownSuffix}`,
    text.statusActionTip,
    {
      confirmButtonText: text.confirm,
      cancelButtonText: text.cancel,
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await voucherStatusByStatus({ id: row.id, status: targetStatus });
      if (res.code === 1) {
        ElMessage.success(`${actionText}${text.actionSuccessSuffix}`);
        fetchVoucherList();
      } else {
        ElMessage.error(res.msg || `${actionText}${text.actionFailedSuffix}`);
      }
    } catch (error) {
      console.error(error);
    }
  }).catch(() => {});
};

const isEnded = (row) => Number(row.displayStatus) === 2;

onMounted(() => {
  fetchVoucherList();
});
</script>

<template>
  <div class="voucher-page">
    <div class="voucher-page__toolbar">
      <div class="voucher-page__filters">
        <label>{{ text.titleFilterLabel }}</label>
        <el-input
          v-model="filters.title"
          :placeholder="text.titlePlaceholder"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />

        <label>{{ text.statusFilterLabel }}</label>
        <el-select
          v-model="filters.status"
          :placeholder="text.statusPlaceholder"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <label>{{ text.rangeFilterLabel }}</label>
        <el-date-picker
          v-model="filters.dateRange"
          type="datetimerange"
          :start-placeholder="text.startPlaceholder"
          :end-placeholder="text.endPlaceholder"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />

        <el-button type="primary" @click="handleSearch">{{ text.search }}</el-button>
        <el-button @click="handleReset">{{ text.reset }}</el-button>
      </div>

      <el-button type="primary" @click="openAddDialog">+ {{ text.addVoucher }}</el-button>
    </div>

    <div class="voucher-page__table-wrap">
      <el-table
        :data="tableData"
        stripe
        v-loading="tableLoading"
        class="voucher-page__table"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontWeight: 600 }"
      >
        <el-table-column prop="title" :label="text.titleColumn" min-width="220" show-overflow-tooltip />
        <el-table-column :label="text.payValueColumn" width="120">
          <template #default="{ row }">{{ text.currency }}{{ formatAmount(row.payValue) }}</template>
        </el-table-column>
        <el-table-column :label="text.actualValueColumn" width="120">
          <template #default="{ row }">{{ text.currency }}{{ formatAmount(row.actualValue) }}</template>
        </el-table-column>
        <el-table-column prop="stock" :label="text.stockColumn" width="100" />
        <el-table-column :label="text.dateColumn" min-width="320">
          <template #default="{ row }">
            {{ formatDateTime(row.beginTime) }} {{ text.until }} {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="text.statusColumn" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(Number(row.displayStatus))">
              {{ row.displayStatusLabel || text.unknownStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="text.createTimeColumn" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="text.actionColumn" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="isEnded(row)"
              @click="openEditDialog(row.id)"
            >
              {{ text.edit }}
            </el-button>
            <el-button
              link
              :type="Number(row.status) === 3 ? 'success' : 'warning'"
              :disabled="isEnded(row)"
              @click="handleToggleStatus(row)"
            >
              {{ Number(row.status) === 3 ? text.up : text.down }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      class="voucher-page__pagination"
      :current-page="page"
      :page-size="pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      :before-close="beforeDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="110px"
      >
        <el-form-item :label="text.dialogTitleLabel" prop="title">
          <el-input
            v-model="formData.title"
            :placeholder="text.titlePlaceholder"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="text.payValueLabel" prop="payValue">
          <el-input-number
            v-model="formData.payValue"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 220px"
          />
        </el-form-item>

        <el-form-item :label="text.actualValueLabel" prop="actualValue">
          <el-input-number
            v-model="formData.actualValue"
            :min="0.01"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 220px"
          />
        </el-form-item>

        <el-form-item :label="text.stockLabel" prop="stock">
          <el-input-number
            v-model="formData.stock"
            :min="1"
            :step="1"
            :precision="0"
            controls-position="right"
            style="width: 220px"
          />
        </el-form-item>

        <el-form-item :label="text.beginTimeLabel" prop="beginTime">
          <el-date-picker
            v-model="formData.beginTime"
            type="datetime"
            :placeholder="text.startPlaceholder"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item :label="text.endTimeLabel" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            :placeholder="text.endPlaceholder"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item :label="text.launchStatusLabel" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio
              v-for="item in formStatusOptions"
              :key="item.value"
              :label="item.value"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="voucher-page__dialog-footer">
          <el-button @click="handleCloseDialog">{{ text.cancel }}</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ text.save }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.voucher-page {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.voucher-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.voucher-page__filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.voucher-page__filters label {
  color: #606266;
  font-size: 14px;
}

.voucher-page__table-wrap {
  margin-bottom: 20px;
}

.voucher-page__pagination {
  display: flex;
  justify-content: flex-end;
}

.voucher-page__dialog-footer {
  display: inline-flex;
  gap: 12px;
}

@media (max-width: 1200px) {
  .voucher-page__toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
