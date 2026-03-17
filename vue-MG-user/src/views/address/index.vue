<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import {
  createAddress,
  deleteAddress,
  getAddressById,
  getAddressList,
  setDefaultAddress,
  updateAddress
} from '@/api/address';
import chinaDivisionOptions from '@/assets/china-mainland-pca-code.json';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('create');
const formRef = ref();
const addresses = ref([]);

const createEmptyForm = () => ({
  id: null,
  consignee: '',
  phone: '',
  sex: '1',
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  regionCodes: [],
  detail: '',
  label: ''
});

const form = reactive(createEmptyForm());
const regionOptions = chinaDivisionOptions;
const regionCascaderProps = {
  value: 'code',
  label: 'name',
  children: 'children',
  emitPath: true
};

const rules = {
  consignee: [{ required: true, message: 'Please enter the consignee name', trigger: 'blur' }],
  phone: [
    { required: true, message: 'Please enter the phone number', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!/^1\d{10}$/.test(value || '')) {
          callback(new Error('Please enter a valid 11-digit phone number'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  regionCodes: [
    {
      required: true,
      message: 'Please select a province, city, and district',
      trigger: 'change'
    }
  ],
  detail: [{ required: true, message: 'Please enter the detailed address', trigger: 'blur' }]
};

const sortedAddresses = computed(() =>
  [...addresses.value].sort((a, b) => {
    if (Number(b.isDefault || 0) !== Number(a.isDefault || 0)) {
      return Number(b.isDefault || 0) - Number(a.isDefault || 0);
    }
    return Number(b.id || 0) - Number(a.id || 0);
  })
);

const defaultAddress = computed(() =>
  sortedAddresses.value.find((item) => Number(item.isDefault || 0) === 1) || null
);

const formatFullAddress = (item) =>
  [item.provinceName, item.cityName, item.districtName, item.detail].filter(Boolean).join(' ');

const resetRegionFields = () => {
  form.provinceCode = '';
  form.provinceName = '';
  form.cityCode = '';
  form.cityName = '';
  form.districtCode = '';
  form.districtName = '';
};

const getRegionNodesByCodes = (codes = []) => {
  const [provinceCode, cityCode, districtCode] = codes;
  const province = regionOptions.find((item) => item.code === provinceCode);
  const city = province?.children?.find((item) => item.code === cityCode);
  const district = city?.children?.find((item) => item.code === districtCode);

  if (!province || !city || !district) {
    return [];
  }

  return [province, city, district];
};

const resolveRegionCodes = (payload = {}) => {
  if (payload.provinceCode && payload.cityCode && payload.districtCode) {
    const codes = [payload.provinceCode, payload.cityCode, payload.districtCode];
    if (getRegionNodesByCodes(codes).length === 3) {
      return codes;
    }
  }

  if (payload.provinceName && payload.cityName && payload.districtName) {
    const province = regionOptions.find((item) => item.name === payload.provinceName);
    const city = province?.children?.find((item) => item.name === payload.cityName);
    const district = city?.children?.find((item) => item.name === payload.districtName);

    if (province && city && district) {
      return [province.code, city.code, district.code];
    }
  }

  return [];
};

const syncRegionFields = (codes = []) => {
  const nodes = getRegionNodesByCodes(codes);

  if (nodes.length !== 3) {
    resetRegionFields();
    return;
  }

  const [province, city, district] = nodes;
  form.provinceCode = province.code;
  form.provinceName = province.name;
  form.cityCode = city.code;
  form.cityName = city.name;
  form.districtCode = district.code;
  form.districtName = district.name;
};

const fillForm = (payload = {}) => {
  const regionCodes = resolveRegionCodes(payload);
  Object.assign(form, createEmptyForm(), payload, { regionCodes });

  if (regionCodes.length === 3) {
    syncRegionFields(regionCodes);
  }
};

const normalizePayload = () => ({
  id: form.id || undefined,
  consignee: form.consignee.trim(),
  phone: form.phone.trim(),
  sex: form.sex,
  provinceCode: form.provinceCode?.trim() || '',
  provinceName: form.provinceName.trim(),
  cityCode: form.cityCode?.trim() || '',
  cityName: form.cityName.trim(),
  districtCode: form.districtCode?.trim() || '',
  districtName: form.districtName.trim(),
  detail: form.detail.trim(),
  label: form.label?.trim() || ''
});

const handleRegionChange = (codes = []) => {
  form.regionCodes = Array.isArray(codes) ? codes : [];

  if (form.regionCodes.length !== 3) {
    resetRegionFields();
    return;
  }

  syncRegionFields(form.regionCodes);
  formRef.value?.validateField('regionCodes').catch(() => {});
};

const fetchAddresses = async () => {
  loading.value = true;

  try {
    const res = await getAddressList();
    if (res.code === 1) {
      addresses.value = res.data || [];
    }
  } finally {
    loading.value = false;
  }
};

const openCreateDialog = () => {
  dialogMode.value = 'create';
  fillForm();
  dialogVisible.value = true;
  nextTick(() => formRef.value?.clearValidate());
};

const openEditDialog = async (id) => {
  submitting.value = true;

  try {
    const res = await getAddressById(id);
    if (res.code === 1) {
      dialogMode.value = 'edit';
      fillForm(res.data || {});
      dialogVisible.value = true;
      nextTick(() => formRef.value?.clearValidate());
    }
  } finally {
    submitting.value = false;
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  fillForm();
  formRef.value?.clearValidate();
};

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false);
  if (!valid) return;

  submitting.value = true;

  try {
    const payload = normalizePayload();
    const res =
      dialogMode.value === 'create'
        ? await createAddress(payload)
        : await updateAddress(payload);

    if (res.code === 1) {
      ElMessage.success(dialogMode.value === 'create' ? 'Address created' : 'Address updated');
      closeDialog();
      await fetchAddresses();
    }
  } finally {
    submitting.value = false;
  }
};

const handleSetDefault = async (item) => {
  if (Number(item.isDefault || 0) === 1) return;

  const res = await setDefaultAddress(item.id);
  if (res.code === 1) {
    ElMessage.success('Default address updated');
    await fetchAddresses();
  }
};

const handleDelete = (item) => {
  ElMessageBox.confirm(`Delete address for ${item.consignee}?`, 'Confirm', {
    confirmButtonText: 'Delete',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    const res = await deleteAddress(item.id);
    if (res.code === 1) {
      ElMessage.success('Address deleted');
      await fetchAddresses();
    }
  }).catch(() => {});
};

onMounted(async () => {
  await fetchAddresses();
});
</script>

<template>
  <div class="address-page" v-loading="loading">
    <section class="address-page__hero">
      <div>
        <p class="address-page__eyebrow">Address Book</p>
        <h2>Manage Shipping Addresses</h2>
        <span>
          Save consignee information, edit delivery details, delete old addresses, and mark one
          address as the default option.
        </span>
      </div>

      <el-button type="warning" class="address-page__create" @click="openCreateDialog">
        Add Address
      </el-button>
    </section>

    <section v-if="defaultAddress" class="address-page__default-card">
      <p class="address-page__section-label">Default Address</p>
      <div class="address-page__default-main">
        <div>
          <h3>{{ defaultAddress.consignee }}</h3>
          <span>{{ defaultAddress.phone }}</span>
        </div>
        <el-tag effect="dark" type="warning">Default</el-tag>
      </div>
      <p class="address-page__default-address">{{ formatFullAddress(defaultAddress) }}</p>
      <small v-if="defaultAddress.label">{{ defaultAddress.label }}</small>
    </section>

    <section v-if="sortedAddresses.length" class="address-list">
      <article v-for="item in sortedAddresses" :key="item.id" class="address-card">
        <div class="address-card__top">
          <div>
            <div class="address-card__title">
              <h3>{{ item.consignee }}</h3>
              <el-tag v-if="Number(item.isDefault || 0) === 1" effect="dark" type="warning">
                Default
              </el-tag>
            </div>
            <p class="address-card__meta">
              <span>{{ item.phone }}</span>
              <span>{{ item.sex === '0' ? 'Female' : 'Male' }}</span>
            </p>
          </div>

          <div class="address-card__actions">
            <el-button
              plain
              type="warning"
              :disabled="Number(item.isDefault || 0) === 1"
              @click="handleSetDefault(item)"
            >
              Set Default
            </el-button>
            <el-button plain @click="openEditDialog(item.id)">Edit</el-button>
            <el-button plain type="danger" @click="handleDelete(item)">Delete</el-button>
          </div>
        </div>

        <p class="address-card__address">{{ formatFullAddress(item) }}</p>
        <div class="address-card__footer">
          <span v-if="item.label">{{ item.label }}</span>
          <span v-else>No label</span>
        </div>
      </article>
    </section>

    <section v-else class="address-page__empty">
      <el-empty description="No address has been added yet." />
      <el-button type="warning" @click="openCreateDialog">Create First Address</el-button>
    </section>

    <el-dialog
      :model-value="dialogVisible"
      :title="dialogMode === 'create' ? 'Add Address' : 'Edit Address'"
      width="720px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="address-form">
        <div class="address-form__grid">
          <el-form-item label="Consignee" prop="consignee">
            <el-input v-model="form.consignee" placeholder="Enter consignee name" />
          </el-form-item>

          <el-form-item label="Phone" prop="phone">
            <el-input v-model="form.phone" placeholder="Enter phone number" maxlength="11" />
          </el-form-item>

          <el-form-item label="Sex">
            <el-radio-group v-model="form.sex">
              <el-radio-button label="1">Male</el-radio-button>
              <el-radio-button label="0">Female</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="Label">
            <el-input v-model="form.label" placeholder="Home / Office / Gym" />
          </el-form-item>

          <el-form-item label="Region" prop="regionCodes" class="address-form__region">
            <el-cascader
              v-model="form.regionCodes"
              :options="regionOptions"
              :props="regionCascaderProps"
              clearable
              filterable
              placeholder="Select province / city / district"
              @change="handleRegionChange"
            />
          </el-form-item>

          <el-form-item label="Province Code">
            <el-input v-model="form.provinceCode" placeholder="Auto generated" readonly />
          </el-form-item>

          <el-form-item label="City Code">
            <el-input v-model="form.cityCode" placeholder="Auto generated" readonly />
          </el-form-item>

          <el-form-item label="District Code">
            <el-input v-model="form.districtCode" placeholder="Auto generated" readonly />
          </el-form-item>
        </div>

        <el-form-item label="Detailed Address" prop="detail">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="3"
            placeholder="Street, building, room number, landmark, etc."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="address-form__footer">
          <el-button @click="closeDialog">Cancel</el-button>
          <el-button type="warning" :loading="submitting" @click="handleSubmit">
            Save Address
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.address-page {
  animation: fadeUp 0.45s ease;
}

.address-page__hero,
.address-page__default-card,
.address-list,
.address-page__empty {
  background: rgba(255, 251, 244, 0.94);
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.address-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
}

.address-page__eyebrow,
.address-page__section-label {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.address-page__hero h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #321f12;
}

.address-page__hero span {
  color: #7a5b3e;
  line-height: 1.7;
}

.address-page__create {
  min-width: 150px;
  height: 48px;
  border: none;
  border-radius: 18px;
}

.address-page__default-card {
  margin-top: 20px;
  padding: 26px;
  background:
    radial-gradient(circle at top right, rgba(255, 216, 104, 0.2), transparent 30%),
    linear-gradient(145deg, rgba(255, 248, 238, 0.96), rgba(249, 236, 215, 0.84));
}

.address-page__default-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.address-page__default-main h3 {
  margin: 0;
  font-size: 28px;
  color: #2f1d10;
}

.address-page__default-main span {
  display: inline-block;
  margin-top: 6px;
  color: #7b5b3f;
}

.address-page__default-address {
  margin: 14px 0 8px;
  color: #5e4327;
  line-height: 1.8;
}

.address-page__default-card small {
  color: #9a7348;
}

.address-list {
  margin-top: 20px;
  padding: 24px;
  display: grid;
  gap: 16px;
}

.address-card {
  padding: 20px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(113, 76, 31, 0.08);
}

.address-card__top {
  display: flex;
  justify-content: space-between;
  gap: 18px;
}

.address-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.address-card__title h3 {
  margin: 0;
  color: #311f12;
  font-size: 24px;
}

.address-card__meta {
  display: flex;
  gap: 12px;
  margin: 10px 0 0;
  color: #8b6946;
}

.address-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.address-card__address {
  margin: 16px 0 10px;
  color: #6f5237;
  line-height: 1.8;
}

.address-card__footer {
  color: #ad7c45;
  font-size: 13px;
}

.address-page__empty {
  margin-top: 20px;
  padding: 30px;
  display: grid;
  place-items: center;
  gap: 12px;
}

.address-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.address-form__region {
  grid-column: 1 / -1;
}

.address-form__region :deep(.el-cascader) {
  width: 100%;
}

.address-form__footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1180px) {
  .address-page__hero,
  .address-card__top {
    flex-direction: column;
    align-items: flex-start;
  }

  .address-form__grid {
    grid-template-columns: 1fr;
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
