<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/data-table/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/overflow-menu/index";
import "@carbon/web-components/es/components/pagination/index";
import "@carbon/web-components/es/components/select/index";
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/tag/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useFetchTo } from "@/composables/useFetch";
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
import useSvg from "@/composables/useSvg";
// Types
import type { SubmissionList } from "@/dto/CommonTypesDto";
import { formatDistanceToNow, format } from "date-fns";
import { toTitleCase } from "@/services/ForestClientService";
import { adminEmail, getObfuscatedEmailLink } from "@/services/ForestClientService";
// Session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// @ts-ignore
import Approved16 from "@carbon/icons-vue/es/task--complete/16";
// @ts-ignore
import Review16 from "@carbon/icons-vue/es/data--view--alt/16";
import summit from "@carbon/pictograms/es/summit";

const router = useRouter();

// Reference for the skelleton table
const skeletonReference = ref(null);
const tableReference = ref("");

// Table data
const tableData = ref<SubmissionList[]>([]);
const pageNumber = ref(1);
const totalItems = ref(0);
const pageSize = ref(10);

// Request data that changes based on the pagination
const uri = computed(
  () =>
    `/api/clients/submissions?page=${pageNumber.value - 1}&size=${
      pageSize.value
    }${tableReference.value}`
);

const { response, fetch, loading } = useFetchTo(uri, tableData);

// Watch for changes on the uri to fetch the new data
watch(uri, () => {
  if (!loading.value) fetch();
});

// Update the total items on the table
watch(
  response,
  () => {
    const totalCount = parseInt(response.value.headers["x-total-count"] || "0");
    totalItems.value = totalCount;
  }
);

// Update values on pagination
const paginate = (event: any) => {
  pageNumber.value = event.detail.page;
  pageSize.value = event.detail.pageSize;
};

// Update the tag colors
const tagColor = (status: string) => {
  switch (status) {
    case "New":
      return "blue";
    case "Approved":
      return "green";
    case "Rejected":
      return "red";
    default:
      return "purple";
  }
};

// Select an entry to go to the details page
const selectEntry = (entry: SubmissionList) => {
  const params = { id: entry.id };
  if (entry.requestType !== "Submission pending processing")
    router.push({ name: "review", params });
};

const formattedDate = (date: string): string => {
  return format(new Date(date), "MMM dd, yyyy");
};

// Get the icon for the row
const iconForRow = (row: SubmissionList) => {
  if (row.requestType === "Auto approved client") return Approved16;
  return Review16;
};

// Disable the skelleton table header
const disableSkelleton = () => {
  if (skeletonReference && skeletonReference.value) {
    skeletonReference.value.showHeader = false;
    skeletonReference.value.showToolbar = false;
  }
};
// This is to make the skelleton data-table to hide some elements
onMounted(() => {
  disableSkelleton();
  watch(skeletonReference, disableSkelleton);
});

const userhasAuthority = [
  "CLIENT_VIEWER",
  "CLIENT_EDITOR",
  "CLIENT_ADMIN",
  "CLIENT_SUSPEND",
].some((authority) => ForestClientUserSession.authorities.includes(authority));
const summitSvg = useSvg(summit);
</script>

<template>
  <div id="screen" class="table-list">
    <div id="title" v-if="userhasAuthority">
      <div>
        <div class="form-header-title mg-sd-25">
          <h1>Submissions</h1>
          <p class="body-compact-01">
            Check and manage client submissions. Please note: Let applicants know they can apply for
            a number online at
            <a target="_blank" href="https://www.gov.bc.ca/ClientNumber">
              www.gov.bc.ca/ClientNumber
            </a>
          </p>
        </div>        
      </div>
    </div>
    
    <div id="datatable" v-if="userhasAuthority">
      <cds-table id="submissions-table" use-zebra-styles v-if="!loading">
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell class="col-padding" />
            <cds-table-header-cell>Client name</cds-table-header-cell>
            <cds-table-header-cell>Client type</cds-table-header-cell>
            <cds-table-header-cell>District</cds-table-header-cell>
            <cds-table-header-cell>Submitted on</cds-table-header-cell>
            <cds-table-header-cell>Submission status</cds-table-header-cell>
            <cds-table-header-cell class="col-padding" />
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="row in tableData" :key="row.name" @click="selectEntry(row)">
            <cds-table-cell />
            <cds-table-cell><span>{{ toTitleCase(row.name) }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.clientType }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.district }}</span></cds-table-cell>
            <cds-table-cell><span>{{ formattedDate(row.submittedAt) }}</span></cds-table-cell>
            <cds-table-cell>
              <div>
                <cds-tag :type="tagColor(row.status)" title=""><span>{{ row.status }}</span></cds-tag>
              </div>
            </cds-table-cell>
            <cds-table-cell />
          </cds-table-row>
        </cds-table-body>
      </cds-table>

      <cds-table-skeleton
      v-else
      ref="skeletonReference"
      zebra
      :row-count="pageSize"
      :headers="['Client name', 'Client type', 'District', 'Submitted on', 'Submission status']"
      />
    </div>

    <div class="empty-table-list" 
        v-if="totalItems == 0 && userhasAuthority && !loading">
        <summit-svg alt="Summit pictogram" 
                    class="standard-svg"/>
        <p class="heading-02">No submissions to show yet</p>
        <p class="body-compact-01">They will appear once submitted.</p>
        <p class="body-compact-01">Submissions remain here for only 7 days after they're reviewed.</p>
    </div>
    
    <div class="paginator" v-if="totalItems && userhasAuthority">
      <cds-pagination
          items-per-page-text="Submissions per page"        
          :page-size="pageSize" 
          :total-items="totalItems"
          @cds-pagination-changed-current="paginate"
          >
            <cds-select-item :value="10" selected>10</cds-select-item>
            <cds-select-item :value="20">20</cds-select-item>
            <cds-select-item :value="30">30</cds-select-item>
            <cds-select-item :value="40">40</cds-select-item>
            <cds-select-item :value="50">50</cds-select-item>
      </cds-pagination>
    </div>

    <cds-actionable-notification
          v-if="!userhasAuthority"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="You are not authorized to access this page"
        >
          <div>
          Please email <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help
          </div>
        </cds-actionable-notification>
  </div>

</template>


