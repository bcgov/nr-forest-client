<template>
  <div v-for="(item, itemIndex) in addresses" :key="itemIndex">
    <b-row>
      <AddressComponent 
      :address="item"
      :id="itemIndex"
      :validationMessages="[]"
      :countryCodes="countryCodes"
      />

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

  <b-button class="chefsBlue" @click="addAddress()">
    <bi-plus-lg></bi-plus-lg>
    Add another address
  </b-button>
</template>
  
<script setup lang="ts">
  import { conversionFn } from '@/services/FetchService';
  import { addNewAddress, useFetch } from '@/services/ForestClientService';
  import { computed } from 'vue';
  import BiXCircle from "~icons/bi/x-circle";
  import BiPlusLg from "~icons/bi/plus-lg";
  import type { Address } from '@/dto/ApplyClientNumberDto';
  import type { ValidationMessageType } from '@/core/CommonTypes';  
  import AddressComponent from '@/common/AddressComponent.vue';
  
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
  
  //As we need this data only once, we load it here for all addresses to use
  const { data: activeCountryCodes } = useFetch('/api/clients/activeCountryCodes?page=0&size=250', { method:'get', initialData:[] });
  const countryCodes = computed(() => {
    return activeCountryCodes.value.map(conversionFn);
  });
  
  const deleteAddress = (index: number) => {
    props.addresses.splice(index, 1);
  };

  const addAddress = () => {
    return addNewAddress(props.addresses);
  };
  
</script>

<script lang="ts">
  import { defineComponent } from "vue";
  export default defineComponent({
    name: "AddressSectionComponent",
  });
</script>
  
<style scoped>
.chefsBlue {
  background: #003366;
}

.rowSpace {
  padding-bottom: 0.7rem !important;
}
</style>