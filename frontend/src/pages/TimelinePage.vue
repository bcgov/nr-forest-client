<script setup lang="ts">
import { ref } from "vue";
import type { AuditLogResult } from "@/dto/CommonTypesDto";
import { useFetchTo } from "@/composables/useFetch";

// Carbon components
import "@carbon/web-components/es/components/data-table/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/pagination/index";
import "@carbon/web-components/es/components/select/index";
import "@carbon/web-components/es/components/tag/index";

// Carbon icons
import Avatar16 from "@carbon/icons-vue/es/user--avatar/16";
import Location20 from "@carbon/icons-vue/es/location/20";

const auditLogs = ref<AuditLogResult[]>();

useFetchTo(`/api/clients/audit-log/00149081`, auditLogs);

// Function to format the date properly
const formatAuditLogDate = (timestamp: number): string => {
  const figmaFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
  });
  return figmaFormatter.format(new Date(timestamp));
};
</script>

<template>
  <div id="screen" class="client-details-screen">
    <div class="audit-log-content">
      <div
        v-for="(auditLog, index) in auditLogs"
        :key="auditLog.idx"
        class="grouping-07"
        style="border-left: 2px solid #B0B0B0;"
      >
        <h4 class="review-icon-title">
          <span v-if="auditLog.tableName === 'CLI_LOCN_AUDIT'">
            <Location20 />
            Location {{ auditLog.idx }}
          </span>
          <span v-if="auditLog.changeType === 'UPDATE'">updated</span>
          <span v-if="auditLog.changeType === 'DELETE'">deleted</span>
          <span v-if="auditLog.changeType === 'INSERT'">inserted</span>
        </h4>

        <div class="grouping-23">
          <span class="body-compact-01">
            <p class="label-02" v-if="auditLog.columnName === 'ADDRESS_1'">Street address or PO box:</p>
            <p class="label-02" v-if="auditLog.columnName === 'ADDRESS_2'">Delivery information:</p>
            <p class="label-02" v-if="auditLog.columnName === 'ADDRESS_3'">Additional delivery information:</p>
            <p class="label-02" v-if="auditLog.columnName === 'LOCN_EXPIRED_IND'">Location expired:</p>
            <p class="label-02" v-if="auditLog.columnName === 'CLIENT_LOCN_NAME'">Location name:</p>
            <p class="label-02" v-if="auditLog.columnName === 'CITY'">City:</p>
            <p class="label-02" v-if="auditLog.columnName === 'PROVINCE'">Province/State:</p>
            <p class="label-02" v-if="auditLog.columnName === 'COUNTRY'">Country:</p>
            <p class="label-02" v-if="auditLog.columnName === 'POSTAL_CODE'">Postal Code:</p>
            <p><del>{{ auditLog.oldValue }}</del> {{ auditLog.newValue }}</p>
            <br>
            <p><Avatar16 /> {{ auditLog.updateUserid }} &#8226; {{ formatAuditLogDate(auditLog.updateTimestamp) }}</p>
          </span>
        </div>

        <hr>
      </div>
    </div>
  </div>
</template>
