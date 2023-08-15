<template>
  <form>
    <div class="bx--content">
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
          sub-title=""
          :index="0"
          :valid="false"
          :wizard="slotProps"
        >
          <template v-slot:pre-header>
            <label class="heading-04 heading">Before you begin</label>
            <ol type="1" class="bulleted-list body-02">
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
            <div>
            <label class="heading-03">Mailing address</label>
            <p class="body-01">
              This is the primary address you will receive mail.
              <br /><br />
              If you’d like another address, for example a seed orchard or if
              your street address is different from your mailing address, select
              the ”Add another address” button below.
            </p>
          </div>
          </template>
          <template v-slot="{ validateStep, active }">
            <div>
            <address-wizard-step
              v-if="active"
              v-model:data="formData"
              :active="active"
              @valid="validateStep"
            />
          </div>
          </template>
        </wizard-tab-component>

        <wizard-tab-component
          title="Contacts"
          :index="2"
          :valid="false"
          :wizard="slotProps"
        >
          <template v-slot:header>
            <label class="heading-03"
              >Add authorized people to the account</label
            >
            <p class="body-01">
              Your first name, last name and email address are from your BCeID.
              If they're incorrect,
              <a
                href="https://bceid.ca"
                target="_blank"
                rel="noopener noreferrer"
                >go to BCeID</a
              >
              to update them before submitting your form. Be sure to add your
              phone number, location and role.
            </p>
            <p class="bx--description">
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
import { reactive, watch, toRef, ref, getCurrentInstance } from 'vue'
import { useEventBus } from '@vueuse/core'
import { useRouter } from 'vue-router'
import BusinessInformationWizardStep from '@/pages/applyform/BusinessInformationWizardStep.vue'
import AddressWizardStep from '@/pages/applyform/AddressWizardStep.vue'
import ContactWizardStep from '@/pages/applyform/ContactWizardStep.vue'
import ReviewWizardStep from '@/pages/applyform/ReviewWizardStep.vue'
import {
  newFormDataDto,
  type FormDataDto,
  type Contact
} from '@/dto/ApplyClientNumberDto'

import type { ValidationMessageType } from '@/dto/CommonTypesDto'
import { usePost } from '@/composables/useFetch'
import ForestClientUserSession from '@/helpers/ForestClientUserSession'

const submitterInformation = ForestClientUserSession.user
const errorBus = useEventBus<ValidationMessageType[]>(
  'submission-error-notification'
)
const generalErrorBus = useEventBus<string>('general-error-notification')

const router = useRouter()

const instance = getCurrentInstance()
const session = instance?.appContext.config.globalProperties.$session

const submitterContact: Contact = {
  locationNames: [],
  contactType: { value: '', text: '' },
  phoneNumber: '',
  firstName: session?.user?.firstName ?? '',
  lastName: session?.user?.lastName ?? '',
  email: session?.user?.email ?? ''
}

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() })

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
  }
})

watch([error], () => {
  if (error.value.response.status === 400) {
    const validationErrors: ValidationMessageType[] = error.value.response.data
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
  usePost(
    '/api/clients/mail',
    {
      incorporation: formData.businessInformation.incorporationNumber,
      name: formData.businessInformation.businessName,
      userName: submitterInformation?.name || '',
      userId: submitterInformation?.userId || '',
      mail: submitterInformation?.email || ''
    },
    {}
  )
}

const logOut = () => {
  session?.logOut()
}
</script>
