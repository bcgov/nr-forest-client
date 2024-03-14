<script setup lang="ts">
import { ref, watchEffect, getCurrentInstance } from "vue";
import { openMailtoLink, getObfuscatedEmail } from "@/services/ForestClientService";

// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/ui-shell/index";
import type { CDSHeaderPanel } from "@carbon/web-components";
import type CDSHeaderGlobalAction from "@carbon/web-components/es/components/ui-shell/header-global-action";

// Composables
import { isSmallScreen, isMediumScreen } from "@/composables/useScreenSize";
import { useRoute } from "vue-router";
// Types
import { nodeEnv, appVersion } from "@/CoreConstants";
// Routes
import { CONFIRMATION_ROUTE_NAME } from "@/routes";
// @ts-ignore
import Logout16 from "@carbon/icons-vue/es/logout/16";
// @ts-ignore
import Help16 from "@carbon/icons-vue/es/help/16";
// @ts-ignore
import Avatar16 from "@carbon/icons-vue/es/user--avatar/24";
// @ts-ignore
import Result16 from "@carbon/icons-vue/es/result/16";
// @ts-ignore
import SignOut16 from "@carbon/icons-vue/es/user--follow/16";
// @ts-ignore
import Close16 from "@carbon/icons-vue/es/close/16";

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
}

const route = useRoute();

const onClickLogout = () => {
  if (route.name === CONFIRMATION_ROUTE_NAME) {
    logout();
  } else {
    logoutModalActive.value = true;
  }
}

const adminEmail = "forhvap.cliadmin@gov.bc.ca"; 
</script>

<template>

  <cds-header aria-label="Forest Client Management System">

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
      <logo />
    </a>
    
    <cds-header-name @click.prevent>
      <span class="heading-compact-02" v-if="$session?.user?.provider !== 'idir'">Ministry of Forests</span>
      <span class="heading-compact-02" v-else>Forest Client Management System</span>
      <span class="heading-compact-02" v-if="env !== 'Prod' && !isSmallScreen">Env. {{ env }} - Rel. {{appVersion}}</span>
    </cds-header-name>
    
    <div class="heading-buttons">
    <cds-button
    v-if="!$route.meta.profile"
      id="help-btn"
      data-id="help-btn"
      kind="ghost"
      size="sm"
      @click.prevent="helpModalActive = true"
    >
      <span v-if="!isSmallScreen && !isMediumScreen">Help with application</span>
      <Help16 slot="icon" />
    </cds-button>

    <cds-button    
      v-if="$session?.isLoggedIn() && !$route.meta.profile"
      data-id="logout-btn"
      kind="tertiary"
      size="sm"
      @click.prevent="onClickLogout"
    >
      <span v-if="!isSmallScreen && !isMediumScreen">Logout</span>
      <Logout16 slot="icon" />
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
              
              />
          </div>
        </div>
        <hr class="divider" />
        <div class="grouping-21" id="panel-content--links">
          <cds-side-nav-items>
            <cds-side-nav-link title="Options" class="unbolded side-nav-link--non-link" />
            <cds-side-nav-link href="#" title="Sign Out" @click.prevent="logoutModalActive = true">            
              <SignOut16 slot="title-icon" />
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

  <cds-side-nav v-if="$route.meta.sideMenu" v-shadow=1>
    <cds-side-nav-items v-shadow=1>      
      <cds-side-nav-link active href="/submissions" large>
        <span>Submissions</span>
        <Result16 slot="title-icon" />
      </cds-side-nav-link>
    </cds-side-nav-items>

    <cds-side-nav-items v-shadow=1 class="lower-side-nav"> 
      <cds-side-nav-link href="#" class="unbolded">
        <span>Support</span>
      </cds-side-nav-link>
      <cds-side-nav-link 
        href="#" 
        large 
        class="unbolded" 
        @click.prevent="helpModalActive = true"
      >
        <span class="body-compact-02 disabled">Need help?</span>
        <Help16 slot="title-icon" class="disabled" />
      </cds-side-nav-link>
    </cds-side-nav-items>
  
  </cds-side-nav>

  <cds-modal
    id="help-modal"
    size="sm"
    :open="helpModalActive"
    @cds-modal-closed="helpModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading>
        Help with application
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body>
      <p>
        Can’t proceed with your application? Let us know by emailing your issue to 
        <button class="link-button" @click="openMailtoLink(adminEmail)" aria-label="Contact Admin via Email">
          <span v-bind:innerHTML="getObfuscatedEmail(adminEmail)"></span>
        </button>
      </p>
    </cds-modal-body>
  </cds-modal>

  <cds-modal
    id="logout-modal"
    size="sm"
    :open="logoutModalActive"
    @cds-modal-closed="logoutModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading>
        Are you sure you want to logout? Your data will not be saved.
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body><p></p></cds-modal-body>

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
