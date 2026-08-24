<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const exams = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  const res = await http.get('/exams').catch(() => null)
  loading.value = false
  if (!res || res.data.code !== 200) {
    ElMessage.error(res?.data?.msg || '加载失败')
    return
  }
  exams.value = res.data.data || []
}

function canEnter(status) {
  return String(status).toUpperCase() === 'IN_PROGRESS'
}

function enterExam(examId, status) {
  if (!canEnter(status)) return
  router.push(`/s/exams/${examId}/take`)
}

// 映射状态颜色
function getStatusType(status) {
  const s = String(status).toUpperCase()
  if (s === 'IN_PROGRESS') return 'success'
  if (s === 'NOT_STARTED') return 'info'
  return 'danger'
}
function getStatusText(status) {
  const s = String(status).toUpperCase()
  if (s === 'NOT_STARTED') return '未开始'
  if (s === 'IN_PROGRESS') return '进行中'
  if (s === 'ENDED') return '已结束'
  return status
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>📝 当前可用考试</span>
        <el-button circle icon="Refresh" @click="load"></el-button>
      </div>
    </template>

    <el-table :data="exams" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="examName" label="试卷名称" min-width="150" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="duration" label="时长(分)" width="100" />

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
              type="primary"
              size="small"
              :disabled="!canEnter(scope.row.status)"
              @click="enterExam(scope.row.examId, scope.row.status)"
          >
            进入考试
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>