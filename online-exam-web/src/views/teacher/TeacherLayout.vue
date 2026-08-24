<script setup>
import http from '../../api/http'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Plus } from '@element-plus/icons-vue'
const router = useRouter()
const route = useRoute()

async function logout() {
  await ElMessageBox.confirm('确定要退出登录吗?', '提示', { type: 'warning' })
  await http.post('/user/logout')
  router.push('/login')
  ElMessage.success('已退出')
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside-menu">
      <div class="logo">👨‍🏫 教师端</div>
      <!-- router 开启后，index 直接写路径即可跳转 -->
      <el-menu
          active-text-color="#ffd04b"
          background-color="#545c64"
          class="el-menu-vertical"
          default-active="/t/exams"
          text-color="#fff"
          :router="true"
          :default-active="route.path"
      >
        <el-menu-item index="/t/exams">
          <el-icon><List /></el-icon>
          <span>试卷管理</span>
        </el-menu-item>
        <el-menu-item index="/t/exams/create">
          <el-icon><Plus /></el-icon>
          <span>创建新试卷</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体内容 -->
    <el-container>
      <el-header class="header">
        <div class="breadcrumb">后台管理系统</div>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </el-header>

      <el-main>
        <!-- 淡入淡出过渡动画 -->
        <router-view v-slot="{ Component }">
          <transition name="el-fade-in-linear" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container { height: 100vh; }
.aside-menu { background-color: #545c64; color: white; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  background-color: #434a50;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-main { background-color: #f5f7fa; padding: 20px; }
</style>