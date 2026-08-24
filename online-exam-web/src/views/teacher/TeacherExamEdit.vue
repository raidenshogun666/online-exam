<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const examId = Number(route.params.examId)
const loading = ref(false)

const exam = ref({
  examName: '',
  startTime: '',
  endTime: '',
  duration: 90,
  maxAttempts: 1,
  allowLate: false,
  latePenaltyPercent: 0
})

const questions = ref([])

// 新增题目表单
const qForm = ref({
  content: '',
  score: 5,
  a: '', b: '', c: '', d: '',
  correctIndex: 'A'
})

// 时间格式化辅助
function fromServerTime(v) { return v ? String(v).slice(0, 19) : '' }

async function loadExam() {
  const res = await http.get(`/exams/${examId}`).catch(() => null)
  if (!res || res.data.code !== 200) return ElMessage.error('加载试卷信息失败')

  const d = res.data.data
  exam.value = {
    ...d,
    startTime: fromServerTime(d.startTime),
    endTime: fromServerTime(d.endTime),
    allowLate: Boolean(d.allowLate)
  }
}

async function loadQuestions() {
  const res = await http.get(`/exams/${examId}/questions`).catch(() => null)
  if (res && res.data.code === 200) {
    questions.value = res.data.data || []
  }
}

async function saveExam() {
  loading.value = true
  const payload = {
    ...exam.value,
    duration: Number(exam.value.duration),
    maxAttempts: Number(exam.value.maxAttempts),
    latePenaltyPercent: Number(exam.value.latePenaltyPercent),
    startTime: exam.value.startTime, // 假设 Element 返回格式已正确
    endTime: exam.value.endTime
  }
  const res = await http.put(`/exams/${examId}`, payload).catch(() => null)
  loading.value = false
  if (res && res.data.code === 200) ElMessage.success('保存成功')
  else ElMessage.error('保存失败')
}

async function addQuestion() {
  if (!qForm.value.content || !qForm.value.a) return ElMessage.warning('请补全题目信息')

  const payload = { ...qForm.value, score: Number(qForm.value.score) }
  const res = await http.post(`/exams/${examId}/questions/add`, payload)

  if (res && res.data.code === 200) {
    ElMessage.success('添加题目成功')
    // 重置表单
    qForm.value = { content: '', score: 5, a: '', b: '', c: '', d: '', correctIndex: 'A' }
    loadQuestions()
  } else {
    ElMessage.error(res?.data?.msg || '添加失败')
  }
}

async function deleteQuestion(qid) {
  await ElMessageBox.confirm('确定删除此题吗?', '提示', { type: 'warning' })
  const res = await http.delete(`/exams/${examId}/questions/${qid}`)
  if (res && res.data.code === 200) {
    ElMessage.success('删除成功')
    loadQuestions()
  } else {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { loadExam(); loadQuestions(); })
</script>

<template>
  <div>
    <!-- 上半部分：试卷基本信息 -->
    <el-card class="box-card" style="margin-bottom:20px;">
      <template #header><span>📄 试卷设置 (ID: {{ examId }})</span></template>
      <el-form :inline="true" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="exam.examName" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="exam.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="exam.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="exam.duration" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveExam" :loading="loading" style="margin-top:30px;">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <!-- 左下：添加题目 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>➕ 新增题目</span></template>
          <el-form label-position="top">
            <el-form-item label="题干">
              <el-input type="textarea" v-model="qForm.content" :rows="3" />
            </el-form-item>
            <el-form-item label="分值">
              <el-input-number v-model="qForm.score" :min="1" />
            </el-form-item>
            <el-form-item label="正确选项">
              <el-radio-group v-model="qForm.correctIndex">
                <el-radio-button label="A" />
                <el-radio-button label="B" />
                <el-radio-button label="C" />
                <el-radio-button label="D" />
              </el-radio-group>
            </el-form-item>

            <el-form-item label="选项 A"><el-input v-model="qForm.a" /></el-form-item>
            <el-form-item label="选项 B"><el-input v-model="qForm.b" /></el-form-item>
            <el-form-item label="选项 C"><el-input v-model="qForm.c" /></el-form-item>
            <el-form-item label="选项 D"><el-input v-model="qForm.d" /></el-form-item>

            <el-button type="success" style="width:100%" @click="addQuestion">添加并保存</el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右下：题目列表 -->
      <el-col :span="16">
        <el-card>
          <template #header><span>📚 题目列表 ({{ questions.length }})</span></template>
          <el-table :data="questions" border height="600">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="content" label="题干" min-width="150" show-overflow-tooltip />
            <el-table-column prop="score" label="分" width="60" />
            <el-table-column label="选项预览" width="200">
              <template #default="scope">
                <div v-for="op in scope.row.options" :key="op.optionId" :style="{color: op.isCorrect ? 'green' : '#666'}">
                  {{ op.content }} {{ op.isCorrect ? '✔' : '' }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button type="danger" icon="Delete" circle size="small" @click="deleteQuestion(scope.row.questionId)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>