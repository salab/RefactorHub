<template>
  <v-container fill-height>
    <v-col>
      <v-row v-if="!$auth.loggedIn" justify="center">
        <v-btn @click="$auth.loginWith('github')">
          <v-icon left>fab fa-github</v-icon> Login
        </v-btn>
      </v-row>
      <template v-else>
        <v-row v-if="$auth.loggedIn" justify="center">
          <div>Welcome, {{ $auth.user.name }}</div>
        </v-row>
        <v-divider class="my-4" />
        <link-buttons />
      </template>
    </v-col>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import LinkButtons from '~/components/common/LinkButtons.vue'

@Component({
  components: {
    LinkButtons
  }
})
export default class extends Vue {}
</script>
