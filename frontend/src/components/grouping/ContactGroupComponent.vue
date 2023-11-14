<script setup lang="ts">
import { reactive, watch, ref, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Importing composables
import { useFocus } from "@/composables/useFocus";
// Importing types
import type { CodeDescrType, CodeNameType } from "@/dto/CommonTypesDto";
import type { Contact } from "@/dto/ApplyClientNumberDto";
// Importing validatons
import { getValidations } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// @ts-ignore
import Delete16 from "@carbon/icons-vue/es/trash-can/16";

//Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Contact;
  enabled?: boolean;
  roleList: Array<CodeNameType>;
  addressList: Array<CodeNameType>;
  validations: Array<Function>;
  revalidate?: boolean;
  hideAddressNameField?: boolean;
  requiredLabel?: boolean;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:model-value", value: Contact | undefined): void;
  (e: "remove", value: number): void;
}>();

const { setFocusedComponent } = useFocus();
const noValidation = (value: string) => "";

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Contact>(props.modelValue);
const validateData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0]("Name", props.id + "");
const error = ref<string | undefined>("");

const uniquenessValidation = () => {
  error.value = validateData(
    `${selectedValue.firstName} ${selectedValue.lastName}`
  );
};

//Watch for changes on the input
watch([selectedValue], () => {
  uniquenessValidation();
  emit("update:model-value", selectedValue);
});

watch(
  () => props.revalidate,
  () => uniquenessValidation(),
  { immediate: true }
);

//Validations
const validation = reactive<Record<string, boolean>>({});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

//Data conversion

const nameTypeToCodeDescr = (
  value: CodeNameType | undefined
): CodeDescrType => {
  if (value) return { value: value.code, text: value.name };
  return { value: "", text: "" };
};

const nameTypesToCodeDescr = (
  values: CodeNameType[] | undefined
): CodeDescrType[] => {
  if (values) return values.map(nameTypeToCodeDescr);
  return [];
};

onMounted(() => {
  setFocusedComponent(`address_${props.id}`, 800);
});

const updateContactType = (value: CodeNameType | undefined) => {
  if (value) {
    selectedValue.contactType = { value: value.code, text: value.name };
  }
};
</script>

<template>
  <div class="frame-01">
  <multiselect-input-component
    :id="'addressname_' + id"
    v-if="!hideAddressNameField"
    label="Address name"
    tip="Select an address name for the contact. A contact can have more than one address"
    :initial-value="''"
    :model-value="addressList"
    :selectedValues="selectedValue.locationNames?.map((location:CodeDescrType) => location?.text)"
    :validations="[
      ...getValidations('location.contacts.*.locationNames'),
      submissionValidation(`location.contacts[${id}].locationNames`)
    ]"
    :requiredLabel="requiredLabel"
    @update:selected-value="
      selectedValue.locationNames = nameTypesToCodeDescr($event)
    "
    @empty="validation.locationNames = !$event"
  />

  <dropdown-input-component
    :id="'role_' + id"
    label="Primary role"
    tip="Choose the primary role for this contact"
    :initial-value="selectedValue.contactType?.text"
    :model-value="roleList"
    :validations="[
      ...getValidations('location.contacts.*.contactType.text'),
      submissionValidation(`location.contacts[${id}].contactType`)
    ]"
    :requiredLabel="requiredLabel"
    @update:selected-value="updateContactType($event)"
    @empty="validation.contactType = !$event"
  />

  <text-input-component
    :id="'firstName_' + id"
    label="First name"
    placeholder=""
    v-model="selectedValue.firstName"
    :validations="[
      ...getValidations('location.contacts.*.firstName'),
      submissionValidation(`location.contacts[${id}].firstName`)
    ]"
    :enabled="enabled"
    :requiredLabel="requiredLabel"
    :error-message="error"
    @empty="validation.firstName = !$event"
    @error="validation.firstName = !$event"
  />

  <text-input-component
    :id="'lastName_' + id"
    label="Last name"
    placeholder=""
    v-model="selectedValue.lastName"
    :validations="[
      ...getValidations('location.contacts.*.lastName'),
      submissionValidation(`location.contacts[${id}].lastName`)
    ]"
    :enabled="enabled"
    :requiredLabel="requiredLabel"
    :error-message="error"
    @empty="validation.lastName = !$event"
    @error="validation.lastName = !$event"
  />

  <text-input-component
    :id="'email_' + id"
    label="Email address"
    placeholder=""
    v-model="selectedValue.email"
    :validations="[
      ...getValidations('location.contacts.*.email'),
      submissionValidation(`location.contacts[${id}].email`)
    ]"
    :enabled="enabled"
    :requiredLabel="requiredLabel"
    @empty="validation.email = !$event"
    @error="validation.email = !$event"
  />

  <text-input-component
    :id="'phoneNumber_' + id"
    label="Phone number"
    placeholder="( ) ___-____"
    mask="(###) ###-####"
    v-model="selectedValue.phoneNumber"
    :enabled="true"
    :validations="[
      ...getValidations('location.contacts.*.phoneNumber'),
      submissionValidation(`location.contacts[${id}].phoneNumber`)
    ]"
    :requiredLabel="requiredLabel"
    @empty="validation.phoneNumber = !$event"
    @error="validation.phoneNumber = !$event"
  />

  <div class="grouping-06">
  <cds-button
    v-if="id > 0"
    :id="'deleteContact_' + id"
    kind="danger--tertiary"
    @click.prevent="emit('remove', id)"
  >
    <span>Delete contact</span>
    <Delete16 slot="icon" />
  </cds-button>
</div>
</div>
</template>
