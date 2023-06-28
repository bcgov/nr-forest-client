<template>
  <form ref="clientForm" @reset="resetForm">
    <div>
      <wizard-wrapper-component
        title="New client application"
        subtitle="All fields are mandatory unless noted"
        :submit="submit"
        :mail="sendEmail"
        :end="logOut"
        v-slot="slotProps"
      >
        <wizard-tab-component
          title="Business information"
          sub-title="Enter the business information"
          :index="0"
          :valid="false"
          :wizard="slotProps"
        >
          <template v-slot:pre-header>
            <h4 class="form-header">Before you begin</h4>
            <ol type="1" class="bulleted-list">
              <li>
                A registered business must be in good standing with BC
                Registries
              </li>
              <li>
                You must be able to receive email at
                {{ submitterContact.email }}
              </li>
            </ol>
            <hr />
          </template>
          <template v-slot="{ validateStep, active }">
            <business-information-wizard-step
              v-if="active"
              v-model:data="formData"
              :active="active"
              @valid="validateStep"
            />
          </template>
        </wizard-tab-component>

        <wizard-tab-component
          title="Address"
          :index="1"
          :valid="false"
          :wizard="slotProps"
        >
          <template v-slot:header>
            <h3 class="inner-heading">Mailing address</h3>
            <p class="inner-text inner-text-spaced">
              This is the primary address you will receive mail.
            </p>
            <p class="inner-text inner-text-spaced">
              If you’d like another address, for example a seed orchard or if
              your street address is different from your mailing address, select
              the ”Add another address” button below.
            </p>
          </template>
          <template v-slot="{ validateStep, active }">
            <address-wizard-step
              v-if="active"
              v-model:data="formData"
              :active="active"
              @valid="validateStep"
            />
          </template>
        </wizard-tab-component>

        <wizard-tab-component
          title="Contacts"
          :index="2"
          :valid="false"
          :wizard="slotProps"
        >
          <template v-slot:header>
            <h3 class="inner-heading">Add authorized people to the account</h3>
            <p class="inner-text inner-text-spaced">
              Your first name, last name and email address are from your BCeID.
              If they're incorrect,
              <a href="https://bceid.ca" target="_blank">go to BCeID</a> to
              update them before submitting your form. Be sure to add your phone
              number, location and role.
            </p>
            <p class="inner-text inner-text-spaced">
              To add another contact to the account, select "Add another
              contact" button below.
            </p>
          </template>
          <template v-slot="{ validateStep, active }">
            <contact-wizard-step
              v-if="active"
              v-model:data="formData"
              :active="active"
              @valid="validateStep"
            />
          </template>
        </wizard-tab-component>

        <wizard-tab-component
          title="Review application"
          sub-title='Review the content and make any changes by navigating through the steps above or using the "Edit" buttons in each section below.'
          :index="3"
          :valid="false"
          :wizard="slotProps"
          v-slot="{ validateStep, active, goToStep }"
        >
          <review-wizard-step
            v-if="active"
            v-model:data="formData"
            :active="active"
            @valid="validateStep"
            :goToStep="goToStep"
          />
        </wizard-tab-component>
      </wizard-wrapper-component>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch, inject, toRef, onMounted, ref } from 'vue'
import { useEventBus } from '@vueuse/core'
import { useRouter } from 'vue-router'
import {
  newFormDataDto,
  type FormDataDto,
  type Contact
} from '@/dto/ApplyClientNumberDto'

import type { Submitter, ValidationMessageType } from '@/core/CommonTypes'
import { usePost } from '@/services/ForestClientService'

const submitterInformation = inject<Submitter>('submitterInformation')
const errorBus = useEventBus<ValidationMessageType[]>(
  'submission-error-notification'
)
const generalErrorBus = useEventBus<string>('general-error-notification')

const router = useRouter()

const submitterContact: Contact = {
  locationNames: [],
  contactType: { value: '', text: '' },
  phoneNumber: '',
  firstName: submitterInformation?.firstName || '',
  lastName: submitterInformation?.lastName || '',
  email: submitterInformation?.email || ''
}

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() })

onMounted(() => {
  formDataDto.value = newFormDataDto()
})

//---- Form Data ----//
let formData = reactive<FormDataDto>({
  ...formDataDto.value,
  location: {
    addresses: formDataDto.value.location.addresses,
    contacts: [submitterContact]
  }
})

const { response, error, fetch } = usePost(
  '/api/clients/submissions',
  toRef(formData).value,
  {
    skip: true,
    headers: {
      'x-user-id': submitterInformation?.userId || '',
      'x-user-email': submitterInformation?.email || '',
      'x-user-name': submitterInformation?.firstName || ''
    }
  }
)

watch([response], () => {
  if (response.value.status === 201) {
    router.push({ name: 'confirmation' })
    resetForm()
  }
})

watch([error], () => {
  if (error.value.status === 400) {
    const validationErrors: ValidationMessageType[] = error.value.data
    const fieldIds = [
      'businessInformation.businessType',
      'businessInformation.legalType',
      'businessInformation.clientType'
    ]

    const matchingFields = validationErrors.find((item) =>
      fieldIds.includes(item.fieldId)
    )
    if (matchingFields) {
      generalErrorBus.emit(
        `There was an error submitting your application. ${matchingFields.errorMsg}`
      )
    }
  } else {
    generalErrorBus.emit(
      `There was an error submitting your application. ${error.value.data}}`
    )
  }
})

const submit = () => {
  errorBus.emit([])
  generalErrorBus.emit('')
  fetch()
}

const sendEmail = () => {
  console.log('sendEmail')
  usePost(
    '/api/clients/mail',
    {
      incorporation: formData.businessInformation.incorporationNumber,
      name: formData.businessInformation.businessName,
      userName: submitterInformation?.firstName || '',
      userId: submitterInformation?.userId || '',
      mail: submitterInformation?.email || ''
    },
    {}
  )
}

const logOut = () => {
  console.log('logOut')
}

const clientForm = ref<HTMLFormElement | null>(null)

const resetForm = () => {
  if (clientForm.value) {
    clientForm.value.reset()
  }
}
</script>

<style scoped></style>
