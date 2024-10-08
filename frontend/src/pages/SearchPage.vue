<script setup lang="ts">
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import { ref, computed, watch } from "vue";
import { useFetchTo } from "@/composables/useFetch";
import type { ClientSearchResult, CodeNameType } from "@/dto/CommonTypesDto";
import { adminEmail, getObfuscatedEmailLink } from "@/services/ForestClientService";
import summit from "@carbon/pictograms/es/summit";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import Search16 from "@carbon/icons-vue/es/search/16";
import { greenDomain } from "@/CoreConstants";
import {
  isAscii,
  isMaxSizeMsg,
  isMinSizeMsg,
  optional,
} from "@/helpers/validators/GlobalValidators";

const summitSvg = useSvg(summit);

const userhasAuthority = ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_ADMIN"].some(authority => ForestClientUserSession.authorities.includes(authority));

// Table data
const tableData = ref<ClientSearchResult[]>([]);
const pageNumber = ref(1);
const totalItems = ref(0);
const pageSize = ref(10);

const searchKeyword = ref("");

const predictiveSearchUri = computed(
  () => `/api/clients/predictive-search?keyword=${encodeURIComponent(searchKeyword.value)}`,
);

const searchResultToCodeNameType = (searchResult: ClientSearchResult): CodeNameType => {
  const { clientNumber, clientName, clientType, city, clientStatus } = searchResult;
  const result = {
    code: clientNumber,
    name: `${clientNumber}, ${clientName}, ${clientType}, ${city} (${clientStatus})`,
  };
  return result;
};

const fullSearchUri = computed(
  () =>
    `/api/clients/full-search?page=${pageNumber.value - 1}&size=${pageSize.value}&keyword=${encodeURIComponent(searchKeyword.value)}`,
);

const search = () => {
  const { response, fetch, loading } = useFetchTo(fullSearchUri, tableData);
  if (!loading.value) fetch();
};

const selectEntry = (entry: ClientSearchResult) => {
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

const selectedClient = ref<CodeNameType>();

const goToClientDetails = (client: CodeNameType) => {
  if (client) {
    selectedClient.value = client;
    window.location.href = `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${client.code}`;
  }
};

const ariaLabel = "Search terms";

const lowerCaseLabel = ariaLabel.toLowerCase();

const validationsOnChange = [isAscii(lowerCaseLabel), isMaxSizeMsg(lowerCaseLabel, 50)];

const validations = [optional(isMinSizeMsg(lowerCaseLabel, 3)), ...validationsOnChange];

const valid = ref(!!searchKeyword.value);
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

    <div id="search-line">
      <data-fetcher
        v-model:url="predictiveSearchUri"
        :min-length="3"
        :init-value="[]"
        :init-fetch="false"
        :disabled="!searchKeyword || !valid || searchKeyword === selectedClient?.name"
        #="{ content, loading, error }"
      >
        <AutoCompleteInputComponent
          id="search-box"
          label=""
          :aria-label="ariaLabel"
          autocomplete="off"
          tip=""
          placeholder="Search by client number, name or acronym"
          v-model="searchKeyword"
          :contents="content?.map(searchResultToCodeNameType)"
          :validations="validations"
          :validations-on-change="validationsOnChange"
          :loading="loading"
          @update:selected-value="goToClientDetails"
          @update:model-value="valid = false"
          @error="valid = !$event"
        />
      </data-fetcher>
      <cds-button kind="primary" @click.prevent="search" id="search-button">
        <span>Search</span>
        <Search16 slot="icon" />
      </cds-button>
    </div>
    <div id="datatable" v-if="userhasAuthority">

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
            <cds-table-cell><span>{{ row.clientAcronym }}</span></cds-table-cell>
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
