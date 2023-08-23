<script setup lang="ts">
import { reactive, watch, toRef, ref, getCurrentInstance, computed } from 'vue'
import { useEventBus } from '@vueuse/core'
import { useRouter } from 'vue-router'
import ArrowRight16 from '@carbon/icons-vue/es/arrow--right/16'
import Save16 from '@carbon/icons-vue/es/save/16'
import LogOut16 from '@carbon/icons-vue/es/logout/16'
import Check16 from '@carbon/icons-vue/es/checkmark/16'
import BusinessInformationWizardStep from '@/pages/applyform/BusinessInformationWizardStep.vue'
import AddressWizardStep from '@/pages/applyform/AddressWizardStep.vue'
import ContactWizardStep from '@/pages/applyform/ContactWizardStep.vue'
import ReviewWizardStep from '@/pages/applyform/ReviewWizardStep.vue'
import {
  newFormDataDto,
  type FormDataDto,
  type Contact
} from '@/dto/ApplyClientNumberDto'

import type { ValidationMessageType, ModalNotification } from '@/dto/CommonTypesDto'
import { usePost } from '@/composables/useFetch'
import ForestClientUserSession from '@/helpers/ForestClientUserSession'
import useFocus from '@/composables/useFocus';

const { setFocusedComponent } = useFocus();
const submitterInformation = ForestClientUserSession.user
const errorBus = useEventBus<ValidationMessageType[]>(
  'submission-error-notification'
)
const generalErrorBus = useEventBus<string>('general-error-notification')
const exitBus = useEventBus<Record<string, boolean | null>>('exit-notification')

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
      setFocusedComponent('top-notification')
    }
  } else {
    generalErrorBus.emit(
      `There was an error submitting your application. ${error.value.data}}`
    )
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})

const submit = () => {
  errorBus.emit([])
  generalErrorBus.emit('')
  fetch()
}


// Tab system
const progressData = reactive(
  ['Business information','Address','Contacts','Review']
  .map((title, index) => {
    return {
      title,
      subtitle: `Step ${index + 1}`,
      kind: index === 0 ? 'current' : 'queued',
      enabled: true,
      valid: false
    }
  })
  
)

const currentTab = ref(0);

const stateIcon = (index: number) => {
  if (currentTab.value == index) return 'current'
  if (currentTab.value > index || progressData[index].valid) return 'complete'
  return 'queued'
}

const isLast = computed(() => currentTab.value === progressData.length - 1)
const isFirst = computed(() => currentTab.value === 0)
const isCurrentValid = computed(() => progressData[currentTab.value].valid)
const isNextAvailable = computed(() => !isCurrentValid.value || isLast.value)
const isFormValid = computed(() => progressData.every((entry: any) => entry.valid))
const endAndLogOut = ref<boolean>(false)
const mailAndLogOut = ref<boolean>(false)

const goToStep = (index: number) => (currentTab.value = index)

const onNext = () => {
  if (currentTab.value + 1 < progressData.length) {
    currentTab.value++
    progressData[currentTab.value-1].kind = stateIcon(currentTab.value-1)
    progressData[currentTab.value].kind = stateIcon(currentTab.value)
    setFocusedComponent(`focus-${currentTab.value}`)
  }
}
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--
    progressData[currentTab.value+1].kind = stateIcon(currentTab.value+1)
    progressData[currentTab.value].kind = stateIcon(currentTab.value)
    setFocusedComponent(`focus-${currentTab.value}`)
  }
}
const validateStep = (valid: boolean) => {
  progressData[currentTab.value].valid = valid
}
const openToast = (event: ModalNotification) => {
  console.log(event)
}

const saveChange = () => {
  openToast({
    message: `“${progressData[currentTab.value].title}” changes was saved successfully.`,
    kind: 'Success',
    active: true,
    handler: () => {}
  })
  goToStep(3)
}

const processAndLogOut = () => {
  if (mailAndLogOut.value) {
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
  session?.logOut()
}

exitBus.on((event: Record<string, boolean | null>) => {
  endAndLogOut.value = event.goodStanding ? event.goodStanding : false
  mailAndLogOut.value = event.duplicated ? event.duplicated : false
})

const globalErrorMessage = ref<string>('')
generalErrorBus.on((event: string) => (globalErrorMessage.value = event))
</script>

<template>
  <div class="form-header">
    <div class="form-header-title">
      <span class="heading-05">New client application</span>
      <p class="body-01">All fields are mandatory unless noted</p>
    </div>
      <wizard-progress-indicator-component
      :model-value="progressData"
      @go-to="goToStep"
    />
  </div>

  <div class="form-steps">

    <div v-if="currentTab == 0" class="form-steps-01">
      <div class="form-steps-01-title">
        <span class="heading-04">Before you begin</span>
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
      </div>

      <hr class="divider" />

      <div class="form-steps-section">
        <label class="heading-04" data-scroll="focus-0">{{ progressData[0].title}}</label>
        <div class="frame-01">
          <business-information-wizard-step
              v-model:data="formData"
              :active="currentTab == 0"
              @valid="validateStep"
          />
        </div>
      </div>
    </div>

    <div v-if="currentTab == 1" class="form-steps-02">
      <div class="form-steps-section">
        <span class="heading-04">{{ progressData[1].title}}</span>
        
        <div class="form-steps-section-01">
          <label class="heading-03">Mailing address</label>
            <p class="body-01">
              This is the primary address you will receive mail.
              <br /><br />
              If you’d like another address, for example a seed orchard or if
              your street address is different from your mailing address, select
              the ”Add another address” button below.
            </p>
        </div>
      
        <address-wizard-step
            v-model:data="formData"
            :active="currentTab == 1"
            @valid="validateStep"
          />
      </div>    
    </div>

    <div v-if="currentTab == 2" class="form-steps-03">
      <div class="form-steps-section">
        <span class="heading-04">{{ progressData[2].title}}</span>
        
        <div class="form-steps-section-01">
          <span class="heading-03"
              >Add authorized people to the account</span
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
              to update them before submitting your form. 
              <br /><br />Be sure to add your phone number, location and role.
            </p>
        </div>
      
        <contact-wizard-step
            v-model:data="formData"
            :active="currentTab == 2"
            @valid="validateStep"
          />
      </div>
    </div>

    <div v-if="currentTab == 3" class="form-steps-04">

      <display-block-component
      v-if="globalErrorMessage"
      kind="error"
      title="Your application could not be submitted"
      :subtitle="globalErrorMessage"
    >
    </display-block-component>


      <div class="form-steps-section form-steps-section-04">
          <span class="heading-04" data-scroll="focus-3">{{ progressData[3].title}}</span>
          <span class="body-02">Review the content and make any changes by navigating through the steps above or using the "Edit" buttons in each section below.</span>

        <review-wizard-step
            v-model:data="formData"
            :active="currentTab == 3"
            @valid="validateStep"
            :goToStep="goToStep"
          />

      </div>
    </div>

    <hr class="divider"/>
  </div>

  <div class="form-footer">
    <div class="form-footer-group">
      <div class="form-footer-group-next">

        <span class="body-compact-01" v-if="!isLast && !progressData[currentTab].valid"
        >All fields must be filled out correctly to enable the "Next" button
        below</span
      >
      <div class="form-footer-group-buttons">
        <bx-btn
            data-test="wizard-back-button"
            v-if="!isFirst"
            kind="secondary"
            iconLayout=""
            class="bx--btn"
            :disabled="isFirst"
            @click.prevent="onBack"
          >
          <span>Back</span>
        </bx-btn>

        <bx-btn
            id="nextBtn"
            v-if="!isLast && !isFormValid && !endAndLogOut && !mailAndLogOut"
            data-test="wizard-next-button"
            kind="primary"
            iconLayout=""
            class="bx--btn"
            :disabled="progressData[currentTab].valid === false"
            @click.prevent="onNext"
          >
          <span>Next</span>
          <ArrowRight16 slot="icon" />
        </bx-btn>

        <bx-btn
          v-if="isLast && !endAndLogOut && !mailAndLogOut"
            data-test="wizard-submit-button"
            kind="primary"
            iconLayout=""
            class="bx--btn"
            @click.prevent="submit"
          >
          <span>Submit application</span>
          <Check16 slot="icon" />
        </bx-btn>

        <bx-btn
          data-test="wizard-save-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          :disabled="isNextAvailable"
          v-if="!isLast && isFormValid && !endAndLogOut && !mailAndLogOut"
          @click.prevent="saveChange"
        >
          <span>Save</span>
          <Save16 slot="icon" />
        </bx-btn>

        <bx-btn
          data-test="wizard-logout-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          v-show="!isLast && (endAndLogOut || mailAndLogOut)"
          @click.prevent="processAndLogOut"
        >
          <span
            >{{ endAndLogOut ? 'End application' : 'Receive email' }} and
            logout</span
          >
          <LogOut16 slot="icon" />
        </bx-btn>

        </div>
      </div>
    
    </div>
  </div>
</template>
