<script setup lang="ts">
import { watch, ref, onMounted, computed, reactive } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tooltip/index";

// Composables
import { useEventBus } from "@vueuse/core";
import { useFocus } from "@/composables/useFocus";
// Types
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType } from "@/dto/CommonTypesDto";
// @ts-ignore
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Enterprise20 from "@carbon/icons-vue/es/enterprise/20"
import LocationStar20 from "@carbon/icons-vue/es/location--star/20"
import Location20 from "@carbon/icons-vue/es/location/20"
import User20 from "@carbon/icons-vue/es/user/20"
import Information16 from "@carbon/icons-vue/es/information/16";

import { useFetchTo } from "@/composables/useFetch";
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
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
  `/api/codes/client-types/${formData.value.businessInformation.clientType}`,
  receviedClientType
);

const clientType = computed(() => {
  return codeConversionFn(receviedClientType.value);
});

//We emit valid here because there is nothing else to be done here apart from showing information
emit("valid", true);

const { setFocusedComponent } = useFocus();

onMounted(() => {
  revalidateBus.emit();
  setFocusedComponent("focus-3", 0);
});

const nameError = ref<string | undefined>("");

// Validations
const validation = reactive<Record<string, boolean>>({
  notes: true
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );

watch([validation], () => {
  emit("valid", checkValid())
});

emit("valid", false);
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
      </div>
      <div class="grouping-22" 
          v-if="clientType.value === 'I'">
        <div class="grouping-22-item">
          <p class="label-01">{{ formData.businessInformation.identificationType.text }}</p>
          <p class="body-compact-01">{{ formData.businessInformation.clientIdentification }}</p>
        </div>
        <div class="grouping-22-item">
          <p class="label-01">Birthdate</p>
          <p class="body-compact-01">{{ formData.businessInformation.birthdate }}</p>
        </div>
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

  <div class="grouping-09">
    <h3>Notes</h3>
    <p class="body-01 light-theme-text-text-primary">
      Is there anything else you need to add about this client?
    </p>
    <br />
    <textarea-input-component
      id="notes"
      label="Notes"
      enable-counter
      :max-count="4000"
      :rows="7"
      placeholder=""
      v-model="formData.businessInformation.notes"
      :enabled="true"
      :validations="[
        ...getValidations('businessInformation.notes'),
        submissionValidation('businessInformation.notes'),
      ]"
      :error-message="nameError"
      @empty="validation.notes = true"
      @error="validation.notes = !$event"
    >
      <div slot="label-text" class="label-with-icon">
        <div class="cds-text-input-label">
          <span>Notes</span>
        </div>
        <cds-tooltip>
          <Information16 />
          <cds-tooltip-content>
            For example, any information about the client, their locations or specific instructions for contacting them
          </cds-tooltip-content>
        </cds-tooltip>
      </div>
    </textarea-input-component>
  </div>
</template>
