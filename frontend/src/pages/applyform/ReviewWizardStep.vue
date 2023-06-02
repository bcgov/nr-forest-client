<template></template>
<script setup lang="ts">
import { provide, watch, computed, ref } from "vue";

import type FormDataDto from "@/dto/ApplyClientNumberDto";
import RadioInputComponent from "@/components/forms/RadioInputComponent.vue";
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
