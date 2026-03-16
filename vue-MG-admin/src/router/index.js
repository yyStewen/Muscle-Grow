import { createRouter, createWebHistory } from 'vue-router'

import IndexView from '@/views/index/index.vue'
import EmpView from '@/views/emp/index.vue'
import AddEmpView from '@/views/emp/add.vue'
import LayoutView from '@/views/layout/index.vue'
import LoginView from '@/views/login/index.vue'
import SupplementView from '@/views/supplement/index.vue'
import SupplementEditView from '@/views/supplement/edit.vue'
import SupplementCategoryView from '@/views/supplementCategory/index.vue'
import OrdersView from '@/views/orders/index.vue'
import SetmealView from '@/views/setmeal/index.vue'
import SetmealAddView from '@/views/setmeal/add.vue'
import SoldTop10View from '@/views/report/soldTop10/index.vue'
import IncomeReportView from '@/views/report/incomeReport/index.vue'
import OrdersReportView from '@/views/report/ordersReport/index.vue'
import UserReportView from '@/views/report/userReport/index.vue'
import VouchersView from '@/views/vouchers/index.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '',
      component: LayoutView,
      redirect: '/index', //重定向
      children: [
        { path: 'index', name: 'index', component: IndexView },
        { path: 'emp', name: 'emp', component: EmpView },
        { path: 'emp/add', name: 'addEmp', component: AddEmpView },
        { path: 'supplement', name: 'supplement', component: SupplementView },
        { path: 'supplement/edit', name: 'supplementEdit', component: SupplementEditView },
        { path: 'supplementCategory', name: 'supplementCategory', component: SupplementCategoryView },
        { path: 'orders', name: 'orders', component: OrdersView },
        { path: 'setmeal', name: 'setmeal', component: SetmealView },
        { path: 'setmeal/add', name: 'setmealAdd', component: SetmealAddView },
        { path: 'soldTop10', name: 'soldTop10', component: SoldTop10View },
        { path: 'incomeReport', name: 'incomeReport', component: IncomeReportView },
        { path: 'ordersReport', name: 'ordersReport', component: OrdersReportView },
        { path: 'userReport', name: 'userReport', component: UserReportView },
        { path: 'vouchers', name: 'vouchers', component: VouchersView }

      ]
    },
    { path: '/login', name: 'login', component: LoginView }
  ]
})

export default router
