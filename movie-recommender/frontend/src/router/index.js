import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/chat', component: () => import('../views/ChatView.vue'), meta: { auth: true } },
  { path: '/favorites', component: () => import('../views/FavoritesView.vue'), meta: { auth: true } },
  { path: '/', redirect: '/chat' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.auth && !localStorage.getItem('token')) {
    return '/login'
  }
})

export default router
