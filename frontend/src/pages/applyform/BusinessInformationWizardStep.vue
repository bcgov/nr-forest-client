<script setup lang="ts">
import { watch, computed, ref, reactive } from "vue";
import type { BusinessSearchResult } from "@/core/CommonTypes";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import RadioInputComponent from "@/components/forms/RadioInputComponent.vue";
import Label from "@/common/LabelComponent.vue";
import Note from "@/common/NoteComponent.vue";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch([formData], () => emit("update:data", formData.value));

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  businessType: false,
  business: false,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

// -- Auto completion --
const showAutoComplete = computed(
  () => formData.value.businessInformation.businessType === "R"
);

const autoCompleteUrl = computed(
  () => `/api/clients/name/${formData.value.businessInformation.businessName}`
);

//Using this as we have to handle the selected result to get
//incorporation number and client type
const autoCompleteResult = ref<BusinessSearchResult>();
watch([autoCompleteResult], () => {
  if (autoCompleteResult.value) {
    validation.business = true;
    formData.value.businessInformation.incorporationNumber =
      autoCompleteResult.value.code;
    formData.value.businessInformation.clientType =
      autoCompleteResult.value.legalType;
  }
});
</script>

<template>
  <h4>Before you begin</h4>
  <Label
    label="1. A registered business must be in good standing with BC Registries"
    id="bil1"
  />

  <Label label="2. You must be able to receive email" id="bil2" />
  <hr />
  <br />

  <h4>Business information</h4>
  <Note note="Enter the business information" />
  <br /><br /><br />

  <radio-input-component
    id="businessType"
    label="Type of business (choose one of these options)"
    :modelValue="[
      {
        value: 'R',
        text: 'I have a BC registered business (corporation, sole proprietorship, society, etc.)',
      },
      { value: 'U', text: 'I have an unregistered sole proprietorship' },
    ]"
    :validations="[]"
    @update:model-value="
      formData = {
        ...formData,
        businessInformation: {
          ...formData.businessInformation,
          businessType: $event ?? '',
        },
      }
    "
    @empty="validation.businessType = !$event"
  />

  <data-fetcher
    v-model:url="autoCompleteUrl"
    :min-length="3"
    :init-value="[]"
    :init-fetch="false"
    #="{ content }"
  >
    <AutoCompleteInputComponent
      v-if="showAutoComplete"
      id="business"
      label="B.C. registered business name"
      tip="The name must be exactly the same as in BC Registries"
      v-model="formData.businessInformation.businessName"
      :contents="content"
      :validations="[]"
      @update:selected-value="autoCompleteResult = $event"
    />
  </data-fetcher>
</template>
