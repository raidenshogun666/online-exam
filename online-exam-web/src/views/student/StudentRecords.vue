<script setup>
import { onMounted, ref } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const exams = ref([])

// 详情相关数据
const drawerVisible = ref(false)
const detailLoading = ref(false)
const summary = ref(null) // 考试概要
const detail = ref([])    // 题目详情列表

async function loadList() {
  loading.value = true
  const res = await http.get('/me/exams').catch(() => null)
  loading.value = false
  if (res && res.data.code === 200) {
    exams.value = res.data.data || []
  } else {
    ElMessage.error(res?.data?.msg || '加载记录失败')
  }
}

// 打开抽屉并加载详情
async function openDetail(examId) {
  drawerVisible.value = true
  detailLoading.value = true
  summary.value = null
  detail.value = []

  const res = await http.get(`/me/exams/${examId}`).catch(() => null)
  detailLoading.value = false

  if (res && res.data.code === 200) {
    summary.value = res.data.data.summary
    detail.value = res.data.data.detail || []
  } else {
    ElMessage.error(res?.data?.msg || '加载详情失败')
  }
}

onMounted(loadList)
</script>

<template>
  <div>
    <el-card>
      <template #header><span>📊 历史成绩记录</span></template>
      <el-table :data="exams" stripe v-loading="loading">
        <el-table-column prop="examId" label="ID" width="80" />
        <el-table-column prop="examName" label="试卷名称" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="totalScore" label="最终得分" width="100">
          <template #default="scope">
            <span style="font-weight:bold; color:#F56C6C;">{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row.examId)">查看错题</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 侧边抽屉显示详情 -->
    <el-drawer v-model="drawerVisible" title="考试详情" size="50%">
      <div v-loading="detailLoading">
        <div v-if="summary" style="margin-bottom:20px; padding:15px; background:#f4f4f5; border-radius:4px;">
          <h3 style="margin:0;">{{ summary.examName }}</h3>
          <p>提交时间：{{ summary.submitTime }}</p>
          <p>总分：<span style="color:#F56C6C; font-weight:bold; font-size:18px;">{{ summary.totalScore }}</span></p>
        </div>

        <el-table :data="detail" border v-if="detail.length">
          <el-table-column label="题号" type="index" width="60" />
          <el-table-column prop="questionContent" label="题目" min-width="200" />
          <el-table-column prop="myAnswer" label="我的答案" width="100" align="center" />
          <el-table-column prop="correctAnswer" label="正确答案" width="100" align="center">
            <template #default="scope">
              <span style="color:green; font-weight:bold;">{{ scope.row.correctAnswer }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结果" width="80" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isCorrect" type="success">正确</el-tag>
              <el-tag v-else type="danger">错误</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="60" align="center" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>