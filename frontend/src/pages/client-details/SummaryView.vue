<script setup lang="ts">
import { computed } from "vue";
import type { ClientDetails } from "@/dto/CommonTypesDto";
import {
  getFormattedHtml,
  getTagColorByClientStatus,
  goodStanding,
} from "@/services/ForestClientService";

// @ts-ignore
import Check20 from "@carbon/icons-vue/es/checkmark--filled/20";
// @ts-ignore
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";

const props = defineProps<{
  data: ClientDetails;
}>();

const clientRegistrationNumber = computed(() => {
  const { registryCompanyTypeCode, corpRegnNmbr } = props.data;
  if (!registryCompanyTypeCode || !corpRegnNmbr) {
    return undefined;
  }
  return `${registryCompanyTypeCode}${corpRegnNmbr}`;
});

const doingBusinessAs = computed(() => {
  const { doingBusinessAs } = props.data;
  if (doingBusinessAs && doingBusinessAs.length) {
    return doingBusinessAs.map((value) => value.doingBusinessAsName).join("\n");
  }
  return undefined;
});
</script>

<template>
  <div class="grouping-10 no-padding">
    <read-only-component label="Client number" id="clientNumber" label-class="label-02">
      <span class="body-compact-01">{{ props.data.clientNumber }}</span>
    </read-only-component>
    <read-only-component
      label="Acronym"
      id="acronym"
      label-class="label-02"
      v-if="props.data.clientAcronym"
    >
      <span class="body-compact-01">{{ props.data.clientAcronym }}</span>
    </read-only-component>
    <read-only-component
      label="Doing business as"
      id="doingBusinessAs"
      label-class="label-02"
      v-if="doingBusinessAs"
    >
      <span class="body-compact-01" v-dompurify-html="getFormattedHtml(doingBusinessAs)"></span>
    </read-only-component>
    <read-only-component label="Client type" id="clientType" label-class="label-02">
      <span class="body-compact-01">{{ props.data.clientTypeDesc }}</span>
    </read-only-component>
    <read-only-component
      label="Registration number"
      id="registrationNumber"
      label-class="label-02"
      v-if="clientRegistrationNumber"
    >
      <span class="body-compact-01">{{ clientRegistrationNumber }}</span>
    </read-only-component>
    <read-only-component
      label="BC Registries standing"
      id="goodStanding"
      label-class="label-02"
      v-if="clientRegistrationNumber"
    >
      <div class="internal-grouping-01">
        <span class="body-compact-01 default-typography">{{
          goodStanding(props.data.goodStandingInd)
        }}</span>
        <Check20 v-if="props.data.goodStandingInd === 'Y'" class="good" />
        <Warning20 v-if="props.data.goodStandingInd !== 'Y'" class="warning" />
      </div>
    </read-only-component>
    <read-only-component
      :label="props.data.clientIdTypeDesc"
      id="identification"
      label-class="label-02"
      v-if="props.data.clientIdTypeDesc && props.data.clientIdentification"
    >
      <span class="body-compact-01">{{ props.data.clientIdentification }}</span>
    </read-only-component>
    <read-only-component
      label="Date of birth"
      id="dataOfBirth"
      label-class="label-02"
      v-if="props.data.birthdate"
    >
      <span class="body-compact-01">{{ props.data.birthdate }}</span>
    </read-only-component>
    <read-only-component label="Status" id="status" label-class="label-02">
      <span class="body-compact-01">
        <cds-tag :type="getTagColorByClientStatus(props.data.clientStatusDesc)">
          <span>{{ props.data.clientStatusDesc }}</span>
        </cds-tag>
      </span>
    </read-only-component>
  </div>
  <div class="grouping-10 no-padding" v-if="props.data.clientComment">
    <read-only-component
      label="Notes"
      id="notes"
      label-class="label-02"
      v-if="props.data.clientComment"
    >
      <span
        class="body-compact-01"
        v-dompurify-html="getFormattedHtml(props.data.clientComment)"
      ></span>
    </read-only-component>
  </div>
</template>

<style scoped>
.internal-grouping-01 {
  display: flex;
  gap: 0.5rem;
}

.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
