<template>
  <div class="grouping-05">
    <label class="heading-03">Business information</label>
    <div class="grouping-06">
      <p class="heading-02">
        {{ formData.businessInformation.businessName }}
      </p>
      <p class="body-compact-01">
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

  <div class="grouping-05">
    <label class="heading-03">Address</label>

    <div
      v-for="(address, index) in formData.location.addresses"
      :key="address.locationName"
      class="grouping-07"
    >
      <hr v-if="index > 0"/>
      <span class="heading-02">{{ address.locationName }}</span>
      <span class="body-compact-01">{{ address.streetAddress }}</span>
      <span class="body-compact-01">{{ address.city }}, {{ address.province.text }}</span>
      <span class="body-compact-01">{{ address.country.text }}</span>
      <span class="body-compact-01">{{ address.postalCode }}</span>
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

  <div class="grouping-05">
    <label class="heading-03">Contacts</label>

    <div
      v-for="(contact, index) in formData.location.contacts"
      :key="contact.email"
      class="grouping-07"
    >
      <hr v-if="index > 0"/>
      <span class="heading-02">{{ contact.firstName }} {{ contact.lastName }}</span>
      <span class="body-compact-01">
        {{ contact.locationNames.map((codeDesc) => codeDesc.text).join(', ') }}
      </span>
      <span class="body-compact-01">{{ contact.contactType.text }}</span>
      <span class="body-compact-01">{{ contact.email }}</span>
      <span class="body-compact-01">{{ contact.phoneNumber }}</span>
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
  border: 1px solid #DFDFE1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 32px;
  gap: 40px;
}
</style>
