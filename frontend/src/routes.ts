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
// The generic error page
import ErrorPage from '@/pages/ErrorPage.vue'

import ForestClientUserSession from '@/helpers/ForestClientUserSession'

const routes: RouteRecordRaw[] = [
  {
    path: '/landing',
    name: 'home',
    component: LandingPage,
    props: true,
    meta: {
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: false,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc'],
      redirectTo: {
        idir: 'internal',
        bceidbusiness: 'form'
      }
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
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc']
    }
  },
  {
    path: '/form-submitted',
    name: 'confirmation',
    component: FormSubmittedPage,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc']
    }
  },
  {
    path: '/submissions',
    name: 'internal',
    component: ReviewApplicationPage,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc']
    }
  },
  {
    path: '/dashboard',
    name: 'loading',
    component: UserLoadingPage,
    props: true,
    meta: {
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc'],
      redirectTo: {
        idir: 'form',
        bceidbusiness: 'form',
        bcsc: 'internal'
      }
    }
  },
  {
    path: '/error',
    name: 'error',
    component: ErrorPage,
    props: true,
    meta: {
      hideHeader: false,
      requireAuth: false,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc']
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.requireAuth) {
    if (!ForestClientUserSession.isLoggedIn()) {
      next({ name: 'home' })
    } else {
      const user = ForestClientUserSession.loadDetails()
      to.meta.visibleTo.includes(user!!.provider)
        ? next()
        : next({ name: 'error' })
    }
  } else {
    if (!to.meta.showLoggedIn) {
      const user = ForestClientUserSession.loadDetails()
      if (user) {
        next({ name: to.meta.redirectTo?.[user.provider] })
        return
      }
    }
    next()
  }
})

export { routes, router }

declare module 'vue-router' {
  // eslint-disable-next-line no-unused-vars
  interface RouteMeta {
    hideHeader: boolean
    requireAuth: boolean
    showLoggedIn: boolean
    visibleTo: Array<string>
    redirectTo?: Record<string, string>
  }
}
