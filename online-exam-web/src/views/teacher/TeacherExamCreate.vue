<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const form = ref({
  examName: '',
  startTime: '', // 格式: "2026-01-08 20:00:00"
  endTime: '',
  duration: 90,
  maxAttempts: 1,
  allowLate: false,
  latePenaltyPercent: 0
})


function fixTime(v) {
  if (!v) return v
  return v
}

async function submit() {
  if (!form.value.examName || !form.value.startTime || !form.value.endTime) {
    return ElMessage.warning('请填写必填项')
  }

  loading.value = true
  const payload = {
    ...form.value,
    // 确保转换类型
    duration: Number(form.value.duration),
    maxAttempts: Number(form.value.maxAttempts),
    latePenaltyPercent: Number(form.value.latePenaltyPercent),
    allowLate: Boolean(form.value.allowLate),
    // Element如果配置了format，这里直接传
    startTime: form.value.startTime,
    endTime: form.value.endTime
  }

  const res = await http.post('/exams', payload).catch(() => null)
  loading.value = false

  if (res && res.data.code === 200) {
    ElMessage.success('创建成功')
    router.push('/t/exams')
  } else {
    ElMessage.error(res?.data?.msg || '创建失败')
  }
}
</script>

<template>
  <el-card style="max-width: 800px; margin: 0 auto;">
    <template #header><h3>➕ 创建新试卷</h3></template>

    <el-form label-width="140px" style="max-width: 600px;">
      <el-form-item label="试卷名称" required>
        <el-input v-model="form.examName" placeholder="例如：2026期末考试" />
      </el-form-item>

      <el-form-item label="开始时间" required>
        <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="结束时间" required>
        <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="考试时长(分钟)">
        <el-input-number v-model="form.duration" :min="1" />
      </el-form-item>

      <el-divider content-position="left">提交策略</el-divider>

      <el-form-item label="最大提交次数">
        <el-input-number v-model="form.maxAttempts" :min="1" />
      </el-form-item>

      <el-form-item label="允许迟交">
        <el-switch v-model="form.allowLate" active-text="允许" inactive-text="禁止" />
      </el-form-item>

      <el-form-item label="迟交扣分(%)" v-if="form.allowLate">
        <el-slider v-model="form.latePenaltyPercent" show-input />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submit" :loading="loading">立即创建</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>