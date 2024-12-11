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
</script>

<template>
  <div class="flex-column-1_5rem">
    <div :id="`location-${data.clientLocnCode}-address-section`" class="grouping-23">
      <span class="body-compact-01">{{ data.addressOne }}</span>
      <span class="body-compact-01">{{ data.city }}, {{ data.provinceDesc }}</span>
      <span class="body-compact-01">{{ data.country }}</span>
      <span class="body-compact-01">{{ data.postalCode }}</span>
    </div>
    <div
      :id="`location-${data.clientLocnCode}-delivery-section`"
      class="grouping-23"
      v-if="deliveryInformation"
    >
      <read-only-component
        label="Delivery information"
        :id="`deliveryInformation-${data.clientLocnCode}`"
      >
        <span
          class="body-compact-01"
          v-dompurify-html="getFormattedHtml(deliveryInformation)"
        ></span>
      </read-only-component>
    </div>
    <div
      :id="`location-${data.clientLocnCode}-email-section`"
      class="grouping-23"
      v-if="data.emailAddress"
    >
      <read-only-component label="Email address" :id="`emailAddress-${data.clientLocnCode}`">
        <span class="body-compact-01">{{ data.emailAddress }}</span>
      </read-only-component>
    </div>
    <div
      :id="`location-${data.clientLocnCode}-phone-section`"
      class="grouping-10 no-padding margin-left-1_75rem"
      v-if="data.businessPhone || data.cellPhone || data.faxNumber"
    >
      <read-only-component
        label="Primary phone number"
        :id="`businessPhone-${data.businessPhone}`"
        v-if="data.businessPhone"
      >
        <span class="body-compact-01">{{ data.businessPhone }}</span>
      </read-only-component>
      <read-only-component
        label="Secondary phone number"
        :id="`businessPhone-${data.cellPhone}`"
        v-if="data.cellPhone"
      >
        <span class="body-compact-01">{{ data.cellPhone }}</span>
      </read-only-component>
      <read-only-component
        label="Fax"
        :id="`businessPhone-${data.faxNumber}`"
        v-if="data.faxNumber"
      >
        <span class="body-compact-01">{{ data.faxNumber }}</span>
      </read-only-component>
    </div>
    <div
      :id="`location-${data.clientLocnCode}-note-section`"
      class="grouping-23"
      v-if="data.cliLocnComment"
    >
      <read-only-component label="Note" :id="`locationNote-${data.clientLocnCode}`">
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
