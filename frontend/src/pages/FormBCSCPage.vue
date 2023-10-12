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

   
    <div class="grouping-01">
      <p class="heading-02">
        Full name
      </p>
      <p class="body-compact-01">
        {{ formData.businessInformation.businessName }}
      </p>
    </div>

    <div class="grouping-01">
      <p class="heading-02">
        Date of birth
      </p>
      <p class="body-compact-01">
        {{ formData.businessInformation.birthDate }}
      </p>
    </div>

    <div class="grouping-01">
      <p class="heading-02">
        Address
      </p>
      <p class="body-compact-01">
        TBD => {{ formData.businessInformation.address }} <br />
        {{ formData.businessInformation.address.streetAddress }} <br />
        {{ formData.businessInformation.address.city }}, {{ formData.businessInformation.address.province.value }} <br />
        {{ formData.businessInformation.address.country.text }} <br />
        {{ formData.location.addresses }}
      </p>
    </div>

    <div class="grouping-06">
      <!-- <cds-button kind="tertiary" @click.prevent="goToStep(0)">
        <span>Edit business information</span>
        <Edit16 slot="icon" />
      </cds-button> -->
    </div>

    
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
    locationName: "Mailing address",
  };

  formData.location.addresses.push(formData.businessInformation.address);
})

</script>
