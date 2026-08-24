<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { Select, CloseBold } from '@element-plus/icons-vue'
const route = useRoute()
const examId = Number(route.params.examId)

const submissions = ref([])
const loading = ref(false)

// 详情控制
const drawerVisible = ref(false)
const detail = ref([])
const selectedStudentId = ref(null)

async function loadSubmissions() {
  loading.value = true
  const res = await http.get(`/exams/${examId}/submissions`).catch(() => null)
  loading.value = false
  if (res && res.data.code === 200) {
    submissions.value = res.data.data || []
  } else {
    ElMessage.error('加载提交记录失败')
  }
}

async function openDetail(studentId) {
  selectedStudentId.value = studentId
  drawerVisible.value = true
  detail.value = []

  const res = await http.get(`/exams/${examId}/submissions/${studentId}`).catch(() => null)
  if (res && res.data.code === 200) {
    detail.value = res.data.data || []
  } else {
    ElMessage.error('加载学生答卷详情失败')
  }
}

// 导出与复制
const scoreTableText = computed(() => {
  const lines = ['studentId,username,totalScore,submitTime']
  for (const s of submissions.value) {
    lines.push(`${s.studentId},${s.username},${s.totalScore},${s.submitTime}`)
  }
  return lines.join('\n')
})

function copyScoreTable() {
  navigator.clipboard.writeText(scoreTableText.value).then(() => {
    ElMessage.success('已复制 CSV 格式到剪贴板')
  })
}
function exportFile(fmt) {
  window.open(`/api/exams/${examId}/export?format=${fmt}`, '_blank')
}

onMounted(loadSubmissions)
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>📈 成绩与提交管理 (ExamID: {{ examId }})</span>
          <el-button-group>
            <el-button @click="copyScoreTable">复制数据</el-button>
            <el-button type="primary" plain @click="exportFile('csv')">导出CSV</el-button>
            <el-button type="success" plain @click="exportFile('xlsx')">导出Excel</el-button>
          </el-button-group>
        </div>
      </template>

      <el-table :data="submissions" stripe v-loading="loading">
        <el-table-column prop="studentId" label="学号/ID" width="100" sortable />
        <el-table-column prop="username" label="姓名/用户名" />
        <el-table-column prop="submitTime" label="提交时间" sortable />
        <el-table-column prop="totalScore" label="总分" sortable>
          <template #default="scope">
            <span style="font-weight:bold; color:#409EFF">{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row.studentId)">查看答卷</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 侧边栏展示答卷详情 -->
    <el-drawer v-model="drawerVisible" title="答卷详情" size="50%">
      <div v-if="detail.length">
        <el-table :data="detail" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="questionContent" label="题目" />
          <el-table-column prop="studentAnswer" label="学生答案" width="100" align="center" />
          <el-table-column prop="correctAnswer" label="标准答案" width="100" align="center" />
          <el-table-column label="判定" width="80" align="center">
            <template #default="scope">
              <el-icon v-if="scope.row.isCorrect" color="green"><Select /></el-icon>
              <el-icon v-else color="red"><CloseBold /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="earnedScore" label="得分" width="60" align="center" />
        </el-table>
      </div>
      <el-empty v-else description="暂无数据或加载中..." />
    </el-drawer>
  </div>
</template>