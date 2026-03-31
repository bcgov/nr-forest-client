<script setup lang="ts">
import { useFetchTo } from '@/composables/useFetch';
import { ref } from 'vue';

import ToolBox20 from "@carbon/icons-vue/es/tool-box/20";
import Friendship20 from "@carbon/icons-vue/es/friendship/20";

const props = defineProps<{
  clientNumber: string;
}>();

const data = ref();
const { loading } = useFetchTo(`/api/clients/bc-registry/${props.clientNumber}`, data);

</script>
<template>
  <div
    class="tab-panel tab-panel--empty"
    :class="{
      'flex-center': loading,
    }"
    v-if="loading || !data"
  >
    <cds-loading v-if="loading"></cds-loading>
    <span v-if="!loading && !data" class="body-compact-01">
      BC Registry information not available.
    </span>
  </div>
  <template v-else>
    <div class="tab-header space-between">
      <h3 class="padding-left-1rem">
        BC Registry information
      </h3>
    </div>
    <div class="tab-panel tab-panel--populated">
      <cds-accordion
        :id="`bc-business-information`"
      >
        <cds-accordion-item
          size="lg"
          class="grouping-13"
          v-shadow="1"
        >
          <div
            slot="title"
            class="flex-column-0_25rem"
          >
            <span class="label-with-icon">
              <ToolBox20 />
              Business information
            </span>
          </div>
          <div
            id="bc-business-information-general-section"
            class="grouping-12"
          >
            <div class="flex-column-1_5rem">
              <read-only-component label="Business name" id="bc-bi-businessName">
                <span class="body-compact-01">{{ data.name }}</span>
              </read-only-component>
              <read-only-component label="Business status" id="bc-bi-businessStatus">
                <span class="body-compact-01">{{ data.state }}</span>
              </read-only-component>
              <read-only-component label="Business type" id="bc-bi-businessType">
                <span class="body-compact-01">{{ data.type }}</span>
              </read-only-component>
              <read-only-component label="Registration number" id="bc-bi-registrationNumber">
                <span class="body-compact-01">{{ data.registrationNumber }}</span>
              </read-only-component>
              <read-only-component label="Registration date" id="bc-bi-registrationDate">
                <span class="body-compact-01">{{ data.registrationDate }}</span>
              </read-only-component>
              <read-only-component label="Registered office address" id="bc-bi-officeAddress">
                <span class="body-compact-01">{{ data.address }}</span>
              </read-only-component>
            </div>
          </div>
        </cds-accordion-item>
      </cds-accordion>
      <cds-accordion
        :id="`bc-partner-information`"
      >
        <cds-accordion-item
          size="lg"
          class="grouping-13"
          v-shadow="1"
        >
          <div
            slot="title"
            class="flex-column-0_25rem"
          >
            <span class="label-with-icon">
              <Friendship20 />
              Partner information
            </span>
          </div>
          <div class="grouping-12">
            <cds-table
              id="partners-table"
              use-zebra-styles
            >
              <cds-table-head>
                <cds-table-header-row>
                  <cds-table-header-cell class="col-padding-10-px" />
                  <cds-table-header-cell class="col-280-px">Individual or Company name</cds-table-header-cell>
                  <cds-table-header-cell class="col-500-px">Residential or Registered address</cds-table-header-cell>
                  <cds-table-header-cell class="col-280-px">Incorporation or Registration</cds-table-header-cell>
                </cds-table-header-row>
              </cds-table-head>
              <cds-table-body>
                <cds-table-row v-for="row in []" :key="row">
                  <cds-table-cell class="no-padding" />
                  <cds-table-cell>
                    <span>{{ row.name }}</span>
                  </cds-table-cell>
                  <cds-table-cell>
                    <span>{{ row.address }}</span>
                  </cds-table-cell>
                  <cds-table-cell>
                    <span>{{ row.identification }}</span>
                  </cds-table-cell>
                </cds-table-row>
              </cds-table-body>
            </cds-table>
          </div>
        </cds-accordion-item>
      </cds-accordion>
    </div>
  </template>
</template>
