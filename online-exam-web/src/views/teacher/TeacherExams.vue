<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentAdd, Edit, Delete, View } from '@element-plus/icons-vue' // 引入图标
import http from '../../api/http'

const router = useRouter()
const exams = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  const res = await http.get('/exams').catch(() => null)
  loading.value = false
  if (res?.data?.code === 200) {
    exams.value = res.data.data || []
  }
}

async function removeExam(id) {
  try {
    await ElMessageBox.confirm('确定删除该试卷吗？此操作不可恢复', '警告', { type: 'warning' })
    await http.delete(`/exams/${id}`)
    ElMessage.success('删除成功')
    load()
  } catch { /* cancel */ }
}

onMounted(load)
</script>

<template>
  <el-card shadow="never" class="custom-card animate__animated animate__fadeIn">
    <template #header>
      <div class="page-header">
        <div class="left">
          <span class="title">📂 试卷管理</span>
          <span class="subtitle">管理所有考试及批改</span>
        </div>
        <el-button type="primary" size="large" :icon="DocumentAdd" @click="router.push('/t/exams/create')" round>
          创建新试卷
        </el-button>
      </div>
    </template>

    <el-table :data="exams" v-loading="loading" :header-cell-style="{background:'#f5f7fa', color:'#606266'}" style="width: 100%">
      <el-table-column prop="examId" label="ID" width="80" align="center" />
      <el-table-column prop="examName" label="试卷名称" min-width="150">
        <template #default="{ row }">
          <span style="font-weight: 600; color: #303133">{{ row.examName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180">
        <template #default="{ row }">
          <el-tag type="info" effect="plain" size="small">{{ row.startTime }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'IN_PROGRESS' ? 'success' : 'info'" effect="dark" round>
            {{ row.status === 'IN_PROGRESS' ? '进行中' : row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" :icon="Edit" size="small" @click="router.push(`/t/exams/${row.examId}/edit`)" plain>编辑</el-button>
            <el-button type="success" :icon="View" size="small" @click="router.push(`/t/exams/${row.examId}/submissions`)" plain>批改</el-button>
            <el-button type="danger" :icon="Delete" size="small" @click="removeExam(row.examId)" plain>删除</el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.custom-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title { font-size: 18px; font-weight: bold; color: #303133; }
.subtitle { font-size: 13px; color: #909399; margin-left: 10px; font-weight: normal; }
</style>