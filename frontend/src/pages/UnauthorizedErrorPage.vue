<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/notification/index";
</script>

<template>
  <div
    id="screen"
    :class="$route.name === 'unauthorized-idir-role' ? 'no-menu-content' : 'idir-content'"
  >
    <cds-actionable-notification
      v-shadow="true"
      low-contrast="true"
      hide-close-button="true"
      open="true"
      kind="error"
      title="You are not authorized to access this system or page"
    >
      <div>
        To request or change your access, complete an "<a
          target="_blank"
          href="https://apps.nrs.gov.bc.ca/int/jira/servicedesk/customer/portal/1/create/42"
          >Application Access</a
        >" ticket
      </div>
    </cds-actionable-notification>
  </div>
</template>
