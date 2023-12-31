<script setup lang="ts">
import { ChangeParameters } from '@/composables/useAnnotation'

interface ContinueButton {
  text: string
  color: string
  onClick(): void
}

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
  changeParametersList: {
    type: Object as () => ChangeParameters[],
    required: true,
  },
  continueButton: {
    type: Object as () => ContinueButton | undefined,
    default: undefined,
  },
})

const parameterInformationList = computed(() =>
  useAnnotation().getChangeParametersTextModels(props.changeParametersList),
)

const dialogIsOpening = ref(false)
</script>

<template>
  <v-dialog v-model="dialogIsOpening" activator="parent" width="80%" scrollable>
    <v-card>
      <v-sheet color="primary">
        <v-card-title>{{ title }}</v-card-title>
        <v-card-subtitle v-if="subtitle"> {{ subtitle }}</v-card-subtitle>
      </v-sheet>
      <v-divider />
      <v-card-text>
        <v-sheet
          v-for="information in parameterInformationList"
          :key="information.changeId"
        >
          <div class="text-h6">
            {{ information.changeType.name }}
          </div>
          <v-divider />
          <div class="text-body-1 font-weight-light mb-1">
            {{ information.changeType.description }}
          </div>
          <v-sheet
            v-for="parameterPair in information.diff"
            :key="parameterPair.parameterName"
          >
            <div class="text-subtitle-1 font-weight-medium">
              {{ parameterPair.parameterName }}
            </div>
            <v-row>
              <v-col :cols="6">
                <div class="text-body-2 font-weight-light">
                  {{ parameterPair.before.description }}
                </div>
              </v-col>
              <v-col :cols="6">
                <div class="text-body-2 font-weight-light">
                  {{ parameterPair.after.description }}
                </div>
              </v-col>
            </v-row>
            <monaco-editor
              :before="parameterPair.before.model"
              :after="parameterPair.after.model"
            />
          </v-sheet>
          <v-row>
            <v-col :cols="6">
              <v-sheet
                v-for="parameter in information.before"
                :key="parameter.parameterName"
                class="pe-3"
              >
                <div class="text-subtitle-1 font-weight-medium">
                  {{ parameter.parameterName }}
                </div>
                <div class="text-body-2 font-weight-light">
                  {{ parameter.description }}
                </div>
                <monaco-editor :before="parameter.model" />
              </v-sheet>
            </v-col>
            <v-col :cols="6">
              <v-sheet
                v-for="parameter in information.after"
                :key="parameter.parameterName"
                class="ps-3"
              >
                <div class="text-subtitle-1 font-weight-medium">
                  {{ parameter.parameterName }}
                </div>
                <div class="text-body-2 font-weight-light">
                  {{ parameter.description }}
                </div>
                <monaco-editor :before="parameter.model" />
              </v-sheet>
            </v-col>
          </v-row>
        </v-sheet>
      </v-card-text>
      <v-divider />
      <v-sheet color="primary" class="d-flex justify-space-evenly pa-1">
        <v-btn
          variant="flat"
          size="small"
          color="secondary"
          text="close"
          @click="() => (dialogIsOpening = false)"
        />
        <v-btn
          v-if="continueButton"
          variant="flat"
          size="small"
          :color="`${continueButton.color}`"
          :text="`${continueButton.text}`"
          @click="
            () => {
              continueButton?.onClick()
              dialogIsOpening = false
            }
          "
        />
      </v-sheet>
    </v-card>
  </v-dialog>
</template>
