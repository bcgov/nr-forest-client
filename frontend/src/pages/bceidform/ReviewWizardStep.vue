<script setup lang="ts">
import { watch, ref, onMounted, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
// @ts-ignore
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Enterprise20 from "@carbon/icons-vue/es/enterprise/20"
import LocationStar20 from "@carbon/icons-vue/es/location--star/20"
import Location20 from "@carbon/icons-vue/es/location/20"
import User20 from "@carbon/icons-vue/es/user/20"
import { useFetchTo } from "@/composables/useFetch";
import { CodeNameType } from "@/dto/CommonTypesDto";
import { codeConversionFn } from "@/services/ForestClientService";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  districtsList: Array<CodeNameType>;
  active: boolean;
  goToStep: Function;
}>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

const revalidateBus = useEventBus<void>("revalidate-bus");

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch([formData], () => emit("update:data", formData.value));

const districtObject = computed(() =>
  props.districtsList.find(
    (district) => district.code === formData.value.businessInformation.district,
  ),
);

const receviedClientType = ref({} as CodeNameType);

useFetchTo(
  `/api/codes/clientTypes/${formData.value.businessInformation.clientType}`,
  receviedClientType
);

const clientType = computed(() => {
  return codeConversionFn(receviedClientType.value);
});

//We emit valid here because there is nothing else to be done here apart from showing information
emit("valid", true);

onMounted(() => {
  revalidateBus.emit();
});
</script>

<template>
  <div class="grouping-05">
    <h3>Business information</h3>
    <div class="grouping-22">
      <div class="grouping-01">
        <h4 class="review-icon-title">
          <Enterprise20 />
          {{ formData.businessInformation.businessName }}
        </h4>
        <p class="body-compact-01 grouping-22-item" id="clientTypeId">
          {{ clientType.text }}
        </p>
        <p class="body-compact-01 grouping-22-item" id="district">
          {{ districtObject.name }}
        </p>
      </div>
      <div v-if="formData.businessInformation.birthdate" class="grouping-22-item">
        <p class="label-01">Proprietor’s birthdate</p>
        <p class="body-compact-01">{{ formData.businessInformation.birthdate }}</p>
      </div>
    </div>
    <div class="grouping-06">
      <cds-button kind="tertiary" @click.prevent="goToStep(0)">
        <span>Edit business information</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
  </div>
  <div class="grouping-05">
    <h3>Address</h3>
    <div v-for="(address, index) in formData.location.addresses" 
        :key="address.locationName" 
        class="grouping-07">
      <hr class="divider" v-if="index > 0" />
      <h4 class="review-icon-title">
        <LocationStar20 v-if="index === 0" />
        <Location20 v-else />{{ address.locationName }}
      </h4>
      <div class="grouping-23">
        <span class="body-compact-01">{{ address.streetAddress }}</span>
        <span class="body-compact-01">{{ address.city }}, {{ address.province.text }}</span>
        <span class="body-compact-01">{{ address.country.text }}</span>
        <span class="body-compact-01">{{ address.postalCode }}</span>
      </div>
    </div>
    <div class="grouping-06">
      <cds-button kind="tertiary" @click.prevent="goToStep(1)">
        <span>Edit address</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
  </div>
  <div class="grouping-05">
    <h3>Contacts</h3>
    <div v-for="(contact, index) in formData.location.contacts" 
        :key="contact.email" 
        class="grouping-07">
      <hr class="divider" 
          v-if="index > 0" />
      <h4 class="review-icon-title">
        <User20 />{{ contact.firstName }} {{ contact.lastName }}
      </h4>
      <div class="grouping-23">
        <span class="body-compact-01" v-if="$features.BCEID_MULTI_ADDRESS">
          {{ contact.locationNames.map((codeDesc) => codeDesc.text).join(', ') }}
        </span>
        <span class="body-compact-01">{{ contact.contactType.text }}</span>
        <span class="body-compact-01">{{ contact.email }}</span>
        <span class="body-compact-01">{{ contact.phoneNumber }}</span>
      </div>
    </div>
    <div class="grouping-06">
      <cds-button kind="tertiary" @click.prevent="goToStep(2)">
        <span>Edit contacts</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
  </div>
</template>
