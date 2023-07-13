import {
  createRouter,
  createWebHashHistory,
  type RouteRecordRaw
} from 'vue-router'

import ReviewApplicationPage from '@/pages/ReviewApplicationPage.vue'
import ApplyClientNumber from '@/pages/ApplyClientNumberPage.vue'
import FormSubmittedPage from '@/pages/FormSubmittedPage.vue'
import LandingPage from '@/pages/LandingPage.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/landing', // Should be the root path in the future
    name: 'home',
    component: LandingPage,
    props: true,
    meta: {
      hideHeader: true
    }
  },
  {
    path: '/new-client',
    alias: '/',
    name: 'form',
    component: ApplyClientNumber,
    props: true
  },
  {
    path: '/form-submitted',
    name: 'confirmation',
    component: FormSubmittedPage,
    props: true
  },
  {
    path: '/submissions',
    name: 'internal',
    component: ReviewApplicationPage,
    props: true
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export { routes, router }
