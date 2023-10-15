<script setup lang="ts">
const commit = computed(() => useDraft().commit.value)
const messageLines = computed(() =>
  commit.value ? commit.value.message.split('\n') : [],
)
</script>

<template>
  <v-sheet class="pa-3">
    <h3>Commit Information</h3>
    <div class="text-subtitle-1 font-weight-medium">
      <span>{{ commit?.owner }}</span>
      /
      <span>{{ commit?.repository }}</span>
      /
      <a :href="commit?.url" target="_blank" rel="noopener">{{
        commit?.sha.substring(0, 7)
      }}</a>
    </div>
    <v-divider class="my-1" />
    <div>
      <div
        v-for="(line, index) in messageLines"
        :key="index"
        :class="{
          'text-body-1 font-weight-medium': index === 0,
          'text-body-2': index > 0,
        }"
      >
        {{ line }}
      </div>
    </div>
    <v-divider class="my-1" />
    <div>
      <span class="text-subtitle-2">{{ commit?.author }}</span>
      <span class="text-body-2"> committed on </span>
      <span class="text-body-2">{{
        commit ? new Date(commit.authorDate).toLocaleString() : ''
      }}</span>
    </div>
  </v-sheet>
</template>
