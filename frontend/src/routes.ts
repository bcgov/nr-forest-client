import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import ReviewApplicationPage from '@/pages/ReviewApplicationPage.vue'
import ApplyClientNumber from '@/pages/ApplyClientNumberPage.vue'
import FormSubmittedPage from '@/pages/FormSubmittedPage.vue'
import UserLoadingPage from '@/pages/UserLoadingPage.vue'
import LandingPage from '@/pages/LandingPage.vue'
import ErrorPage from '@/pages/ErrorPage.vue'
import NotFoundPage from '@/pages/NotFoundPage.vue'
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
      visibleTo: [],
      redirectTo: {
        idir: 'internal',
        bceidbusiness: 'form',
        bcsc: 'form'
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
      visibleTo: ['bceidbusiness', 'bcsc'],
      redirectTo: {
        idir: 'internal'
      }
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
      visibleTo: ['bceidbusiness', 'bcsc'],
      redirectTo: {
        idir: 'internal'
      }
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
      visibleTo: ['idir'],
      redirectTo: {
        bceidbusiness: 'form',
        bcsc: 'form'
      }
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
      showLoggedIn: false,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc'],
      redirectTo: {
        idir: 'internal',
        bceidbusiness: 'form',
        bcsc: 'form'
      }
    }
  },
  {
    path: '/error',
    name: 'error',
    component: ErrorPage,
    props: true,
    meta: {
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: true,
      visibleTo: ['idir', 'bceidbusiness', 'bcsc']
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundPage,
    props: true,
    meta: {
      hideHeader: true,
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
  const user = ForestClientUserSession.loadDetails()

  // Page requires auth
  if (to.meta.requireAuth) {
    // User is logged in
    if (user) {
      // If user can see this page, continue, otherwise go to specific page or error
      to.meta.visibleTo.includes(user.provider)
        ? next()
        : next({ name: to.meta.redirectTo?.[user.provider] || 'error' })
    } else {
      // User is not logged in, redirect to home for login
      next({ name: 'home' })
    }
    // Page does not require auth
  } else {
    if ((user && !to.meta.showLoggedIn)) {
      next({ name: to.meta.redirectTo?.[user?.provider || 'error'] ?? 'error' })
      next()
    }
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
