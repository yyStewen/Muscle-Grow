<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { getAddressList } from '@/api/address';
import { submitOrder } from '@/api/order';
import { useCartStore } from '@/stores/cart';
import { formatCurrency } from '@/utils/shop';

const router = useRouter();
const cartStore = useCartStore();

const loading = ref(false);
const submitting = ref(false);
const addresses = ref([]);
const selectedAddressId = ref(null);

const form = reactive({
  payMethod: 1,
  remark: '',
  estimatedDeliveryTime: ''
});

const defaultDeliveryTime = () => {
  const date = new Date(Date.now() + 60 * 60 * 1000);
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const selectedAddress = computed(() =>
  addresses.value.find((item) => Number(item.id) === Number(selectedAddressId.value)) || null
);

const totalAmount = computed(() => cartStore.totalAmount.value);
const totalCount = computed(() => cartStore.totalCount.value);

const formatAddress = (item) =>
  [item?.provinceName, item?.cityName, item?.districtName, item?.detail].filter(Boolean).join(' ');

const fetchCheckoutData = async () => {
  loading.value = true;

  try {
    await cartStore.fetchCart();
    const res = await getAddressList();
    if (res.code === 1) {
      addresses.value = res.data || [];
      const defaultItem =
        addresses.value.find((item) => Number(item.isDefault || 0) === 1) || addresses.value[0];
      selectedAddressId.value = defaultItem?.id || null;
    }
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!cartStore.state.items.length) {
    ElMessage.warning('Your cart is empty.');
    return;
  }

  if (!selectedAddressId.value) {
    ElMessage.warning('Please select a shipping address first.');
    return;
  }

  submitting.value = true;

  try {
    const res = await submitOrder({
      addressBookId: selectedAddressId.value,
      payMethod: form.payMethod,
      remark: form.remark?.trim() || '',
      estimatedDeliveryTime: form.estimatedDeliveryTime || defaultDeliveryTime(),
      deliveryStatus: 1,
      tablewareNumber: 1,
      tablewareStatus: 1,
      packAmount: 0,
      amount: totalAmount.value
    });

    if (res.code === 1) {
      ElMessage.success('Order created. Please complete payment next.');
      await cartStore.fetchCart();
      router.push(`/user/orders/${res.data.id}`);
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  form.estimatedDeliveryTime = defaultDeliveryTime();
  await fetchCheckoutData();
});
</script>

<template>
  <div class="order-checkout" v-loading="loading">
    <section class="order-checkout__hero">
      <div>
        <p class="order-checkout__eyebrow">Create Order</p>
        <h2>Confirm Your Order</h2>
        <span>
          This step submits the current cart as one order. Payment is simulated on the next screen.
        </span>
      </div>

      <div class="order-checkout__hero-actions">
        <el-button @click="router.push('/user/cart')">Back To Cart</el-button>
        <el-button type="warning" :loading="submitting" @click="handleSubmit">Submit Order</el-button>
      </div>
    </section>

    <section v-if="!cartStore.state.items.length" class="order-checkout__empty">
      <el-empty description="Your cart is empty." />
      <el-button type="warning" @click="router.push('/user/home')">Go Shopping</el-button>
    </section>

    <div v-else class="order-checkout__grid">
      <section class="order-checkout__panel">
        <div class="order-checkout__section-title">
          <div>
            <p>Address</p>
            <h3>Select Shipping Address</h3>
          </div>
          <el-button text type="warning" @click="router.push('/user/address')">Manage Address</el-button>
        </div>

        <div v-if="addresses.length" class="address-grid">
          <label
            v-for="item in addresses"
            :key="item.id"
            class="address-card"
            :class="{ 'address-card--active': Number(selectedAddressId) === Number(item.id) }"
          >
            <input v-model="selectedAddressId" type="radio" :value="item.id" class="address-card__radio" />

            <div class="address-card__head">
              <div>
                <strong>{{ item.consignee }}</strong>
                <span>{{ item.phone }}</span>
              </div>
              <el-tag v-if="Number(item.isDefault || 0) === 1" type="warning" effect="dark">Default</el-tag>
            </div>

            <p>{{ formatAddress(item) }}</p>
            <small>{{ item.label || 'No label' }}</small>
          </label>
        </div>

        <el-empty v-else description="No address available." />

        <div class="order-checkout__section-title order-checkout__section-title--form">
          <div>
            <p>Note</p>
            <h3>Delivery Notes</h3>
          </div>
        </div>

        <div class="order-form">
          <el-form label-position="top">
            <el-form-item label="Payment Method">
              <el-radio-group v-model="form.payMethod">
                <el-radio-button :label="1">Mock WeChat Pay</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="Estimated Delivery Time">
              <el-date-picker
                v-model="form.estimatedDeliveryTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="Select estimated delivery time"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="Remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="Training note, building number, or any merchant message."
              />
            </el-form-item>
          </el-form>
        </div>
      </section>

      <aside class="order-summary">
        <p class="order-summary__eyebrow">Cart Snapshot</p>
        <h3>Order Summary</h3>

        <div class="order-summary__address" v-if="selectedAddress">
          <strong>{{ selectedAddress.consignee }} / {{ selectedAddress.phone }}</strong>
          <span>{{ formatAddress(selectedAddress) }}</span>
        </div>

        <div class="order-summary__items">
          <article
            v-for="item in cartStore.state.items"
            :key="item.key"
            class="order-summary__item"
          >
            <img :src="item.image" :alt="item.name" />
            <div>
              <strong>{{ item.name }}</strong>
              <span v-if="item.specText">{{ item.specText }}</span>
              <small>{{ item.quantity }} x ¥{{ formatCurrency(item.price) }}</small>
            </div>
          </article>
        </div>

        <div class="order-summary__row">
          <span>Total quantity</span>
          <strong>{{ totalCount }}</strong>
        </div>

        <div class="order-summary__row order-summary__row--amount">
          <span>Order amount</span>
          <strong>¥{{ formatCurrency(totalAmount) }}</strong>
        </div>

        <el-button class="order-summary__submit" type="warning" :loading="submitting" @click="handleSubmit">
          Submit Order
        </el-button>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.order-checkout {
  animation: fadeUp 0.45s ease;
}

.order-checkout__hero,
.order-checkout__panel,
.order-summary,
.order-checkout__empty {
  background: rgba(255, 251, 244, 0.94);
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.order-checkout__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
}

.order-checkout__eyebrow,
.order-checkout__section-title p,
.order-summary__eyebrow {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.order-checkout__hero h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #321f12;
}

.order-checkout__hero span {
  color: #7a5b3e;
  line-height: 1.7;
}

.order-checkout__hero-actions {
  display: flex;
  gap: 12px;
}

.order-checkout__grid {
  display: grid;
  grid-template-columns: 1.35fr 0.8fr;
  gap: 20px;
  margin-top: 20px;
}

.order-checkout__panel,
.order-summary {
  padding: 26px;
}

.order-checkout__section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.order-checkout__section-title h3,
.order-summary h3 {
  margin: 0;
  color: #311f12;
  font-size: 28px;
}

.order-checkout__section-title--form {
  margin-top: 28px;
}

.address-grid {
  display: grid;
  gap: 14px;
}

.address-card {
  position: relative;
  display: block;
  padding: 18px;
  background: #ffffff;
  border: 1px solid rgba(186, 145, 83, 0.12);
  border-radius: 22px;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.address-card--active {
  border-color: #e2a246;
  transform: translateY(-1px);
}

.address-card__radio {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.address-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.address-card strong {
  color: #2f1d10;
}

.address-card span,
.address-card p,
.address-card small {
  display: block;
  color: #7b5b3f;
  line-height: 1.7;
}

.order-summary__address {
  padding: 16px 18px;
  background: #fff8ef;
  border-radius: 20px;
}

.order-summary__address strong,
.order-summary__address span {
  display: block;
}

.order-summary__address span {
  margin-top: 8px;
  color: #826245;
  line-height: 1.7;
}

.order-summary__items {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.order-summary__item {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 14px;
  align-items: center;
}

.order-summary__item img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 18px;
  background: #fff0dc;
}

.order-summary__item strong,
.order-summary__item span,
.order-summary__item small {
  display: block;
}

.order-summary__item span,
.order-summary__item small {
  margin-top: 6px;
  color: #866447;
}

.order-summary__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid rgba(188, 153, 103, 0.12);
  color: #725438;
}

.order-summary__row--amount strong {
  font-size: 28px;
  color: #ff7d1c;
}

.order-summary__submit {
  width: 100%;
  height: 50px;
  margin-top: 22px;
  border: none;
  border-radius: 18px;
}

.order-checkout__empty {
  margin-top: 20px;
  padding: 30px;
  display: grid;
  place-items: center;
  gap: 12px;
}

@media (max-width: 1320px) {
  .order-checkout__grid {
    grid-template-columns: 1fr;
  }

  .order-checkout__hero {
    flex-direction: column;
    align-items: flex-start;
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
