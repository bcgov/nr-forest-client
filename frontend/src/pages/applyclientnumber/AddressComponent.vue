<template>

  <span v-if="id == 0">
    <h5><strong>Mailing address</strong></h5>
    <br />
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
      This is the primary address where you'll receive mail.
    </div>
    <br />
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
      If you'd like another location, for example a seed orchard or a street address different from your mailing address, select the "+ Add another address" button below. 
    </div>
    <br />
  </span>

  <span v-if="id > 0">
    <h5><strong>Additional address</strong></h5>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
      Provide a name to identify your additional address.
    </div>
    <br />
  </span>

  <b-col xs="10" sm="10" md="11" lg="11" 
         style="border: 1px solid lightgray; padding: 0.7rem !important;">

    <b-row v-if="id > 0" class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <Label label="Location or address name" 
               :required="true" />
      </div>

      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <b-form-input :id="'todoId' + id" >
        </b-form-input>

        <!-- <ValidationMessages :fieldId="`location.addresses[${id}].todo`" 
                            :validationMessages="validationMessages"
                            :modelValue="country" /> -->
      </div>
    </b-row>

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
                            :validationMessages="validationMessages"
                            :modelValue="country" />
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
                            :validationMessages="validationMessages"
                            :modelValue="address.streetAddress" />
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
                            :validationMessages="validationMessages"
                            :modelValue="address.city" />
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
        <b-form-select :id="'provinceId' + id" 
                       v-model="province" 
                       :options="provinceCodes" />
        <ValidationMessages :fieldId="`location.addresses[${id}].province`" 
                            :validationMessages="validationMessages"
                            :modelValue="province" />
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
                            :validationMessages="validationMessages"
                            :modelValue="address.postalCode" />
      </div>
    </b-row>

    <br />

    <b-row class="rowSpace">
      <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <CollapseCard id="contactsId" 
                      title="Business contacts" 
                      defaultOpen>

          <span v-if="id == 0">
            <strong>Add authorized people to your account</strong>
            <br /><br />
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
              Your first name, last name and email address are from your BCeID. If they’re incorrect, go to BCeID to update them before submitting your form. Be sure to add your phone number, location and role.
            </div>
            <br />
            To add another contact to the account, select the "+ Add another contact" button below.
          </span>

          <ContactSectionComponent id="contactListId" 
                                  :index="id"
                                  :contacts="address.contacts"
                                  :validationMessages="validationMessages" />

          <ValidationMessages :fieldId="`location.addresses[${id}].contacts`"
                              :validationMessages="validationMessages"
                              :modelValue="address.contacts[0]" />
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
  const country = ref({ value: "", text: "" } as CodeDescrType);
  const province = ref({ value: "", text: "" } as CodeDescrType);

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
