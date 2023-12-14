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

const receviedClientType = ref({} as CodeNameType);

useFetchTo(
  `/api/clients/getClientTypeByCode/${formData.value.businessInformation.clientType}`,
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
    <label class="heading-03">Business information</label>
    <div class="grouping-22">
      <div class="grouping-01">
        <p class="heading-02 review-icon-title">
          <Enterprise20 />
          {{ formData.businessInformation.businessName }}
        </p>
        <p class="body-compact-01" id="clientTypeId">
          {{ clientType.text }}
        </p>
      </div>
      <p class="body-compact-01" id="birthdate" v-if="formData.businessInformation.birthdate">
        Proprietor’s birthdate: {{ formData.businessInformation.birthdate }}
      </p>
    </div>
    <div class="grouping-06">
      <cds-button kind="tertiary" @click.prevent="goToStep(0)">
        <span>Edit business information</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
  </div>
  <div class="grouping-05">
    <label class="heading-03">Address</label>
    <div v-for="(address, index) in formData.location.addresses" 
        :key="address.locationName" 
        class="grouping-07">
      <hr class="divider" v-if="index > 0" />
      <span class="heading-02 review-icon-title">
        <LocationStar20 v-if="index === 0" />
        <Location20 v-else />{{ address.locationName }}
      </span>
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
    <label class="heading-03">Contacts</label>
    <div v-for="(contact, index) in formData.location.contacts" 
        :key="contact.email" 
        class="grouping-07">
      <hr class="divider" 
          v-if="index > 0" />
      <span class="heading-02 review-icon-title">
        <User20 />{{ contact.firstName }} {{ contact.lastName }}
      </span>
      <div class="grouping-23">
        <span class="body-compact-01">
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
