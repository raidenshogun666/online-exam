<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Monitor } from '@element-plus/icons-vue' // 增加了一个 Monitor 图标
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

async function login() {
  if(!username.value || !password.value) return ElMessage.warning('请输入用户名和密码')

  loading.value = true
  const res = await http.post('/user/login', {
    username: username.value,
    password: password.value
  }).catch(() => null)
  loading.value = false

  if (!res) { ElMessage.error('网络错误'); return }
  if (res.data.code !== 200) { ElMessage.error(res.data.msg || '登录失败'); return }

  ElMessage.success('登录成功')
  const role = res.data.data?.role?.toLowerCase()
  if (role === 'teacher') router.push('/t')
  else if (role === 'student') router.push('/s')
}
</script>

<template>
  <div class="login-container">
    <div class="shape shape-1"></div>
    <div class="shape shape-2"></div>

    <el-card class="login-card animate__animated animate__fadeInUp">
      <template #header>
        <div class="card-header">
          <el-icon :size="40" color="#409eff" class="logo-icon"><Monitor /></el-icon>
          <h3>在线考试系统</h3>
          <p class="subtitle">Online Exam Management System</p>
        </div>
      </template>

      <el-form label-position="top" size="large">
        <el-form-item label="用户名">
          <el-input v-model="username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password @keyup.enter="login"/>
        </el-form-item>

        <el-button type="primary" class="login-btn" :loading="loading" @click="login" round>
          立即登录
        </el-button>

        <div class="footer-links">
          <router-link to="/register" class="link-text">没有账号？去注册 &rarr;</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  position: relative;
  background-color: #f0f2f5;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 动态背景球 */
.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 1;
  animation: float 5s infinite ease-in-out alternate;
}
.shape-1 {
  width: 350px;
  height: 350px;
  background: #667eea;
  top: -50px;
  left: -50px;
}
.shape-2 {
  width: 550px;
  height: 550px;
  background: #764ba2;
  bottom: -80px;
  right: -80px;
  animation-delay: -2s;
}

@keyframes float {
  0% { transform: translate(0, 0); }
  100% { transform: translate(60px, 60px); }
}

.login-card {
  width: 420px;
  z-index: 2;
  border-radius: 16px;
  /* 毛玻璃效果 */
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}
.logo-icon {
  margin-bottom: 10px;
  animation: rotate 50s linear infinite;
}
.subtitle {
  color: #909399;
  font-size: 12px;
  margin-top: -10px;
  letter-spacing: 1px;
}

.login-btn {
  width: 100%;
  margin-top: 20px;
  font-weight: bold;
  height: 45px;
  font-size: 16px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: transform 0.2s;
}
.login-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(118, 75, 162, 0.4);
}

.footer-links {
  margin-top: 20px;
  text-align: center;
}
.link-text {
  font-size: 14px;
  color: #606266;
  text-decoration: none;
  transition: color 0.3s;
}
.link-text:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 简单旋转动画 */
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>