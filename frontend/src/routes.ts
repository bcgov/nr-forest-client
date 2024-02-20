/**
 * Router configuration for the application.
 */
import { createRouter, createWebHistory } from "vue-router";
import { useLocalStorage } from '@vueuse/core';

import SubmissionList from '@/pages/SubmissionListPage.vue'
import SubmissionReview from '@/pages/SubmissionReviewPage.vue'
import BCeIDForm from "@/pages/FormBCeIDPage.vue";
import BCSCForm from "@/pages/FormBCSCPage.vue";
import FormSubmittedPage from "@/pages/FormSubmittedPage.vue";
import UserLoadingPage from "@/pages/UserLoadingPage.vue";
import LandingPage from "@/pages/LandingPage.vue";
import ErrorPage from "@/pages/ErrorPage.vue";
import NotFoundPage from "@/pages/NotFoundPage.vue";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

import { nodeEnv } from "@/CoreConstants";

const CONFIRMATION_ROUTE_NAME = "confirmation";

const routes = [
  {
    path: "/landing",
    name: "home",
    component: LandingPage,
    props: true,
    meta: {
      format: "full",
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: false,
      visibleTo: [],
      redirectTo: {
        idir: "internal",
        bceidbusiness: "form",
        bcsc: "bcsc-form",
      },
      style: 'content-landing',
      headersStyle: 'headers',
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/new-client",
    alias: "/",
    name: "form",
    component: BCeIDForm,
    props: true,
    meta: {
      format: "screen",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["bceidbusiness"],
      redirectTo: {
        idir: "internal",
        bcsc: "bcsc-form",
      },
      style: "content",
      headersStyle: 'headers',
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/new-client-bcsc",
    alias: "/",
    name: "bcsc-form",
    component: BCSCForm,
    props: true,
    meta: {
      format: "screen",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["bcsc"],
      redirectTo: {
        idir: "internal",
        bceidbusiness: "form",
      },
      style: "content",
      headersStyle: 'headers',
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/form-submitted",
    name: CONFIRMATION_ROUTE_NAME,
    component: FormSubmittedPage,
    props: true,
    meta: {
      format: "full-centered",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["bceidbusiness", "bcsc"],
      redirectTo: {
        idir: "internal",
      },
      style: "content",
      headersStyle: 'headers',
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: '/submissions',
    name: 'internal',
    component: SubmissionList,
    props: true,
    meta: {
      format: 'full',
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ['idir'],
      redirectTo: {
        bceidbusiness: 'form',
        bcsc: 'form',
      },
      style: 'content-stretched',
      headersStyle: 'headers-compact',
      sideMenu: true,
      profile: true,
    },
  },
  {
    path: '/submissions/:id',
    name: 'review',
    component: SubmissionReview,
    props: true,
    meta: {
      format: 'full',
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ['idir'],
      redirectTo: {
        bceidbusiness: 'form',
        bcsc: 'form',
      },
      style: 'content-stretched',
      headersStyle: 'headers-compact',
      sideMenu: true,
      profile: true,
    },
  },
  {
    path: "/dashboard",
    name: "loading",
    component: UserLoadingPage,
    props: true,
    meta: {
      format: "full-centered",
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: false,
      visibleTo: ["idir", "bceidbusiness", "bcsc"],
      redirectTo: {
        idir: "internal",
        bceidbusiness: "form",
        bcsc: "bcsc-form",
      },
      style: "content",
      headersStyle: 'headers',
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/error",
    name: "error",
    component: ErrorPage,
    props: true,
    meta: {
      format: "full",
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: true,
      visibleTo: ["idir", "bceidbusiness", "bcsc"],
    },
    style: "content",
    headersStyle: 'headers',
    sideMenu: false,
    profile: false,
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: NotFoundPage,
    props: true,
    meta: {
      format: "full",
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: true,
      visibleTo: ["idir", "bceidbusiness", "bcsc"],
    },
    style: "content",
    headersStyle: 'headers',
    sideMenu: false,
    profile: false,
  },
];

if (nodeEnv === "openshift-dev") {
  const names = ["form", "confirmation"];

  routes.forEach((route) => {
    if (names.includes(route.name as string)) {
      route.meta?.visibleTo.push("idir");
    }
  });
}

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach(async (to, from, next) => {
  const user = ForestClientUserSession.loadDetails();
  const targetPathStorage = useLocalStorage('targetPath', '');
  
  if(to.query.fd_to){
    targetPathStorage.value = to.query.fd_to as string;
  }

  // Page requires auth
  if (to.meta.requireAuth) {    
    // User is logged in
    if (user) {      
      // If user can see this page, continue, otherwise go to specific page or error
      if(to.meta.visibleTo.includes(user.provider)){
        // If there is a target path, redirect to it and clear the storage
        if(targetPathStorage.value){  
          next({ path: targetPathStorage.value });
          targetPathStorage.value = '';
        }else{
          // Otherwise, continue to the page
          next();
        }
      }else{
        // If user is not allowed to see this page, redirect to specific page or error
        next({ name: to.meta.redirectTo?.[user.provider] || "error" });
      }

    } else {
      // User is not logged in, redirect to home for login
      next({ name: "home",query: { fd_to: to.path }});
    }
    // Page does not require auth
  } else {
    if (user && !to.meta.showLoggedIn) {
      // If user is logged in and the page is not for logged in users, redirect to specific page or error
      next({
        name: to.meta.redirectTo?.[user?.provider || "error"] ?? "error",
      });
    } else {
      // Otherwise, continue to the page
      next();
    }
  }
});

export { routes, router, CONFIRMATION_ROUTE_NAME };

declare module 'vue-router' {
  // eslint-disable-next-line no-unused-vars
  interface RouteMeta {
    format: string // Main body style class
    hideHeader: boolean // Show/Hide the header
    requireAuth: boolean // Force user to be logged in to see this page
    showLoggedIn: boolean // Show/Hide the page for a logged user
    visibleTo: Array<string> // Which user types/providers can see this page
    redirectTo?: Record<string, string> // Where to redirect the user if they are not allowed to see this page
    style: string // Main body style class
    headersStyle: string // Header style class
    sideMenu: boolean // Show/Hide the side menu
    profile: boolean // Show/Hide the profile menu
  }
}
