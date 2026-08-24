import { createRouter, createWebHistory } from 'vue-router'
import http from '../api/http'

// 公共页
import Login from '../views/public/Login.vue'
import Register from '../views/public/Register.vue'

// 布局
import TeacherLayout from '../views/teacher/TeacherLayout.vue'
import StudentLayout from '../views/student/StudentLayout.vue'

// 教师页面
import TeacherExams from '../views/teacher/TeacherExams.vue'
import TeacherExamCreate from '../views/teacher/TeacherExamCreate.vue'
import TeacherExamEdit from '../views/teacher/TeacherExamEdit.vue'
import TeacherSubmissions from '../views/teacher/TeacherSubmissions.vue'

// 学生页面
import StudentExams from '../views/student/StudentExams.vue'
import StudentTakeExam from '../views/student/StudentTakeExam.vue'
import StudentRecords from '../views/student/StudentRecords.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', redirect: '/login' },

        { path: '/login', component: Login, meta: { public: true } },
        { path: '/register', component: Register, meta: { public: true } },

        // 教师端
        {
            path: '/t',
            component: TeacherLayout,
            meta: { role: 'teacher' },
            children: [
                { path: '', redirect: '/t/exams' },
                { path: 'exams', component: TeacherExams },
                { path: 'exams/create', component: TeacherExamCreate },
                { path: 'exams/:examId/edit', component: TeacherExamEdit },
                { path: 'exams/:examId/submissions', component: TeacherSubmissions }
            ]
        },

        // 学生端
        {
            path: '/s',
            component: StudentLayout,
            meta: { role: 'student' },
            children: [
                { path: '', redirect: '/s/exams' },
                { path: 'exams', component: StudentExams },
                { path: 'exams/:examId/take', component: StudentTakeExam },
                { path: 'records', component: StudentRecords }
            ]
        }
    ]
})

// 简单守卫：未登录 -> /login；角色不对 -> /login
router.beforeEach(async (to) => {
    if (to.meta.public) return true

    const me = await http.get('/user/me').catch(() => null)
    if (!me || me.data.code !== 200) return '/login'

    const roleNeed = to.matched.find(r => r.meta?.role)?.meta?.role
    if (roleNeed && me.data.data?.role?.toLowerCase() !== roleNeed) return '/login'

    // 如果用户访问 /login 但已登录：按角色跳转
    if (to.path === '/login') {
        const role = me.data.data?.role?.toLowerCase()
        if (role === 'teacher') return '/t'
        if (role === 'student') return '/s'
    }

    return true
})

export default router