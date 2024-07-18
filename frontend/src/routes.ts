/**
 * Router configuration for the application.
 */
import { createRouter, createWebHistory } from "vue-router";
import { useLocalStorage } from "@vueuse/core";
import { Hub } from "aws-amplify/utils";

import SubmissionList from "@/pages/SubmissionListPage.vue";
import SubmissionReview from "@/pages/SubmissionReviewPage.vue";
import BCeIDForm from "@/pages/FormBCeIDPage.vue";
import BCSCForm from "@/pages/FormBCSCPage.vue";
import StaffForm from "@/pages/FormStaffPage.vue";
import FormSubmittedPage from "@/pages/FormSubmittedPage.vue";
import FormStaffConfirmationPage from "@/pages/FormStaffConfirmationPage.vue";
import UserLoadingPage from "@/pages/UserLoadingPage.vue";
import LandingPage from "@/pages/LandingPage.vue";
import ErrorPage from "@/pages/ErrorPage.vue";
import NotFoundPage from "@/pages/NotFoundPage.vue";
import LogoutPage from "@/pages/LogoutPage.vue";

import ForestClientUserSession from "@/helpers/ForestClientUserSession";

import { featureFlags } from "@/CoreConstants";

const CONFIRMATION_ROUTE_NAME = "confirmation";
const staffConfirmationRoute = "staff-confirmation";
const targetPathStorage = useLocalStorage("targetPath", "");
const userProviderInfo = useLocalStorage("userProviderInfo", "");

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
      style: "content-landing",
      headersStyle: "headers",
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
      headersStyle: "headers",
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
      headersStyle: "headers",
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
      headersStyle: "headers",
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/client-created",
    name: staffConfirmationRoute,
    component: FormStaffConfirmationPage,
    props: true,
    meta: {
      format: "full-centered",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["idir"],
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
      },
      style: "content",
      headersStyle: "headers",
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/submissions",
    name: "internal",
    component: SubmissionList,
    props: true,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["idir"],
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
      },
      style: "content-stretched",
      headersStyle: "headers-compact",
      sideMenu: true,
      profile: true,
    },
  },
  {
    path: "/new-client-staff",
    name: "staff-form",
    component: StaffForm,
    props: true,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["CLIENT_EDITOR","CLIENT_ADMIN"],
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
        idir: "internal",
      },
      style: "content-stretched",
      headersStyle: "headers-compact",
      sideMenu: true,
      profile: true,
      featureFlagged: "STAFF_CREATE",
    },
  },
  {
    path: "/submissions/:id",
    name: "review",
    component: SubmissionReview,
    props: true,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["idir"],
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
      },
      style: "content-stretched",
      headersStyle: "headers-compact",
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
      headersStyle: "headers",
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
    headersStyle: "headers",
    sideMenu: false,
    profile: false,
  },
  {
    path: "/notfound",
    name: "notfoundstatus",
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
    headersStyle: "headers",
    sideMenu: false,
    profile: false,
  },
  {
    path: "/logout",
    name: "logout",
    component: LogoutPage,
    props: true,
    meta: {
      format: "full",
      hideHeader: true,
      requireAuth: false,
      showLoggedIn: false,
      visibleTo: [],
    },
    style: "content",
    headersStyle: "headers",
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
    headersStyle: "headers",
    sideMenu: false,
    profile: false,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach(async (to, from, next) => {
  if (to.name === "not-found") {
    next({ name: "notfoundstatus" });
  } else {
    await ForestClientUserSession.loadUser();
    const user = ForestClientUserSession.loadDetails();
    const authorities = ForestClientUserSession.loadAuthorities();

    if (to.query.fd_to) {
      targetPathStorage.value = to.query.fd_to as string;
    }

    // If the page requires a feature flag and the feature flag is not enabled, redirect to error page
    if(to.meta.featureFlagged && !featureFlags[to.meta.featureFlagged]){
      next({ name: to.meta.redirectTo?.[user.provider] || "error" });
    }

    // Page requires auth
    if (to.meta.requireAuth) {
      // User is logged in
      if (user) {
        // Save user provider info for logout
        userProviderInfo.value = user.provider;

        // If user can see this page, continue, otherwise go to specific page or error
        // We also check if the page requires a feature flag to be visible
        if (to.meta.visibleTo.includes(user.provider) || to.meta.visibleTo.some(visible => authorities.includes(visible))) {
          // If there is a target path, redirect to it and clear the storage
          if (targetPathStorage.value) {
            next({ path: targetPathStorage.value });
            targetPathStorage.value = "";
          } else {
            // Otherwise, continue to the page
            next();
          }
        } else {
          // If user is not allowed to see this page, redirect to specific page or error
          next({ name: to.meta.redirectTo?.[user.provider] || "error" });
        }
      } else {
        // User is not logged in, redirect to home for login
        next({ name: "home", query: { fd_to: to.path } });
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
  }
});

Hub.listen("auth", async ({ payload }) => {
  switch (payload.event) {
    case "signInWithRedirect":
      await ForestClientUserSession.loadUser();
      break;
  }
});

export { routes, router, CONFIRMATION_ROUTE_NAME, staffConfirmationRoute };

declare module "vue-router" {
  // eslint-disable-next-line no-unused-vars
  interface RouteMeta {
    format: string; // Main body style class
    hideHeader: boolean; // Show/Hide the header
    requireAuth: boolean; // Force user to be logged in to see this page
    showLoggedIn: boolean; // Show/Hide the page for a logged user
    visibleTo: Array<string>; // Which user types/providers can see this page
    redirectTo?: Record<string, string>; // Where to redirect the user if they are not allowed to see this page
    style: string; // Main body style class
    headersStyle: string; // Header style class
    sideMenu: boolean; // Show/Hide the side menu
    profile: boolean; // Show/Hide the profile menu
    featureFlagged?: string; // Name of the feature flag (if any) that controls access to this page
  }
}
