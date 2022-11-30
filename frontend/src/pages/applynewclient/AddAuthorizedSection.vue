<template>
  <CollapseCard
    :title="authorizedSectionStructure.title"
    defaultOpen
    :id="authorizedSectionStructure.id"
  >
    <div
      v-for="(row, rowIndex) in authorizedSectionStructure.content"
      :key="rowIndex"
    >
      <FormTable
        v-if="row.type == 'table'"
        :data="data"
        :addButtonText="row.addButtonText"
        :columns="row.columns"
        @updateFormTable="updateFormTable"
        @addRow="addRow"
        @deleteRow="deleteRow"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import { ref, onUpdated } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
import FormTable from "../../common/FormTable.vue";
import { authorizedSectionStructure, newClientData } from "./NewClient";

const data = ref([...newClientData[authorizedSectionStructure.id]]);

const updateFormTable = (id, value, row) => {
  data.value[row][id] = value;
};

const countIndex = ref(0); // must use this to generate unique index

const addRow = () => {
  countIndex.value += 1;
  const defaultNew = JSON.parse(
    JSON.stringify(newClientData[authorizedSectionStructure.id][0])
  );
  data.value.push({ ...defaultNew, index: countIndex.value });
};

const deleteRow = (row) => {
  data.value.splice(row, 1);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "AddAuthorizedSection",
});
</script>

<style scoped></style>
