<script setup lang="ts">
import { ref,computed, watch, onMounted } from 'vue';
// Carbon
import '@carbon/web-components/es/components/data-table/index';
import '@carbon/web-components/es/components/button/index';
import '@carbon/web-components/es/components/overflow-menu/index';
import '@carbon/web-components/es/components/pagination/index';
import '@carbon/web-components/es/components/select/index';
import '@carbon/web-components/es/components/ui-shell/index';
import '@carbon/web-components/es/components/tag/index';
import '@carbon/web-components/es/components/tooltip/index';
// Composables
import { useFetchTo } from '@/composables/useFetch'
// Types
import type {SubmissionList} from '@/dto/CommonTypesDto'
import { formatDistanceToNow, format} from 'date-fns'
// @ts-ignore
import Filter16 from '@carbon/icons-vue/es/filter/16';
// @ts-ignore
import Result16 from '@carbon/icons-vue/es/result/16';

// Reference for the skelleton table
const skeleton = ref(null)
const searchbox = ref(null)
const tb = ref('')

const tableData = ref<SubmissionList[]>([]);
const pageNumber = ref(0)
const pageSize = ref(10)
const totalItems = ref(0)
const uri = computed(() => `/api/clients/submissions?page=${pageNumber.value}&size=${pageSize.value}${tb.value}`);
const {response, fetch, loading } = useFetchTo(uri, tableData)

watch(uri, fetch)
watch(response,() => (totalItems.value = parseInt(response.value.headers['x-total-count'] || '0')))

const paginate = (event:any) =>{  
  pageNumber.value = (event.detail.page-1)
  pageSize.value = event.detail.pageSize
}

const tagColor = (status: string) =>{
  switch(status){
    case 'New':
      return 'blue'
    case 'Approved':
      return 'green'
    case 'Rejected':
      return 'red'
    default:
      return 'purple'
  }
}

const selectEntry = (id:string) => {
  console.log(`Selected entry ${id}`)
}

const friendlyDate = (date:string):string => {
  return `${formatDistanceToNow(new Date(date))} ago`
}
const formattedDate = (date:string):string => {
  return format(new Date(date),'MMM dd, yyyy')
}
const normalizeString = (input:string):string => {  
  const words = input.split(' ');
  const capitalizedWords = words.map(word => {    
    if (word.length > 0) {
      return word[0].toUpperCase() + word.slice(1).toLocaleLowerCase();
    } else {
      return '';
    }
  });  
  return capitalizedWords.join(' ');
}

// Disable the skelleton table header
const disableSkelleton = () => {
  if(skeleton && skeleton.value){
    skeleton.value.showHeader = false
    skeleton.value.showToolbar = false
  }
}
// This is to make the skelleton data-table to hide some elements
onMounted(() => {  
  disableSkelleton()
  watch(skeleton,disableSkelleton)
})
</script>


<template>

  <div id="side" class="submission-sidebar"> 
    <cds-side-nav>
      <cds-side-nav-items>
        <cds-side-nav-link active href="javascript:void(0)" large>
          <span>Submissions</span>
          <Result16 slot="title-icon" />
        </cds-side-nav-link>
      </cds-side-nav-items>
    </cds-side-nav>    
  
  </div>

  <div id="screen" class="submission-content">
    
    <div id="title">
      <div>
        <div class="form-header-title mg-sd-25">
          <p class="heading-05">Submissions</p>
          <p class="body-compact-01">Check and manage client submissions</p>
        </div>
      </div>
    </div>

    <div id="datatable">
      <cds-table use-zebra-styles v-if="!loading">

        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell>Submission type</cds-table-header-cell>
            <cds-table-header-cell>Client name</cds-table-header-cell>
            <cds-table-header-cell>Client type</cds-table-header-cell>
            <cds-table-header-cell>Last updated</cds-table-header-cell>
            <cds-table-header-cell>Submission status</cds-table-header-cell>
          </cds-table-header-row>
        </cds-table-head>

        <cds-table-body>
          <cds-table-row v-for="row in tableData" :key="row.name" @click="selectEntry(row.id)">
            <cds-table-cell>{{ row.requestType }}</cds-table-cell>
            <cds-table-cell>{{ normalizeString(row.name) }}</cds-table-cell>
            <cds-table-cell>{{row.clientType}}</cds-table-cell>
            <cds-table-cell>
              {{ (row.user || '') }} 
              <cds-tooltip align="top">
                <div class="sb-tooltip-trigger" aria-labelledby="content">| {{ friendlyDate(row.updated) }}</div>
                <cds-tooltip-content id="content">{{ formattedDate(row.updated) }}</cds-tooltip-content>
              </cds-tooltip>
            </cds-table-cell>
            <cds-table-cell><cds-tag :type="tagColor(row.status)"><span>{{ row.status }}</span></cds-tag></cds-table-cell>
          </cds-table-row>
        </cds-table-body>
      </cds-table>

      <cds-table-skeleton
        v-else
        ref="skeleton"
        zebra
        :row-count="pageSize"
        :headers="['Submission type','Client name','Client type','Last updated','Submission status']"
        />
      
      <cds-pagination 
        v-if="!loading"
        items-per-page-text="Submissions per page"
        :page-size="pageSize" 
        :total-items="totalItems"
        @cds-pagination-changed-current="paginate"
        @cds-pagination-page-sizes-select-changed="paginate">
        <cds-select-item value="10">10</cds-select-item>
        <cds-select-item value="20">20</cds-select-item>
        <cds-select-item value="30">30</cds-select-item>
        <cds-select-item value="40">40</cds-select-item>
        <cds-select-item value="50">50</cds-select-item>
      </cds-pagination>

    </div>
  </div>

</template>


