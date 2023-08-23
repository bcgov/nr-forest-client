<script setup lang="ts">
import { reactive, watch, ref, onMounted } from 'vue'
import Delete16 from '@carbon/icons-vue/es/trash-can/16'
import type { CodeDescrType, CodeNameType } from '@/dto/CommonTypesDto'
import type { Contact } from '@/dto/ApplyClientNumberDto'
import {
  isNotEmpty,
  isEmail,
  isPhoneNumber,
  isMaxSize,
  isMinSize,
  isNoSpecialCharacters
} from '@/helpers/validators/GlobalValidators'
import { submissionValidation } from '@/helpers/validators/SubmissionValidators'
import useFocus from '@/composables/useFocus'

//Define the input properties for this component
const props = defineProps<{
  id: number
  modelValue: Contact
  enabled?: boolean
  roleList: Array<CodeNameType>
  addressList: Array<CodeNameType>
  validations: Array<Function>
  revalidate?: boolean
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'valid', value: boolean): void
  (e: 'update:model-value', value: Contact | undefined): void
  (e: 'remove', value: number): void
}>()

const { setFocusedComponent } = useFocus();
const noValidation = (value: string) => ''

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Contact>(props.modelValue)
const validateData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0]('Name', props.id + '')
const error = ref<string | undefined>('')

const uniquenessValidation = () => {
  error.value = validateData(
    `${selectedValue.firstName} ${selectedValue.lastName}`
  )

}

//Watch for changes on the input
watch([selectedValue], () => {
  uniquenessValidation()
  emit('update:model-value', selectedValue)
})

watch(
  () => props.revalidate,
  () => uniquenessValidation(),
  { immediate: true }
)

//Validations
const validation = reactive<Record<string, boolean>>({})

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  )

watch([validation], () => emit('valid', checkValid()))
emit('valid', false)

//Data conversion

const nameTypeToCodeDescr = (
  value: CodeNameType | undefined
): CodeDescrType => {
  if (value) return { value: value.code, text: value.name }
  return { value: '', text: '' }
}

const nameTypesToCodeDescr = (
  values: CodeNameType[] | undefined
): CodeDescrType[] => {
  if (values) return values.map(nameTypeToCodeDescr)
  return []
}

onMounted(() =>{
  setFocusedComponent(`address_${props.id}`)
})
</script>

<template>
  <div class="frame-01">
  <multiselect-input-component
    :id="'address_' + id"
    label="Address name"
    tip="Select an address name for the contact. A contact can have more than one address"
    :initial-value="''"
    :model-value="addressList"
    :selectedValues="selectedValue.locationNames?.map((location:CodeDescrType) => location?.value)"
    :validations="[
      isNotEmpty,
      submissionValidation(`location.contacts[${id}].province`)
    ]"
    @update:selected-value="
      selectedValue.locationNames = nameTypesToCodeDescr($event)
    "
    @empty="validation.locationNames = !$event"
  />

  <dropdown-input-component
    :id="'role_' + id"
    label="Primary role"
    tip="Choose the primary role for this contact"
    :initial-value="selectedValue.contactType.value"
    :model-value="roleList"
    :validations="[
      submissionValidation(`location.contacts[${id}].contactType`)
    ]"
    @update:selected-value="
      selectedValue.contactType = nameTypeToCodeDescr($event)
    "
    @empty="validation.contactType = !$event"
  />

  <text-input-component
    :id="'firstName_' + id"
    label="First name"
    placeholder="First name"
    v-model="selectedValue.firstName"
    :validations="[
      isMinSize(1),
      isMaxSize(25),
      isNotEmpty,
      isNoSpecialCharacters,
      submissionValidation(`location.contacts[${id}].firstName`)
    ]"
    :enabled="enabled"
    :error-message="error"
    @empty="validation.firstName = !$event"
  />

  <text-input-component
    :id="'lastName_' + id"
    label="Last name"
    placeholder="Last name"
    v-model="selectedValue.lastName"
    :validations="[
      isMinSize(1),
      isMaxSize(25),
      isNotEmpty,
      isNoSpecialCharacters,
      submissionValidation(`location.contacts[${id}].lastName`)
    ]"
    :enabled="enabled"
    :error-message="error"
    @empty="validation.lastName = !$event"
  />

  <text-input-component
    :id="'email_' + id"
    label="Email address"
    placeholder="Email"
    v-model="selectedValue.email"
    :validations="[
      isNotEmpty,
      isEmail,
      isMinSize(6),
      isMaxSize(50),
      submissionValidation(`location.contacts[${id}].email`)
    ]"
    :enabled="enabled"
    @empty="validation.email = !$event"
  />

  <text-input-component
    :id="'phoneNumber_' + id"
    label="Phone number"
    placeholder="( ) ___-____"
    mask="(###) ###-####"
    v-model="selectedValue.phoneNumber"
    :enabled="true"
    :validations="[
      isNotEmpty,
      isPhoneNumber,
      isMaxSize(15),
      isMinSize(10),
      submissionValidation(`location.contacts[${id}].phoneNumber`)
    ]"
    @empty="validation.phoneNumber = !$event"
  />

  <bx-btn
    v-show="id > 0"
    kind="danger-ghost"
    iconLayout=""
    class="bx--btn"
    @click.prevent="emit('remove', id)"
    size="field"
  >
    <span>Delete contact</span>
    <Delete16 slot="icon" />
  </bx-btn>
</div>
</template>
