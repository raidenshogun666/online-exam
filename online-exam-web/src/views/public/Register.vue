<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, VideoPause } from '@element-plus/icons-vue' // 引入图标
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref('')
const password = ref('')
const role = ref('student')
const loading = ref(false)

async function register() {
  if (!username.value || !password.value) return ElMessage.warning('请填写完整信息')

  loading.value = true
  const res = await http.post('/user/register', {
    username: username.value,
    password: password.value,
    role: role.value
  }).catch(() => null)
  loading.value = false

  if (res && res.data.code === 200) {
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } else {
    ElMessage.error((res?.data?.msg) || '注册失败')
  }
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card animate__animated animate__fadeInUp">
      <template #header>
        <div class="card-header">
          <h3>注册新账号</h3>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="设置密码" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item label="角色">
          <el-radio-group v-model="role">
            <el-radio-button label="student">我是学生</el-radio-button>
            <el-radio-button label="teacher">我是老师</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-button type="primary" style="width:100%; margin-top:10px;" :loading="loading" @click="register">
          立即注册
        </el-button>

        <div style="margin-top:15px; text-align: center;">
          <router-link to="/login" style="font-size: 14px; color: #606266;">返回登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  height: 100vh;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}
.register-card { width: 400px; border-radius: 12px; }
.card-header { text-align: center; font-weight: bold; }
</style>