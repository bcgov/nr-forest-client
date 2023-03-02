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
            <b-form-select id="countryId"
                           v-model="item.country" 
                           :options="countryCodes"
                           @change="updateProvinceCodes($event, itemIndex)" />
          </div>
        </b-row>
        <b-row class="rowSpace">
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <Label label="Street Address" 
                  :required="true" />
          </div>
          <div>
            <b-form-input v-model="item.streetAddress">
            </b-form-input>
          </div>
        </b-row>
        <b-row class="rowSpace">
          <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <Label label="City" 
                  :required="true" />
          </div>
          <div>
            <b-form-input v-model="item.city">
            </b-form-input>
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
                           v-model="item.province"
                           :options="provinceCodes" />
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
            <b-form-input v-model="item.postalCode">
            </b-form-input>
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
  import BiXCircle from "~icons/bi/x-circle";
  import BiPlusLg from "~icons/bi/plus-lg";
  import type { Address } from '../../dto/ApplyClientNumberDto';
  import type { CodeDescrType } from '@/core/CommonTypes';
  import axios from 'axios';
  
  const props = defineProps({
    addresses: { 
      type: Array<Address>, 
      required: true 
    },
    id: { 
      type: String, 
      required: true 
    }
  });
  
  const { data: activeCountryCodes } = useFetch('/api/clients/activeCountryCodes?page=0&size=250', { method:'get', initialData:[] });
  const countryCodes = computed(() => {
    return activeCountryCodes.value.map(conversionFn);
  });

  let provinceCodes = ref([] as CodeDescrType[]);
  async function updateProvinceCodes(event: CodeDescrType, index: number) {
      // Clear the dependent select
      //provinceCodes.value = [{value:"PU", text:"Peee"}];
      const countryCode = event.value;
      const response = await axios.get('http://localhost:3000/api/clients/activeCountryCodes/' + countryCode + '?page=0&size=250');
      console.log("response = " + JSON.stringify(response));
      provinceCodes.value = response.data.map(conversionFn);
      //return provinceCodes;
  }
  
  /*const provinceCodes = computed(() => {
    return async (index: number) => {
        const countryCode = props.addresses[index].country?.value;
        const response = await axios.get('http://localhost:3000/api/clients/activeCountryCodes/' + countryCode + '?page=0&size=250');

        if (props.addresses[index].country?.value && response) {
          console.log(JSON.stringify(response.data.map(conversionFn)));
          //result.value = response.data.map(conversionFn);
          return response.data.map(conversionFn);
        }
        else {
          return null;
        }
      }
  });*/

  /*const emit = defineEmits(["updateValue"]);
  const computedValue = computed({
    get() {
          return props.addresses
    },
    set(newValue) {
      emit('updateValue', newValue)
    }
  });*/

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