<script setup lang="ts">
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import { ref, unref, computed, watch, onMounted } from "vue";

// Carbon
import "@carbon/web-components/es/components/data-table/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/pagination/index";
import "@carbon/web-components/es/components/select/index";
import "@carbon/web-components/es/components/tag/index";

import AdvancedSearch from "@/pages/search/AdvancedSearch.vue";

import { useFetchTo, usePost } from "@/composables/useFetch";
import { useEventBus } from "@vueuse/core";

import type { ClientSearchParameters, ClientSearchResult, CodeNameValue } from "@/dto/CommonTypesDto";
import {
  adminEmail,
  getObfuscatedEmailLink,
  toTitleCase,
  toUpperCase,
  highlightMatch,
  getTagColorByClientStatus,
  searchResultToText,
  isNumeric,
  extractKeywords,
} from "@/services/ForestClientService";

import summit from "@carbon/pictograms/es/summit";
import userSearch from "@carbon/pictograms/es/user--search";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import Search16 from "@carbon/icons-vue/es/search/16";
import FilterEdit16 from "@carbon/icons-vue/es/filter--edit/16";
import {
  isAscii,
  isMaxSizeMsg,
  isMinSizeMsg,
  optional,
} from "@/helpers/validators/GlobalValidators";
import { useRouter } from "vue-router";

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const summitSvg = useSvg(summit);
const userSearchSvg = useSvg(userSearch);

const userhasAuthority = ["CLIENT_VIEWER", "CLIENT_EDITOR", "CLIENT_ADMIN", "CLIENT_SUSPEND"].some(authority => ForestClientUserSession.authorities.includes(authority));

const networkErrorMsg = ref("");

// Table data
const tableData = ref<ClientSearchResult[]>();
const pageNumber = ref(1);
const totalItems = ref(0);
const pageSize = ref(100);

const rawSearchKeyword = ref("");

const searchKeyword = computed(() => rawSearchKeyword.value.trim());
const lastSearchKeyword = ref("");

// empty is valid
const valid = ref(true);

const basicSearchUri = "/api/clients/search";

const advancedSearchUri = "/api/clients/advanced-search";

const predictiveSearchUri = computed(
  () => `${basicSearchUri}?size=5&keyword=${encodeURIComponent(searchKeyword.value)}`,
);

const advancedFilters = ref<ClientSearchParameters>({});

const commonFetchParams = computed(() => ({
  page: pageNumber.value - 1,
  size: pageSize.value,
}));

const basicFetchParams = computed(() => ({
  ...commonFetchParams.value,
  keyword: searchKeyword.value,
}));

const advancedFetchParams = computed(() => ({
  ...commonFetchParams.value,
  ...advancedFilters.value,
}));

const commonFetchConfig = {
  skip: true,
  paramsSerializer: (params: Record<string, any>) => new URLSearchParams(params).toString(),
};

const basicFetchConfig = computed(() => ({
  ...commonFetchConfig,
  params: basicFetchParams.value,
}));

// For POST, page and size must be in the URL, not in the body
const advancedSearchQuery = computed(() => {
  const params = new URLSearchParams({
    page: String(pageNumber.value - 1),
    size: String(pageSize.value),
  });
  return `${advancedSearchUri}?${params.toString()}`;
});

const advancedFetchConfig = computed(() => ({
  ...commonFetchConfig,
  skip: true,
}));

const searchType = ref<"basic" | "advanced">("basic");

// Make fetch logic reactive to searchType
function toCsvFilters(filters) {
  const csvFields = ["clientIdType", "clientType", "clientStatus"];
  const result = { ...filters };
  for (const key of csvFields) {
    if (Array.isArray(result[key])) {
      result[key] = result[key].join(",");
    }
  }
  return result;
}

const fetchHandlers = computed(() => {
  if (searchType.value === "advanced") {
    // Use POST, with page/size in URL, filters in body (CSV for specific fields)
    return usePost(advancedSearchQuery.value, toCsvFilters(advancedFilters.value), advancedFetchConfig.value);
  } else {
    return useFetchTo(basicSearchUri, tableData, basicFetchConfig.value);
  }
});

const searchResponse = computed(() => unref(fetchHandlers.value.response));
const fetchSearch = async () => {
  const handler = fetchHandlers.value;
  await handler.fetch();
  // For advanced search (POST), update tableData manually
  if (searchType.value === "advanced" && handler.response && handler.response.value && handler.response.value.data) {
    tableData.value = handler.response.value.data;
  }
};
const loadingSearch = computed(() => unref(fetchHandlers.value.loading));
const searchError = computed(() => unref(fetchHandlers.value.error));

const noExactMatch = ref<boolean|null>(null);

const search = (skipResetPage = false) => {
  revalidateBus.emit();

  if (!valid.value) {
    return;
  }

  // resets any error message
  networkErrorMsg.value = "";

  if (!skipResetPage) {
    pageNumber.value = 1;
    noExactMatch.value = null;
  }

  lastSearchKeyword.value = searchKeyword.value;
  fetchSearch();
};

const searchBasic = () => {
  searchType.value = "basic";

  // Clears advanced filters
  advancedFilters.value = {};

  // Clears the current typed userId (even if not selected)
  advancedTypedUserId.value = "";

  search();
};

const searchAdvanced = () => {
  searchType.value = "advanced";
  rawSearchKeyword.value = "";
  search();
};

const advancedModalActive = ref(false);

const advancedTypedUserId = ref("");

const resultsIncludeExactMatch = () => {
  const searchWordsList = extractKeywords(lastSearchKeyword.value);
  const searchFieldsList: (keyof ClientSearchResult)[] = [
    "clientNumber",
    "clientAcronym",
    "clientFullName",
  ];

  // Search row by row in the returned results
  const foundExactMatch = (tableData.value ?? []).find((row) => {
    for (const field of searchFieldsList) {
      const fieldValue = row[field];
      if (fieldValue === null) {
        continue;
      }
      if (typeof fieldValue === "string") {
        const fieldWordsList = extractKeywords(fieldValue);

        /*
        Checking all the words from the search terms are included in the current field value.
        Which matches the current behavior of the search API, since it doesn't seem to be able to
        match different words in different fields.
        i.e. the search API only includes a client in the results when all the search terms match
        the value in **one** of the search fields.
        */
        const isExactMatch = searchWordsList.every((searchWord) =>
          fieldWordsList.some(
            (fieldWord) =>
              isNumeric(searchWord)
                ? isNumeric(fieldWord) && Number(searchWord) === Number(fieldWord)
                : fieldWord === searchWord, // like an `includes`
          ),
        );

        if (isExactMatch) {
          return true;
        }
      } else if (typeof fieldValue === "number") {
        if (lastSearchKeyword.value === String(fieldValue)) {
          return true;
        }
      }
    }
    return false;
  });
  return !!foundExactMatch;
};

watch(searchResponse, () => {
  const headers = searchResponse.value && searchResponse.value.headers ? searchResponse.value.headers : {};
  const totalCount = Number.parseInt(headers["x-total-count"] || "0");
  totalItems.value = totalCount;

  // Skip the check for exact matches if this is not a basic search
  if (searchType.value !== "basic") {
    return;
  }

  /*
  noExactMatch null means this is a new search.
  Otherwise the user is navigating with the pagination buttons.

  Note 1: We also want to re-check it when noExactMatch is true because it could happen that:
  1. The newly returned page of results has an exact match (even though it's unlike since the first
  exact match is expected to be found in the first page), or;
  2. The user navigates back to the first page and there is a newly created client that is an exact
  match.
  In both cases we need to remove the No exact match message.

  Note 2: If it was already checked to be false (there is an exact match), we should not re-check
  it. For example: first page has an exact match. Second page might not have one, but it doesn't
  matter - there IS an exact match, it's just not in the current page.
  */
  if (totalCount > 0 && (noExactMatch.value === null || noExactMatch.value === true)) {
    noExactMatch.value = !resultsIncludeExactMatch();
  }
});

watch(searchError, (err) => {
  if (err && err.message) {
    networkErrorMsg.value = err.message;
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

const router = useRouter();

const openClientDetails = (clientCode: string) => {
  if (clientCode) {
    const url = `/clients/details/${clientCode}`;
    router.push(url);
  }
};

const inputAriaLabel = "Search terms";

const lowerCaseInputLabel = inputAriaLabel.toLowerCase();

const validationsOnChange = [isAscii(lowerCaseInputLabel), isMaxSizeMsg(lowerCaseInputLabel, 50)];

const validations = [optional(isMinSizeMsg(lowerCaseInputLabel, 3)), ...validationsOnChange];

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

    <div id="search-section">
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
            :aria-label="inputAriaLabel"
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
            @press:enter:item="openClientDetails"
            @update:model-value="valid = false"
            @error="valid = !$event"
            @press:enter="searchBasic()"
            #="{ value }"
          >
            <div class="search-result-item link-container" v-if="value">
              <span
                v-dompurify-html="highlightMatch(searchResultToText(value), searchKeyword)"
              ></span>
              <cds-tag :type="getTagColorByClientStatus(value.clientStatus)" title="">
                <span>{{ value.clientStatus }}</span>
              </cds-tag>
              <router-link
                :to="`/clients/details/${value.clientNumber}`"
                class="row-link"
                :aria-label="`View client “${toUpperCase(value.clientFullName)}“`"
              />
            </div>
          </AutoCompleteInputComponent>
        </data-fetcher>
        <cds-button kind="tertiary" @click.prevent="advancedModalActive = true" id="open-advanced-search-button">
          <FilterEdit16 slot="icon" />
        </cds-button>
        <cds-button kind="primary" @click.prevent="searchBasic()" id="search-button">
          <span>Search</span>
          <Search16 slot="icon" />
        </cds-button>
      </div>
    </div>

    <cds-inline-notification
      v-shadow="2"
      id="no-exact-match"
      class="table-notification"
      v-if="noExactMatch"
      low-contrast="true"
      open="true"
      kind="info">
      <div class="cds--inline-notification-content">
        We couldn’t find an exact match. Check these records for what you need.
      </div>
    </cds-inline-notification>

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
            <cds-table-cell><span>{{ toUpperCase(row.clientFullName) }}</span></cds-table-cell>
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
              :aria-label="`View client “${toUpperCase(row.clientFullName)}“`"
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
      <p class="heading-02">No results for
        <template v-if="searchType === 'basic'">“{{ lastSearchKeyword }}”</template>
        <template v-else>your advanced search</template>
      </p>
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
        <cds-select-item :value="100">100</cds-select-item>
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
  <advanced-search
    v-model:active="advancedModalActive"
    v-model:filters="advancedFilters"
    v-model:typed-user-id="advancedTypedUserId"
    @search="searchAdvanced"
  />
</template>
