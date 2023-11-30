<script setup lang="ts">
import { ref, watch, computed, reactive } from "vue";
// Imported components
import DateInputPart from "./DateInputPart.vue";
// Carbon
import "@carbon/web-components/es/components/text-input/index";
// Composables
import { useEventBus } from "@vueuse/core";
import { useFocus } from "@/composables/useFocus";
// Types
import { isEmpty } from "@/dto/CommonTypesDto";
import { DatePart, type DateValidator } from "./common";
// Validators
import {
  isNotEmpty,
  isOnlyNumbers,
  isMinSize,
  isMaxSize,
  isWithinRange,
  isValidDayOfMonth,
  isValidDayOfMonthYear,
} from "@/helpers/validators/GlobalValidators";

// Define the input properties for this component
const props = defineProps<{
  id: string;

  /** Used in validation messages */
  title: string;

  enabled?: boolean;
  modelValue: string;
  validations: Array<DateValidator>;
  errorMessage?: string;
  mask?: string;
  requiredLabel?: boolean;
}>();

// Events we emit during component lifecycle
const emit = defineEmits<{
  /**
   * An error message which might be related to any date part.
   * `falsy` means no parts contain error.
   */
  (e: "error", value: string | undefined): void;

  /**
   * `true` means one or more parts are empty.
   * `false` means no parts are empty.
   */
  (e: "empty", value: boolean): void;

  /**
   * Means the currently focused part is not empty and the 2 other parts are valid.
   */
  (e: "possibly-valid", value: boolean): void;

  (e: "update:model-value", value: string): void;
}>();

// We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<void>("revalidate-bus");

const partError = reactive({
  [DatePart.year]: "",
  [DatePart.month]: "",
  [DatePart.day]: "",
});

const fullDateError = ref("");

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
    fullDateError.value = errorMessage;
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

    if (error.value) {
      // Stop highlighting all parts because now we have a part-specific error, thus we can't validate the full date.
      fullDateError.value = "";
    }
  }

  // Do not emit error (falsy) if there are invalid parts.
  // The error is currently falsy just because some date parts were not interected with yet.
  if (!error.value && !areAllPartsValid()) {
    return;
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

const regex = /(.*)-(.*)-(.*)/;

const getDatePart = (datePart: DatePart) =>
  props.modelValue ? regex.exec(props.modelValue)[datePartPositions[datePart]] : "";

// We set it as a separated ref due to props not being updatable
const selectedValue = ref<string | null>(props.modelValue);

const selectedYear = ref<string>(getDatePart(DatePart.year));
const selectedMonth = ref<string>(getDatePart(DatePart.month));
const selectedDay = ref<string>(getDatePart(DatePart.day));

const buildFullDate = () => `${selectedYear.value}-${selectedMonth.value}-${selectedDay.value}`

const areAllPartsValid = () =>
  validation[DatePart.year] && validation[DatePart.month] && validation[DatePart.day];

const isAnyPartEmpty = () =>
  isEmpty(selectedYear.value) || isEmpty(selectedMonth.value) || isEmpty(selectedDay.value);

// We set the value prop as a reference for update reason
emit("empty", isAnyPartEmpty());

const focusedPart = ref<DatePart | null>(null);

const getPartsExcept = (datePart: DatePart) =>
  Object.values(DatePart)
    .filter((current) => typeof current === typeof DatePart.year) // get the type from any enum value
    .filter((current) => current !== datePart) as DatePart[];

// This function emits the events on update
const emitValueChange = (newValue: string): void => {
  emit("update:model-value", newValue);
  const someEmpty = isAnyPartEmpty();
  emit("empty", someEmpty);
  let possiblyValid = false;
  if (!someEmpty && focusedPart.value !== null) {
    const otherParts = getPartsExcept(focusedPart.value);
    const allOtherAreValid = otherParts.every((part) => validation[part]);
    if (allOtherAreValid) {
      possiblyValid = true;
    }
  }
  emit("possibly-valid", possiblyValid);
};

// Watch for changes on the input
watch([selectedValue], () => {
  emitValueChange(selectedValue.value)
});

// We call all the part validations
const validatePart = (datePart: DatePart) => {
  const newValue = datePartRefs[datePart].value;
  const error = partValidators.value[datePart]
    .map((validation) => validation(newValue))
    .filter((errorMessage) => {
      if (errorMessage) return true
      return false
    })
    .shift();
  setError(error, datePart);
  return !error;
}

// We call all the full date validations
const validateFullDate = (newValue: string) => {
  if (props.validations) {
    const error = props.validations
        .filter(({ datePart }) => datePart === undefined)
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true
          return false
        })
        .shift() ?? props.errorMessage;
    setError(error);
    return !error
  }
  return true;
}

const datePartRefs = {
  [DatePart.year]: selectedYear,
  [DatePart.month]: selectedMonth,
  [DatePart.day]: selectedDay,
}

const validation = reactive({
  [DatePart.year]: false,
  [DatePart.month]: false,
  [DatePart.day]: false,
});

const validationFullDate = ref(false);

const year4DigitsMessage = "Year must have 4 digits";
const month2DigitsMessage = "Month must have 2 digits. For example, 01 for January";
const day2DigitsMessage = "Day must have 2 digits";

const partValidators = computed(() => ({
  [DatePart.year]: [
    isNotEmpty(`${props.title} must include a year`),
    isOnlyNumbers(year4DigitsMessage),
    isMinSize(year4DigitsMessage)(4),
    isMaxSize(year4DigitsMessage)(4),
    ...props.validations.filter(({ datePart }) => datePart === DatePart.year),
  ],
  [DatePart.month]: [
    isNotEmpty(`${props.title} must include a month`),
    isOnlyNumbers(month2DigitsMessage),
    isMinSize(month2DigitsMessage)(2),
    isMaxSize(month2DigitsMessage)(2),
    isWithinRange(1, 12, `${props.title} must be a real date`),
    ...props.validations.filter(({ datePart }) => datePart === DatePart.month),
  ],
  [DatePart.day]: [
    isNotEmpty(`${props.title} must include a day`),
    isOnlyNumbers(day2DigitsMessage),
    isMinSize(day2DigitsMessage)(2),
    isMaxSize(day2DigitsMessage)(2),
    isWithinRange(1, 31, `${props.title} must be a real date`),
    (value: string) => {
      if (!validation[DatePart.month]) {
        return "";
      }
      return isValidDayOfMonth(selectedMonth.value, `${props.title} must be a real date`)(value);
    },
    (value: string) => {
      if (!validation[DatePart.year] || !validation[DatePart.month]) {
        return "";
      }
      return isValidDayOfMonthYear(
        selectedYear.value,
        selectedMonth.value,
        `${props.title} must be a real date`,
      )(value);
    },
    ...props.validations.filter(({ datePart }) => datePart === DatePart.day),
  ],
}));

// Update validation status on setup
validation[DatePart.year] = selectedYear.value && validatePart(DatePart.year);
validation[DatePart.month] = selectedYear.value && validatePart(DatePart.month);
validation[DatePart.day] = selectedYear.value && validatePart(DatePart.day);

if (areAllPartsValid()) {
  validationFullDate.value = validateFullDate(selectedValue.value);
}

const onBlurPart = (datePart: DatePart) => (partNewValue: string) => {
  focusedPart.value = null;
  const datePartRef = datePartRefs[datePart];
  datePartRef.value = partNewValue;

  validation[datePart] = validatePart(datePart);

  if (
    datePart !== DatePart.day &&
    Number(selectedDay.value) >= 29 &&
    Number(selectedDay.value) <= 31
  ) {
    validation[DatePart.day] = validatePart(DatePart.day);
  }

  if (areAllPartsValid()) {
    validateFullDate(selectedValue.value);
  }
  isUserEvent.value = true;
};

const onBlurYear = onBlurPart(DatePart.year);
const onBlurMonth = onBlurPart(DatePart.month);
const onBlurDay = onBlurPart(DatePart.day);

revalidateBus.on(() => {
  validation[DatePart.year] = validatePart(DatePart.year);
  validation[DatePart.month] = validatePart(DatePart.month);
  validation[DatePart.day] = validatePart(DatePart.day);
  validateFullDate(selectedValue.value);
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
        validateFullDate(selectedValue.value)
      }
    }

    // resets variable
    isUserEvent.value = false;
  },
);

// Tells whether the current change was done manually by the user.
const isUserEvent = ref(false);

const partLength = {
  [DatePart.year]: 4,
  [DatePart.month]: 2,
  [DatePart.day]: 2,
};

const { setFocusedComponent } = useFocus();

const selectValue = (datePart: DatePart) => (event: any) => {
  focusedPart.value = datePart;
  const datePartRef = datePartRefs[datePart];
  datePartRef.value = event.target.value;
  isUserEvent.value = true;
  selectedValue.value = buildFullDate();
  if (datePartRef.value.length >= partLength[datePart]) {
    const nextDatePartIndex = datePart + 1;
    if (DatePart[nextDatePartIndex] !== undefined) {
      const nextDatePart = nextDatePartIndex as DatePart;
      const nextComponent = datePartComponentRefs[nextDatePart].value;
      if (nextComponent) {
        setFocusedComponent(nextComponent.id)
      }
    }
  }
};

const selectYear = selectValue(DatePart.year);
const selectMonth = selectValue(DatePart.month);
const selectDay = selectValue(DatePart.day);

const datePartComponentRefs = {
  [DatePart.year]: ref<InstanceType<typeof DateInputPart> | null>(null),
  [DatePart.month]: ref<InstanceType<typeof DateInputPart> | null>(null),
  [DatePart.day]: ref<InstanceType<typeof DateInputPart> | null>(null),
};

const datePartOrder = [DatePart.year, DatePart.month, DatePart.day];
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
        :ref="datePartComponentRefs[DatePart.year]"
        :parent-id="id"
        :datePart="DatePart.year"
        :selectedValue="selectedYear"
        :enabled="enabled"
        :invalid="!!partError[DatePart.year] || !!fullDateError"
        @blur="(event: any) => onBlurYear(event.target.value)"
        @input="selectYear"
      />
      <date-input-part
        :ref="datePartComponentRefs[DatePart.month]"
        :parent-id="id"
        :datePart="DatePart.month"
        :selectedValue="selectedMonth"
        :enabled="enabled"
        :invalid="!!partError[DatePart.month] || !!fullDateError"
        @blur="(event: any) => onBlurMonth(event.target.value)"
        @input="selectMonth"
      />
      <date-input-part
        :ref="datePartComponentRefs[DatePart.day]"
        :parent-id="id"
        :datePart="DatePart.day"
        :selectedValue="selectedDay"
        :enabled="enabled"
        :invalid="!!partError[DatePart.day] || !!fullDateError"
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
