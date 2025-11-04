<script setup lang="ts" generic="T">
import { ref, computed, watch, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/combo-box/index";
import "@carbon/web-components/es/components/inline-loading/index";
import type { CDSComboBox } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { BusinessSearchResult, CodeNameType, CodeNameValue } from "@/dto/CommonTypesDto";
import { highlightMatch } from "@/services/ForestClientService";
import { isEmpty, type ValidationMessageType } from "@/dto/CommonTypesDto";
import type { DROPDOWN_SIZE } from "@carbon/web-components/es/components/dropdown/defs";

//Define the input properties for this component
const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    ariaLabel?: string;
    tip?: string;
    placeholder?: string;
    size?: `${DROPDOWN_SIZE}`;
    modelValue: string;
    contents: Array<CodeNameValue<T>>;
    validations: Array<Function>;
    errorMessage?: string;
    loading?: boolean;
    showLoadingAfterTime?: number;
    required?: boolean;
    requiredLabel?: boolean;
    autocomplete?: string;
    validationsOnChange?: boolean | Array<Function>;
    preventSelection?: boolean;
  }>(),
  {
    showLoadingAfterTime: 2000,
  }
);

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:model-value", value: string): void;
  (e: "update:selected-value", value: BusinessSearchResult | undefined): void;
  (e: "press:enter"): void;
  (e: "select:item", value: string): void;
  (e: "click:item", value: string): void;
  (e: "press:enter:item", value: string): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const warning = ref(false);

watch(
  () => props.errorMessage,
  () => setError(props.errorMessage)
);

//We set the value prop as a reference for update reason
const inputValue = ref(props.modelValue);

const loadingName = "loading...";

const showLoadingTimer = ref<number>();

const showLoading = ref(false);

watch(
  () => props.loading && props.modelValue,
  (value, oldValue) => {
    if (oldValue || !value) {
      clearTimeout(showLoadingTimer.value);
      showLoading.value = false;
    }
    if (value) {
      showLoadingTimer.value = setTimeout(() => {
        showLoading.value = true;
      }, props.showLoadingAfterTime);
    }
  }
);

// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<CodeNameValue<T>>>(() => {
  if (props.contents?.length > 0) {
    return props.contents.filter((entry) => entry.name);
  } else if (props.modelValue !== userValue.value) {
    // Needed when the component mounts with a pre-filled value.
    return [{ name: props.modelValue, code: "", value: undefined }];
  } else if (props.modelValue && showLoading.value) {
    // Just to give a "loading" feedback.
    return [{ name: loadingName, code: "", value: undefined }];
  }
  return [];
});

const validationsOnChange = computed(() => {
  if (props.validationsOnChange) {
    return typeof props.validationsOnChange === "boolean"
      ? props.validations
      : props.validationsOnChange;
  }
  return false;
});

//This function emits the events on update
const emitValueChange = (newValue: string, isSelectEvent = false): void => {
  let selectedValue: BusinessSearchResult | undefined;
  if (isSelectEvent) {
    // Prevent selecting the empty value included when props.contents is empty.
    selectedValue = newValue
      ? inputList.value.find((entry) => entry.code === newValue)
      : undefined;
  }

  emit("update:model-value", selectedValue?.name ?? newValue ?? "");
  emit("update:selected-value", selectedValue);
  emit("empty", isEmpty(newValue));
};

emit("empty", isEmpty(props.modelValue));

const isUserEvent = ref(false);
const userValue = ref("");

const cdsComboBoxRef = ref<InstanceType<typeof CDSComboBox> | null>(null);
watch(
  () => props.modelValue,
  () => {
    inputValue.value = props.modelValue;
    if (!isUserEvent.value && cdsComboBoxRef.value) {
      cdsComboBoxRef.value._filterInputValue = props.modelValue || "";

      // Validate the SELECTED value immediately.
      validateInput(props.modelValue);
    }
    isUserEvent.value = false;
  }
);
watch([inputValue], () => {
  if (isUserEvent.value) {
    emitValueChange(inputValue.value);
    if (validationsOnChange.value) {
      validateInput(inputValue.value, validationsOnChange.value);
    }
  }
});

/**
 * Sets the error and emits an error event.
 * @param errorObject - the error object or string
 */
const setError = (errorObject: string | ValidationMessageType | undefined) => {
  const errorMessage =
    typeof errorObject === "object" ? errorObject.errorMsg : errorObject;
  error.value = errorMessage || "";

  warning.value = false;
  if (typeof errorObject === "object") {
    warning.value = errorObject.warning;
  }

  /*
  The error should be emitted whenever it is found, instead of watching and emitting only when it
  changes.
  Because the empty event is always emitted, even when it remains the same payload, and then we
  rely on empty(false) to consider a value "valid". In turn we need to emit a new error event after
  an empty one to allow subscribers to know in case the field still has the same error.
  */
  emit("error", error.value);
};

//We call all the validations
const validateInput = (newValue: string, validations = props.validations) => {
  if (validations) {
    setError(
      validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .reduce((acc, errorMessage) => acc || errorMessage, props.errorMessage)
    );
  }
};

const isClickSelectEvent = ref(false);
const isKeyboardSelectEvent = ref(false);

const selectAutocompleteItem = (event: any) => {
  const newValue = event?.detail?.item?.getAttribute("data-id");
  emitValueChange(newValue, true);
  validateInput(newValue);
};

const preSelectAutocompleteItem = (event: any) => {
  if (!isClickSelectEvent.value) {
    isKeyboardSelectEvent.value = true;
  }

  if (event?.detail?.item) {
    const newValue = event?.detail?.item?.getAttribute("data-id");
    emit("select:item", newValue);
    if (isClickSelectEvent.value) {
      emit("click:item", newValue);
    } else {
      emit("press:enter:item", newValue);
    }
    if (props.preventSelection) {
      event?.preventDefault();
    }
  }

  // resets the flag
  isClickSelectEvent.value = false;
};

const onPressEnter = () => {
  // prevents emitting the event when this is a selection with the keyboard
  if (!isKeyboardSelectEvent.value) {
    emit("press:enter");
  }

  // resets the flag
  isKeyboardSelectEvent.value = false;
};

const onItemClick = () => {
  isClickSelectEvent.value = true;
};

const onTyping = (event: any) => {
  isUserEvent.value = true;
  inputValue.value = event.srcElement._filterInputValue;
  userValue.value = inputValue.value;
  emit("update:model-value", inputValue.value);
};

revalidateBus.on((keys: string[] | undefined) => {
  if (keys === undefined || keys.includes(props.id)) {
    validateInput(inputValue.value);
  }
});

/*
By applying a suffix which is impossible to be typed to the items' names, the search input will
never be exactly the same as any item name.
This allows the user to select the item even when the provided search input is exactly the same as
the item name. (see: FSADT1-918)
Note: removing the value prop from the cds-combo-box-item is not an option, since it causes another
kind of issue when the field gets cleared.
*/
const nameSuffix = "\0";

/*
By checking the item has a code, we know this is a real option instead of a mock one.
We need the mock one (with no suffix) when the component mounts with a pre-filled value.
*/
const getComboBoxItemValue = (item: CodeNameType) =>
  item.name + (item.code ? nameSuffix : "");

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));
const isFocused = ref(false);

watch(
  [
    cdsComboBoxRef,
    () => props.required,
    () => props.label,
    isFocused,
    ariaInvalidString,
  ],
  async ([cdsComboBox]) => {
    if (cdsComboBox) {
      // wait for the DOM updates to complete
      await nextTick();

      const helperTextId = "helper-text";
      const helperText = cdsComboBox.shadowRoot?.querySelector(
        "[name='helper-text']"
      );
      
      if (helperText) {
        helperText.id = helperTextId;

        // For some reason the role needs to be dynamically changed to "alert" to announce.
        if (isFocused.value) {
          helperText.role = "generic";
        } else {
          helperText.role =
            ariaInvalidString.value === "true" ? "alert" : "generic";
        }
      }

      const input = cdsComboBox.shadowRoot?.querySelector("input");
      if (input) {
        // Propagate attributes to the input
        input.required = props.required;
        input.ariaLabel = props.ariaLabel || props.label;
        input.ariaInvalid = ariaInvalidString.value;

        // Use the helper text as a field description
        input.setAttribute("aria-describedby", helperTextId);
      }
    }
  }
);

// For some reason, if helper-text is empty, invalid-text message doesn't work.
const safeHelperText = computed(() => props.tip || " ");
</script>

<template>
  <div class="grouping-02">
    <div class="input-group">
      <cds-combo-box
        ref="cdsComboBoxRef"
        :id="id"
        :size="size"
        :class="warning ? 'warning' : ''"
        :autocomplete="autocomplete"
        :title-text="label"
        :aria-label="ariaLabel || label"
        :clear-selection-label="`Clear ${label}`"
        :required="required"
        :data-required-label="requiredLabel"
        :label="placeholder"
        :value="inputValue"
        filterable
        :invalid="!warning && error ? true : false"
        :aria-invalid="ariaInvalidString"
        :invalid-text="!warning && error"
        :warn="warning"
        :warn-text="warning && error"
        @cds-combo-box-selected="selectAutocompleteItem"
        @cds-combo-box-beingselected="preSelectAutocompleteItem"
        v-on:input="onTyping"
        @focus="isFocused = true"
        @blur="
          (event: any) => {
            isFocused = false;
            validateInput(event.srcElement._filterInputValue);
          }
        "
        @keypress.enter="onPressEnter"
        :data-focus="id"
        :data-scroll="id"
        :data-id="'input-' + id"
        v-shadow="4"
      >
        <cds-combo-box-item
          v-for="item in inputList"
          :key="item.code"
          :data-id="item.code"
          :data-value="item.name"
          :value="getComboBoxItemValue(item)"
          v-shadow
          :data-loading="item.name === loadingName"
          @click="onItemClick"
        >
          <template v-if="item.name === loadingName">
            <cds-inline-loading />
          </template>
          <template v-else>
            <slot :value="item.value">
              <span v-dompurify-html="highlightMatch(item.name, inputValue)"></span>
            </slot>
          </template>
        </cds-combo-box-item>
        <div slot="helper-text" v-if="!error" class="display-contents">
          <slot name="tip">
            {{ safeHelperText }}
          </slot>
        </div>
      </cds-combo-box>
    </div>
  </div>
</template>
