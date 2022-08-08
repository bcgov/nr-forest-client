<template>
  <div style="margin: 40px; text-align: left; display: flex">
    <div style="width: 48%">
      <h5 style="margin-bottom: 20px">
        Enter the client name or the client number to search:
      </h5>
      <FormInput
        :inputValue="clientName"
        :fieldProps="clientNameProps"
        @updateFormData="
          (id, newValue) => {
            clientName = newValue;
            if (newValue && newValue !== '') buttonDisabled = false;
            else if (clientNumber == '' && newValue == '')
              buttonDisabled = true;
          }
        "
      />
      <FormInput
        :inputValue="clientNumber"
        :fieldProps="clientNumberProps"
        @updateFormData="
          (id, newValue) => {
            clientNumber = newValue;
            if (newValue && newValue !== '') buttonDisabled = false;
            else if (clientName == '' && newValue == '') buttonDisabled = true;
          }
        "
      />
      <b-button
        variant="primary"
        :style="'background-color:' + primary + ';margin-top: 20px'"
        :disabled="buttonDisabled"
        @click="searchClient()"
      >
        Search
      </b-button>
    </div>
    <div style="width: 48%; margin-left: 4%">
      <h5 style="margin-bottom: 20px">Search Result:</h5>
      <b-spinner v-if="loading" variant="primary"></b-spinner>
      <b-card style="height: 100%">{{ result }}</b-card>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FormInput from "../common/FormInput.vue";
import { searchClientApi } from "../api/ForestClientRequest";
import { FormFieldTemplateType } from "../core/AppType";
import { primary } from "../utils/color";

export default defineComponent({
  components: {
    FormInput,
  },
  data() {
    return {
      primary,
      clientName: "" as string,
      clientNameProps: {
        label: "Client Name",
      } as FormFieldTemplateType,
      clientNumber: "" as string,
      clientNumberProps: {
        label: "Client Number",
      } as FormFieldTemplateType,
      result: "" as string,
      buttonDisabled: true as boolean,
      loading: false as boolean,
    };
  },
  methods: {
    searchClient() {
      this.loading = true;
      searchClientApi(this.clientNumber, this.clientName).then((response) => {
        this.result = response;
        this.loading = false;
      });
    },
  },
});
</script>

<style scoped></style>
