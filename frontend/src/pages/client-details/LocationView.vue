<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { ClientLocation, UserRole } from "@/dto/CommonTypesDto";
import { formatPhoneNumber, getFormattedHtml, includesAnyOf } from "@/services/ForestClientService";

import Edit16 from "@carbon/icons-vue/es/edit/16";

const props = defineProps<{
  data: ClientLocation;
  userRoles: UserRole[];
}>();

const indexString = props.data.clientLocnCode;

const businessPhone = computed(() => formatPhoneNumber(props.data.businessPhone));
const cellPhone = computed(() => formatPhoneNumber(props.data.cellPhone));
const homePhone = computed(() => formatPhoneNumber(props.data.homePhone));
const faxNumber = computed(() => formatPhoneNumber(props.data.faxNumber));

let originalData: ClientLocation;
const formData = ref<ClientLocation>();

const isEditing = ref(false);
const hasAnyChange = ref(false);

const resetFormData = () => {
  originalData = JSON.parse(JSON.stringify(props.data));
  formData.value = JSON.parse(JSON.stringify(props.data));
  hasAnyChange.value = false;
};

resetFormData();

watch(
  formData,
  () => {
    if (isEditing.value) {
      hasAnyChange.value = JSON.stringify(formData.value) !== JSON.stringify(originalData);
    }
  },
  { deep: true },
);

const edit = () => {
  // TODO
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);
</script>

<template>
  <div class="grouping-12">
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
              {{ data.city }}, {{ data.province }}
            </span>
            <span :id="`location-${indexString}-country`" class="body-compact-01">
              {{ data.country }}
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
          <a :href="`tel:${businessPhone}`">
            <span class="body-compact-01 colorless">{{ businessPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component
          label="Secondary phone number"
          :id="`location-${indexString}-secondaryPhoneNumber`"
          v-if="data.cellPhone"
        >
          <a :href="`tel:${cellPhone}`">
            <span class="body-compact-01 colorless">{{ cellPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component
          label="Tertiary phone number"
          :id="`location-${indexString}-tertiaryPhoneNumber`"
          v-if="data.homePhone"
        >
          <a :href="`tel:${homePhone}`">
            <span class="body-compact-01 colorless">{{ homePhone }}</span>
          </a>
        </read-only-component>
        <read-only-component label="Fax" :id="`location-${indexString}-fax`" v-if="data.faxNumber">
          <a :href="`tel:${faxNumber}`">
            <span class="body-compact-01 colorless">{{ faxNumber }}</span>
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
    <div v-if="canEdit && !isEditing">
      <cds-button :id="`location-${indexString}-EditBtn`" kind="tertiary" size="md" @click="edit">
        <span class="width-unset">Edit location</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
  </div>
</template>

<style scoped>
.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
