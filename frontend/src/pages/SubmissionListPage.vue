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
import { useRouter } from 'vue-router'
// Types
import type {SubmissionList} from '@/dto/CommonTypesDto'
import { formatDistanceToNow, format} from 'date-fns'
// @ts-ignore
import Approved16 from '@carbon/icons-vue/es/task--complete/16';
// @ts-ignore
import Review16 from '@carbon/icons-vue/es/data--view--alt/16';

const router = useRouter()

// Reference for the skelleton table
const skeletonReference = ref(null)
const tableReference = ref('')
const paginationReference = ref(null)

// Table data
const tableData = ref<SubmissionList[]>([]);
const pageNumber = ref(1)
const totalItems = ref(0)
const pageSize = ref(10)

// Request data that changes based on the pagination
const uri = computed(() => `/api/clients/submissions?page=${pageNumber.value-1}&size=${pageSize.value}${tableReference.value}`);
const {response, fetch, loading } = useFetchTo(uri, tableData)

// Watch for changes on the uri to fetch the new data
watch(uri, () =>{
  if(!loading.value)
    fetch()
})
// Update the total items on the table
watch(response,() => totalItems.value = parseInt(response.value.headers['x-total-count'] || '0'))

// Update values on pagination
const paginate = (event:any) =>{  
  pageNumber.value = (event.detail.page-1)
  pageSize.value = event.detail.pageSize
}
// Update the tag colors
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
// Select an entry to go to the details page
const selectEntry = (entry:SubmissionList) => {
  const params = { id: entry.id }
  if(entry.requestType !== 'Submission pending processing') router.push({ name: "review", params });
}
// Format the date to a friendly format
const friendlyDate = (date:string):string => {
  return `${formatDistanceToNow(new Date(date))} ago`
}
const formattedDate = (date:string):string => {
  return format(new Date(date),'MMM dd, yyyy')
}
// Normalize the string to capitalize the first letter of each word
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
// Get the icon for the row
const iconForRow = (row:SubmissionList) => {
  if(row.requestType === 'Auto approved client') return Approved16    
  return Review16  
}
// Disable the skelleton table header
const disableSkelleton = () => {
  if(skeletonReference && skeletonReference.value){
    skeletonReference.value.showHeader = false
    skeletonReference.value.showToolbar = false
  }
}
// This is to make the skelleton data-table to hide some elements
onMounted(() => {  
  disableSkelleton()
  watch(skeletonReference,disableSkelleton)
})

</script>

<template>

  <div id="screen" class="submission-list">
    
    <div id="title">
      <div>
        <div class="form-header-title mg-sd-25">
          <h3>Submissions</h3>
          <p class="body-compact-01">Check and manage client submissions</p>
        </div>        
      </div>
    </div>
    
    <div id="datatable">
      <cds-table use-zebra-styles v-if="!loading">
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell />
            <cds-table-header-cell>Submission type</cds-table-header-cell>
            <cds-table-header-cell>Client name</cds-table-header-cell>
            <cds-table-header-cell>Client type</cds-table-header-cell>
            <cds-table-header-cell>Last updated</cds-table-header-cell>
            <cds-table-header-cell>Submission status</cds-table-header-cell>
            <cds-table-header-cell />
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="row in tableData" :key="row.name" @click="selectEntry(row)">
            <cds-table-cell />
            <cds-table-cell>
              <div class="sp-1">
                <component
                  data-testid="display-row-icon"
                  :is="iconForRow(row)"      
                  :alt="row.requestType"
                />
                <span>{{ row.requestType }}</span>
              </div>
            </cds-table-cell>
            <cds-table-cell><span>{{ normalizeString(row.name) }}</span></cds-table-cell>
            <cds-table-cell><span>{{row.clientType}}</span></cds-table-cell>
            <cds-table-cell>
              <div>
                {{ (row.user || '') }} 
                <cds-tooltip align="top">
                  <div class="sb-tooltip-trigger" aria-labelledby="content">| {{ friendlyDate(row.updated) }}</div>
                  <cds-tooltip-content id="content">{{ formattedDate(row.updated) }}</cds-tooltip-content>
                </cds-tooltip>              
              </div>
            </cds-table-cell>
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
      :headers="['Submission type','Client name','Client type','Last updated','Submission status']"
      />
            
    </div>
    <div class="paginator">
      <cds-pagination 
          ref="paginationReference"
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
  </div>

</template>


