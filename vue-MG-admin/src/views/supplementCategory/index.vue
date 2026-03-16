<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryPage,
  deleCategory,
  editCategory,
  addCategory,
  enableOrDisableEmployee
} from '@/api/category'

const options = [
  { value: '1', label: '补剂分类' },
  { value: '2', label: '套餐分类' }
]

// 分页与搜索状态
const name = ref('')
const categoryType = ref('')
const page = ref(1)
const pageSize = ref(10)
const counts = ref(0)
const records = ref([])
const loading = ref(false)

// 弹窗状态
const dialogVisible = ref(false)
const title = ref('新增分类')
const classData = ref({
  id: '',
  name: '',
  sort: '',
  type: '1'
})

// 表单校验引用
const classFormRef = ref()

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 初始化加载数据
const init = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      name: name.value ? name.value : undefined,
      type: categoryType.value ? categoryType.value : undefined
    }
    const res = await getCategoryPage(params)
    if (res.code === 1) {
      records.value = res.data.records
      counts.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取分类列表失败')
    }
  } catch (error) {
    console.error('加载分类异常', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  init()
})

// 分页方法
const handleSizeChange = (val) => {
  pageSize.value = val
  init()
}

const handleCurrentChange = (val) => {
  page.value = val
  init()
}

// 弹窗相关
const addCategoryHandle = (type) => {
  title.value = type === '1' ? '新增补剂分类' : '新增套餐分类'
  dialogVisible.value = true
  classData.value = {
    id: '',
    name: '',
    sort: '',
    type: type
  }
}

const editHandle = (row) => {
  title.value = '修改分类'
  dialogVisible.value = true
  classData.value = {
    id: row.id,
    name: row.name,
    sort: row.sort,
    type: String(row.type)
  }
}

const handleClose = () => {
  dialogVisible.value = false
  if (classFormRef.value) {
    classFormRef.value.resetFields()
  }
}

// 保存表单
const submitForm = async (formEl) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (classData.value.id) {
          // 修改
          res = await editCategory(classData.value)
        } else {
          // 新增
          res = await addCategory(classData.value)
        }

        if (res.code === 1) {
          ElMessage.success('操作成功')
          handleClose()
          init()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('保存异常', error)
      }
    }
  })
}

// 删除
const deleteHandle = (id) => {
  ElMessageBox.confirm('确认删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleCategory(id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        init()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
       console.error('删除异常', error)
    }
  }).catch(() => {
    // block empty catch
  })
}

// 状态启停
const statusHandle = (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  const actionText = targetStatus === 0 ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${actionText}该分类吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await enableOrDisableEmployee({ id: row.id, status: targetStatus })
      if (res.code === 1) {
        ElMessage.success('状态修改成功')
        init()
      } else {
        ElMessage.error(res.msg || '状态修改失败')
      }
    } catch (error) {
       console.error('状态修改异常', error)
    }
  }).catch(() => {
    // block empty catch
  })
}

</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar" style="display: inline-block; width: 100%; margin-bottom: 20px;">
        <label style="margin-right: 10px">分类名称：</label>
        <el-input 
          v-model="name"
          placeholder="请填写分类名称"
          style="width: 15%"
          clearable 
          @keyup.enter="init"
        />

        <label style="margin-right: 5px; margin-left: 20px">分类类型：</label>
        <el-select 
          v-model="categoryType"
          placeholder="请选择"
          clearable
          style="width: 15%"
          @change="init"
        >
          <el-option 
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value" 
          />
        </el-select>

        <el-button type="warning" style="margin-left: 20px;" @click="init">
          查询
        </el-button>

        <div style="float: right">
          <el-button type="primary" @click="addCategoryHandle('1')">
            + 新增补剂分类
          </el-button>
          <el-button type="primary" style="margin-left:20px" @click="addCategoryHandle('2')">
            + 新增套餐分类
          </el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" class="tableBox" stripe>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="type" label="分类类型">
          <template #default="scope">
            <span>{{ scope.row.type == '1' ? '补剂分类' : '套餐分类' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" />
        
        <el-table-column label="状态">
          <template #default="scope">
            <div class="tableColumn-status" :class="{ 'stop-use': scope.row.status === 0 }">
              {{ scope.row.status === 0 ? '禁用' : '启用' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="updateTime" label="操作时间" />
        
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editHandle(scope.row)">
              修改
            </el-button>
            <el-button type="danger" size="small" @click="deleteHandle(scope.row.id)">
              删除
            </el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              size="small" 
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

    <!-- 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="title"
      width="30%"
      :before-close="handleClose"
    >
      <el-form 
        :model="classData"
        ref="classFormRef"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称:" prop="name">
          <el-input 
            v-model="classData.name"
            placeholder="请输入分类名称"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="排序:" prop="sort">
          <el-input 
            v-model="classData.sort"
            placeholder="请输入排序"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取 消</el-button>
          <el-button type="primary" @click="submitForm(classFormRef)">确 定</el-button>
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
  padding: 40px;
  border-radius: 4px;
}
.tableBox {
  margin-top: 20px;
  margin-bottom: 20px;
}
.pageList {
  text-align: right;
}
</style>
