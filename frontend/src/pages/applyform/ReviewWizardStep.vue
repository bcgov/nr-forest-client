<template>


    <div class="bordered">
      <h5>Business information</h5>
      <p>Company Name</p>
      <p>Company Type</p>

      <button>Button here with an icon</button>
    </div>

    <div class="bordered">
      <h5>Address</h5>

      <button>Button here with an icon</button>
    </div>

    <div class="bordered">
      <h5>Contacts</h5>

      <button>Button here with an icon</button>
    </div>


</template>

<script setup lang="ts">
import { provide, watch, computed, ref } from "vue";

import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import Label from "@/common/LabelComponent.vue";
import Note from "@/common/NoteComponent.vue";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch([formData], () => emit("update:data", formData.value));
</script>

<style scoped>

.bordered{

  box-sizing: border-box;
  border: 1px solid #DFDFE1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 32px;
  gap: 40px;

}

</style>