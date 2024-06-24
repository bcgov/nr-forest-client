<script setup lang="ts">
import { ref, computed, reactive } from "vue";
// Carbon
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
import { isSmallScreen } from "@/composables/useScreenSize";
// Types
import type { CodeNameType, ModalNotification } from "@/dto/CommonTypesDto";

//Defining the props and emiter to reveice the data and emit an update
const clientTypesList: CodeNameType[] = [
{
    code:'BCR',
    name:'BC registered business'
  },
  {
    code:'R',
    name:'First Nation'
  },
  {
    code:'G',
    name:'Government'
  },
  {
    code:'I',
    name:'Individual'
  },
  {
    code:'F',
    name:'Ministry of Forests'
  },
  {
    code:'U',
    name:'Unregistered company'
  }
];

const toastBus = useEventBus<ModalNotification>("toast-notification");

//Route related
const router = useRouter();

let formData = ref({ });

// Tab system
const progressData = reactive([
  {
    title: "Client information",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Locations",
    subtitle: "Step 2",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 1,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Contacts",
    subtitle: "Step 3",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 2,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Review",
    subtitle: "Step 4",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 3,
    fields: [],
    extraValidations: [],
  },
]);
const currentTab = ref(0);

const clientTypeInitialValue = computed(() => clientTypesList[0]);

const updateType = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.type = value.code;
  }
};

const validation = reactive<Record<string, boolean>>({ });

</script>

<template>
  
  <div id="screen" class="submission-content">
    <div class="form-header" role="header">
      
      <div class="form-header-title">
        <h1>
          <div data-scroll="top" class="header-offset"></div>
          Create client
        </h1>
      </div>

      <div class="sr-only" role="status">
        Current step: {{ progressData[currentTab].title }}. Step {{ currentTab + 1 }} of {{ progressData.length }}.
      </div>

      <cds-progress-indicator space-equally :vertical="isSmallScreen" aria-label="Form steps">
        <cds-progress-step 
          v-for="item in progressData"
          ref="cdsProgressStepArray"
          :key="item.step"
          :label="item.title"
          :secondary-label="item.subtitle"
          :state="item.kind"
          :class="item.step <= currentTab ? 'step-active' : 'step-inactive'"          
          :disabled="item.disabled"
          v-shadow="3"
          :aria-current="item.step === currentTab ? 'step' : 'false'"
        />
      </cds-progress-indicator>

      <div class="hide-when-less-than-two-children">
        <div data-scroll="top-notification" class="header-offset"></div>
        
      </div>

    </div>

    <div class="form-steps" role="main">
      <div class="form-steps-section">
        <h2 data-focus="focus-0" tabindex="-1">
          <div data-scroll="step-title" class="header-offset"></div>
          Client information
        </h2>
        <dropdown-input-component
          id="clientType"
          label="Client type"
          :initial-value="clientTypeInitialValue?.name"
          required
          required-label
          :model-value="clientTypesList"
          :enabled="true"
          tip=""
          :validations="[]"
          @update:selected-value="updateType($event)"
          @empty="validation.type = !$event"
        />
      </div>
    </div>

  </div>

</template>
