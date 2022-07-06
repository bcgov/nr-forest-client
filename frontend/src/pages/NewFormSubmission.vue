<template>
  <div id="form-container" style="margin: 40px">
    <h4 style="margin-bottom: 24px">Priority deferral field verification</h4>
    <FieldVerificationSubmission v-model="formData" :nrdList="nrdList" />

    <b-button
      variant="primary"
      :style="'background-color:' + primary + ';margin-top: 24px'"
      @click="generateReport()"
    >
      Submit
    </b-button>
    <div style="margin-top: 24px">
      Demo Test Email (enter an email address to receive the form)
      <input :value="testEmail" @input="updateTestEmail" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import html2pdf from "html2pdf.js";
import FieldVerificationSubmission from "../containers/fieldverificationform/FieldVerificationForm.vue";
import { sendEmail } from "../api/OldGrowthRequest";
import { store } from "../helpers/AppState";
import { primary } from "../utils/color";

import { formData, nrdList } from "./NewFormData";

export default defineComponent({
  components: {
    FieldVerificationSubmission,
  },
  data() {
    return {
      formData,
      nrdList,
      primary,
      testEmail: store.testEmail, // this is just use for demo, will remove later
    };
  },
  methods: {
    generateReport() {
      var element = document.getElementById("pdf-form-div");

      // display all the hidden content
      this.showHiddenContent("form-contact");
      this.showHiddenContent("form-field-obs");
      this.showHiddenContent("form-attachment");

      if (this.testEmail !== "") {
        // save pdf web form to a variable
        html2pdf()
          .set({ margin: 14 })
          .from(element)
          .toPdf()
          .output("datauristring")
          .then(function (pdfAsString: string) {
            // process pdf string data
            let fileContent = "";
            const fileData = pdfAsString.split(";");
            if (fileData.length > 2) {
              const fileInfo = fileData[2].split(",");
              if (fileInfo.length > 1) {
                fileContent = fileInfo[1];
              }
            }

            // console.log("store form uploaded files", store.formUploadFiles);

            if (fileContent && fileContent !== "") {
              sendEmail(
                "An Old Growth Field Observation form and package is attached.",
                [
                  {
                    content: fileContent,
                    contentType: "application/pdf",
                    encoding: "base64",
                    filename: "field_verification_form.pdf",
                  },
                ],
                [store.testEmail] //[this.fieldObsSelectData.modelValue]
              );
            } else {
              console.log("Failed to convert webform to pdf");
            }
          });
      } else {
        console.log("no email adderess provided");
        console.log("formData", this.formData);
      }
    },

    showHiddenContent(childId: string) {
      if (!document.getElementById(childId).classList.contains("show")) {
        document.getElementById(`header-${childId}`)!.click();
      }
    },

    updateTestEmail(e: any) {
      this.testEmail = e.target.value;
      store.updateTestEmail(e.target.value);
    },
  },
});
</script>

<style scoped>
#form-container {
  text-align: left;
  /* margin: 40px; */
}
</style>
