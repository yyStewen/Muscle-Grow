<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployeeList, enableOrDisableEmployee } from '@/api/employee'

const router = useRouter()

// 查询输入框的值
const input = ref('')

// 分页数据
const counts = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 表格数据列表
const tableData = ref([])

// 获取员工列表数据
const initData = async () => {
  const params = {
    page: currentPage.value,
    pageSize: pageSize.value,
    name: input.value ? input.value : undefined
  }
  
  try {
    const res = await getEmployeeList(params)
    if (res.code === 1) {
      tableData.value = res.data.records || []
      counts.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取数据失败', error)
  }
}

// 首次加载初始化数据
onMounted(() => {
  initData()
})

// 分页条数改变
const handleSizeChange = (val) => {
  pageSize.value = val
  initData()
}

// 分页页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  initData()
}

// 查询按钮
const handleQuery = () => {
  currentPage.value = 1
  initData()
}

// 修改账号状态
const statusHandle = (row) => {
  const id = row.id
  const targetStatus = row.status === 1 ? 0 : 1
  
  ElMessageBox.confirm('确认调整该账号的状态吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await enableOrDisableEmployee({ id: id, status: targetStatus })
      if (res.code === 1) {
        ElMessage.success('账号状态成功调整！')
        initData()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
       console.error('状态修改异常', error)
    }
  })
}
</script>

<template>
  <h1>员工管理</h1>
  <br><br>
<div class="container">
  <div class="tableBar">
        <label style="margin-right: 5px">员工姓名：</label>
        <el-input
          v-model="input"
          placeholder="请输入员工姓名"
          style="width: 15%"
        />
        <el-button type="warning" class="normal-btn continue" @click="handleQuery"
          >查询</el-button
        >
        <el-button
          type="primary"
          style="float: right"
          @click="() => router.push('/emp/add')"
        >
          + 添加员工
        </el-button>
  </div>
  <el-table
        :data="tableData"
        stripe
        v-if="tableData.length"
        class="tableBox"
      >
        <el-table-column prop="name" label="员工姓名" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="账号状态">
          <template #default="scope">
            <div
              class="tableColumn-status"
              :class="{ 'stop-use': scope.row.status === 0 }"
            >
              {{ scope.row.status === 0 ? '禁用' : '启用' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button
              type="text"
              size="small"
              class="blueBug"
              :class="{ 'disabled-text': scope.row.username === 'admin' }"
              :disabled="scope.row.username === 'admin'"
              @click="() => router.push({ path: '/emp/add', query: { id: scope.row.id } })"
            >
              修改
            </el-button>
            <el-button
              :disabled="scope.row.username === 'admin'"
              type="text"
              size="small"
              class="non"
              :class="{
                'disabled-text': scope.row.username === 'admin',
                blueBug: scope.row.status === 0,
                delBut: scope.row.status !== 0,
              }"
              @click="statusHandle(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
</div>
</template>

<style scoped>
.tableBar {
  margin-bottom: 20px;
}
.pageList {
  margin-top: 20px;
  justify-content: flex-end;
}
.stop-use {
  color: #F56C6C;
}
.disabled-text {
  color: #C0C4CC !important;
  cursor: not-allowed;
}
</style>