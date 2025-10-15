/**
 * Router configuration for the application.
 */
import { createRouter, createWebHistory, type RouteMeta } from "vue-router";
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
import SearchPage from "@/pages/SearchPage.vue";
import ClientDetailsPage from "@/pages/ClientDetailsPage.vue";
import UnauthorizedErrorPage from "@/pages/UnauthorizedErrorPage.vue";

import ForestClientUserSession from "@/helpers/ForestClientUserSession";

import { featureFlags } from "@/CoreConstants";

const targetPathStorage = useLocalStorage("targetPath", "");
const userProviderInfo = useLocalStorage("userProviderInfo", "");

export const routes = [
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
        idir: "search",
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
        idir: "search",
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
        idir: "search",
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
    name: "confirmation",
    component: FormSubmittedPage,
    props: true,
    meta: {
      format: "full-centered",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["bceidbusiness", "bcsc"],
      redirectTo: {
        idir: "search",
      },
      style: "content",
      headersStyle: "headers",
      sideMenu: false,
      profile: false,
    },
  },
  {
    path: "/unauthorized",
    name: "unauthorized-idir-role",
    component: UnauthorizedErrorPage,
    props: true,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: {
        idirRoles: true, // any idir user, even if they have no client-related role.
      },
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
      },
      style: "content-stretched",
      headersStyle: "headers-compact",
      sideMenu: false,
      profile: true,
    },
  },
  {
    path: "/client-created",
    name: "staff-confirmation",
    component: FormStaffConfirmationPage,
    props: route => ({ 
      clientNumber: history.state.clientNumber, 
      clientEmail: history.state.clientEmail,
      notifyClientInd: history.state.notifyClientInd,
    }),
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
      sideMenu: true,
      profile: true,
    },
  },
  {
    path: "/client-submitted/:submissionId",
    name: "staff-processing",
    component: () => import("@/pages/FormStaffProcessingPage.vue"),
    props: (route) => ({
      submissionId: route.params.submissionId,
      clientEmail: history.state.clientEmail,
      notifyClientInd: history.state.notifyClientInd,
    }),
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
      sideMenu: true,
      profile: true,
    },
  },
  {
    path: "/submissions",
    name: "submissions-list",
    component: SubmissionList,
    props: true,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: ["idir"],
      redirectTo: {
        idir: "search",
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
    path: "/search",
    name: "search",
    component: SearchPage,
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
    path: "/clients/details/:id",
    name: "client-details",
    component: ClientDetailsPage,
    meta: {
      format: "full",
      hideHeader: false,
      requireAuth: true,
      showLoggedIn: true,
      visibleTo: {
        idirRoles: ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"],
      },
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
        idir: "search",
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
      visibleTo: {
        idirRoles: ["CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"],
      },
      redirectTo: {
        bceidbusiness: "form",
        bcsc: "form",
        idir: "search",
      },
      style: "content-stretched",
      headersStyle: "headers-compact",
      sideMenu: true,
      profile: true
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
        idir: "search",
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

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to) => {
    if (to.hash) {
      return {
        el: to.hash, // scroll to the element with id equal to the hash
      };
    }
    return { top: 0 };
  },
});

// If the route requires the idir provider, it automatically requires one of the following roles by default.
const clientRoles = ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"];

const isStaffRoute = (meta: RouteMeta) => {
  return !Array.isArray(meta.visibleTo) || meta.visibleTo.includes("idir");
};

const isClientUser = (authorities: string[]) => {
  return clientRoles.some((role) => authorities.includes(role));
};

const isUserRoleAllowed = (authorities: string[], meta: RouteMeta) => {
  if (!isStaffRoute(meta)) {
    // Route is not allowed for any kind of idir users.
    return false;
  }

  let allowedRoles = clientRoles;
  if (!Array.isArray(meta.visibleTo)) {
    if (typeof meta.visibleTo.idirRoles === "boolean") {
      return meta.visibleTo.idirRoles;
    }
    allowedRoles = meta.visibleTo.idirRoles;
  }

  return allowedRoles.some((role) => authorities.includes(role));
};

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

        let isVisibleToUser = false;
        let isClientUserWithInsufficientRole = false;

        if (user.provider === "idir" && isStaffRoute(to.meta)) {
          isVisibleToUser = isUserRoleAllowed(authorities, to.meta);

          if (!isVisibleToUser && isClientUser(authorities)) {
            isClientUserWithInsufficientRole = true;
          }
        }

        if (user.provider !== "idir" && Array.isArray(to.meta.visibleTo)) {
          isVisibleToUser = to.meta.visibleTo.includes(user.provider);
        }

        // If user can see this page, continue, otherwise go to specific page or error
        // We also check if the page requires a feature flag to be visible
        // If user is not allowed but has any other client role, continue to the same route.
        if (isVisibleToUser || isClientUserWithInsufficientRole) {
          // set flag to replace the content defined in the route with the unauthorized view
          to.meta.showUnauthorized = !isVisibleToUser;

          // If there is a target path, redirect to it and clear the storage
          if (targetPathStorage.value) {
            next({ path: targetPathStorage.value });
            targetPathStorage.value = "";
          } else {
            // Otherwise, continue to the page
            next();
          }
        } else {
          /*
          If user provider is idir and user has no client role, redirects to the unauthorized route.
          Otherwise redirects to "error".
          */
          const defaultErrorPage = user.provider === "idir" ? "unauthorized-idir-role" : "error";

          // If user is not allowed to see this page, redirect to specific page or error
          next({ name: to.meta.redirectTo?.[user.provider] || defaultErrorPage });
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

declare module "vue-router" {
  interface IdirRoles {
    idirRoles: Array<string> | boolean;
  }

  // eslint-disable-next-line no-unused-vars
  interface RouteMeta {
    format: string; // Main body style class
    hideHeader: boolean; // Show/Hide the header
    requireAuth: boolean; // Force user to be logged in to see this page
    showLoggedIn: boolean; // Show/Hide the page for a logged user
    visibleTo: Array<string> | IdirRoles; // Which user types/providers can see this page
    redirectTo?: Record<string, string>; // Where to redirect the user if they are not allowed to see this page
    style: string; // Main body style class
    headersStyle: string; // Header style class
    sideMenu: boolean; // Show/Hide the side menu
    profile: boolean; // Show/Hide the profile menu
    featureFlagged?: string; // Name of the feature flag (if any) that controls access to this page
    showUnauthorized?: boolean; // Output value that tells wether the route content should be replaced with the unauthorized view
  }
}
