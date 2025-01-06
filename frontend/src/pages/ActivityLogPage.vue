<script setup lang="ts">
import { ref, computed, watch } from "vue";
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

const auditLogs = ref<AuditLogResult[]>([]);
const detailsVisible = ref<{ [key: number]: boolean }>({});

useFetchTo(`/api/clients/audit-log/00149081`, auditLogs);

const datetimeFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    timeZone: "UTC",
    hour12: false,
  });

const formatAuditLogDatetime = (timestamp: number): string => {
  return datetimeFormatter.format(new Date(timestamp));
};

// Computed property to group audit logs
const groupedAuditLogs = computed(() => {
  const groups: { [key: string]: AuditLogResult[] } = {};
  auditLogs.value.forEach(log => {
    const key = `${log.idx}-${log.changeType}-${log.updateTimestamp}-${log.updateUserid}`;
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(log);
  });
  return Object.values(groups);
});

// Initialize detailsVisible dynamically when groupedAuditLogs changes
watch(groupedAuditLogs, (newGroups) => {
  newGroups.forEach((_, index) => {
    detailsVisible.value[index] = true;
  });
}, { immediate: true });

const toggleDetails = (index: number) => {
  detailsVisible.value[index] = !detailsVisible.value[index];
};
</script>

<template>
  <div id="screen" class="client-details-screen">
    <div class="audit-log-content">
      <div
        v-for="(group, index) in groupedAuditLogs"
        :key="index"
        class="grouping-07"
        style="border-left: 0.1rem solid #B0B0B0; padding-left: 1rem;"
      >
        <table>
          <tr>
            <td style="width: 15% !important;"><Location20 /></td>
            <td class="label-02" style="vertical-align: middle;">
              <h4>
                Location
                <span v-if="group[0].changeType === 'UPDATE'">updated</span>
                <span v-if="group[0].changeType === 'DELETE'">deleted</span>
                <span v-if="group[0].changeType === 'INSERT'">added</span>
              </h4>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="label-02">Location {{ group[0].idx }}</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td></td>
            <td class="label-02">
              <a @click="toggleDetails(index)">{{ detailsVisible[index] ? 'Hide' : 'Show more' }}</a>
            </td>
          </tr>
        </table>

        <div v-if="detailsVisible[index]" :id="'logDetails'+index" class="grouping-05" style="width: 80%;">
          <div class="body-compact-01">
            <div v-for="log in group">
              <p class="label-02" v-if="log.columnName === 'ADDRESS_1'">Street address or PO box:</p>
              <p class="label-02" v-if="log.columnName === 'ADDRESS_2'">Delivery information:</p>
              <p class="label-02" v-if="log.columnName === 'ADDRESS_3'">Additional delivery information:</p>
              <p class="label-02" v-if="log.columnName === 'LOCN_EXPIRED_IND'">Location expired:</p>
              <p class="label-02" v-if="log.columnName === 'CLIENT_LOCN_NAME'">Location name:</p>
              <p class="label-02" v-if="log.columnName === 'CITY'">City:</p>
              <p class="label-02" v-if="log.columnName === 'PROVINCE'">Province/State:</p>
              <p class="label-02" v-if="log.columnName === 'COUNTRY'">Country:</p>
              <p class="label-02" v-if="log.columnName === 'POSTAL_CODE'">Postal Code:</p>
              <p><del>{{ log.oldValue }}</del> {{ log.newValue }}</p>
              <br>
            </div>
            <p class="label-02">Performed by:</p>
            <p><Avatar16 /> {{ group[0].updateUserid }} &#8226; {{ formatAuditLogDatetime(group[0].updateTimestamp) }}</p>
          </div>
        </div>
        <br />
      </div>
    </div>
  </div>
</template>
