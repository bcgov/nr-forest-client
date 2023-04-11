<template>
  <tr>
      <td>
          <b-form-select :id="'contactTypeId' + id"
                          v-model="contact.contactType" 
                          :options="contactTypeCodes" />
          <ValidationMessages :fieldId = "`location.addresses[${index}].contacts[${id}].contactType`"
                              :validationMessages="validationMessages" />
      </td>
      <td>
          <b-form-input :id="'contactFirstNameId' + id"
                        v-model="contact.firstName">
          </b-form-input>
          <ValidationMessages :fieldId = "`location.addresses[${index}].contacts[${id}].firstName`"
                              :validationMessages="validationMessages" />
      </td>
      <td>
          <b-form-input :id="'contactLastNameId' + id"
                        v-model="contact.lastName">
          </b-form-input>
          <ValidationMessages :fieldId = "`location.addresses[${index}].contacts[${id}].lastName`"
                              :validationMessages="validationMessages" />
      </td>
      <td>
          <b-form-input :id="'contactPhoneNumberId' + id"
                        v-model="contact.phoneNumber"
                        v-mask="'##########'">
          </b-form-input>
          <ValidationMessages :fieldId = "`location.addresses[${index}].contacts[${id}].phoneNumber`"
                              :validationMessages="validationMessages" />
      </td>
      <td>
          <b-form-input :id="'contactEmailId' + id"
                        v-model="contact.email">
          </b-form-input>
          <ValidationMessages :fieldId = "`location.addresses[${index}].contacts[${id}].email`"
                              :validationMessages="validationMessages" />
      </td>
      <td>
          <b-button enabled="contacts.length > 1"
                    @click="parentFunctions.removeById(id)">
              <bi-x-circle></bi-x-circle>
          </b-button>
      </td>
  </tr>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue';  
  import BiXCircle from "~icons/bi/x-circle";
  import BiPlusLg from "~icons/bi/plus-lg";
  import type { CodeDescrType, ValidationMessageType } from '@/core/CommonTypes';
  const props = defineProps({
    contact: {
      type: Object,
      required: true
    },
    id: {
      type: Number,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    validationMessages: {
      type: Array<ValidationMessageType>,
      required: true
    },
    //We don't need to fetch it here as it is the same, so we receive it
    contactTypeCodes: {
      type: Array<CodeDescrType>,
      required: true
    },
    parentFunctions: {
      type: Function,
      required: true
    }
  });

</script>

<script lang="ts">
import { defineComponent } from "vue";
import ValidationMessages from "@/common/ValidationMessagesComponent.vue";  

export default defineComponent({
  name: "ContactComponent"
});
</script>
