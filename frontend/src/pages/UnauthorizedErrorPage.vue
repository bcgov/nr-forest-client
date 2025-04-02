<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/notification/index";
</script>

<template>
  <div id="screen" class="no-menu-content">
    <cds-actionable-notification
      v-shadow="true"
      low-contrast="true"
      hide-close-button="true"
      open="true"
      kind="error"
      title="You are not authorized to access this page"
    >
      <div>
        Please create an "Application Access"
        <a target="_blank" href="https://apps.nrs.gov.bc.ca/int/jira/servicedesk/customer/portals">
          request ticket</a
        >
        to gain access to this system.
      </div>
    </cds-actionable-notification>
  </div>
</template>
