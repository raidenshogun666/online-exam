<script setup>
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Timer, Checked, Warning, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)

const loading = ref(false)
const examStatus = ref('')
const questions = ref([])
const answers = ref({})

// --- 倒计时相关变量 ---
const remainingSeconds = ref(0)
const timerId = ref(null)

// --- 防作弊相关变量 ---
const switchCount = ref(0)
const MAX_SWITCH_LIMIT = 3

// 计算属性：将秒数转换为 HH:mm:ss 格式
const formattedTime = computed(() => {
  if (remainingSeconds.value <= 0) return '00:00:00'
  const h = Math.floor(remainingSeconds.value / 3600)
  const m = Math.floor((remainingSeconds.value % 3600) / 60)
  const s = remainingSeconds.value % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

// 计算属性：是否最后5分钟（用于变红提醒）
const isUrgent = computed(() => remainingSeconds.value > 0 && remainingSeconds.value < 300)

async function load() {
  loading.value = true
  const res = await http.get(`/exams/${examId}/take`).catch(() => null)
  loading.value = false

  // 1. 基础错误检查
  if (!res) { ElMessage.error('网络错误'); return }
  if (res.data.code !== 200) {
    ElMessage.error(res.data.msg || '无法加载题目')
    return
  }

  const d = res.data.data
  // console.log('后端返回数据:', d)

  // 2. 赋值基础数据
  examStatus.value = d.status || 'IN_PROGRESS'
  questions.value = d.questions || []

  // 3. 直接使用 duration (分钟) 来设置倒计时
  if (d.duration && d.duration > 0) {
    // 将分钟转为秒
    remainingSeconds.value = d.duration * 60
    console.log(`✅ 已获取考试时长: ${d.duration}分钟，倒计时开始`)
  } else {
    // 如果后端没传 duration，给个默认值（比如90分钟）或者提示
    console.warn('⚠️ 未获取到 duration，默认设置为 90 分钟')
    remainingSeconds.value = 90 * 60
  }

  // 4. 启动倒计时
  if (examStatus.value === 'IN_PROGRESS') {
    startCountdown()
  }
}

// --- 倒计时逻辑 ---
function startCountdown() {
  if (timerId.value) clearInterval(timerId.value)

  timerId.value = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      // 时间到
      clearInterval(timerId.value)
      ElMessageBox.alert('考试时间已到，系统正在自动交卷...', '时间到', {
        confirmButtonText: '确定',
        type: 'warning',
        callback: () => submit(true) // 强制提交
      })
    }
  }, 1000)
}

function isAllAnswered() {
  return questions.value.every(q => answers.value[q.questionId])
}

// 提交函数 (isForce: 是否强制提交)
async function submit(isForce = false) {
  if (!questions.value.length) return

  if (!isForce && !isAllAnswered()) {
    return ElMessage.warning('请先完成所有题目再提交')
  }

  try {
    if (!isForce) {
      await ElMessageBox.confirm('确定提交吗？提交后将无法修改', '交卷', {
        confirmButtonText: '确认交卷',
        cancelButtonText: '再检查一下',
        type: 'warning',
        center: true
      })
    }

    const payload = {}
    for (const q of questions.value) {
      // 防止 undefined 报错
      payload[`answer_${q.questionId}`] = answers.value[q.questionId] ? String(answers.value[q.questionId]) : ''
    }

    const res = await http.post(`/exams/${examId}/submit`, payload)

    // 停止倒计时
    if (timerId.value) clearInterval(timerId.value)

    if (res.data.code === 200) {
      const score = res.data.data?.totalScore ?? '待定'
      if (isForce) {
        ElMessage.success(`系统已自动交卷，最终得分：${score}`)
      } else {
        ElMessage.success(`提交成功！得分：${score}`)
      }
      router.replace('/s/records')
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  } catch (e) { /* cancelled */ }
}

// --- 防作弊逻辑 ---
function handleVisibilityChange() {
  if (document.hidden) {
    recordSwitch()
  }
}
function handleWindowBlur() {
  recordSwitch() // 只要切出窗口就算
}
function recordSwitch() {
  if (examStatus.value !== 'IN_PROGRESS') return
  switchCount.value++

  ElNotification({
    title: '警告：检测到切屏行为！',
    message: `请留在考试页面。当前切屏次数：${switchCount.value} / ${MAX_SWITCH_LIMIT}`,
    type: 'warning',
    duration: 3000
  })

  if (switchCount.value > MAX_SWITCH_LIMIT) {
    removeListeners()
    submit(true) // 超过切屏次数，强制交卷
  }
}

function addListeners() {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('blur', handleWindowBlur)
}
function removeListeners() {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('blur', handleWindowBlur)
}

onMounted(() => {
  load()
  addListeners()
})

onUnmounted(() => {
  if (timerId.value) clearInterval(timerId.value)
  removeListeners()
})
</script>

<template>
  <div v-loading="loading" class="exam-paper-container">
    <el-alert
        v-if="examStatus && examStatus !== 'IN_PROGRESS'"
        :title="`考试不可用: ${examStatus}`"
        type="error"
        show-icon
        center
        style="margin-bottom:20px;"
    />

    <div v-else class="paper-wrapper">
      <div class="sticky-header">
        <div class="exam-title">
          <h3>✍️ 答题中</h3>
          <span class="sub-info">共 {{ questions.length }} 题</span>
        </div>

        <div class="monitor-area">
          <el-tag :type="switchCount > 0 ? 'warning' : 'info'" effect="light" class="monitor-tag">
            <el-icon><Warning /></el-icon>
            切屏: {{ switchCount }}/{{ MAX_SWITCH_LIMIT }}
          </el-tag>
        </div>

        <div class="exam-timer" :class="{ 'urgent-timer': isUrgent }">
          <el-icon><Clock /></el-icon>
          <span class="timer-text">剩余时间: {{ formattedTime }}</span>
        </div>
      </div>

      <div class="questions-list animate__animated animate__fadeIn">
        <el-card
            v-for="(q, idx) in questions"
            :key="q.questionId"
            class="question-card"
            :class="{ 'answered-card': answers[q.questionId] }"
            shadow="hover"
        >
          <template #header>
            <div class="q-header">
              <el-tag effect="dark" round color="#667eea" style="border:none; margin-right: 10px;">
                第 {{ idx + 1 }} 题
              </el-tag>
              <span class="q-content">{{ q.content }}</span>
              <span class="q-score">{{ q.score }} 分</span>
            </div>
          </template>

          <el-radio-group v-model="answers[q.questionId]" class="options-group">
            <div
                v-for="op in q.options"
                :key="op.optionId"
                class="option-item"
                :class="{ 'is-selected': answers[q.questionId] === op.optionId }"
                @click="answers[q.questionId] = op.optionId"
            >
              <el-radio :value="op.optionId" class="custom-radio">
                <span class="option-label">{{ op.content }}</span>
              </el-radio>
            </div>
          </el-radio-group>
        </el-card>

        <div class="submit-area">
          <el-button
              type="primary"
              size="large"
              @click="submit(false)"
              class="submit-btn"
              round
              :icon="Checked"
          >
            提交试卷
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exam-paper-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 60px;
  user-select: none;
}

.paper-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

/* 吸顶头部布局调整 */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border: 1px solid #ebeef5;
}

.exam-title h3 { margin: 0; font-size: 18px; color: #303133; display: inline-block; }
.sub-info { font-size: 13px; color: #909399; margin-left: 10px; }

/* 倒计时样式 */
.exam-timer {
  color: #606266;
  font-weight: bold;
  display: flex;
  align-items: center;
  background: #f0f2f5;
  padding: 8px 15px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  font-family: monospace; /* 等宽字体，防止数字跳动 */
  font-size: 16px;
  transition: all 0.3s;
}

.timer-text { margin-left: 8px; }

/* 紧急状态（少于5分钟） */
.urgent-timer {
  color: #f56c6c;
  background: #fef0f0;
  border-color: #f56c6c;
  animation: pulse 1s infinite; /* 闪烁动画 */
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.monitor-area { margin: 0 15px; }
.monitor-tag { font-weight: bold; }

/* 题目卡片 */
.question-card {
  margin-bottom: 25px;
  border-radius: 12px;
  border: none;
  transition: all 0.3s;
}
.answered-card { border: 1px solid #67c23a; }

.q-header { display: flex; align-items: flex-start; line-height: 1.5; }
.q-content { font-size: 16px; font-weight: 600; color: #303133; flex: 1; }
.q-score { color: #909399; font-size: 13px; margin-left: 10px; white-space: nowrap;}

.options-group { width: 100%; display: flex; flex-direction: column; gap: 10px; margin-top: 10px; }
.option-item {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px 15px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.option-item:hover { background-color: #f5f7fa; border-color: #c0c4cc; }
.option-item.is-selected {
  background-color: #ecf5ff;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff inset;
}
.custom-radio { width: 100%; height: auto; margin-right: 0; }
.option-label { font-size: 15px; color: #606266; white-space: normal; line-height: 1.4; }

.submit-area { text-align: center; margin-top: 40px; }
.submit-btn { width: 200px; height: 50px; font-size: 18px; box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3); }
.submit-btn:hover { transform: translateY(-2px); }
</style>