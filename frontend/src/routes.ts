import { createRouter, createWebHashHistory } from 'vue-router'

import ReviewApplicationPage from '@/pages/ReviewApplicationPage.vue'
import ApplyClientNumber from '@/pages/ApplyClientNumberPage.vue'
import FormSubmittedPage from '@/pages/FormSubmittedPage.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: ApplyClientNumber, // Should be HomePage in the future
    props: true
  },
  {
    path: '/new-client',
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

export const router = createRouter({
  history: createWebHashHistory(),
  routes
})
