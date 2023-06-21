<template>
  <div class="table-responsive">
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
        <ContactComponent
          v-for="(item, itemIndex) in contacts"
          :id="itemIndex"
          :index="index"
          :validationMessages="validationMessages"
          :contact="item"
          :contactTypeCodes="contactTypeCodes"
          :parentFunctions="reactiveFunctions"
        />
      </tbody>
    </table>
  </div>

  <b-button class="chefsBlue" @click="addContact()">
    <bi-plus-lg></bi-plus-lg>
    Add a contact
  </b-button>
</template>

<script setup lang="ts">
import { conversionFn } from '@/services/GeneralServices'
import { addNewContact, useFetch } from '@/services/ForestClientService'
import { computed } from 'vue'
import ValidationMessages from '@/common/ValidationMessagesComponent.vue'
import type { Contact } from '@/dto/ApplyClientNumberDto'

const props = defineProps({
  contacts: {
    type: Array<Contact>,
    required: true
  },
  id: {
    type: String,
    required: true
  },
  index: {
    type: Number,
    required: true
  },
  validationMessages: {
    type: Array<ValidationMessageType>,
    required: true
  }
})

const reactiveFunctions = reactive({
  removeById: (id: number) => {
    props.contacts.splice(id, 1)
  }
})

const { data: activeContactTypeCodes } = useFetch(
  '/api/clients/activeContactTypeCodes?page=0&size=250',
  { method: 'get', initialData: [] }
)
const contactTypeCodes = computed(() => {
  return activeContactTypeCodes.value.map(conversionFn)
})

const addContact = () => {
  return addNewContact(props.contacts)
}
</script>

<script lang="ts">
import { defineComponent, reactive } from 'vue'
import ContactComponent from '@/pages/applyclientnumber/ContactComponent.vue'
import type { ValidationMessageType } from '@/core/CommonTypes'
export default defineComponent({
  name: 'ContactSectionComponent'
})
</script>

<style scoped>
.chefsBlue {
  background: #003366;
}

.rowSpace {
  padding-bottom: 0.7rem !important;
}
</style>
