<template>
  <CollapseCard title="Cut block information" :id="id" defaultOpen alwaysOpen>
    <div v-for="(dataKey, dataIndex) in Object.keys(data)" :key="dataIndex">
      <FormInput
        v-if="columns[dataIndex].type == 'input'"
        :fieldProps="columns[dataIndex]"
        :inputValue="data[dataKey]"
        @updateFormData="updateBlockData"
      ></FormInput>
      <FormCheckboxGroup
        v-if="columns[dataIndex].type == 'checkbox'"
        :fieldProps="columns[dataIndex]"
        :name="columns[dataIndex].id + dataIndex + 'radio'"
        :options="columns[dataIndex].options"
        :checked="data[dataKey]"
        @updateFormData="updateBlockData"
      ></FormCheckboxGroup>
    </div>
    <b-button
      v-if="enableAdd"
      variant="primary"
      :style="
        `background-color:` + primary + ';margin-top: 16px; margin-right: 16px'
      "
      @click="addCutBlock"
      >+ Add Another Cut Block</b-button
    >
    <b-button
      v-if="enableDelete"
      variant="primary"
      :style="`background-color:` + primary + ';margin-top: 16px;'"
      @click="deleteCutBlock"
      >- Remove this Cut Block</b-button
    >
  </CollapseCard>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
import FormInput from "../../common/FormInput.vue";
import FormCheckboxGroup from "../../common/FormCheckboxGroup.vue";
import type { FormGridColumnType } from "../../core/AppType";
import { primary } from "../../utils/color";

export default defineComponent({
  components: {
    CollapseCard,
    FormInput,
    FormCheckboxGroup,
  },
  props: {
    columns: {
      type: Array as PropType<Array<FormGridColumnType>>,
      required: true,
    },
    data: {
      type: Object as PropType<{ [key: string]: any }>,
      required: true,
    },
    id: {
      type: String,
      default: "form-fieldobs-cutblock",
    },
    enableAdd: {
      type: Boolean,
      default: true,
    },
    enableDelete: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      primary,
    };
  },
  methods: {
    addCutBlock() {
      this.$emit("addCutBlock");
    },
    deleteCutBlock() {
      this.$emit("deleteCutBlock");
    },
    updateBlockData(id: string, newValue: string | Array<string>) {
      this.$emit("updateBlockData", id, newValue);
    },
  },
});
</script>

<style scoped></style>
