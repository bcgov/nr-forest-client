<template>
  <b-col xs="10" sm="10" md="11" lg="11" 
         style="border: 1px solid lightgray; padding: 0.7rem !important;">

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <Label label="Country" 
               :required="true" />
      </div>

      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <b-form-select :id="'countryId' + id" 
                       v-model="country" 
                       :options="countryCodes" />
        <ValidationMessages :fieldId="`location.addresses[${id}].country`" 
                            :validationMessages="validationMessages" />
      </div>

    </b-row>

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <Label label="Street Address" 
               :required="true" />
      </div>
      <div>
        <b-form-input :id="'streetAddressId' + id" 
                      v-model="address.streetAddress">
        </b-form-input>
        <ValidationMessages :fieldId="`location.addresses[${id}].streetAddress`" 
                            :validationMessages="validationMessages" />
      </div>
    </b-row>

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <Label label="City" 
               :required="true" />
      </div>
      <div>
        <b-form-input :id="'cityId' + id" 
                      v-model="address.city">
        </b-form-input>
        <ValidationMessages :fieldId="`location.addresses[${id}].city`" 
                            :validationMessages="validationMessages" />
      </div>
    </b-row>

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <span v-if="address.country?.value == 'US'">
          <Label label="State" 
                 :required="true" />
        </span>
        <span v-if="address.country?.value != 'US'">
          <Label label="Province" 
                 :required="true" />
        </span>
      </div>
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <b-form-select :id="'provinceId' + id" v-model="province" 
                       :options="provinceCodes" />
        <ValidationMessages :fieldId="`location.addresses[${id}].province`" 
                            :validationMessages="validationMessages" />
      </div>
    </b-row>

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <span v-if="address.country?.value == 'US'">
          <Label label="Zip code" 
                 :required="true" />
        </span>
        <span v-if="address.country?.value != 'US'">
          <Label label="Postal code" 
                :required="true" />
        </span>
      </div>
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <span v-if="address.country?.value == 'US'">
          <b-form-input :id="'postalCodeId' + id" 
                        v-model="address.postalCode"
                        v-mask="'#####'">
          </b-form-input>
        </span>
        <span v-if="address.country?.value == 'CA'">
          <b-form-input :id="'postalCodeId' + id" 
                        v-model="address.postalCode"
                        v-mask="'A#A#A#'">
          </b-form-input>
        </span>
        <span v-if="address.country?.value != 'US' && address.country?.value != 'CA'">
          <b-form-input :id="'postalCodeId' + id" 
                        v-model="address.postalCode"
                        :maxlength="10">
          </b-form-input>
        </span>
        <ValidationMessages :fieldId="`location.addresses[${id}].postalCode`" 
                            :validationMessages="validationMessages" />
      </div>
    </b-row>

    <br />

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <CollapseCard id="contactsId" 
                      title="Business contacts" 
                      defaultOpen>

          <span>
            <strong>Enter an authorized person you want to add for this address</strong>
          </span>

          <br /><br />
          <ContactSectionComponent id="contactListId" 
                                  :index="id"
                                  :contacts="address.contacts"
                                  :validationMessages="validationMessages" />
          <ValidationMessages :fieldId="`location.addresses[${id}].contacts`"
                              :validationMessages="validationMessages" />
        </CollapseCard>
      </div>
    </b-row>

  </b-col>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue';
  import Label from "@/common/LabelComponent.vue";
  import { conversionFn } from '@/services/FetchService';
  import { addNewContact, useFetchTo } from '@/services/ForestClientService';
  import ValidationMessages from "@/common/ValidationMessagesComponent.vue";
  import ContactSectionComponent from '@/pages/applyclientnumber/ContactSectionComponent.vue';
  import type { CodeDescrType, ValidationMessageType } from '@/core/CommonTypes';
  const props = defineProps({
    address: {
      type: Object,
      required: true
    },
    id: {
      type: Number,
      required: true
    },
    validationMessages: {
      type: Array<ValidationMessageType>,
      required: true
    },
    //We don't need to fetch it here as it is the same, so we receive it
    countryCodes: {
      type: Array<CodeDescrType>,
      required: true
    }
  });

  //This will hold the original values temporary
  const provinceCodesOriginal = ref([]);
  //The list of province codes will be computed based on the value received from above
  const provinceCodes = computed(() => {
    return provinceCodesOriginal.value.map(conversionFn);
  });

  //Both constants are temporary, to hold the data for the dropdown
  const country = ref({});
  const province = ref({});

  //We watch the props due to possible after loading addresses when selecting client
  watch([props], () => {
    //Those ifs are for safeguard in case of no data loaded
    if (props.address) {
      if (props.address.country) {
        country.value = props.address.country;
      }
      if (props.address.province) {
        province.value = props.address.province;
      }
      if(!props.address.contacts){
        props.address.contacts = addNewContact([]);
      }
    }
  });

  //We watch the country change to load the provinces
  watch([country], () => {
    if (country.value && country.value.value) {
      useFetchTo(`/api/clients/activeCountryCodes/${country.value.value}?page=0&size=250`, provinceCodesOriginal, { method: 'get' });
      //Set the value on the props
      props.address.country = country.value;
    }
  });

  //We watch the province update to set the value on the props
  watch([province], () => { 
    props.address.province = province.value 
  });

</script>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "AddressComponent"
});
</script>
