<script setup lang="ts">
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import { ref, computed, watch, onMounted } from "vue";

// Carbon
import "@carbon/web-components/es/components/data-table/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/pagination/index";
import "@carbon/web-components/es/components/select/index";
import "@carbon/web-components/es/components/tag/index";

import { useFetchTo } from "@/composables/useFetch";
import { useEventBus } from "@vueuse/core";

import type { ClientSearchResult, CodeNameValue } from "@/dto/CommonTypesDto";
import {
  adminEmail,
  getObfuscatedEmailLink,
  toTitleCase,
  highlightMatch,
  getTagColorByClientStatus,
  searchResultToText,
} from "@/services/ForestClientService";

import summit from "@carbon/pictograms/es/summit";
import userSearch from "@carbon/pictograms/es/user--search";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import Search16 from "@carbon/icons-vue/es/search/16";
import {
  isAscii,
  isMaxSizeMsg,
  isMinSizeMsg,
  optional,
} from "@/helpers/validators/GlobalValidators";

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const summitSvg = useSvg(summit);
const userSearchSvg = useSvg(userSearch);

const userhasAuthority = ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_ADMIN", "CLIENT_SUSPEND"].some(authority => ForestClientUserSession.authorities.includes(authority));

const networkErrorMsg = ref("");

// Table data
const tableData = ref<ClientSearchResult[]>();
const pageNumber = ref(1);
const totalItems = ref(0);
const pageSize = ref(10);

const rawSearchKeyword = ref("");

const searchKeyword = computed(() => rawSearchKeyword.value.trim());
const lastSearchKeyword = ref("");

// empty is valid
const valid = ref(true);

const predictiveSearchUri = computed(
  () => `/api/clients/search?size=5&keyword=${encodeURIComponent(searchKeyword.value)}`,
);

const fullSearchUri = computed(
  () =>
    `/api/clients/search?page=${pageNumber.value - 1}&size=${pageSize.value}&keyword=${encodeURIComponent(searchKeyword.value)}`,
);

const {
  response: searchResponse,
  fetch: fetchSearch,
  loading: loadingSearch,
  error: searchError,
} = useFetchTo(fullSearchUri, tableData, { skip: true });

const search = (skipResetPage = false) => {
  revalidateBus.emit();

  if (!valid.value) {
    return;
  }

  // resets any error message
  networkErrorMsg.value = "";

  if (!skipResetPage) {
    pageNumber.value = 1;
  }

  lastSearchKeyword.value = searchKeyword.value;
  fetchSearch();
};

watch(searchResponse, () => {
  const totalCount = parseInt(searchResponse.value.headers["x-total-count"] || "0");
  totalItems.value = totalCount;
});

watch([searchError], () => {
  if (searchError.value.message) {
    networkErrorMsg.value = searchError.value.message;
  }
});

const paginate = (event: any) => {
  const hasPageChanged = event.detail.page !== pageNumber.value;
  pageNumber.value = event.detail.page;
  pageSize.value = event.detail.pageSize;
  if (hasPageChanged) {
    search(true);
  }
};

/**
 * Converts a client search result to a code/name/value representation.
 * @param searchResult The client search result
 */
const searchResultToCodeNameValue = (
  searchResult: ClientSearchResult,
): CodeNameValue<ClientSearchResult> => {
  const { clientNumber, clientFullName } = searchResult;
  const result = {
    code: clientNumber,
    name: clientFullName,
    value: searchResult,
  };
  return result;
};

const searchResultToCodeNameValueList = (list: ClientSearchResult[]) =>
  list?.map(searchResultToCodeNameValue);

const openClientDetails = (clientCode: string) => {
  if (clientCode) {
    const url = `/clients/details/${clientCode}`;
    window.open(url, "_self");
  }
};

const ariaLabel = "Search terms";

const lowerCaseLabel = ariaLabel.toLowerCase();

const validationsOnChange = [isAscii(lowerCaseLabel), isMaxSizeMsg(lowerCaseLabel, 50)];

const validations = [optional(isMinSizeMsg(lowerCaseLabel, 3)), ...validationsOnChange];

const skeletonReference = ref(null);

const disableSkelleton = () => {
  if (skeletonReference.value) {
    skeletonReference.value.showHeader = false;
    skeletonReference.value.showToolbar = false;
  }
};
// This is to make the skelleton data-table to hide some elements
onMounted(() => {
  disableSkelleton();
  watch(skeletonReference, disableSkelleton);
});
</script>

<template>
  <div id="screen" class="table-list">
    <div id="title" v-if="userhasAuthority">
      <div>
        <div class="form-header-title mg-sd-25">
          <h1>Client search</h1>

          <div class="hide-when-less-than-two-children">
            <div data-scroll="top-notification" class="header-offset"></div>
            <cds-actionable-notification
              v-if="networkErrorMsg !== '' && userhasAuthority"
              v-shadow="true"
              low-contrast="true"
              hide-close-button="true"
              open="true"
              kind="error"
              title="Something went wrong:">    
              <div>
                We're working to fix a problem with our network. Please try searching later.
                If this error persists, please email <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help.
              </div>
            </cds-actionable-notification>
          </div>   
        </div>      
      </div>
    </div>

    <div id="search-line">
      <data-fetcher
        v-model:url="predictiveSearchUri"
        :min-length="3"
        :init-value="[]"
        :init-fetch="false"
        :disabled="!searchKeyword || !valid"
        #="{ content, loading, error }"
      >
        <AutoCompleteInputComponent
          id="search-box"
          label=""
          :aria-label="ariaLabel"
          autocomplete="off"
          tip=""
          placeholder="Search by client number, name, acronym, mailing address, email address and more"
          size="lg"
          v-model="rawSearchKeyword"
          :contents="searchResultToCodeNameValueList(content)"
          :validations="validations"
          :validations-on-change="validationsOnChange"
          :loading="loading"
          prevent-selection
          @click:option="openClientDetails"
          @update:model-value="valid = false"
          @error="valid = !$event"
          @press:enter="search()"
          #="{ value }"
        >
          <div class="search-result-item" v-if="value">
            <span
              v-dompurify-html="highlightMatch(searchResultToText(value), searchKeyword)"
            ></span>
            <cds-tag :type="getTagColorByClientStatus(value.clientStatus)" title="">
              <span>{{ value.clientStatus }}</span>
            </cds-tag>
          </div>
        </AutoCompleteInputComponent>
      </data-fetcher>
      <cds-button kind="primary" @click.prevent="search()" id="search-button">
        <span>Search</span>
        <Search16 slot="icon" />
      </cds-button>
    </div>
    <div id="datatable" v-if="userhasAuthority">

      <cds-table id="search-table" use-zebra-styles v-if="!loadingSearch">
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell class="col-padding" />
            <cds-table-header-cell class="col-6_75">Client number</cds-table-header-cell>
            <cds-table-header-cell class="col-6_75">Acronym</cds-table-header-cell>
            <cds-table-header-cell class="col-19_4375">Name</cds-table-header-cell>
            <cds-table-header-cell class="col-14_75">Type</cds-table-header-cell>
            <cds-table-header-cell class="col-14_75">City</cds-table-header-cell>
            <cds-table-header-cell class="col-7_0625">Status</cds-table-header-cell>
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row class="link-container" v-for="row in tableData" :key="row.clientNumber">
            <cds-table-cell />
            <cds-table-cell><span>{{ row.clientNumber }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.clientAcronym || "-" }}</span></cds-table-cell>
            <cds-table-cell><span>{{ toTitleCase(row.clientFullName) }}</span></cds-table-cell>
            <cds-table-cell><span>{{ row.clientType }}</span></cds-table-cell>
            <cds-table-cell><span>{{ toTitleCase(row.city) }}</span></cds-table-cell>
            <cds-table-cell>
              <div>
                <cds-tag :type="getTagColorByClientStatus(row.clientStatus)" title="">
                  <span>{{ row.clientStatus }}</span>
                </cds-tag>
              </div>
            </cds-table-cell>
            <router-link
              :to="`/clients/details/${row.clientNumber}`"
              class="row-link"
              :aria-label="`View client “${toTitleCase(row.clientFullName)}“`"
            />
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

    <div class="empty-table-list" v-if="!tableData && userhasAuthority && !loadingSearch">
      <summit-svg alt="Summit pictogram" class="standard-svg" />
      <p class="heading-02">Nothing to show yet!</p>
      <p class="body-compact-01">Enter at least one criteria to start the search.</p>
      <p class="body-compact-01">The list will display here.</p>
    </div>

    <div
      class="empty-table-list"
      v-if="tableData && totalItems == 0 && userhasAuthority && !loadingSearch"
    >
      <user-search-svg alt="User search pictogram" class="standard-svg" />
      <p class="heading-02">No results for “{{ lastSearchKeyword }}”</p>
      <p class="body-compact-01">Consider adjusting your search term(s) and try again.</p>
    </div>

    <div class="paginator" v-if="totalItems && userhasAuthority">
      <cds-pagination
        items-per-page-text="Clients per page"
        :key="`${totalItems}-${pageSize}`"
        :page="pageNumber"
        :page-size="pageSize"
        :total-items="totalItems"
        @cds-pagination-changed-current="paginate"
        :formatStatusWithDeterminateTotal="
          ({ start, end, count }) => `${start}–${end} of ${count} client${count <= 1 ? '' : 's'}`
        "
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
