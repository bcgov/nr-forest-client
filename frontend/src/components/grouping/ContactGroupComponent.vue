<script setup lang="ts">
import { reactive, watch, ref, onMounted, getCurrentInstance } from "vue";
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

const updateContactType = (value: CodeNameType | undefined) => {
  if (value) {
    selectedValue.contactType = { value: value.code, text: value.name };
  }
};

const changePersonalInfoModalActive = ref(false);

const instance = getCurrentInstance();
const session = instance?.appContext.config.globalProperties.$session;

const logoutAndRedirect = () => {
  window.open("https://www.bceid.ca/", "_blank", "noopener");
  session?.logOut();
}
</script>

<template>
  <div class="frame-01">
    <div v-if="id === 0" class="card">
      <div>
        <cds-inline-notification
          v-shadow="2"
          low-contrast="true"
          open="true"
          kind="info"
          hide-close-button="true"
          title="">
          <p class="cds--inline-notification-content">
            <strong>Read-only: </strong>
            If something is incorrect
            <a
              id="change-personal-info-link"
              href="#"
              target="_blank"
              rel="noopener noreferrer"
              @click.prevent="changePersonalInfoModalActive = true"
              >go to BCeID</a
            >
            to correct it and then restart your application.
          </p>
        </cds-inline-notification>
        <br /><br />

        <p class="label-01">Full name</p>
        <p id="fullName_0" class="body-compact-01">
          {{ selectedValue.firstName }} {{ selectedValue.lastName }}
        </p>
      </div>
      <hr class="divider" />
      <div>
        <p class="label-01">Email address</p>
        <p id="email_0" class="body-compact-01">{{ selectedValue.email }}</p>
      </div>
    </div>

    <template v-else>
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
        required
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
        required
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
        required
        :requiredLabel="requiredLabel"
        @empty="validation.email = !$event"
        @error="validation.email = !$event"
      />
    </template>

    <text-input-component
      :id="'phoneNumber_' + id"
      label="Phone number"
      type="tel"
      placeholder="( ) ___-____"
      mask="(###) ###-####"
      v-model="selectedValue.phoneNumber"
      :enabled="true"
      :validations="[
        ...getValidations('location.contacts.*.phoneNumber'),
        submissionValidation(`location.contacts[${id}].phoneNumber`)
      ]"
      required
      :requiredLabel="requiredLabel"
      @empty="validation.phoneNumber = !$event"
      @error="validation.phoneNumber = !$event"
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
      @error="validation.contactType = !$event"
    />

    <multiselect-input-component
      :id="'addressname_' + id"
      v-if="$features.BCEID_MULTI_ADDRESS && !hideAddressNameField"
      label="Location or address name"
      tip="A contact can have more than one address"
      :initial-value="''"
      :model-value="addressList"
      :selectedValues="selectedValue.locationNames?.map((location:CodeDescrType) => location?.text)"
      :validations="[
        ...getValidations('location.contacts.*.locationNames'),
        submissionValidation(`location.contacts[${id}].locationNames`)
      ]"
      required
      :requiredLabel="requiredLabel"
      @update:selected-value="
        selectedValue.locationNames = nameTypesToCodeDescr($event)
      "
      @empty="validation.locationNames = !$event"
      @error="validation.locationNames = !$event"
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

  <cds-modal
    v-if="id === 0"
    id="logout-and-redirect-modal"
    size="md"
    :open="changePersonalInfoModalActive"
    @cds-modal-closed="changePersonalInfoModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading>
        You will be automatically logged out and redirected to BCeID
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body>
      <p>Update your personal information at BCeID and then log back into this application.</p>
      <br />
      <p>Your data will not be saved.</p>
    </cds-modal-body>
    <cds-modal-footer>
      <cds-modal-footer-button kind="secondary" data-modal-close class="cds--modal-close-btn">
        Cancel
      </cds-modal-footer-button>

      <cds-modal-footer-button
        kind="danger"
        class="cds--modal-submit-btn"
        v-on:click="logoutAndRedirect"
      >
        Logout and redirect
        <Logout16 slot="icon" />
      </cds-modal-footer-button>

    </cds-modal-footer>
  </cds-modal>
</template>
