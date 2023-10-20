<template>
<!--   <div class="cds--css-grid">
    <div class="cds--col-span-4">Span 4 columns</div>
    <div class="cds--col-span-2">Span 2 columns</div>
  </div> -->
  <div class="form-header">
    <div class="form-header-title">
      <span class="heading-05" data-scroll="top">
        New client application
      </span>
    </div>
  </div>
   
  <div class="form-steps-section">
    <span class="heading-04" data-scroll="scroll-0">
      Personal information
    </span>
    <p class="body-compact-01">
      Review the personal information below. It’s from your BC Services card.<br />
      We use it to know who we're giving a number to and for communicating with clients. 
    </p>

    <div class="grouping-tbd">
      <div class="cds--css-grid">
        <div class="cds--css-grid-column cds--sm:col-span-3">
          <p class="body-compact-01">Full name</p>
          <p>{{ formData.businessInformation.businessName }}</p>
        </div>
        <div class="cds--css-grid-column cds--sm:col-span-3">
          <p class="body-compact-01">Date of birth</p>
          <p>{{ formData.businessInformation.birthDate }}</p>
        </div>
        <div class="cds--css-grid-column cds--sm:col-span-3">
          <p class="body-compact-01">Email address</p>
          <p>{{ formData.location.contacts[0].email }}</p>
        </div>
        <div class="cds--css-grid-column cds--sm:col-span-3">
          <p class="body-compact-01">Address</p>
          <p>
            {{ formData.businessInformation.address.streetAddress }} <br />
            {{ formData.businessInformation.address.city }}, {{ formData.businessInformation.address.province.value }} <br />
            {{ formData.businessInformation.address.country.text }} <br />
            {{ formData.businessInformation.address.postalCode }}
          </p>
        </div>
      </div>
    </div>

    TODO

    <hr class="divider" />

    <span class="heading-04" data-scroll="scroll-0">
      Contact information
    </span>
    <p class="body-compact-01">
      We need your phone number to communicate with you.
    </p>

    <div class="grouping-01">
      <text-input-component
        id="phoneNumberId"
        label="Phone number"
        placeholder=""
        :enabled="true" 
        v-model="formData.location.contacts[0].phoneNumber"
        mask="(###) ###-####"
      />
    </div>

    <p class="body-compact-01">
      You can add contacts to the account. For example, a person you want to give or receive information on your behalf.
    </p>
    
    <cds-button
      kind="tertiary"
      @click.prevent="addContact"
      v-if="formData.location.contacts.length < 5">
      <span>Add another contact</span>
      <Add16 slot="icon" />
    </cds-button>

    {{ formData.location.contacts }}
  </div>

</template>


<script setup lang="ts">
import { reactive, watch, ref, getCurrentInstance, computed, onMounted } from "vue";
import { newFormDataDto, emptyContact } from "@/dto/ApplyClientNumberDto";
import { BusinessTypeEnum, ClientTypeEnum, LegalTypeEnum } from "@/dto/CommonTypesDto";
import { useFetchTo } from "@/composables/useFetch";
import { useEventBus } from "@vueuse/core";
import { useFocus } from "@/composables/useFocus";
import { codeConversionFn } from "@/services/ForestClientService";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType, ModalNotification } from "@/dto/CommonTypesDto";
import Add16 from "@carbon/icons-vue/es/add/16";

const instance = getCurrentInstance();
const session = instance?.appContext.config.globalProperties.$session;
const { setFocusedComponent } = useFocus();

const submitterContact: Contact = {
  locationNames: [],
  contactType: { value: "", text: "" },
  phoneNumber: "",
  firstName: session?.user?.firstName ?? "",
  lastName: session?.user?.lastName ?? "",
  email: session?.user?.email ?? "",
};

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() });

const formatter = new Intl.DateTimeFormat('en-US', {
  year: 'numeric',
  month: 'short',
  day: 'numeric',
});

const birthDate = new Date(session?.user?.birthDate ?? "");
const formattedDate = formatter.format(birthDate);


//---- Form Data ----//
let formData = reactive<FormDataDto>({
  ...formDataDto.value,
  businessInformation :{
    businessType: BusinessTypeEnum.U.toString(),
    legalType: LegalTypeEnum.SP.toString(),
    clientType: ClientTypeEnum.I.toString(),
    incorporationNumber: '',
    businessName: session?.user?.name ?? "",
    goodStandingInd: "Y",
    birthDate: formattedDate,
    address: session?.user?.address,
  },
  location: {
    addresses: [],
    contacts: [submitterContact],
  },
});

const receviedCountry = ref({} as CodeNameType);

useFetchTo(`/api/clients/getCountryByCode/${session?.user?.address?.country?.code}`, 
            receviedCountry);

const country = computed(
  () => {
    return codeConversionFn(receviedCountry.value);
  }
);

watch(country, (newValue) => {
  formData.businessInformation.address = {
    ...formData.businessInformation.address,
    country: { value: newValue.value, text: newValue.text },
    province: codeConversionFn(formData.businessInformation.address.province),
    postalCode: formData.businessInformation.address.postalCode.replace(/\s/g, ""),
    locationName: "Mailing address",
  };

  formData.location.addresses.push(formData.businessInformation.address);
});

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));
const addContact = (autoFocus = true) => {
  const newLength = formData.location.contacts.push(
    JSON.parse(JSON.stringify(emptyContact))
  );
  if (autoFocus) {
    const focusIndex = newLength - 1;
    setFocusedComponent(`addressname_${focusIndex}`);
  }
  return newLength;
};

const uniqueValues = isUniqueDescriptive();

const removeContact = (index: number) => () => {
  updateContact(undefined, index);
  delete validation[index];
  uniqueValues.remove("Name", index + "");
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const revalidate = ref(false);

const updateContact = (value: Contact | undefined, index: number) => {
  if (index < formData.location.contacts.length) {
    if (value) formData.location.contacts[index] = value;
    else {
      const contactCopy: Contact[] = [...formData.location.contacts];
      contactCopy.splice(index, 1);
      formData.location.contacts = contactCopy;
    }
  }
  revalidate.value = !revalidate.value;
};

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const updateValidState = (index: number, valid: boolean) => {
  if (validation[index] !== valid) {
    validation[index] = valid;
  }
};

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();


watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const bus = useEventBus<ModalNotification>("modal-notification");

const handleRemove = (index: number) => {
  const selectedContact =
    formData.location.contacts[index].firstName.length !== 0
      ? `${formData.location.contacts[index].firstName} ${formData.location.contacts[index].lastName}`
      : "Contact #" + index;
  bus.emit({
    message: selectedContact,
    kind: "Contact deleted",
    toastTitle: "The additional contact was deleted",
    handler: removeContact(index),
    active: true,
  });
};

//onMounted(() => setFocusedComponent("addressname_0", 800));

defineExpose({
  addContact,
});
</script>
