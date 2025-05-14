<script setup lang="ts">
import type { HistoryLogResult } from '@/dto/CommonTypesDto';
import { useFetchTo } from '@/composables/useFetch';
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getLabelByColumnName, getTagColorByClientStatus } from "@/services/ForestClientService";
import Avatar16 from "@carbon/icons-vue/es/user--avatar/16";
import Location16 from "@carbon/icons-vue/es/location/16";
import Document16 from "@carbon/icons-vue/es/document/16";
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
import ChevronUp16 from "@carbon/icons-vue/es/chevron--up/16";
import ChevronDown16 from "@carbon/icons-vue/es/chevron--down/16";
import Enterprise16 from "@carbon/icons-vue/es/enterprise/16";
import TaskAdd16 from '@carbon/icons-vue/es/task--add/16';


const router = useRouter();
const clientNumber = router.currentRoute.value.params.id as string;

const historyLogs = ref<HistoryLogResult[]>([]);
const { loading } = useFetchTo(`/api/clients/history-logs/${clientNumber}`, historyLogs);

const datetimeFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
    hour12: false,
});

const formatDatetime = (timestamp: Date): string => {
  return datetimeFormatter.format(new Date(timestamp));
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

const getSplitDetails = (details) => {
  const addressGroup = [];
  const nonGrouped = [];

  for (const item of details) {
    const label = getLabelByColumnName(item.columnName);
    if (label === 'Address') {
      addressGroup.push(item);
    } else {
      nonGrouped.push({ ...item, label });
    }
  }

  return { addressGroup, nonGrouped };
};

const shouldPrintAddressLabel = (log, allLogs, idx) => {
  const isAddress = getLabelByColumnName(log.columnName) === 'Address';
  if (!isAddress) return false;

  for (let i = 0; i < idx; i++) {
    if (getLabelByColumnName(allLogs[i].columnName) === 'Address') {
      return false;
    }
  }
  return true;
}
</script>

<template>
  <div class="card-02">
    <!-- TODO: Remove this when implementing the skeleton -->
    <cds-inline-loading v-if="loading" />

    <div
      v-if="!loading"
      v-for="(historyLog, index) in historyLogs"
      style="border-left: 0.1rem solid #dfdfe1; padding-left: 1rem; padding-bottom: 2rem;"
    >
      <table style="width: 100%; table-layout: fixed;"
        v-if="!loading">
        <colgroup>
          <col style="width: 1.5rem;" />
          <col />
        </colgroup>
        <tbody>
          <tr>
            <td style="vertical-align: middle;">
              <span v-if="historyLog.tableName === 'ClientLocation'"><Location16 /></span>
              <span v-if="historyLog.tableName === 'ClientContact'"><Avatar16 /></span>
              <span v-if="historyLog.tableName === 'RelatedClient'"><NetworkEnterprise16 /></span>
              <span v-if="historyLog.tableName === 'ClientDoingBusinessAs'"><Enterprise16 /></span>
              <span v-if="historyLog.tableName === 'ClientInformation' 
                          && historyLog.identifierLabel !== 'Client created'"><Document16 />
              </span>
              <span v-if="historyLog.tableName === 'ClientInformation' 
                          && historyLog.identifierLabel === 'Client created'"><TaskAdd16 /></span>
            </td>
            <td style="vertical-align: bottom;">
              <h5>{{ historyLog.identifierLabel }}</h5>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="label-02">{{ formatDatetime(historyLog.updateTimestamp) }}</td>
          </tr>
          <tr>
            <td>  
              <div class="hide-show-button">
                <cds-button 
                  kind="ghost" 
                  @click="toggleDetails(index)">
                  <span style="display: inline-flex; align-items: center; gap: 0.25rem; padding: 0 0.5rem;">
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
                  <span v-if="historyDtlsLog.oldValue"><del>{{ historyDtlsLog.oldValue }}</del>&nbsp;</span>

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
              </div>
            </td>
          </tr>

<!--           <tr v-if="showDetails[index]" :id="'logDetails' + index">
            <td></td>
            <td>
              <div class="card-03">
                <div v-for="(log, idx) in historyLog.details" 
                    :key="idx"
                    :class="getLabelByColumnName(log.columnName) === 'Address' ? 'grouped-logs' : 'non-grouped-log'">
                  <p
                    class="label-02"
                    v-if="shouldPrintAddressLabel(log, historyLog.details, idx)">
                    Address
                  </p>
                  <p
                    class="label-02"
                    v-else-if="getLabelByColumnName(log.columnName) !== 'Address'">
                    {{ getLabelByColumnName(log.columnName) }}
                  </p>
                  <p>
                    <span v-if="log.oldValue"><del>{{ log.oldValue }}</del>&nbsp;</span>
                    <span
                      v-if="log.columnName === 'clientStatusDesc' ||
                              log.columnName === 'locnExpiredInd'">
                      <cds-tag :type="getTagColorByClientStatus(log.newValue)">
                        <span>{{ log.newValue }}</span>
                      </cds-tag>
                    </span>
                    <span v-else>{{ log.newValue }}</span>
                  </p>
                </div>
              </div>
            </td>
          </tr> -->

        </tbody>
      </table>
    </div>
  </div>
</template>
