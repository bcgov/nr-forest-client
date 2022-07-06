<template>
  <CollapseCard
    title="Contact information"
    id="form-contact"
    nextId="header-form-field-obs"
    nextText="Enter Field Observations Data"
  >
    <FormInput
      v-for="row in contactProps"
      :key="row.id"
      :fieldProps="row"
      :inputValue="data[row.id]"
      @updateFormData="updateFormData"
    />
  </CollapseCard>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
import FormInput from "../../common/FormInput.vue";
import { formProperty } from "../../pages/NewFormData";
import type {
  FormFieldTemplateType,
  CommonObjectType,
} from "../../core/AppType";

export default defineComponent({
  components: {
    CollapseCard,
    FormInput,
  },
  props: {
    data: {
      type: Object as PropType<CommonObjectType>,
      default: {
        submitterLastName: "",
        submitterEmail: "",
      },
    },
  },
  data() {
    return {
      contactProps: formProperty.contact as Array<FormFieldTemplateType>,
    };
  },
  methods: {
    updateFormData(id: string, newValue: string) {
      this.$emit("updateFormData", { [id]: newValue });
    },
  },
});
</script>

<style scoped></style>
