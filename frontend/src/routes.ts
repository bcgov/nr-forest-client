import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// The landing page
import HomePage from '@/pages/HomePage.vue'
// The Submission page, AKA internal user page
import ReviewApplicationPage from '@/pages/ReviewApplicationPage.vue'
// The external user page
import ApplyClientNumber from '@/pages/ApplyClientNumberPage.vue'
// The form submitted page
import FormSubmittedPage from '@/pages/FormSubmittedPage.vue'
// The page user will see while loading the login data
import UserLoadingPage from '@/pages/UserLoadingPage.vue'
// The page users will land when redirected to the application and not logged in
import LoginPage from '@/pages/LoginPage.vue'
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
  },
  {
    path: '/dashboard',
    name: 'loading',
    component: UserLoadingPage,
    props: true
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export { routes, router }
