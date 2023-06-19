<script setup lang="ts">
import { reactive, watch, ref } from "vue";
import type { CodeDescrType, CodeNameType } from "@/core/CommonTypes";
import type { Contact } from "@/dto/ApplyClientNumberDto";
import { isNotEmpty,isEmail, isPhoneNumber,isMaxSize,isMinSize } from "@/helpers/validators/GlobalValidators";

//Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Contact;
  enabled?: boolean;
  roleList: Array<CodeNameType>;
  addressList: Array<CodeNameType>;
  validations: Array<Function>;
  revalidate?: boolean;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:model-value", value: Contact | undefined): void;
}>();

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Contact>(props.modelValue);
const validateData = props.validations[0]('Name',props.id+'');
const error = ref<string | undefined>("");

//Watch for changes on the input
watch([selectedValue], () => {
  error.value = validateData(`${selectedValue.firstName} ${selectedValue.lastName}`);  
  emit("update:model-value", selectedValue);
});

watch(() => props.revalidate,() =>{
  error.value = validateData(`${selectedValue.firstName} ${selectedValue.lastName}`);
});

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

const nameTypeToCodeDescr = (value: CodeNameType | undefined) : CodeDescrType =>{
  if(value)
    return { value: value.code, text: value.name };
  return {value:"",text:""};
}

const nameTypesToCodeDescr = (values: CodeNameType[] | undefined) : CodeDescrType[] =>{
  if(values) return values.map(nameTypeToCodeDescr);  
  return [];
}

</script>

<template>
  <multiselect-input-component
    :id="'address_'+id"
    label="Address name"
    tip="Select an address name for the contact. A contact can have more than one address"
    :initial-value="''"
    :model-value="addressList"
    :selectedValues="selectedValue.locationNames?.map((location:CodeDescrType) => location?.value)"
    :validations="[isNotEmpty]"
    @update:selected-value="selectedValue.locationNames = nameTypesToCodeDescr($event)"
    @empty="validation.locationNames = !$event"
  />

  <dropdown-input-component
    :id="'role_'+id"
    label="Primary role"
    tip="Choose the primary role for this contact"
    :initial-value="selectedValue.contactType.value"
    :model-value="roleList"    
    :validations="[]"
    @update:selected-value="selectedValue.contactType = nameTypeToCodeDescr($event)"
    @empty="validation.contactType = !$event"
  />

  <text-input-component
    :id="'firstName_'+id"
    label="First name"
    placeholder="First name"
    v-model="selectedValue.firstName"
    :validations="[]"
    :enabled="enabled"
    :error-message="error"
    @empty="validation.firstName = !$event"
  />

  <text-input-component
    :id="'lastName_'+id"
    label="Last name"
    placeholder="Last name"
    v-model="selectedValue.lastName"
    :validations="[]"
    :enabled="enabled"
    :error-message="error"
    @empty="validation.lastName = !$event"
  />

  <text-input-component
    :id="'email_'+id"
    label="Email address"
    placeholder="Email"
    v-model="selectedValue.email"
    :validations="[isNotEmpty,isEmail]"
    :enabled="enabled"
    @empty="validation.email = !$event"
  />

  <text-input-component
    :id="'phoneNumber_'+id"
    label="Phone number"
    placeholder="( ) ___-____"
    v-model="selectedValue.phoneNumber"
    :enabled="true"
    :validations="[isNotEmpty, isPhoneNumber]"
    @empty="validation.phoneNumber = !$event"
  />
</template>
