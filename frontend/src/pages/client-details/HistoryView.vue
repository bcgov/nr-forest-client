<script setup lang="ts">
import type { HistoryLogResult } from '@/dto/CommonTypesDto';
import { useFetchTo } from '@/composables/useFetch';
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
        getLabelByColumnName, 
        getTagColorByClientStatus, 
        fieldActionMap, 
        removePrefix 
      } from "@/services/ForestClientService";
import User16 from "@carbon/icons-vue/es/user/16";
import Location16 from "@carbon/icons-vue/es/location/16";
import Document16 from "@carbon/icons-vue/es/document/16";
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
import ChevronUp16 from "@carbon/icons-vue/es/chevron--up/16";
import ChevronDown16 from "@carbon/icons-vue/es/chevron--down/16";
import Enterprise16 from "@carbon/icons-vue/es/enterprise/16";
import TaskAdd16 from '@carbon/icons-vue/es/task--add/16';
import UserAvatar20 from '@carbon/icons-vue/es/user--avatar/20';


const router = useRouter();
const clientNumber = router.currentRoute.value.params.id as string;

const historyLogs = ref<HistoryLogResult[]>([]);
const { loading } = useFetchTo(`/api/clients/history-logs/${clientNumber}`, historyLogs);

const dateFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
    hour12: false,
});

const formatDate = (timestamp: Date): string => {
  return dateFormatter.format(new Date(timestamp));
};

const formatDatetime = (timestamp: Date | string): string => {
  const date = new Date(timestamp);

  const options: Intl.DateTimeFormatOptions = {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
  };

  const datePart = date.toLocaleDateString('en-US', options);
  const timePart = date.toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });

  return `${datePart}  ${timePart}`;
};

const showDetails = ref({});
const toggleDetails = (index) => {
  showDetails.value[index] = !showDetails.value[index];
};

watch(
  () => historyLogs.value,
  (newLogs) => {
    newLogs.forEach((_, index) => {
      showDetails.value[index] = true;
    });
  },
  { immediate: true }
);

const fieldReasonPriorityMap: { [actionCode: string]: string[] } = {
  'ADDR': [
    'addressOne',
    'addressTwo',
    'addressThree',
    'city',
    'provinceCode',
    'countryCode',
    'postalCode'
  ],
};

const getReasonForColumn = (
  historyLog: any,
  columnName: string
): string | null => {
  const possibleActionCodes = fieldActionMap[columnName];
  if (!possibleActionCodes || !historyLog.reasons || historyLog.reasons.length === 0) {
    return null;
  }

  for (const actionCode of possibleActionCodes) {
    const reasonObj = historyLog.reasons.find((r: any) => r.actionCode === actionCode);
    if (reasonObj) {
      const priorityList = fieldReasonPriorityMap[actionCode];

      // If this is a grouped field like ADDR, apply filtering logic
      if (priorityList) {
        // Find the last column (from priority list) that appears in the log details
        const lastMatchingColumn = [...priorityList]
          .reverse()
          .find((col) =>
            historyLog.details.some((d: any) => d.columnName === col)
          );

        if (columnName === lastMatchingColumn) {
          return reasonObj.reason;
        } else {
          return null;
        }
      }

      return reasonObj.reason;
    }
  }

  return null;
};
</script>

<template>
  <div class="card-02">

    <div class="history-indicator-line" v-if="loading">
      <div class="skeleton-group">
        <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
        <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
        <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
        <div></div>
      </div>
    </div>

    <div
      v-if="!loading"
      v-for="(historyLog, index) in historyLogs"
      class="history-indicator-line"
    >
      <table class="full-width-fixed-table">
        <colgroup>
          <col style="width: 1.5rem;" />
          <col />
        </colgroup>
        <tbody>
          <tr style="text-align: left;">
            <th style="vertical-align: middle;">
              <span v-if="historyLog.tableName === 'ClientLocation'"><Location16 /></span>
              <span v-if="historyLog.tableName === 'ClientContact'"><User16 /></span>
              <span v-if="historyLog.tableName === 'RelatedClient'"><NetworkEnterprise16 /></span>
              <span v-if="historyLog.tableName === 'ClientDoingBusinessAs'"><Enterprise16 /></span>
              <span v-if="historyLog.tableName === 'ClientInformation' 
                          && historyLog.identifierLabel !== 'Client created'"><Document16 />
              </span>
              <span v-if="historyLog.tableName === 'ClientInformation' 
                          && historyLog.identifierLabel === 'Client created'"><TaskAdd16 /></span>
            </th>
            <th style="vertical-align: bottom;">
              <h5>{{ historyLog.identifierLabel }}</h5>
            </th>
          </tr>
          <tr>
            <td></td>
            <td class="label-02">{{ formatDate(historyLog.updateTimestamp) }}</td>
          </tr>
          <tr>
            <td>  
              <div class="hide-show-button">
                <cds-button 
                  kind="ghost" 
                  @click="toggleDetails(index)">
                  <span class="icon-label-inline">
                    {{ showDetails[index] ? 'Hide' : 'Show more' }}
                    <ChevronUp16 v-if="showDetails[index]" />
                    <ChevronDown16 v-if="!showDetails[index]" />
                  </span>
                </cds-button>
              </div>
            </td> 
            <td></td>
          </tr>

          <tr v-if="showDetails[index]" :id="'logDetails' + index">
            <td></td>
            <td class="card-03">
              <div v-for="(historyDtlsLog, index) in historyLog.details">
                <p class="label-02" v-if="getLabelByColumnName(historyDtlsLog.columnName)">
                  {{ getLabelByColumnName(historyDtlsLog.columnName) }}
                </p>
                <p>
                  <span v-if="historyDtlsLog.oldValue">
                    <del class="helper-text">{{ historyDtlsLog.oldValue }}</del>&nbsp;
                  </span>

                  <span v-if="historyDtlsLog.columnName === 'clientStatusDesc' ||
                              historyDtlsLog.columnName === 'locnExpiredInd'">
                    <cds-tag :type="getTagColorByClientStatus(historyDtlsLog.newValue)">
                      <span>{{ historyDtlsLog.newValue }}</span>
                    </cds-tag>
                  </span>

                  <span v-if="historyDtlsLog.columnName !== 'clientStatusDesc' &&
                              historyDtlsLog.columnName !== 'locnExpiredInd'">
                      {{ historyDtlsLog.newValue }}
                  </span>
                </p>

                <div v-if="getReasonForColumn(historyLog, historyDtlsLog.columnName)"
                    class="history-meta-section">
                  <p class="label-02">
                    Reason for change
                  </p>
                  <p>
                    {{ getReasonForColumn(historyLog, historyDtlsLog.columnName) }}
                  </p>
                </div>
              </div>

              <div>
                <p class="label-02">
                  Performed by
                </p>
                <p class="icon-label-inline">
                  <UserAvatar20 /> 
                  {{ removePrefix(historyLog.updateUserid) }} &middot; 
                  {{ formatDatetime(historyLog.updateTimestamp) }}
                </p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
