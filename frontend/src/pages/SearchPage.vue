<template>
  <div>
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
          (id: any, newValue: string) => {
            clientNumber = newValue;
            if (newValue && newValue !== '') numberSearchDisabled = false;
            else numberSearchDisabled = true;
          }
        "
      />
    </SearchTemplate>

    <SearchTemplate
      title="Search all non individual clients:"
      :loading="nonindResultLoading"
      :result="nonindResult"
      @onSearch="seachAllNonIndividuals"
    >
      <FormSelect
        :fieldProps="nonindOrderProps"
        :selectedValue="nonindOrder"
        :options="nonindOrderOption"
        @updateFormData="
          (id: any, newValue: string) => {
            nonindOrder = newValue;
          }
        "
      />

      <FormInput
        :inputValue="nonindPage"
        :fieldProps="nonindPageProps"
        @updateFormData="
          (id: any, newValue: number) => {
            nonindPage = newValue;
          }
        "
      />
      <FormInput
        :inputValue="nonindTake"
        :fieldProps="nonindTakeProps"
        @updateFormData="
          (id: any, newValue: number) => {
            nonindTake = newValue;
          }
        "
      />
    </SearchTemplate>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FormInput from "../common/FormInput.vue";
import FormSelect from "../common/FormSelect.vue";
import SearchTemplate from "../common/SearchTemplate.vue";
import {
  searchInViewByClientNumber,
  searchInViewAllNonIndividuals,
} from "../services/forestClient.service";
import type { FormFieldTemplateType } from "../core/AppType";
import { primary } from "../utils/color";

export default defineComponent({
  components: {
    FormInput,
    FormSelect,
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

      nonindOrder: "ASC" as string,
      nonindOrderOption: [
        { value: "ASC", text: "ASC" },
        { value: "DESC", text: "DESC" },
      ],
      nonindOrderProps: {
        label: "Order",
      } as FormFieldTemplateType,
      nonindPage: 1 as number,
      nonindPageProps: {
        label: "Page",
        note: "Default value 1",
      } as FormFieldTemplateType,
      nonindTake: 10 as number,
      nonindTakeProps: {
        label: "Take",
        note: "Default value 10, Min value 1, Max value 50",
      } as FormFieldTemplateType,
      nonindResult: "" as string,
      nonindResultLoading: false,
    };
  },
  methods: {
    searchClientNumber() {
      this.numberResultLoading = true;
      searchInViewByClientNumber(this.clientNumber).then((response) => {
        this.numberResult = response;
        this.numberResultLoading = false;
      });
    },
    seachAllNonIndividuals() {
      this.nonindResultLoading = true;
      searchInViewAllNonIndividuals(
        this.nonindOrder,
        this.nonindPage,
        this.nonindTake
      ).then((response) => {
        this.nonindResult = response;
        this.nonindResultLoading = false;
      });
    },
  },
});
</script>

<style scoped></style>
