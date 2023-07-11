<template>
  <div class="bordered">
    <label class="bx--title">Business information</label>
    <div>
      <p class="inner-spotlight-topic">
        {{ formData.businessInformation.businessName }}
      </p>
      <p>
        {{
          companyBusinessTypes[`${formData.businessInformation.businessType}`]
        }}
      </p>
    </div>

    <bx-btn
      kind="tertiary"
      iconLayout=""
      class="bx--btn"
      size="field"
      @click.prevent="goToStep(0)"
    >
      <span>Edit business information</span>
      <Edit16 slot="icon" />
    </bx-btn>
  </div>

  <div class="bordered">
    <label class="bx--title">Address</label>

    <div
      v-for="(address, index) in formData.location.addresses"
      :key="address.locationName"
      class="inner-spotlight-group"
      :class="{
        'inner-spotlight-group-markup':
          index < formData.location.contacts.length - 1
      }"
    >
      <p class="inner-spotlight-topic">{{ address.locationName }}</p>
      <p>{{ address.streetAddress }}</p>
      <p>{{ address.city }}, {{ address.province.text }}</p>
      <p>{{ address.country.text }}</p>
      <p>{{ address.postalCode }}</p>
    </div>

    <bx-btn
      kind="tertiary"
      iconLayout=""
      class="bx--btn"
      size="field"
      @click.prevent="goToStep(1)"
    >
      <span>Edit address</span>
      <Edit16 slot="icon" />
    </bx-btn>
  </div>

  <div class="bordered">
    <label class="bx--title">Contacts</label>

    <div
      v-for="(contact, index) in formData.location.contacts"
      :key="contact.email"
      class="inner-spotlight-group"
      :class="{
        'inner-spotlight-group-markup':
          index < formData.location.contacts.length - 1
      }"
    >
      <p class="inner-spotlight-topic">
        {{ contact.firstName }} {{ contact.lastName }}
      </p>
      <p>
        {{ contact.locationNames.map((codeDesc) => codeDesc.text).join(', ') }}
      </p>
      <p>{{ contact.contactType.text }}</p>
      <p>{{ contact.email }}</p>
      <p>{{ contact.phoneNumber }}</p>
    </div>

    <bx-btn
      kind="tertiary"
      iconLayout=""
      class="bx--btn"
      size="field"
      @click.prevent="goToStep(2)"
    >
      <span>Edit contacts</span>
      <Edit16 slot="icon" />
    </bx-btn>
  </div>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue'

import type { FormDataDto } from '@/dto/ApplyClientNumberDto'
import Edit16 from '@carbon/icons-vue/es/edit/16'

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto
  active: boolean
  goToStep: Function
}>()

const emit = defineEmits<{
  (e: 'update:data', value: FormDataDto): void
  (e: 'valid', value: boolean): void
}>()

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data)
watch([formData], () => emit('update:data', formData.value))

//So far, hardcoded the value but should be coming from somewhere else
const companyBusinessTypes: Record<string, string> = {
  R: 'B.C. Registered Business - Corporation',
  U: 'Sole Proprietorship'
}

//We emit valid here because there is nothing else to be done here apart from showing information
emit('valid', true)
</script>

<style scoped>
.bordered {
  box-sizing: border-box;
  border: 1px solid #dfdfe1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 32px;
  gap: 40px;
}
</style>
