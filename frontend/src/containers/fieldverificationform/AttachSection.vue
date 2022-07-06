<template>
  <CollapseCard title="Attach files" id="form-attachment">
    <div style="margin-bottom: 32px">
      <p>Attach the following files:</p>

      <p class="instruction-header">1. Professional rationale</p>
      <p class="instruction-body">
        &#8226; Use the naming style:
        <text style="font-weight: bold"
          >Professional_Rationale_ForestFileID_CuttingPermitID</text
        >
      </p>

      <p class="instruction-header">2. PDF map file</p>
      <p class="instruction-body">
        &#8226; Use the naming style:
        <text style="font-weight: bold"
          >PDF_Map_file_ForestFileID_CuttingPermitID</text
        >
      </p>

      <p class="instruction-header">3. Spatial file</p>
      <p class="instruction-body">
        &#8226; Accepted formats: SHAPE, KML, KMZ, GDB
      </p>
      <p class="instruction-body">
        &#8226; Use the naming style:
        <text style="font-weight: bold"
          >Spatialfile_ForestFileID_CuttingPermitID</text
        >
      </p>

      <p style="margin-top: 32px">
        A single file can't be more than
        <text style="font-weight: bold">25MB</text>. You can attach as many
        files as needed.
      </p>
    </div>

    <FormUpload
      :fieldProps="attachProps"
      :files="data.attachments"
      @updateFormData="updateFormData"
    />
  </CollapseCard>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
import FormUpload from "../../common/FormUpload.vue";
import { formProperty } from "../../pages/NewFormData";
import type {
  CommonObjectType,
  FormFieldTemplateType,
} from "../../core/AppType";

export default defineComponent({
  components: {
    CollapseCard,
    FormUpload,
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
      attachProps: formProperty.attach as FormFieldTemplateType,
    };
  },
  methods: {
    updateFormData(id: string, newValue: string) {
      this.$emit("updateFormData", { [id]: newValue });
    },
  },
});
</script>

<style scoped>
#form-attachment .instruction-header {
  font-weight: bold;
  margin-bottom: 16px;
  margin-top: 28px;
}

#form-attachment .instruction-body {
  padding-left: 18px;
}
</style>
