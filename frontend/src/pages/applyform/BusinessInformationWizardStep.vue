<script setup lang="ts">
import { watch, computed, ref, reactive } from 'vue'
import { useEventBus } from '@vueuse/core'
import { useFetchTo } from '@/composables/useFetch'
import { type BusinessSearchResult, ClientTypeEnum } from '@/core/CommonTypes'
import type {
  FormDataDto,
  ForestClientDetailsDto
} from '@/dto/ApplyClientNumberDto'
import RadioInputComponent from '@/components/forms/RadioInputComponent.vue'
import { isNotEmpty } from '@/helpers/validators/GlobalValidators'
import { submissionValidation } from '@/helpers/validators/SubmissionValidators'
import { retrieveClientType } from '@/helpers/DataConversors'

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>()

const emit = defineEmits<{
  (e: 'update:data', value: FormDataDto): void
  (e: 'valid', value: boolean): void
}>()

//Defining the event bus to send notifications up
const navigationBus = useEventBus<boolean>('navigation-notification')
const exitBus = useEventBus<Record<string, boolean | null>>('exit-notification')
const generalErrorBus = useEventBus<string>('general-error-notification')

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data)
watch(
  () => formData.value,
  () => emit('update:data', formData.value)
)

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  businessType: formData.value.businessInformation.businessType ? true : false,
  business: formData.value.businessInformation.businessName ? true : false
})

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  )

watch([validation], () => emit('valid', checkValid()))
emit('valid', checkValid())

// -- Auto completion --
const selectedOption = computed(() => {
  switch (formData.value.businessInformation.businessType) {
    case 'R':
      return ClientTypeEnum.R
    case 'U':
      return ClientTypeEnum.U
    default:
      return ClientTypeEnum.Unknow
  }
})

const autoCompleteUrl = computed(
  () => `/api/clients/name/${formData.value.businessInformation.businessName}`
)

const showAutoCompleteInfo = ref<boolean>(false)
const showGoodStandingError = ref<boolean>(false)
const showDuplicatedError = ref<boolean>(false)
const showDetailsLoading = ref<boolean>(false)
const detailsData = ref(null)

const toggleErrorMessages = (
  goodStanding: boolean | null,
  duplicated: boolean | null
) => {
  showGoodStandingError.value = goodStanding ? goodStanding : false
  showDuplicatedError.value = duplicated ? duplicated : false

  if (goodStanding || duplicated) {
    navigationBus.emit(false)
    exitBus.emit({ goodStanding, duplicated })
  } else {
    navigationBus.emit(true)
    exitBus.emit({ goodStanding: false, goodStanding: false })
  }
}

//Using this as we have to handle the selected result to get
//incorporation number and client type
const autoCompleteResult = ref<BusinessSearchResult>()
watch([autoCompleteResult], () => {
  // reset business validation state
  validation.business = false

  if (autoCompleteResult.value) {
    toggleErrorMessages(false, false)

    formData.value.businessInformation.incorporationNumber =
      autoCompleteResult.value.code
    formData.value.businessInformation.legalType =
      autoCompleteResult.value.legalType
    formData.value.businessInformation.clientType = retrieveClientType(
      autoCompleteResult.value.legalType
    )
    showAutoCompleteInfo.value = false

    emit('update:data', formData.value)

    const config = {
      headers: {
        'x-user-email': formData.value.location.contacts[0].email,
        'x-user-id': formData.value.location.contacts[0].firstName
      }
    }

    //Also, we will load the backend data to fill all the other information as well
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/clients/${autoCompleteResult.value.code}`,
      detailsData,
      config
    )

    showDetailsLoading.value = true
    watch([error], () => {
      if (error.value.response.status === 409) {
        toggleErrorMessages(null, true)
        return
      }
      if (error.value.response.status === 404) {
        toggleErrorMessages(null, null)
        validation.business = true
        emit('update:data', formData.value)
        return
      }
      generalErrorBus.emit(error.value.response.data.message)
    })

    watch(
      [detailsLoading],
      () => (showDetailsLoading.value = detailsLoading.value)
    )
  }
})

watch([detailsData], () => {
  if (detailsData.value) {
    const forestClientDetails: ForestClientDetailsDto = detailsData.value
    formData.value.location.contacts = [
      formData.value.location.contacts[0],
      ...forestClientDetails.contacts
    ]
    formData.value.location.addresses = forestClientDetails.addresses
    formData.value.businessInformation.goodStandingInd =
      forestClientDetails.goodStanding ? 'Y' : 'N'
    toggleErrorMessages(!forestClientDetails.goodStanding, null)
    validation.business = forestClientDetails.goodStanding

    emit('update:data', formData.value)
  }
})

// -- Unregistered Proprietorship
watch([selectedOption], () => {
  if (selectedOption.value === ClientTypeEnum.U) {
    formData.value.businessInformation.businessType = 'U'
    formData.value.businessInformation.clientType = 'U'
    const { firstName, lastName } = formData.value.location.contacts[0]
    formData.value.businessInformation.businessName = `${firstName} ${lastName}`
    validation.business = true
    emit('update:data', formData.value)
  } else {
    formData.value.businessInformation.businessName = ''
    validation.business = false
    showAutoCompleteInfo.value = true
  }
})
</script>

<template>
  <radio-input-component
    id="businessType"
    label="Type of business (choose one of these options)"
    :initialValue="formData?.businessInformation?.businessType"
    :modelValue="[
      {
        value: 'R',
        text: 'I have a BC registered business (corporation, sole proprietorship, society, etc.)'
      },
      { value: 'U', text: 'I have an unregistered sole proprietorship' }
    ]"
    :validations="[submissionValidation('businessInformation.businessType')]"
    @update:model-value="
      formData.businessInformation.businessType = $event ?? ''
    "
    @empty="validation.businessType = !$event"
  />

  <data-fetcher
    v-model:url="autoCompleteUrl"
    :min-length="3"
    :init-value="[]"
    :init-fetch="false"
    #="{ content, loading, error }"
  >
    <AutoCompleteInputComponent
      v-if="selectedOption === ClientTypeEnum.R"
      id="business"
      label="BC registered business name"
      tip="The name must be exactly the same as in BC Registries"
      v-model="formData.businessInformation.businessName"
      :contents="content"
      :validations="[
        isNotEmpty,
        submissionValidation('businessInformation.businessName')
      ]"
      :loading="loading"
      @update:selected-value="autoCompleteResult = $event"
    />

    <div class="spinner-block" v-if="showDetailsLoading">
      <bx-loading type="small"> </bx-loading>
      <span>Loading client details...</span>
    </div>
    <display-block-component
      kind="info"
      title="BC registered business name"
      v-show="showAutoCompleteInfo && selectedOption === ClientTypeEnum.R"
      id="business"
    >
      <div>
        <p>
          If the name of your registered business does not appear in the list,
          follow these steps:
        </p>
        <ol type="1" class="bulleted-list">
          <li>
            Log into Manage Account in
            <a href="https://www.bceid.ca/" target="_blank">BCeID</a> to find
            your business name
          </li>
          <li>
            If your name isn’t there, call BC Registry toll free at
            <a href="tel:18775261526">1-877-526-1526</a> or email them at
            <a href="mailto:BCRegistries@gov.bc.ca">BCRegistries@gov.bc.ca</a>.
          </li>
        </ol>
      </div>
    </display-block-component>

    <display-block-component
      v-show="showGoodStandingError"
      kind="error"
      title="Not in good standing with BC Registries"
    >
      <p>
        Your request for a client number cannot go ahead because “{{
          formData.businessInformation.businessName
        }}” is not in good standing with BC Registries. Go to your
        <a href="https://www.bcregistry.gov.bc.ca/" target="_blank"
          >BC Registries</a
        >
        account to find out why.
      </p>
    </display-block-component>

    <display-block-component
      v-show="showDuplicatedError"
      kind="error"
      title="Client already exists"
    >
      <p>
        Looks like “{{ formData.businessInformation.businessName }}” has a
        client number. Select the 'Receive email and logout' button below to
        have it sent to you at {{ formData.location.contacts[0].email }}
      </p>
    </display-block-component>
  </data-fetcher>

  <text-input-component
    v-if="selectedOption === ClientTypeEnum.U"
    id="businessName"
    label="Unregistered proprietorship"
    placeholder=""
    v-model="formData.businessInformation.businessName"
    :validations="[]"
    :enabled="false"
  />
  <br />
</template>
