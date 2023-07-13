import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// The Submission page, AKA internal user page
import ReviewApplicationPage from '@/pages/ReviewApplicationPage.vue'
// The external user page
import ApplyClientNumber from '@/pages/ApplyClientNumberPage.vue'
// The form submitted page
import FormSubmittedPage from '@/pages/FormSubmittedPage.vue'
// The page user will see while loading the login data
import UserLoadingPage from '@/pages/UserLoadingPage.vue'
// The landing page
import LandingPage from '@/pages/LandingPage.vue'

import AmplifyUserSession from '@/helpers/AmplifyUserSession'

const routes: RouteRecordRaw[] = [
  {
    path: '/landing',
    name: 'home',
    component: LandingPage,
    props: true,
    meta: {
      hideHeader: true,
      requireAuth: false
    }
  },
  {
    path: '/new-client',
    alias: '/',
    name: 'form',
    component: ApplyClientNumber,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: true
    }
  },
  {
    path: '/form-submitted',
    name: 'confirmation',
    component: FormSubmittedPage,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: true
    }
  },
  {
    path: '/submissions',
    name: 'internal',
    component: ReviewApplicationPage,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: true
    }
  },
  {
    path: '/dashboard',
    name: 'loading',
    component: UserLoadingPage,
    props: true,
    meta: {
      hideHeader: true,
      requireAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/* This is a global guard that will run before each route change
   It will check if the user is logged in and if the route requires auth
  If the user is not logged in and the route requires auth, it will redirect to the home page for login
*/
router.beforeEach(async (to, from, next) => {
  const user = await AmplifyUserSession.isLoggedIn()
  if (to.meta.requireAuth && !user) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export { routes, router }
