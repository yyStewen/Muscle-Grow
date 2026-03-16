<script setup>
  import { ref } from 'vue'
  import { login } from '@/api/employee' 
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  
  let loginForm = ref({username:'', password:''})
  let router=useRouter()

  //登录
  const loginHandler = async () => {
    try {
      const result = await login(loginForm.value)
      if (result.code) {// 登录成功
        ElMessage.success('登录成功')
        localStorage.setItem('loginUser', JSON.stringify(result.data))
        router.push('/')// 跳转
      }else {
        ElMessage.error(result.msg)
      }
    } catch (error) {
      // 捕获请求拦截器抛出的“用户已登录，无法重复登录”等异常，防止控制台 Uncaught (in promise) 报错
      console.log('登录被阻止或请求出错:', error.message)
    }
  }



   //取消
  const clear = () => {
    loginForm.value = {
      username: '',
      password: ''
    }
  }
  
</script>

<template>
  <div id="container">
    <div class="login-form">
      <el-form label-width="80px">
        <p class="title">Muscle-Grow商城管理端</p>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button class="button" type="primary" @click="loginHandler">登 录</el-button>
          <el-button class="button" type="info" @click="clear">重 置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
#container {
  padding: 10%;
  height: 100vh;
  background-image: url('../../assets/bg1.jpg');
  background-repeat: no-repeat;
  background-size: cover;
}

.login-form {
  max-width: 400px;
  padding: 30px;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  background-color: white;
}

.title {
  font-size: 30px;
  font-family: '楷体';
  text-align: center;
  margin-bottom: 30px;
  font-weight: bold;
}

.button {
  margin-top: 30px;
  width: 120px;
}
</style>