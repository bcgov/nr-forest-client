<script setup lang="ts">
import { computed } from "vue";
import type { ClientContact } from "@/dto/CommonTypesDto";
import { formatPhoneNumber } from "@/services/ForestClientService";

const props = defineProps<{
  data: ClientContact;
  index: number;
  associatedLocationsString: string;
}>();

const businessPhone = computed(() => formatPhoneNumber(props.data.businessPhone));
const secondaryPhone = computed(() => formatPhoneNumber(props.data.secondaryPhone));
const faxNumber = computed(() => formatPhoneNumber(props.data.faxNumber));
</script>

<template>
  <div :id="`contact-${index}-general-section`" class="flex-column-1_5rem margin-left-1_75rem">
    <read-only-component label="Contact type" :id="`contact-${index}-contactType`">
      <span class="body-compact-01">{{ data.contactCodeDescription }}</span>
    </read-only-component>
    <read-only-component label="Associated locations" :id="`contact-${index}-associatedLocations`">
      <span class="body-compact-01">{{ associatedLocationsString }}</span>
    </read-only-component>
    <read-only-component
      label="Email address"
      :id="`contact-${index}-emailAddress`"
      v-if="data.emailAddress"
    >
      <a :href="`mailto:${data.emailAddress}`">
        <span class="body-compact-01 colorless">{{ data.emailAddress }}</span>
      </a>
    </read-only-component>
    <div
      :id="`contact-${index}-phone-section`"
      class="grouping-10 no-padding"
      v-if="data.businessPhone || data.secondaryPhone || data.faxNumber"
    >
      <read-only-component
        label="Primary phone number"
        :id="`contact-${index}-primaryPhoneNumber`"
        v-if="data.businessPhone"
      >
        <a :href="`tel:${businessPhone}`">
          <span class="body-compact-01 colorless">{{ businessPhone }}</span>
        </a>
      </read-only-component>
      <read-only-component
        label="Secondary phone number"
        :id="`contact-${index}-secondaryPhoneNumber`"
        v-if="data.secondaryPhone"
      >
        <a :href="`tel:${secondaryPhone}`">
          <span class="body-compact-01 colorless">{{ secondaryPhone }}</span>
        </a>
      </read-only-component>
      <read-only-component label="Fax" :id="`contact-${index}-fax`" v-if="data.faxNumber">
        <a :href="`tel:${faxNumber}`">
          <span class="body-compact-01 colorless">{{ faxNumber }}</span>
        </a>
      </read-only-component>
    </div>
  </div>
</template>

<style scoped>
.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
