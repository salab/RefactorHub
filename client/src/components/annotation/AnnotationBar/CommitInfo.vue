<script setup lang="ts">
const commit = computed(() => useAnnotation().annotation.value?.commit)
const messageLines = computed(() =>
  commit.value ? commit.value.message.split('\n') : [],
)
</script>

<template>
  <v-tooltip v-if="commit" location="bottom center" origin="auto">
    <template #activator="{ props: tooltipProps }">
      <a :href="commit.url" target="_blank" rel="noopener"
        ><v-icon v-bind="tooltipProps" size="small" icon="$mdiGithub"
      /></a>
    </template>

    <h3>Commit Information</h3>
    <div class="text-subtitle-1 font-weight-medium">
      <span>{{ commit.owner }}</span>
      /
      <span>{{ commit.repository }}</span>
      /
      {{ commit.sha }}
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
      <span class="text-subtitle-2">{{ commit.authorName }}</span>
      <span class="text-body-2"> committed on </span>
      <span class="text-body-2">{{
        new Date(commit.authoredDateTime).toLocaleString()
      }}</span>
    </div>
  </v-tooltip>
</template>
