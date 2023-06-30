<script setup lang="ts">
import { reactive, watch, computed, ref } from 'vue'
import { useEventBus } from '@vueuse/core'
import delete16 from '@carbon/icons-vue/es/trash-can/16'
import type { CodeNameType, BusinessSearchResult } from '@/core/CommonTypes'
import type { Address } from '@/dto/ApplyClientNumberDto'
import {
  isNotEmpty,
  isCanadianPostalCode,
  isUsZipCode,
  isOnlyNumbers,
  isMaxSize,
  isMinSize,
  isNoSpecialCharacters
} from '@/helpers/validators/GlobalValidators'
import { submissionValidation } from '@/helpers/validators/SubmissionValidators'
import { useFetchTo } from '@/composables/useFetch'

//Define the input properties for this component
const props = defineProps<{
  id: number
  modelValue: Address
  countryList: Array<CodeNameType>
  validations: Array<Function>
  revalidate?: boolean
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'valid', value: boolean): void
  (e: 'update:model-value', value: Address | undefined): void
  (e: 'remove', value: number): void
}>()

const generalErrorBus = useEventBus<string>('general-error-notification')

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Address>(props.modelValue)
const validateAddressData = props.validations[0]('Address', props.id + '')
const validateAddressNameData = props.validations[0]('Names', props.id + '')
const addressError = ref<string | undefined>('')
const nameError = ref<string | undefined>('')
const showDetailsLoading = ref<boolean>(false)

//Watch for changes on the input
watch([selectedValue], () => {
  addressError.value = validateAddressData(
    `${selectedValue.streetAddress} ${selectedValue.country.value} ${selectedValue.province.value} ${selectedValue.city} ${selectedValue.city} ${selectedValue.postalCode}`
  )
  nameError.value = validateAddressNameData(selectedValue.locationName)
  emit('update:model-value', selectedValue)
})

watch(
  () => props.revalidate,
  () => {
    addressError.value = validateAddressData(
      `${selectedValue.streetAddress} ${selectedValue.country.value} ${selectedValue.province.value} ${selectedValue.city} ${selectedValue.city} ${selectedValue.postalCode}`
    )
    nameError.value = validateAddressNameData(selectedValue.locationName)
  }
)

const updateStateProvince = (
  value: CodeNameType | undefined,
  property: string
) => {
  if (value && (property === 'country' || property === 'province')) {
    selectedValue[property] = { value: value.code, text: value.name }
  }
}

//Province related data
const provinceUrl = computed(
  () =>
    `/api/clients/activeCountryCodes/${selectedValue.country.value}?page=0&size=250`
)

const resetProvinceOnChange = (receivedCountry: any) => {
  if (selectedValue.country.value !== receivedCountry && receivedCountry)
    selectedValue.province = { value: '', text: '' }
}

//Validations
const validation = reactive<Record<string, boolean>>({
  locationName: props.id == 0,
  streetAddress: false,
  country: false,
  province: false,
  city: false,
  postalCode: false
})

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  )

watch([validation], () => emit('valid', checkValid()))
emit('valid', false)

const postalCodeValidators = computed(() => {
  switch (selectedValue.country.value) {
    case 'CA':
      return [
        isCanadianPostalCode,
        submissionValidation(`location.adresses[${props.id}].postalCode`)
      ]
    case 'US':
      return [
        isUsZipCode,
        submissionValidation(`location.adresses[${props.id}].postalCode`)
      ]
    default:
      return [
        isOnlyNumbers,
        isMinSize(5),
        isMaxSize(10),
        submissionValidation(`location.adresses[${props.id}].postalCode`)
      ]
  }
})

const postalCodeMask = computed(() => {
  switch (selectedValue.country.value) {
    case 'CA':
      return 'A#A#A#'
    case 'US':
      return '#####'
    default:
      return '##########'
  }
})

const postalCodePlaceholder = computed(() => {
  switch (selectedValue.country.value) {
    case 'CA':
      return 'A1A1A1'
    case 'US':
      return '99999'
    default:
      return '12345'
  }
})

const provinceNaming = computed(() => {
  switch (selectedValue.country.value) {
    case 'CA':
      return 'Province or territory'
    case 'US':
      return 'State'
    default:
      return 'Province/terrytory or state'
  }
})

const postalCodeNaming = computed(() =>
  selectedValue.country.value === 'US' ? 'Zip code' : 'Postal code'
)

const autoCompleteUrl = computed(
  () =>
    `/api/clients/addresses?country=${selectedValue.country.value}&maxSuggestions=10&searchTerm=${selectedValue.streetAddress}`
)
const autoCompleteResult = ref<BusinessSearchResult>({} as BusinessSearchResult)
const detailsData = ref<Address | null>(null)

watch([autoCompleteResult], () => {
  if (autoCompleteResult.value) {
    showDetailsLoading.value = true
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/clients/addresses/${encodeURIComponent(
        autoCompleteResult.value.code
      )}`,
      detailsData,
      {}
    )

    watch([error], () => {
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
    selectedValue.streetAddress = detailsData.value.streetAddress
    selectedValue.city = detailsData.value.city
    selectedValue.province = detailsData.value.province
    selectedValue.postalCode = detailsData.value.postalCode
  }
})
</script>

<template>
  <text-input-component
    :id="'name_' + id"
    label="Location or address name"
    placeholder="Kamloops office"
    tip="For example, 'Campbell River Region' or 'Castlegar Woods Division'"
    v-model="selectedValue.locationName"
    :enabled="true"
    :validations="[
      isNotEmpty,
      isMinSize(3),
      isMaxSize(50),
      isNoSpecialCharacters,
      submissionValidation(`location.adresses[${id}].locationName`)
    ]"
    :error-message="nameError"
    @empty="validation.locationName = !$event"
    v-if="props.id !== 0"
  />

  <dropdown-input-component
    :id="'country_' + id"
    label="Country"
    :initial-value="selectedValue.country.value"
    tip=""
    :enabled="true"
    :model-value="countryList"
    :validations="[submissionValidation(`location.adresses[${id}].country`)]"
    :error-message="addressError"
    @update:selected-value="updateStateProvince($event, 'country')"
    @update:model-value="resetProvinceOnChange"
    @empty="validation.country = !$event"
  />

  <data-fetcher
    v-model:url="autoCompleteUrl"
    :min-length="3"
    :init-value="[]"
    :init-fetch="false"
    #="{ content, loading, error }"
  >
    <AutoCompleteInputComponent
      :id="'addr_' + id"
      label="Street address or PO box"
      placeholder="Start typing to search for your address or PO box"
      tip=""
      v-model="selectedValue.streetAddress"
      :contents="content"
      :validations="[
        isNotEmpty,
        isMinSize(5),
        isMaxSize(50),
        submissionValidation(`location.adresses[${id}].streetAddress`)
      ]"
      :loading="loading"
      @update:selected-value="autoCompleteResult = $event"
      @update:model-value="validation.streetAddress = false"
      @empty="
        validation.streetAddress = selectedValue.streetAddress ? true : false
      "
    />
    <div class="spinner-block" v-if="showDetailsLoading">
      <bx-loading type="small"> </bx-loading>
      <span>Loading address details...</span>
    </div>
  </data-fetcher>

  <text-input-component
    :id="'city_' + id"
    label="City"
    placeholder="City"
    v-model="selectedValue.city"
    tip=""
    :enabled="true"
    :error-message="addressError"
    :validations="[
      isNotEmpty,
      isMinSize(3),
      isMaxSize(50),
      isNoSpecialCharacters,
      submissionValidation(`location.adresses[${id}].city`)
    ]"
    @empty="validation.city = !$event"
  />

  <data-fetcher
    v-model:url="provinceUrl"
    :min-length="0"
    :init-value="[]"
    :init-fetch="true"
    :params="{ method: 'GET' }"
    #="{ content }"
  >
    <dropdown-input-component
      :id="'province_' + id"
      :label="provinceNaming"
      :initial-value="selectedValue.province.value"
      :model-value="content"
      :enabled="true"
      tip=""
      :validations="[submissionValidation(`location.adresses[${id}].province`)]"
      :error-message="addressError"
      @update:selected-value="updateStateProvince($event, 'province')"
      @empty="validation.province = !$event"
    />
  </data-fetcher>

  <text-input-component
    :id="'postalCode_' + id"
    :label="postalCodeNaming"
    :placeholder="postalCodePlaceholder"
    :enabled="true"
    tip=""
    :error-message="addressError"
    v-model="modelValue.postalCode"
    :mask="postalCodeMask"
    :validations="postalCodeValidators"
    @empty="validation.postalCode = !$event"
  />

  <bx-btn
    v-show="id > 0"
    kind="danger-ghost"
    iconLayout=""
    class="bx--btn"
    @click.prevent="emit('remove', id)"
    size="field"
  >
    <span>Delete address</span>
    <delete16 slot="icon" />
  </bx-btn>
</template>
