<template>
  <SearchTemplate
    :buttonDisabled="numberSearchDisabled"
    title="Enter the client number to search:"
    :loading="numberResultLoading"
    :result="numberResult"
    @onSearch="searchClientNumber"
  >
    <FormInput
      :inputValue="clientNumber"
      :fieldProps="clientNumberProps"
      @updateFormData="
        (id, newValue) => {
          clientNumber = newValue;
          if (newValue && newValue !== '') numberSearchDisabled = false;
          else numberSearchDisabled = true;
        }
      "
    />
  </SearchTemplate>

  <!-- <FormInput
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
      /> -->
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FormInput from "../common/FormInput.vue";
import SearchTemplate from "../common/SearchTemplate.vue";
import { searchByClientNumberApi } from "../api/ForestClientRequest";
import type { FormFieldTemplateType } from "../core/AppType";
import { primary } from "../utils/color";

export default defineComponent({
  components: {
    FormInput,
    SearchTemplate,
  },
  data() {
    return {
      primary,
      clientNumber: "" as string,
      clientNumberProps: {
        label: "Client Number",
        required: true,
      } as FormFieldTemplateType,
      numberResult: "" as string,
      numberSearchDisabled: true as boolean,
      numberResultLoading: false as boolean,

      // clientName: "" as string,
      // clientNameProps: {
      //   label: "Client Name",
      // } as FormFieldTemplateType,
    };
  },
  methods: {
    searchClientNumber() {
      this.numberResultLoading = true;
      searchByClientNumberApi(this.clientNumber).then((response) => {
        this.numberResult = response;
        this.numberResultLoading = false;
      });
    },
  },
});
</script>

<style scoped></style>
