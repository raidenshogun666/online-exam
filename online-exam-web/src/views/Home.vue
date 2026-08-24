<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const exams = ref([])
const msg = ref('')

async function load() {
  msg.value = ''
  const me = await http.get('/user/me')
  if (me.data.code !== 200) {
    await router.push('/login')
    return
  }

  const res = await http.get('/exams')
  if (res.data.code === 200) exams.value = res.data.data || []
  else msg.value = res.data.msg || '加载失败'
}

async function logout() {
  await http.post('/user/logout')
  await router.push('/login')
}

onMounted(load)
</script>

<template>
  <div style="max-width:720px;margin:30px auto;">
    <h3>考试列表</h3>
    <button @click="logout">退出</button>
    <div style="color:red;margin-top:8px;">{{ msg }}</div>

    <table border="1" cellspacing="0" cellpadding="6" style="margin-top:12px;width:100%;">
      <thead>
      <tr>
        <th>ID</th><th>名称</th><th>开始</th><th>结束</th><th>状态</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="e in exams" :key="e.examId">
        <td>{{ e.examId }}</td>
        <td>{{ e.examName }}</td>
        <td>{{ e.startTime }}</td>
        <td>{{ e.endTime }}</td>
        <td>{{ e.status }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>