<script setup>
import {ref, onMounted} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus';
import {useRouter} from 'vue-router'

let router = useRouter()





const loginName = ref('')
//定义钩子函数, 获取登录用户名
onMounted(() => {
  //获取登录用户名
  let loginUser = JSON.parse(localStorage.getItem('loginUser'))
  if (loginUser) {
    loginName.value = loginUser.name
  }
})

//退出登录
const logout = () => {
  //弹出确认框, 如果确认, 则退出登录, 跳转到登录页面
  ElMessageBox.confirm('确认退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {//确认, 则清空登录信息
    ElMessage.success('退出登录成功')
    localStorage.removeItem('loginUser')
    router.push('/login')//跳转到登录页面
  })
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="header">
        <span class="title">Muscle-Grow补剂商城管理端</span>
        <span class="right_tool">
          <a href="">
            <el-icon><EditPen /></el-icon> 修改密码 &nbsp;&nbsp;&nbsp; |  &nbsp;&nbsp;&nbsp;
          </a>
          <a href="javascript:void(0)" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出登录 【{{ loginName }}】
          </a>
        </span>
      </el-header>
      
      <el-container>
        <!-- 左侧菜单 -->
        <el-aside width="200px" class="aside">
          <!-- 左侧菜单栏 -->
          <el-menu router>
            <!-- 首页菜单 -->
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon> 工作台
            </el-menu-item>

            <!-- 补剂分类管理菜单 -->
            <el-menu-item index="/supplementCategory">
              <el-icon><Grid /></el-icon> 补剂分类管理
            </el-menu-item>

            <!-- 补剂管理菜单 -->
            <el-menu-item index="/supplement">
              <el-icon><Goods /></el-icon> 补剂管理
            </el-menu-item>

            <!-- 套餐管理菜单 -->
            <el-menu-item index="/setmeal">
              <el-icon><Handbag /></el-icon> 套餐管理
            </el-menu-item>

            <!-- 订单管理菜单 -->
            <el-menu-item index="/orders">
              <el-icon><Document /></el-icon> 订单管理
            </el-menu-item> 

            <!--员工管理菜单-->
            <el-menu-item index="/emp">
              <el-icon><User /></el-icon> 员工管理
            </el-menu-item>

            <!-- 优惠券管理菜单 -->
            <el-menu-item index="/vouchers">
              <el-icon><Ticket /></el-icon> 优惠券管理
            </el-menu-item>

            <!-- 数据统计管理 -->
            <el-sub-menu index="/report">
              <template #title>
                <el-icon><Histogram /></el-icon>数据统计管理
              </template>
              <el-menu-item index="/incomeReport">
                <el-icon><InfoFilled /></el-icon>营业额统计
              </el-menu-item>
              <el-menu-item index="/userReport">
                <el-icon><Share /></el-icon>用户统计
              </el-menu-item>
              <el-menu-item index="/ordersReport">
                <el-icon><Document /></el-icon>订单统计
              </el-menu-item>

               <el-menu-item index="/soldTop10">
                <el-icon><Document /></el-icon>销量排名前十统计
              </el-menu-item>
            </el-sub-menu>
            
            
            

            



          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
      
    </el-container>
  </div>
</template>

<style scoped>
.header {
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
}

.title {
  color: white;
  font-size: 40px;
  font-family: 楷体;
  line-height: 60px;
  font-weight: bolder;
}

.right_tool{
  float: right;
  line-height: 60px;
}

a {
  color: white;
  text-decoration: none;
}

.aside {
  width: 220px;
  border-right: 1px solid #ccc;
  height: 100vh;
}
</style>
