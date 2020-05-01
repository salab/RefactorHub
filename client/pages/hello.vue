<template>
  <v-container fill-height>
    <v-col>
      <v-row justify="center">
        <v-card outlined>
          <v-card-text>
            {{ message }}
          </v-card-text>
        </v-card>
      </v-row>
      <v-divider class="my-4" />
      <v-row justify="center">
        <v-btn class="mx-2" @click="getHello">get</v-btn>
        <v-btn class="mx-2" @click="postHello">post</v-btn>
        <v-btn class="mx-2" @click="getDraft">draft</v-btn>
      </v-row>
    </v-col>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, onBeforeMount } from '@vue/composition-api'

export default defineComponent({
  name: 'hello',
  head() {
    return { title: 'Hello' }
  },
  setup(_, { root }) {
    const message = ref('')

    const getHello = async () => {
      try {
        message.value = await root.$client.getHello()
      } catch (e) {
        if (e instanceof Error) message.value = e.message
      }
    }
    const postHello = async () => {
      try {
        message.value = await root.$client.postHello()
      } catch (e) {
        if (e instanceof Error) message.value = e.message
      }
    }
    const getDraft = async () => {
      try {
        message.value = (await root.$client.getDraft(1)).description
      } catch (e) {
        if (e instanceof Error) message.value = e.message
      }
    }

    onBeforeMount(async () => {
      await getHello()
    })

    return {
      message,
      getHello,
      postHello,
      getDraft,
    }
  },
})
</script>
