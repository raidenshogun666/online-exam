<script setup>
import http from '../../api/http'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Trophy, SwitchButton } from '@element-plus/icons-vue' // 引入图标

const router = useRouter()
const route = useRoute()

async function logout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', { type: 'warning' })
    await http.post('/user/logout')
    router.push('/login')
    ElMessage.success('已退出')
  } catch(e) {}
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside-menu">
      <div class="logo-area">
        <div class="logo-icon">🎓</div>
        <div class="logo-text">学生考试中心</div>
      </div>

      <el-menu
          active-text-color="#fff"
          background-color="#304156"
          text-color="#aeb9c6"
          :router="true"
          :default-active="route.path"
          class="custom-menu"
      >
        <el-menu-item index="/s/exams">
          <el-icon><Edit /></el-icon>
          <span>可参与考试</span>
        </el-menu-item>
        <el-menu-item index="/s/records">
          <el-icon><Trophy /></el-icon>
          <span>我的成绩</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <span style="color:#909399">当前位置 / </span>
          <span style="font-weight:600">{{ route.path.includes('exams') ? '在线考试' : '成绩查询' }}</span>
        </div>
        <div class="user-action">
          <span style="margin-right:15px; font-size:14px; color:#606266;">同学，你好</span>
          <el-button type="danger" size="small" @click="logout" :icon="SwitchButton" circle plain title="退出"></el-button>
        </div>
      </el-header>

      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container { height: 100vh; }

.aside-menu {
  background-color: #304156;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
  font-weight: bold;
  font-size: 18px;
}
.logo-icon { margin-right: 10px; font-size: 24px; }

.custom-menu { border-right: none; }
/* 选中项的高亮样式 */
.el-menu-item.is-active {
  background-color: #409EFF !important;
}
.el-menu-item:hover {
  background-color: #263445 !important;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 9;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>