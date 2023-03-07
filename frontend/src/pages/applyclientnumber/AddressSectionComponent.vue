<template>
  <div v-for="(item, itemIndex) in addresses"
       :key="itemIndex">
    <b-row>  
      <b-col xs="10" sm="10" md="11" lg="11" 
             style="border: 1px solid lightgray;
                    padding: 0.7rem !important;">
        <b-row class="rowSpace"> 
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <Label label="Country" 
                  :required="true" />
          </div>
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <b-form-select :id="'countryId' + itemIndex"
                           v-model="item.country" 
                           :options="countryCodes"
                           @change="updateProvinceCodes($event, itemIndex)" />
            <ValidationMessages :fieldId = "'countryId' + itemIndex"
                                :validationMessages="validationMessages"  />
          </div>
        </b-row>
        <b-row class="rowSpace">
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <Label label="Street Address" 
                  :required="true" />
          </div>
          <div>
            <b-form-input :id="'streetAddressId' + itemIndex"
                          v-model="item.streetAddress">
            </b-form-input>
            <ValidationMessages :fieldId = "'streetAddressId' + itemIndex"
                                :validationMessages="validationMessages"  />
          </div>
        </b-row>
        <b-row class="rowSpace">
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <Label label="City" 
                  :required="true" />
          </div>
          <div>
            <b-form-input :id="'cityId' + itemIndex"
                          v-model="item.city">
            </b-form-input>
            <ValidationMessages :fieldId = "'cityId' + itemIndex"
                                :validationMessages="validationMessages"  />
          </div>
        </b-row>
        <b-row class="rowSpace"> 
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <span v-if="item.country?.value == 'US'">
              <Label label="State" 
                     :required="true" />
            </span>
            <span v-if="item.country?.value != 'US'">
              <Label label="Province" 
                     :required="true" />
            </span>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <b-form-select :id="'provinceId' + itemIndex"
                           v-model="item.province">
              <option v-for="option in provinceCodes[itemIndex]" 
                      :value="option">
                {{ option.name }}
              </option>
            </b-form-select>
            <ValidationMessages :fieldId = "'provinceId' + itemIndex"
                                :validationMessages="validationMessages"  />
          </div>
        </b-row>
        <b-row class="rowSpace"> 
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <span v-if="item.country?.value == 'US'">
              <Label label="Zip code" 
                     :required="true" />
            </span>
            <span v-if="item.country?.value != 'US'">
              <Label label="Postal code" 
                     :required="true" />
            </span>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <b-form-input :id="'postalCodeId' + itemIndex"
                          v-model="item.postalCode">
            </b-form-input>
            <ValidationMessages :fieldId = "'postalCode' + itemIndex"
                                :validationMessages="validationMessages"  />
          </div>
        </b-row>
        <br />
        <b-row class="rowSpace"> 
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <CollapseCard title="Business contacts" 
                          id="contactsId"
                          defaultOpen>

              <span>
                  <strong>Enter an authorized person you want to add for this address</strong>
              </span>

              <br /><br />
              <ContactSection id="contactListId" 
                              :contacts="item.contacts"
                              :validationMessages="validationMessages" />
            </CollapseCard>
          </div>
        </b-row>
      </b-col>
      
      <b-col xs="2" sm="2" md="1" lg="1" 
             style="border-top: 1px solid lightgray;
                    border-right: 1px solid lightgray;
                    border-bottom: 1px solid lightgray;
                    padding: 0.7rem !important;
                    text-align: center;">
        <b-button enabled="addresses.length > 1"
                  @click="deleteAddress(itemIndex)">
          <bi-x-circle></bi-x-circle>
        </b-button>
      </b-col>
    </b-row>

    <br />
  </div>

  <b-button class="chefsBlue"
            @click="addAddress()">
    <bi-plus-lg></bi-plus-lg>
    Add another address
  </b-button>
</template>
  
<script setup lang="ts">
  import { conversionFn } from '@/services/FetchService';
  import { addNewAddress, useFetch } from '@/services/forestClient.service';
  import { computed, ref, Suspense } from 'vue';
  import Label from "../../common/LabelComponent.vue";
  import CollapseCard from "../../common/CollapseCardComponent.vue";
  import ContactSection from "./ContactSectionComponent.vue";
  import BiXCircle from "~icons/bi/x-circle";
  import BiPlusLg from "~icons/bi/plus-lg";
  import type { Address } from '../../dto/ApplyClientNumberDto';
  import { CodeDescrType, type ValidationMessageType } from '@/core/CommonTypes';
  import axios from 'axios';
  
  axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
  const forestClientBase = import.meta.env.VITE_BACKEND_URL;

  const props = defineProps({
    addresses: { 
      type: Array<Address>, 
      required: true 
    },
    id: { 
      type: String, 
      required: true 
    },
    validationMessages: {
      type: Array<ValidationMessageType>, 
      required: true
    }
  });
  
  const { data: activeCountryCodes } = useFetch('/api/clients/activeCountryCodes?page=0&size=250', { method:'get', initialData:[] });
  const countryCodes = computed(() => {
    return activeCountryCodes.value.map(conversionFn);
  });

  let provinceCodes = ref<CodeDescrType[]>([]);
  async function updateProvinceCodes(event: CodeDescrType, index: number) {
      const countryCode = event.value;
      const response = await axios.get(forestClientBase + '/api/clients/activeCountryCodes/' + countryCode + '?page=0&size=250');
      provinceCodes.value[index] = response.data;
      return provinceCodes;
  }

  const deleteAddress = (index: number) => {
    props.addresses.splice(index, 1);
  };

  const addAddress = () => {
    return addNewAddress(props.addresses);
  };
  
</script>
  
<style scoped>
.chefsBlue {
  background: #003366;
}

.rowSpace {
  padding-bottom: 0.7rem !important;
}
</style>