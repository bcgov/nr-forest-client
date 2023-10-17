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
      You can add contacts to the aacount. For example, a person you want to give or receive information on your behalf.
    </p>
    
    TODO
    
  </div>
</template>


<script setup lang="ts">
import { reactive, watch, toRef, ref, getCurrentInstance, computed } from "vue";
import { newFormDataDto } from "@/dto/ApplyClientNumberDto";
import { BusinessTypeEnum, ClientTypeEnum, CodeNameType, LegalTypeEnum } from "@/dto/CommonTypesDto";
import { useFetchTo } from "@/composables/useFetch";
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import { codeConversionFn } from "@/services/ForestClientService";

const instance = getCurrentInstance();
const session = instance?.appContext.config.globalProperties.$session;

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
</script>
