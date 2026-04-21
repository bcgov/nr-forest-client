<script setup lang="ts">
import { reactive, watch, ref, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Importing composables
import { useFocus } from "@/composables/useFocus";
// Importing types
import type { CodeDescrType, CodeNameType } from "@/dto/CommonTypesDto";
import type { Contact } from "@/dto/ApplyClientNumberDto";
// Importing validatons
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// @ts-ignore
import Delete16 from "@carbon/icons-vue/es/trash-can/16";
import { formatLocation, getContactDescription } from "@/services/ForestClientService";

//Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Contact;
  enabled?: boolean;
  roleList: Array<CodeNameType>;
  addressList: Array<CodeNameType>;
  validations: Array<Function>;
  revalidate?: boolean;
  readOnlyName?: boolean;
  singleInputForName?: boolean;
  requiredLabel?: boolean;
  hideDeleteButton?: boolean;
  showLocationCode?: boolean;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:model-value", value: Contact | undefined): void;
  (e: "remove", id: number): void;
}>();

const { safeSetFocusedComponent } = useFocus();
const noValidation = (value: string) => "";

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Contact>(props.modelValue);
const validateData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0]("Name", props.id + "");
const error = ref<string | undefined>("");

const uniquenessValidation = () => {
  const fullName = props.singleInputForName
    ? selectedValue.fullName
    : `${selectedValue.firstName} ${selectedValue.lastName}`;
  error.value = validateData(fullName);
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
const validation = reactive<Record<string, boolean>>({
  contactType: false,
  firstName: props.singleInputForName || !!selectedValue.firstName,
  lastName: props.singleInputForName || !!selectedValue.lastName,
  fullName: !props.singleInputForName || !!selectedValue.fullName,
  phoneNumber: true,
  secondaryPhoneNumber: true,
  faxNumber: true,
  email: true,
  locationNames: false,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

//Data conversion

const nameTypeToCodeDescr = (value: CodeNameType | undefined): CodeDescrType => {
  if (value) {
    const address = props.showLocationCode
      ? props.addressList.find((address) => address.code === value.code)
      : value;
    return {
      value: address.code,
      text: address.name,
    };
  }
  return { value: "", text: "" };
};

const nameTypesToCodeDescr = (
  values: CodeNameType[] | undefined
): CodeDescrType[] => {
  if (values) return values.map(nameTypeToCodeDescr);
  return [];
};

const updateContactType = (value: CodeNameType | undefined) => {
  if (value) {
    selectedValue.contactType = { value: value.code, text: value.name };
  }
};

// If props.showLocationCode, location descriptions will include the code (code - name).
const addressTitleList = computed<CodeNameType[]>(() =>
  props.addressList?.map((address) => ({
    code: address.code,
    name: props.showLocationCode ? formatLocation(address.code, address.name) : address.name,
  })),
);

const localFormatLocation = (location: CodeDescrType) => {
  if (!location) {
    return undefined;
  }
  if (props.showLocationCode) {
    return formatLocation(location.value, location.text);
  }
  return location.text;
};
</script>

<template>
  <div class="frame-01">
    <text-input-component
      :id="'fullName_' + id"
      label="Full name"
      placeholder=""
      autocomplete="off"
      v-bind:model-value="selectedValue.firstName + ' ' + selectedValue.lastName"
      :validations="[]"
      :enabled="false"
      required
      :requiredLabel="requiredLabel"
      :error-message="error"
      v-if="readOnlyName"
    />

    <template v-if="!readOnlyName">
      <template v-if="singleInputForName">
        <text-input-component
          :id="'fullName_' + id"
          label="Full name"
          placeholder=""
          autocomplete="off"
          v-model="selectedValue.fullName"
          :validations="[
            ...getValidations('location.contacts.*.fullName'),
            submissionValidation(`location.contacts[${id}].fullName`),
          ]"
          :enabled="true"
          required
          required-label
          :error-message="error"
          @empty="validation.fullName = !$event"
          @error="validation.fullName = !$event"
        />
      </template>
      <template v-else>
        <text-input-component
          :id="'firstName_' + id"
          label="First name"
          placeholder=""
          autocomplete="off"
          v-model="selectedValue.firstName"
          :validations="[
            ...getValidations('location.contacts.*.firstName'),
            submissionValidation(`location.contacts[${id}].firstName`)
          ]"
          :enabled="true"
          required
          required-label
          :error-message="error"
          @empty="validation.firstName = !$event"
          @error="validation.firstName = !$event"
        />

        <text-input-component
          :id="'lastName_' + id"
          label="Last name"
          placeholder=""
          autocomplete="off"
          v-model="selectedValue.lastName"
          :validations="[
            ...getValidations('location.contacts.*.lastName'),
            submissionValidation(`location.contacts[${id}].lastName`)
          ]"
          :enabled="true"
          required
          required-label
          :error-message="error"
          @empty="validation.lastName = !$event"
          @error="validation.lastName = !$event"
        />
      </template>
    </template>

    <combo-box-input-component
      :id="'role_' + id"
      label="Contact type"
      tip=""
      :initial-value="selectedValue.contactType?.text"
      :model-value="roleList"
      :validations="[
        ...getValidations('location.contacts.*.contactType.text'),
        submissionValidation(`location.contacts[${id}].contactType`)
      ]"
      required
      required-label
      @update:selected-value="updateContactType($event)"
      @empty="validation.contactType = !$event"
      @error="validation.contactType = !$event"
    />

    <multiselect-input-component
      :id="'addressname_' + id"
      label="Associated locations"
      tip="A contact can have more than one location"
      :initial-value="selectedValue.locationNames.join(',')"
      :model-value="addressTitleList"
      :selectedValues="selectedValue.locationNames?.map(localFormatLocation)"
      :validations="[
        ...getValidations('location.contacts.*.locationNames'),
        submissionValidation(`location.contacts[${id}].locationNames`)
      ]"
      required
      required-label
      @update:selected-value="selectedValue.locationNames = nameTypesToCodeDescr($event)"
      @empty="validation.locationNames = !$event"
      @error="validation.locationNames = !$event"
    />

    <text-input-component
      :id="'emailAddress_' + id"
      label="Email address"
      placeholder=""
      autocomplete="off"
      v-model="selectedValue.email"
      :validations="[
        ...getValidations('location.contacts.*.email'),
        submissionValidation(`location.contacts[${id}].email`)
      ]"
      :enabled="true"
      @empty="validation.email = true"
      @error="validation.email = !$event"
    />

    <div class="horizontal-input-grouping">
      <text-input-component
        :id="'businessPhoneNumber_' + id"
        label="Primary phone number"
        type="tel"
        autocomplete="off"
        placeholder="( ) ___-____"
        mask="(###) ###-####"
        v-model="selectedValue.phoneNumber"
        :enabled="true"
        :validations="[
          ...getValidations('location.contacts.*.phoneNumber'),
          submissionValidation(`location.contacts[${id}].phoneNumber`)
        ]"
        @empty="validation.phoneNumber = true"
        @error="validation.phoneNumber = !$event"
      />

      <text-input-component
        :id="'secondaryPhoneNumber_' + id"
        label="Secondary phone number"
        type="tel"
        autocomplete="off"
        placeholder="( ) ___-____"
        mask="(###) ###-####"
        v-model="selectedValue.secondaryPhoneNumber"
        :enabled="true"
        :validations="[
          ...getValidations('location.contacts.*.secondaryPhoneNumber'),
          submissionValidation(`location.contacts[${id}].secondaryPhoneNumber`)
        ]"
        @empty="validation.secondaryPhoneNumber = true"
        @error="validation.secondaryPhoneNumber = !$event"
      />

      <text-input-component
        :id="'faxNumber_' + id"
        label="Fax"
        type="tel"
        autocomplete="off"
        placeholder="( ) ___-____"
        mask="(###) ###-####"
        v-model="selectedValue.faxNumber"
        :enabled="true"
        :validations="[
          ...getValidations('location.contacts.*.faxNumber'),
          submissionValidation(`location.contacts[${id}].faxNumber`)
        ]"
        @empty="validation.faxNumber = true"
        @error="validation.faxNumber = !$event"
      />
    </div>

    <div class="grouping-06" v-if="!hideDeleteButton && id > 0">
      <cds-button
        :id="'deleteContact_' + id"
        :danger-descriptor="`Delete contact &quot;${getContactDescription(
          selectedValue,
          id,
        )}&quot;`"
        kind="danger--tertiary"
        @click.prevent="emit('remove', id)"
      >
        <span>Delete contact</span>
        <Delete16 slot="icon" />
      </cds-button>
    </div>
  </div>

</template>
