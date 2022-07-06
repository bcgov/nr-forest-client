<template>
  <CollapseCard
    title="Field observations"
    id="form-field-obs"
    nextId="header-form-attachment"
    nextText="Attach Files"
  >
    <FormSelect
      :fieldProps="formSelectProps"
      :selected="selected"
      :options="nrdList"
      @updateFormData="updateFormData"
    />
    <FormInput
      v-for="row in formInputProps"
      :key="row.id"
      :fieldProps="row"
      :inputValue="data[row.id]"
      @updateFormData="updateFormData"
    />
    <CutBlockInfo
      v-for="(block, index) in cutBlocks"
      :key="index"
      :columns="formBlockProps"
      :data="cutBlocks[index]"
      :id="'form-fieldobs-cutblock-' + index"
      :enableAdd="index === cutBlocks.length - 1 ? true : false"
      :enableDelete="cutBlocks.length > 1"
      @updateBlockData="(id, newValue) => updateBlockData(index, id, newValue)"
      @addCutBlock="addCutBlock"
      @deleteCutBlock="deleteCutBlock(index)"
    />
  </CollapseCard>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
import FormInput from "../../common/FormInput.vue";
import FormSelect from "../../common/FormSelect.vue";
import CutBlockInfo from "./CutBlockInfo.vue";
import { formProperty } from "../../pages/NewFormData";
import type {
  FormFieldTemplateType,
  FromSelectOptionType,
  FormGridColumnType,
  CommonObjectType,
} from "../../core/AppType";

export default defineComponent({
  components: {
    CollapseCard,
    FormInput,
    FormSelect,
    CutBlockInfo,
  },
  props: {
    data: {
      type: Object as PropType<CommonObjectType>,
      default: {
        naturalResourceDistrictCode: {},
        forestFileId: "",
        cutBlocks: [],
      },
    },
    nrdList: {
      type: Array as PropType<Array<FromSelectOptionType>>,
      default: [],
    },
  },
  data() {
    return {
      formSelectProps: formProperty.fieldObsSelect as FormFieldTemplateType,
      formInputProps:
        formProperty.fieldObsInput as Array<FormFieldTemplateType>,
      formBlockProps: formProperty.fieldObsBlock as Array<FormGridColumnType>,
      // need these line to trigger the rerender to catch changes
      selected: this.data.naturalResourceDistrictCode,
      cutBlocks: this.data.cutBlocks,
    };
  },
  methods: {
    addCutBlock() {
      this.$emit("addCutBlock");
    },
    deleteCutBlock(index: number) {
      this.$emit("deleteCutBlock", index);
    },

    updateBlockData(
      index: number,
      id: string,
      newValue: string | Array<string>
    ) {
      this.$emit("updateBlockData", index, id, newValue);
    },

    updateFormData(id: string, newValue: string | CommonObjectType) {
      this.$emit("updateFormData", { [id]: newValue });
    },
  },
});
</script>

<style scoped></style>
