<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addEmployee, editEmployee, queryEmployeeById } from '@/api/employee'

const router = useRouter()
const route = useRoute()

// 模式判断：判断 URL 里面有没有 id 参数，如果有则处于修改模式（edit），没有就是新增模式（add）
const actionType = computed(() => {
  return route.query.id ? 'edit' : 'add'
})

// 表单引用的实例
const ruleFormRef = ref()

// 表单数据对象
const ruleForm = ref({
  id: '',
  username: '',
  name: '',
  phone: '',
  sex: 1, // 默认为男 (1:男, 0:女 或者 2:女)
  idNumber: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^[1][3-9][0-9]{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 获取详情进行回显
const initEmployeeDetails = async () => {
  if (actionType.value === 'edit' && route.query.id) {
    try {
      const res = await queryEmployeeById(route.query.id)
      if (res.code === 1) {
        // 数据回显赋值
        Object.assign(ruleForm.value, res.data)
      } else {
        ElMessage.error(res.msg || '获取员工信息失败')
      }
    } catch (error) {
      console.error('获取员工详情异常:', error)
    }
  }
}

onMounted(() => {
  initEmployeeDetails()
})

// 提交表单方法
const submitForm = async (formEl, isContinue = false) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        let res;
        if (actionType.value === 'add') {
           res = await addEmployee(ruleForm.value)
        } else {
           res = await editEmployee(ruleForm.value)
        }

        if (res.code === 1) {
          ElMessage.success(actionType.value === 'add' ? '员工添加成功！' : '员工信息修改成功！')
          if (isContinue) {
            // 继续添加员工：清空表单
            formEl.resetFields()
            ruleForm.value = { id: '', username: '', name: '', phone: '', sex: 1, idNumber: '' }
          } else {
            // 返回列表页
            router.push('/emp')
          }
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('保存异常', error)
      }
    } else {
      console.log('表单校验不通过')
      return false
    }
  })
}

// 返回列表操作
const goBack = () => {
  router.push('/emp')
}
</script>

<template>
  <div class="addBrand-container" id="member-app">
    <div class="container">
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="rules"
        :inline="false"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="账号:" prop="username">
          <el-input
            v-model="ruleForm.username"
            placeholder="请输入账号"
            maxlength="20"
            :disabled="actionType === 'edit'"
          />
        </el-form-item>
        
        <el-form-item label="员工姓名:" prop="name">
          <el-input
            v-model="ruleForm.name"
            placeholder="请输入员工姓名"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="手机号:" prop="phone">
          <el-input
            v-model="ruleForm.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="性别:" prop="sex">
          <el-radio-group v-model="ruleForm.sex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="身份证号:" prop="idNumber">
          <el-input
            v-model="ruleForm.idNumber"
            placeholder="请输入身份证号"
            maxlength="18"
          />
        </el-form-item>

        <div class="subBox address">
          <el-form-item>
            <el-button type="primary" @click="submitForm(ruleFormRef, false)">
              保存
            </el-button>
            <el-button 
              type="success" 
              @click="submitForm(ruleFormRef, true)"
              v-if="actionType === 'add'"
            >
              保存并继续添加员工
            </el-button>
            <el-button @click="goBack">
              返回
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.addBrand-container {
  padding: 30px;
}
.addBrand-container .container {
  background-color: #fff;
  padding: 40px;
  border-radius: 4px;
}
.demo-ruleForm {
  max-width: 600px;
  margin: 0 auto;
}
.subBox {
  margin-top: 40px;
  text-align: center;
}
</style>
