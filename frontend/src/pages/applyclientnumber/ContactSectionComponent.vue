<template>
    <table class="table">
        <thead>
            <tr>
                <th>Role</th>
                <th>First name</th>
                <th>Last name</th>
                <th>Phone number</th>
                <th>Email address</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(item, itemIndex) in contacts" 
                :key="itemIndex">
                <td>
                    <b-form-select :id="'contactTypeId' + itemIndex"
                                    v-model="item.contactType" 
                                    :options="contactTypeCodes" />
                    <ValidationMessages :fieldId = "'contactTypeId' + itemIndex"
                                        :validationMessages="validationMessages"  />
                </td>
                <td>
                    <b-form-input :id="'contactFirstNameId' + itemIndex"
                                  v-model="item.firstName">
                    </b-form-input>
                    <ValidationMessages :fieldId = "'contactFirstNameId' + itemIndex"
                                        :validationMessages="validationMessages"  />
                </td>
                <td>
                    <b-form-input :id="'contactLastNameId' + itemIndex"
                                  v-model="item.lastName">
                    </b-form-input>
                    <ValidationMessages :fieldId = "'contactLastNameId' + itemIndex"
                                        :validationMessages="validationMessages"  />
                </td>
                <td>
                    <b-form-input :id="'contactPhoneNumberId' + itemIndex"
                                v-model="item.phoneNumber">
                    </b-form-input>
                    <ValidationMessages :fieldId = "'contactPhoneNumberId' + itemIndex"
                                        :validationMessages="validationMessages"  />
                </td>
                <td>
                    <b-form-input :id="'contactEmailAddressId' + itemIndex"
                                  v-model="item.emailAddress">
                    </b-form-input>
                    <ValidationMessages :fieldId = "'contactEmailAddressId' + itemIndex"
                                        :validationMessages="validationMessages"  />
                </td>
                <td>
                    <b-button enabled="contacts.length > 1"
                            @click="deleteContact(itemIndex)">
                        <bi-x-circle></bi-x-circle>
                    </b-button>
                </td>
            </tr>
        </tbody>
    </table>
    
    <b-button class="chefsBlue"
            @click="addContact()">
        <bi-plus-lg></bi-plus-lg>
        Add another person for this address
    </b-button>
  </template>
    
  <script setup lang="ts">
    import { conversionFn } from '@/services/FetchService';
    import { addNewContact, useFetch } from '@/services/forestClient.service';
    import { computed, ref, Suspense } from 'vue';
    import ValidationMessages from "../../common/ValidationMessagesComponent.vue";
    import BiXCircle from "~icons/bi/x-circle";
    import BiPlusLg from "~icons/bi/plus-lg";
    import type { Contact } from '../../dto/ApplyClientNumberDto';
    import { CodeDescrType, type ValidationMessageType } from '@/core/CommonTypes';
    import axios from 'axios';
    
    axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
    const forestClientBase = import.meta.env.VITE_BACKEND_URL;
  
    const props = defineProps({
      contacts: { 
        type: Array<Contact>, 
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

    const { data: activeContactTypeCodes } = useFetch('/api/clients/activeContactTypeCodes?page=0&size=250', { method:'get', initialData:[] });
    const contactTypeCodes = computed(() => {
      return activeContactTypeCodes.value.map(conversionFn);
    });

    const deleteContact = (index: number) => {
      props.contacts.splice(index, 1);
    };
  
    const addContact = () => {
      return addNewContact(props.contacts);
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