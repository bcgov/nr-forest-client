<script setup lang="ts">
import type { ClientLocation } from "@/dto/CommonTypesDto";
import { getFormattedHtml } from "@/services/ForestClientService";

const props = defineProps<{
  data: ClientLocation;
}>();

const indexString = props.data.clientLocnCode;
</script>

<template>
  <div class="flex-column-1_5rem">
    <div :id="`location-${indexString}-address-section`" class="grouping-23">
      <read-only-component label="Address" :id="`location-${indexString}-address`">
        <div class="grouping-23 no-margin">
          <span
            :id="`location-${indexString}-addressTwo`"
            class="body-compact-01"
            v-if="data.addressTwo"
          >
            {{ data.addressTwo }}
          </span>
          <span
            :id="`location-${indexString}-addressThree`"
            class="body-compact-01"
            v-if="data.addressThree"
          >
            {{ data.addressThree }}
          </span>
          <span :id="`location-${indexString}-streetAddress`" class="body-compact-01">
            {{ data.addressOne }}
          </span>
          <span :id="`location-${indexString}-city-province`" class="body-compact-01">
            {{ data.city }}, {{ data.provinceDesc }}
          </span>
          <span :id="`location-${indexString}-country`" class="body-compact-01">
            {{ data.countryDesc }}
          </span>
          <span :id="`location-${indexString}-postalCode`" class="body-compact-01">
            {{ data.postalCode }}
          </span>
        </div>
      </read-only-component>
    </div>
    <div :id="`location-${indexString}-email-section`" class="grouping-23" v-if="data.emailAddress">
      <read-only-component label="Email address" :id="`location-${indexString}-emailAddress`">
        <a :href="`mailto:${data.emailAddress}`">
          <span class="body-compact-01 colorless">{{ data.emailAddress }}</span>
        </a>
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
        <a :href="`tel:${data.businessPhone}`">
          <span class="body-compact-01 colorless">{{ data.businessPhone }}</span>
        </a>
      </read-only-component>
      <read-only-component
        label="Secondary phone number"
        :id="`location-${indexString}-secondaryPhoneNumber`"
        v-if="data.cellPhone"
      >
        <a :href="`tel:${data.cellPhone}`">
          <span class="body-compact-01 colorless">{{ data.cellPhone }}</span>
        </a>
      </read-only-component>
      <read-only-component
        label="Tertiary phone number"
        :id="`location-${indexString}-tertiaryPhoneNumber`"
        v-if="data.homePhone"
      >
        <a :href="`tel:${data.homePhone}`">
          <span class="body-compact-01 colorless">{{ data.homePhone }}</span>
        </a>
      </read-only-component>
      <read-only-component label="Fax" :id="`location-${indexString}-fax`" v-if="data.faxNumber">
        <a :href="`tel:${data.faxNumber}`">
          <span class="body-compact-01 colorless">{{ data.faxNumber }}</span>
        </a>
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
