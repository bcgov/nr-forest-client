<script setup lang="ts">
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import { ref, computed, watch } from "vue";
import { useFetchTo } from "@/composables/useFetch";
import type { ClientList } from "@/dto/CommonTypesDto";
import { adminEmail, getObfuscatedEmailLink } from "@/services/ForestClientService";
import summit from "@carbon/pictograms/es/summit";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import Search16 from "@carbon/icons-vue/es/search/16";

const summitSvg = useSvg(summit);

const userhasAuthority = ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_ADMIN"].some(authority => ForestClientUserSession.authorities.includes(authority));

const tableReference = ref("");

// Table data
const tableData = ref<ClientList[]>([]);
const pageNumber = ref(1);
const totalItems = ref(0);
const pageSize = ref(10);

const predictiveSearchKeyword = ref("");
const fullSearchKeyword = ref("");

const predictiveSearchUri = computed(
  () =>
    `/api/clients/predictive-search?page=${pageNumber.value - 1}&size=${pageSize.value}&keyword=${encodeURIComponent(predictiveSearchKeyword.value)}${tableReference.value || ''}`
);

const fullSearchUri = computed(
  () =>
    `/api/clients/full-search?page=${pageNumber.value - 1}&size=${pageSize.value}&keyword=${encodeURIComponent(fullSearchKeyword.value)}${tableReference.value || ''}`
);

const search = () => {
  const { response, fetch, loading } = useFetchTo(fullSearchUri, tableData);
  if (!loading.value) fetch();
};

const selectEntry = (entry: ClientList) => {
  //TODO
};

const tagColor = (status: string) => {
  switch (status) {
    case "Active":
      return "green";
    case "Inactive":
      return "red";
    default:
      return "purple";
  }
};

const paginate = (event: any) => {
  pageNumber.value = event.detail.page;
  pageSize.value = event.detail.pageSize;
};
</script>

<template>
  <div id="screen" class="table-list">
    <div id="title" v-if="userhasAuthority">
      <div>
        <div class="form-header-title mg-sd-25">
          <h1>Client search</h1>
        </div>        
      </div>
    </div>
    
    <div id="datatable" v-if="userhasAuthority">

      <div style="float: right;">
        <cds-button
          kind="primary"
          @click.prevent="search"
        >
          <span>Search</span>
          <Search16 slot="icon" />
        </cds-button>
      </div>

      <cds-table use-zebra-styles v-if="!loading">
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell />
            <cds-table-header-cell>Client number</cds-table-header-cell>
            <cds-table-header-cell>Acronym</cds-table-header-cell>
            <cds-table-header-cell>Name</cds-table-header-cell>
            <cds-table-header-cell>Type</cds-table-header-cell>
            <cds-table-header-cell>City</cds-table-header-cell>
            <cds-table-header-cell>Status</cds-table-header-cell>
            <cds-table-header-cell />
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="row in tableData" :key="row.clientNumber" @click="selectEntry(row)">
            <cds-table-cell />
            <cds-table-cell><span>{{ row.clientNumber }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.acronym }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.clientName }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.clientType }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.city }}</span></cds-table-cell>
            <cds-table-cell>
              <div>
                <cds-tag :type="tagColor(row.clientStatus)" title=""><span>{{ row.clientStatus }}</span></cds-tag>
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
        :headers="['Client number', 'Acronym', 'Name', 'Type', 'City', 'Status']"
      />
    </div>

    <div class="empty-table-list" 
        v-if="totalItems == 0 && userhasAuthority && !loading">
        <summit-svg alt="Summit pictogram" 
                    class="standard-svg"/>
        <p class="heading-02">Nothing to show yet!</p>
        <p class="body-compact-01">Enter at least one criteria to start the search.</p>
        <p class="body-compact-01">The list will display here.</p>
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
