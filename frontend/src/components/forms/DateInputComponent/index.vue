<script setup lang="ts">
import { ref, watch, computed, reactive } from "vue";
// Carbon
import "@carbon/web-components/es/components/text-input/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import { isEmpty } from "@/dto/CommonTypesDto";
import { DatePart } from "./common";
// Validators
import {
  isNotEmpty,
  isOnlyNumbers,
  isMinSize,
  isMaxSize,
  isWithinRange,
  isValidDayOfMonth,
} from "@/helpers/validators/GlobalValidators";

// Define the input properties for this component
const props = defineProps<{
  id: string;
  tip?: string;
  enabled?: boolean;
  modelValue: string;
  validations: Array<Function>;
  errorMessage?: string;
  mask?: string;
  requiredLabel?: boolean;
}>();

// Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void
  (e: "empty", value: boolean): void
  (e: "update:model-value", value: string): void
}>();

// We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<void>("revalidate-bus");

const partError = reactive({
  [DatePart.year]: "",
  [DatePart.month]: "",
  [DatePart.day]: "",
});

/**
 * Sets the error and emits an error event.
 * @param errorMessage - the error message
 */
const setError = (errorMessage: string | undefined, datePart?: DatePart) => {
  /*
  The error should be emitted whenever it is found, instead of watching and emitting only when it
  changes.
  Because the empty event is always emitted, even when it remains the same payload, and then we
  rely on empty(false) to consider a value "valid". In turn we need to emit a new error event after
  an empty one to allow subscribers to know in case the field still has the same error.
  */

  if (datePart === undefined) {
    error.value = errorMessage;
  }

  if (datePart !== undefined) {
    partError[datePart] = errorMessage;
    if (!errorMessage) {
      /*
      Even if the current date part is valid, make sure not to clear the global error in case some
      other part contains error.
      */
      const newErrorMessage =
        partError[DatePart.year] || partError[DatePart.month] || partError[DatePart.day];
      error.value = newErrorMessage;
    } else {
      error.value = errorMessage;
    }
  }

  emit("error", error.value);
};

watch(
  () => props.errorMessage,
  () => setError(props.errorMessage),
)

const datePartPositions = {
  [DatePart.year]: 1,
  [DatePart.month]: 2,
  [DatePart.day]: 3,
};

// const getDatePart = (datePart: DatePart) =>
//   props.modelValue ? props.modelValue.substring(...datePartPositions[datePart]) : "";

const regex = /(.*)-(.*)-(.*)/;

const getDatePart = (datePart: DatePart) =>
  props.modelValue ? regex.exec(props.modelValue)[datePartPositions[datePart]] : "";

// We set it as a separated ref due to props not being updatable
const selectedYear = ref<string>(getDatePart(DatePart.year));
const selectedMonth = ref<string>(getDatePart(DatePart.month));
const selectedDay = ref<string>(getDatePart(DatePart.day));

const buildFullDate = () => `${selectedYear.value}-${selectedMonth.value}-${selectedDay.value}`

const areAllPartsValid = () =>
  validation[DatePart.year] && validation[DatePart.month] && validation[DatePart.day];

const selectedValue = ref<string | null>(props.modelValue);

// We set the value prop as a reference for update reason
emit("empty", isEmpty(props.modelValue));

// This function emits the events on update
const emitValueChange = (newValue: string): void => {
  emit("update:model-value", newValue)
  emit("empty", isEmpty(newValue))
};

// Watch for changes on the input
watch([selectedValue], () => {
  emitValueChange(selectedValue.value)
});

// We call all the part validations
const validatePart = (datePart: DatePart) => {
  const newValue = datePartRefs[datePart].value;
  const error = basicValidators.value[datePart]
    .map((validation) => validation(newValue))
    .filter((errorMessage) => {
      if (errorMessage) return true
      return false
    })
    .shift();
  setError(error, datePart);
  return !error;
}

// We call all the business validations
const validateBusiness = (newValue: string) => {
  if (props.validations) {
    setError(
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true
          return false
        })
        .shift() ?? props.errorMessage,
    )
  }
}

const datePartRefs = {
  [DatePart.year]: selectedYear,
  [DatePart.month]: selectedMonth,
  [DatePart.day]: selectedDay,
}

const title = "Date of birth";

// const validation = computed(() => {
//   const value = {
//     [DatePart.year]: validatePart(DatePart.year),
//     [DatePart.month]: validatePart(DatePart.month),
//     [DatePart.day]: false,
//   };
//   // The validation for day has to occur after the ones for year and month.
//   value[DatePart.day] = validatePart(DatePart.day);
//   return value;
// });

const validation = reactive({
  [DatePart.year]: false,
  [DatePart.month]: false,
  [DatePart.day]: false,
});

const basicValidators = computed(() => {
  const additionalDayValidations = [];
  if (validation[DatePart.year] && validation[DatePart.month]) {
    additionalDayValidations.push(
      isValidDayOfMonth(selectedYear.value, selectedMonth.value, `${title} must be a real date`),
    );
  }
  return {
    [DatePart.year]: [
      isNotEmpty(`${title} must include a year`),
      isOnlyNumbers("Year must include 4 numbers"),
      isMinSize("Year must include 4 numbers")(4),
      isMaxSize("Year must include 4 numbers")(4),
    ],
    [DatePart.month]: [
      isNotEmpty(`${title} must include a month`),
      isOnlyNumbers("Month must include 2 numbers"),
      isMinSize("Month must include 2 numbers")(2),
      isMaxSize("Month must include 2 numbers")(2),
      isWithinRange(1, 12, `${title} must be a real date`),
    ],
    [DatePart.day]: [
      isNotEmpty(`${title} must include a day`),
      isOnlyNumbers("Day must include 2 numbers"),
      isMinSize("Day must include 2 numbers")(2),
      isMaxSize("Day must include 2 numbers")(2),
      isWithinRange(1, 31, `${title} must be a real date`),
      ...additionalDayValidations,
    ],
  };
});

const onBlurPart = (datePart: DatePart) => (partNewValue: string) => {
  const datePartRef = datePartRefs[datePart];
  datePartRef.value = partNewValue;

  validation[datePart] = validatePart(datePart);

  if (datePart !== DatePart.day && Number(selectedDay.value) > 28) {
    validation[DatePart.day] = validatePart(DatePart.day);
  }

  selectedValue.value = buildFullDate();
  if (areAllPartsValid()) {
    validateBusiness(selectedValue.value);
  }
  isUserEvent.value = true;
};

const onBlurYear = onBlurPart(DatePart.year);
const onBlurMonth = onBlurPart(DatePart.month);
const onBlurDay = onBlurPart(DatePart.day);

revalidateBus.on(() => {
  validateBusiness(selectedValue.value);
});

watch(
  () => props.modelValue,
  () => {
    // We don't want to validate at each key pressed, but we do want to validate when it's changed from outside.
    if (!isUserEvent.value) {
      selectedYear.value = getDatePart(DatePart.year);
      validation[DatePart.year] = validatePart(DatePart.year);

      selectedMonth.value = getDatePart(DatePart.month);
      validation[DatePart.month] = validatePart(DatePart.month);

      selectedDay.value = getDatePart(DatePart.day);
      validation[DatePart.day] = validatePart(DatePart.day);

      if (areAllPartsValid()) {
        validateBusiness(selectedValue.value)
      }
    }

    // resets variable
    isUserEvent.value = false;
  },
);

// Tells whether the current change was done manually by the user.
const isUserEvent = ref(false);

const selectValue = (datePart: DatePart) => (event: any) => {
  const datePartRef = datePartRefs[datePart];
  datePartRef.value = event.target.value;
  isUserEvent.value = true;
};

const selectYear = selectValue(DatePart.year);
const selectMonth = selectValue(DatePart.month);
const selectDay = selectValue(DatePart.day);
</script>

<style scoped>
.grouping-02 {
  flex-direction: row;
  gap: 1rem;
}

.grouping-02:has([invalid]) ~ .field-error {
  display: block;
  overflow: visible;
  max-height: 12.5rem;
  font-weight: 400;
  color: var(--cds-text-error,#da1e28);
}

:deep([id$="Year"]) {
  width: 7.5rem;
}
:deep([id$="Month"]), :deep([id$="Day"]) {
  width: 5.625rem;
}
:deep(cds-text-input) {
  display: block;
}
:deep(cds-text-input::part(svg)) {
  right: 1rem;
}
</style>

<template>
  <div>
    <div class="grouping-02" v-if="enabled" :id="id">
      <date-input-part
        :parent-id="id"
        :datePart="DatePart.year"
        :selectedValue="selectedYear"
        :enabled="enabled"
        :invalid="!!partError[DatePart.year]"
        @blur="(event: any) => onBlurYear(event.target.value)"
        @input="selectYear"
      />
      <date-input-part
        :parent-id="id"
        :datePart="DatePart.month"
        :selectedValue="selectedMonth"
        :enabled="enabled"
        :invalid="!!partError[DatePart.month]"
        @blur="(event: any) => onBlurMonth(event.target.value)"
        @input="selectMonth"
      />
      <date-input-part
        :parent-id="id"
        :datePart="DatePart.day"
        :selectedValue="selectedDay"
        :enabled="enabled"
        :invalid="!!partError[DatePart.day]"
        @blur="(event: any) => onBlurDay(event.target.value)"
        @input="selectDay"
      />
    </div>
    <div class="cds--form-requirement field-error">{{ error }}</div>
  </div>

  <div v-if="!enabled" class="grouping-04" :id="id">
    <div :data-scroll="id + 'Year'" class="grouping-04-label">
      <span :for="id + 'Year'" class="label-01">Year</span>
    </div>
    <span class="text-01">{{ selectedYear }}</span>
  </div>
  <div v-if="!enabled" class="grouping-04" :id="id">
    <div :data-scroll="id + 'Month'" class="grouping-04-label">
      <span :for="id + 'Month'" class="label-01">Month</span>
    </div>
    <span class="text-01">{{ selectedMonth }}</span>
  </div>
  <div v-if="!enabled" class="grouping-04" :id="id">
    <div :data-scroll="id + 'Day'" class="grouping-04-label">
      <span :for="id + 'Day'" class="label-01">Day</span>
    </div>
    <span class="text-01">{{ selectedDay }}</span>
  </div>
</template>
