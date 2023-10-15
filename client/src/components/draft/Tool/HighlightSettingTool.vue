<script setup lang="ts">
import { CommonTokenSequenceType } from 'composables/useCommonTokenSequence'

const commonTokenSequenceSetting = useCommonTokenSequence().setting
const commonTokenSequenceTypes = [
  'oneToOne',
  'oneToManyOrManyToOne',
  'manyToMany',
] satisfies CommonTokenSequenceType[]
const updateCommonTokensTypes = (types: CommonTokenSequenceType[]) => {
  useCommonTokenSequence().updateSetting({
    oneToOne: types.includes('oneToOne'),
    oneToManyOrManyToOne: types.includes('oneToManyOrManyToOne'),
    manyToMany: types.includes('manyToMany'),
  })
}
</script>

<template>
  <v-sheet class="pa-3">
    <h3>Highlights of Common Token Sequences</h3>
    <span class="text-body-2">
      3 types that have different relationships between
      <b>the number of occurrences</b> in
      <b
        ><span :style="'background-color: ' + colors.before">
          the code before the change</span
        ></b
      >
      and that in
      <b
        ><span :style="'background-color: ' + colors.after"
          >the code after the change</span
        ></b
      >
    </span>
    <v-chip-group
      filter
      multiple
      :model-value="
        commonTokenSequenceTypes.filter(
          (type) => commonTokenSequenceSetting[type],
        )
      "
      @update:model-value="updateCommonTokensTypes"
    >
      <v-chip :value="commonTokenSequenceTypes[0]">one to one</v-chip>
      <v-chip :value="commonTokenSequenceTypes[1]"
        >one to many / many to one</v-chip
      >
      <v-chip :value="commonTokenSequenceTypes[2]">many to many</v-chip>
    </v-chip-group>
  </v-sheet>
</template>
