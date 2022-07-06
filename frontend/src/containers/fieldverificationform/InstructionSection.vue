<template>
  <CollapseCard
    title="Instruction"
    id="form-instruction"
    defaultOpen
    nextId="header-form-contact"
    nextText="Enter Contact Information"
  >
    <p>
      1. Complete <text style="font-weight: bold">1 form</text> per
      <text style="font-weight: bold">cutting permit.</text>
    </p>

    <p>
      2. Name the files in the format found in "Attach files" section. A single
      file can't be more than 25MB. You can attach as many files as needed.
    </p>

    <p>
      3. Submit 1 professional rationale, 1 PDF map, and 1 spatial file for each
      cutting permit.
    </p>

    <p>
      4. Enter additional cut blocks for the cutting permit using the "Add
      another cut block button".
    </p>
  </CollapseCard>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";

export default defineComponent({
  components: {
    CollapseCard,
  },
});
</script>

<style scoped></style>
