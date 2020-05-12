<template>
  <div>
    <v-divider />
    <v-list-item class="element-location" :value="true">
      <v-list-item-content>
        <v-list-item-subtitle>{{ element.location.path }}</v-list-item-subtitle>
        <v-list-item-action class="ml-0">action</v-list-item-action>
      </v-list-item-content>
    </v-list-item>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from '@vue/composition-api'
import { Element } from 'refactorhub'

export default defineComponent({
  name: 'ElementLocation',
  props: {
    element: {
      type: Object as PropType<Element>,
      required: true,
    },
  },
})
</script>

<style lang="scss" scoped>
.element-location {
  border-left: solid 12px;
  padding: 0 8px;
}
</style>
