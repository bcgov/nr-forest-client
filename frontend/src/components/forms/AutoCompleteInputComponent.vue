<script setup lang="ts">
import { ref, computed, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/combo-box/index";
import "@carbon/web-components/es/components/inline-loading/index";
import type { CDSComboBox } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { BusinessSearchResult, CodeNameType } from "@/dto/CommonTypesDto";
import { isEmpty } from "@/dto/CommonTypesDto";

//Define the input properties for this component
const props = withDefaults(defineProps<{
    id: string;
    label: string;
    tip?: string;
    placeholder?: string;
    modelValue: string;
    contents: Array<BusinessSearchResult>;
    validations: Array<Function>;
    errorMessage?: string;
    loading?: boolean;
    showLoadingAfterTime?: number;
  }>(),
  {
    showLoadingAfterTime: 2000,
  },
);

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:model-value", value: string): void;
  (e: "update:selected-value", value: BusinessSearchResult | undefined): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage || "");

const revalidateBus = useEventBus<void>("revalidate-bus");

watch(
  () => props.errorMessage,
  () => setError(props.errorMessage),
);

//We set the value prop as a reference for update reason
const inputValue = ref(props.modelValue);

const LOADING_NAME = "loading...";

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
  },
)

// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<BusinessSearchResult>>(() => {
  if (props.contents?.length > 0) {
    return props.contents.filter((entry) => entry.name);
  } else if (props.modelValue !== userValue.value) {
    // Needed when the component mounts with a pre-filled value.
    return [{ name: props.modelValue, code: "", status: "", legalType: "" }]
  } else if (props.modelValue && showLoading.value) {
    // Just to give a "loading" feedback.
    return [{ name: LOADING_NAME, code: "", status: "", legalType: "" }];
  }
  return [];
});

let selectedValue: BusinessSearchResult | undefined = undefined;

//This function emits the events on update
const emitValueChange = (newValue: string): void => {

  // Prevent selecting the empty value included when props.contents is empty.
  selectedValue = newValue ? inputList.value.find((entry) => entry.code === newValue) : undefined;

  emit("update:model-value", selectedValue?.name ?? newValue);
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
    }
    isUserEvent.value = false;
  },
);
watch([inputValue], () => {
  emitValueChange(inputValue.value);
});

const setError = (errorMessage: string | undefined) => {
  error.value = errorMessage;
  emit("error", error.value);
}

//We call all the validations
const validateInput = (newValue: string) => {
  if (props.validations) {
    setError(
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .shift() ?? props.errorMessage,
    );
  }
};

const selectAutocompleteItem = (event: any) => {
  const newValue = event?.detail?.item?.getAttribute("data-id");
  emitValueChange(newValue);
  validateInput(newValue);
};

const onTyping = (event: any) => {
  isUserEvent.value = true;
  inputValue.value = event.srcElement._filterInputValue;
  userValue.value = inputValue.value;
  emit("update:model-value", inputValue.value);
};

revalidateBus.on(() => validateInput(inputValue.value));

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
const getComboBoxItemValue = (item: CodeNameType) => item.name + (item.code ? nameSuffix : "");
</script>

<template>
  <div class="grouping-02">
    <cds-combo-box
      ref="cdsComboBoxRef"
      :id="id"
      :name="id"
      :helper-text="tip"
      :title-text="label"
      :value="inputValue"
      filterable
      :invalid="error ? true : false"
      :invalid-text="error"
      @cds-combo-box-selected="selectAutocompleteItem"
      v-on:input="onTyping"
      v-on:blur="(event: any) => validateInput(event.srcElement._filterInputValue)"
      :data-focus="id"
      :data-scroll="id"
      :data-id="'input-' + id"
      v-shadow="3"
    >
      <cds-combo-box-item
        v-for="item in inputList"
        :key="item.code"
        :data-id="item.code"
        :data-value="item.name"
        :value="getComboBoxItemValue(item)"
        v-shadow
        :data-loading="item.name === LOADING_NAME"
      >
        <template v-if="item.name === LOADING_NAME">
          <cds-inline-loading />
        </template>
        <template v-else>
          {{ item.name }}
        </template>
      </cds-combo-box-item>
    </cds-combo-box>
  </div>
</template>
