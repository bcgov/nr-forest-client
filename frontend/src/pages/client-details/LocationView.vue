<script setup lang="ts">
import { computed } from "vue";

import type { ClientLocation } from "@/dto/CommonTypesDto";
import { getFormattedHtml } from "@/services/ForestClientService";

const props = defineProps<{
  data: ClientLocation;
}>();

const deliveryInformation = computed(() => {
  const { addressTwo, addressThree } = props.data;
  const rawList = [addressTwo, addressThree];
  const list = [];
  rawList.forEach((item) => {
    if (item) {
      list.push(item);
    }
  });
  return list.join("\n");
});

const indexString = props.data.clientLocnCode;
</script>

<template>
  <div class="flex-column-1_5rem">
    <div
      :id="`location-${indexString}-delivery-section`"
      class="grouping-23"
      v-if="deliveryInformation"
    >
      <read-only-component
        label="Delivery information"
        :id="`location-${indexString}-deliveryInformation`"
      >
        <span
          class="body-compact-01"
          v-dompurify-html="getFormattedHtml(deliveryInformation)"
        ></span>
      </read-only-component>
    </div>
    <div :id="`location-${indexString}-address-section`" class="grouping-23">
      <span :id="`location-${indexString}-streetAddress`" class="body-compact-01">
        {{ data.addressOne }}
      </span>
      <span :id="`location-${indexString}-city-province`" class="body-compact-01">
        {{ data.city }}, {{ data.provinceDesc }}
      </span>
      <span :id="`location-${indexString}-country`" class="body-compact-01">
        {{ data.country }}
      </span>
      <span :id="`location-${indexString}-postalCode`" class="body-compact-01">
        {{ data.postalCode }}
      </span>
    </div>
    <div :id="`location-${indexString}-email-section`" class="grouping-23" v-if="data.emailAddress">
      <read-only-component label="Email address" :id="`location-${indexString}-emailAddress`">
        <span class="body-compact-01">{{ data.emailAddress }}</span>
      </read-only-component>
    </div>
    <div
      :id="`location-${indexString}-phone-section`"
      class="grouping-10 no-padding margin-left-1_75rem"
      v-if="data.businessPhone || data.cellPhone || data.homePhone || data.faxNumber"
    >
      <read-only-component
        label="Primary phone number"
        :id="`location-${indexString}-primaryPhoneNumber`"
        v-if="data.businessPhone"
      >
        <span class="body-compact-01">{{ data.businessPhone }}</span>
      </read-only-component>
      <read-only-component
        label="Secondary phone number"
        :id="`location-${indexString}-secondaryPhoneNumber`"
        v-if="data.cellPhone"
      >
        <span class="body-compact-01">{{ data.cellPhone }}</span>
      </read-only-component>
      <read-only-component label="Fax" :id="`location-${indexString}-fax`" v-if="data.faxNumber">
        <span class="body-compact-01">{{ data.faxNumber }}</span>
      </read-only-component>
      <read-only-component
        label="Other"
        :id="`location-${indexString}-other`"
        v-if="data.homePhone"
      >
        <span class="body-compact-01">{{ data.homePhone }}</span>
      </read-only-component>
    </div>
    <div
      :id="`location-${indexString}-notes-section`"
      class="grouping-23"
      v-if="data.cliLocnComment"
    >
      <read-only-component label="Notes" :id="`location-${indexString}-notes`">
        <span
          class="body-compact-01"
          v-dompurify-html="getFormattedHtml(data.cliLocnComment)"
        ></span>
      </read-only-component>
    </div>
  </div>
</template>

<style scoped>
.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
