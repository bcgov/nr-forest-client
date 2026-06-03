<script setup lang="ts">
import { ref, watchEffect, getCurrentInstance, computed } from "vue";

// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/ui-shell/index";
import type { CDSHeaderPanel } from "@carbon/web-components";
import type CDSHeaderGlobalAction from "@carbon/web-components/es/components/ui-shell/header-global-action";

// Composables
import { isSmallScreen, isMediumScreen } from "@/composables/useScreenSize";
import { useRoute } from "vue-router";
// Types
import { nodeEnv, appVersion, featureFlags } from "@/CoreConstants";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// @ts-ignore
import Logout16 from "@carbon/icons-vue/es/logout/16";
// @ts-ignore
import Logout20 from "@carbon/icons-vue/es/logout/20";
// @ts-ignore
import Help16 from "@carbon/icons-vue/es/help/16";
// @ts-ignore
import Help20 from "@carbon/icons-vue/es/help/20";
// @ts-ignore
import Avatar16 from "@carbon/icons-vue/es/user--avatar/24";
// @ts-ignore
import Result16 from "@carbon/icons-vue/es/result/16";
// @ts-ignore
import Close16 from "@carbon/icons-vue/es/close/16";
// @ts-ignore
import TaskAdd16 from "@carbon/icons-vue/es/task--add/16";
// @ts-ignore
import Search16 from "@carbon/icons-vue/es/search--locate/16"
import { formatDate } from "@/services/ForestClientService";

const envPrefix = "openshift-";
const env = ref(nodeEnv);
env.value = env.value.slice(envPrefix.length);
env.value = env.value.charAt(0).toUpperCase() + env.value.slice(1);

const helpModalActive = ref(false);
const logoutModalActive = ref(false);
const myProfilePanelId = "my-profile-panel";

const myProfileAction = ref<InstanceType<typeof CDSHeaderGlobalAction> | null>(
  null
);
const closePanel = () => {
  if (myProfileAction.value) {
    myProfileAction.value.click();
  }
};

const myProfilePanel = ref<InstanceType<typeof CDSHeaderPanel> | null>(null);
const myProfileBackdrop = ref<HTMLDivElement | null>(null);

const observer = new MutationObserver((mutationList) => {
  if (!myProfilePanel.value || !myProfileBackdrop.value) {
    return;
  }
  const overlayActiveClassName = "overlay-active";
  for (const mutation of mutationList) {
    if (mutation.attributeName === "expanded") {
      if (myProfilePanel.value.expanded) {
        myProfileBackdrop.value.classList.add(overlayActiveClassName);
      } else {
        myProfileBackdrop.value.classList.remove(overlayActiveClassName);
      }
    }
  }
});

watchEffect((onCleanup) => {
  if (myProfilePanel.value) {
    const options = {
      attributes: true,
    };
    observer.observe(myProfilePanel.value, options);
    onCleanup(() => {
      observer.disconnect();
    });
  }
});

const instance = getCurrentInstance();
const session = instance?.appContext.config.globalProperties.$session;

const logout = () => {
  session?.logOut();
};

const route = useRoute();

const onClickLogout = () => {
  if (route.name.includes("confirmation")) {
    logout();
  } else {
    logoutModalActive.value = true;
  }
};

const headerBarButtonsSize = computed(() =>
  isSmallScreen.value || isMediumScreen.value ? "lg" : "sm"
);

const logoutBtnKind = computed(() =>
  isSmallScreen.value || isMediumScreen.value ? "ghost" : "tertiary"
);

const userHasAuthority = ["CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"].some((authority) =>
  ForestClientUserSession.authorities.includes(authority)
);

const handleLogoutClick = (event) => {
  event.preventDefault();

  if (route.name === "staff-form") {
    logoutModalActive.value = true;
  } else {
    logout();
  }
};

const currentDate = new Date();
</script>

<template>
  <cds-header aria-label="Forests Client Management System">

    <cds-header-menu-button
      v-if="$session?.user?.provider === 'idir'"
      button-label-active="Close menu"
      button-label-inactive="Open menu"
      v-shadow="2"
    >
    </cds-header-menu-button>

    <a href="https://gov.bc.ca" v-if="$session.user?.provider !== 'idir'" class="bclogotop">
      <img
        src="/img/logo-vertical1.svg"
        alt="Go to the Government of British Columbia website"
        v-if="isSmallScreen"
      />
      <img
        src="/img/BCID_H_rgb_rev.svg"
        alt="Go to the Government of British Columbia website"
        v-else
      />      
    </a>
    
    <cds-header-name @click.prevent>
      <span class="heading-compact-02" v-if="$session?.user?.provider !== 'idir'">Ministry of Forests</span>
      <span class="heading-compact-02" v-else>Forests Client Management System</span>
      <span class="heading-compact-02" v-if="env !== 'Prod' && !isSmallScreen">Env. {{ env }} - Rel. {{appVersion}}</span>
      <span id="header-date" class="heading-compact-02 print-only">{{
        formatDate(currentDate)
      }}</span>
    </cds-header-name>
    
    <div class="heading-buttons">
    <cds-button
    v-if="!$route.meta.profile"
      id="help-btn"
      data-id="help-btn"
      kind="ghost"
      :size="headerBarButtonsSize"
      @click.prevent="helpModalActive = true"
    >
      <template v-if="!isSmallScreen && !isMediumScreen">
        <span>Help with application</span>
        <Help16 slot="icon" />
      </template>
      <template v-else>
        <Help20 slot="icon" />
      </template>
    </cds-button>

    <cds-button    
      v-if="$session?.isLoggedIn() && !$route.meta.profile"
      data-id="logout-btn"
      :kind="logoutBtnKind"
      :size="headerBarButtonsSize"
      @click.prevent="onClickLogout"
    >
      <template v-if="!isSmallScreen && !isMediumScreen">
        <span>Logout</span>
        <Logout16 slot="icon" />
      </template>
      <template v-else>
        <Logout20 slot="icon" />
      </template>
    </cds-button>
    </div>

    <cds-header-global-action
      data-testid="panel-action"
      id="my-profile-action"
      :panel-id="myProfilePanelId"
      ref="myProfileAction"
      v-if="$route.meta.profile"
    >
      <Avatar16 slot="icon"/>
    </cds-header-global-action>

    <cds-header-panel :id="myProfilePanelId" v-if="$route.meta.profile" ref="myProfilePanel">
      <div class="grouping-16" id="panel-title">
        <h3>My profile</h3>
        <cds-button kind="ghost" size="sm" @click.prevent="closePanel" class="close-panel-button">
          <Close16 slot="icon" />
        </cds-button>
      </div>
      <div class="grouping-17" id="panel-content">
        <div class="grouping-18" id="panel-content--user">
          <div class="grouping-19" id="panel-content--user-avatar-desc">
            <user-profile-component 
              :name="$session.user?.name || ''" 
              :email="$session.user?.email || ''"
              :identifier="$session.user?.userId || ''"
              :provider="$session.user?.provider || ''"
              :authorities="$session.authorities"
            />
          </div>
        </div>
        <hr class="divider" />
        <div class="grouping-21" id="panel-content--links">
          <cds-side-nav-items>
            <cds-side-nav-link title="Options" class="unbolded side-nav-link--non-link" />
            <cds-side-nav-link href="#" title="Logout" @click.prevent="handleLogoutClick">            
              <Logout16 slot="title-icon" />
            </cds-side-nav-link>
          </cds-side-nav-items>
        </div>
      </div>
      
    </cds-header-panel>
    <div
      data-testid="my-profile-backdrop"
      ref="myProfileBackdrop"
      class="cds--side-nav__overlay"
      @click.prevent="myProfileAction?.click"
    ></div>
  </cds-header>

  <cds-side-nav 
    v-if="$route.meta.sideMenu" 
    v-shadow=1>
    <cds-side-nav-items v-shadow=1>
      <cds-side-nav-link
        :active="$route.name == 'search'"
        href="/search"
        large
        id="menu-list-search"
      >
        <span>Client search</span>
        <Search16 slot="title-icon" />
      </cds-side-nav-link>

      <cds-side-nav-link
        :active="$route.name == 'staff-form'"
        href="/new-client-staff"
        large
        v-if="userHasAuthority"
        v-shadow=1
        id="menu-list-staff-form"
      >
        <span>Create client</span>
        <TaskAdd16 slot="title-icon" />
      </cds-side-nav-link>

      <cds-side-nav-link
        :active="$route.name == 'submissions-list'"
        href="/submissions"
        large
        id="menu-list-table-list"
      >
        <span>Submissions</span>
        <Result16 slot="title-icon" />
      </cds-side-nav-link>
    </cds-side-nav-items>

    <cds-side-nav-items v-shadow=1 class="lower-side-nav"> 
      <cds-side-nav-link href="#" class="unbolded">
        <span>Support</span>
      </cds-side-nav-link>
      <cds-side-nav-link 
        target="_blank"
        href="https://intranet.gov.bc.ca/intranet/content?id=C7F590C387614C4D835E447A21D854BA" 
        large 
        class="unbolded"
      >
        <span class="body-compact-02">Need help?</span>
        <Help16 slot="title-icon" />
      </cds-side-nav-link>
    </cds-side-nav-items>
  
  </cds-side-nav>

  <cds-modal
    id="logout-modal"
    aria-labelledby="logout-modal-heading"
    aria-describedby="logout-modal-body"
    size="sm"
    :open="logoutModalActive"
    @cds-modal-closed="logoutModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="logout-modal-heading">
        Are you sure you want to logout? Your data will not be saved.
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body id="logout-modal-body"><p></p></cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button 
        kind="secondary" 
        data-modal-close class="cds--modal-close-btn">
        Cancel
      </cds-modal-footer-button>
      <cds-modal-footer-button 
        kind="danger" 
        class="cds--modal-submit-btn" 
        v-on:click="logout">
        Logout
        <Logout16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>

</template>
