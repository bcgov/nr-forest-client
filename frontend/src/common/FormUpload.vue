<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <input
      ref="file"
      class="form-control"
      type="file"
      id="formFileMultiple"
      multiple
      @change="handleChange"
    />
    <div
      v-for="(file, index) in rows"
      :key="'file' + index"
      style="margin-top: 16px"
    >
      <text style="font-weight: bold">File Name:</text> {{ file.filename }},
      <text style="font-weight: bold">Size:</text> {{ file.filesize }} KB
      <text style="color: forestgreen">Uploaded successfully </text>
      <b-button
        variant="outline-primary"
        class="btn-sm"
        style="margin-left: 16px; float: right"
        @click="deleteFile(index)"
        >Remove</b-button
      >
    </div>
    <div
      v-for="(file, index) in singeSizeError"
      :key="'file' + index"
      style="margin-top: 16px"
    >
      <text style="font-weight: bold">File Name:</text> {{ file.filename }},
      <text style="font-weight: bold">Size:</text> {{ file.filesize }} KB
      <text style="color: red">Faild to upload, file size exceeds 20MB </text>
    </div>
    <div
      v-for="(file, index) in totalSizeError"
      :key="'file' + index"
      style="margin-top: 16px"
    >
      <text style="font-weight: bold">File Name:</text> {{ file.filename }},
      <text style="font-weight: bold">Size:</text> {{ file.filesize }} KB
      <text style="color: red"
        >Faild to upload, total file size exceeds 100MB
      </text>
    </div>
  </FormFieldTemplate>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import * as coreConsts from "../core/CoreConstants";
import type {
  FormFieldTemplateType,
  FormUploadFileType,
} from "../core/AppType";

export default defineComponent({
  name: "FormUpload",
  components: {
    FormFieldTemplate,
  },
  props: {
    // form field template props (optional): label, required, tooltip, note, id
    fieldProps: {
      type: Object as PropType<FormFieldTemplateType>,
      default: {
        label: "Hello",
      },
    },
    files: {
      type: Array as PropType<Array<FormUploadFileType>>,
      default: [],
    },
  },
  data() {
    return {
      rows: this.files,
      totalSizeError: [] as Array<{ filename: string; filesize: number }>,
      singeSizeError: [] as Array<{ filename: string; filesize: number }>,
      totalFileSize: 0,
    };
  },
  methods: {
    handleChange(e: Event | any) {
      this.totalSizeError = [];
      this.singeSizeError = [];
      // todo: if want to check duplicate files
      if (e.target.files && e.target.files) {
        e.target.files.forEach((f: File) => {
          // file size returns in bytes, convert to mb, each file size needs to be less than 20 mb
          if (f.size <= coreConsts.maxFileSizePerFile) {
            // total file size needs to be less than 100mb
            if (this.totalFileSize + f.size < coreConsts.maxTotalFileSize) {
              this.totalFileSize = this.totalFileSize + f.size;
              this.getBase64(f).then((data: any) => {
                const formattedFile = this.formatFileData(data);
                if (formattedFile) {
                  this.rows.push({
                    ...formattedFile,
                    filename: f.name,
                    filesize: f.size,
                  });
                }
              });
            } else {
              this.totalSizeError.push({ filename: f.name, filesize: f.size });
            }
          } else {
            this.singeSizeError.push({ filename: f.name, filesize: f.size });
          }
        });
      }
      this.updateAttachments(this.rows);
    },
    deleteFile(index: number) {
      const newRows = this.rows.filter((m: any, i: number) => i !== index);
      this.rows = newRows;
      this.updateAttachments(this.rows);
    },
    getBase64(file: File) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = (error) => reject(error);
      });
    },
    formatFileData(filestring: string) {
      const fileData = filestring.split(";");
      if (fileData.length > 1) {
        const fileType = fileData[0].split(":")[1];
        const fileEncode = fileData[1].split(",")[0];
        const fileContent = fileData[1].split(",")[1];

        return {
          content: fileContent,
          contentType: fileType,
          encoding: fileEncode,
        };
      }
      return null;
    },
    updateAttachments(newValue: Array<FormUploadFileType>) {
      this.$emit("updateFormData", this.fieldProps.id, newValue);
    },
  },
});
</script>

<style scoped></style>
