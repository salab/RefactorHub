<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { ChangeParameters } from '@/composables/useAnnotation'
import { ActionName } from '@/apis'

interface ContinueButton {
  text: string
  color: string
  onlyValidAnnotation: boolean
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
function isValid(parameter: {
  required: boolean
  model: monaco.editor.ITextModel
}) {
  return !(parameter.required && parameter.model.getValue() === '')
}
function isEmpty(parameter: { model: monaco.editor.ITextModel }) {
  return parameter.model.getValue() === ''
}
const isValidAnnotation = computed(() => {
  for (const information of parameterInformationList.value) {
    const { diff, before, after } = information
    for (const { before, after } of diff) {
      if (!isValid(before)) return false
      if (!isValid(after)) return false
    }
    for (const parameter of before) {
      if (!isValid(parameter)) return false
    }
    for (const parameter of after) {
      if (!isValid(parameter)) return false
    }
  }
  return true
})

const dialogIsOpening = ref(false)

watch(
  () => dialogIsOpening.value,
  (newDialogIsOpening) => {
    if (newDialogIsOpening) {
      sendAction(ActionName.OpenChangeInformation, {
        title: props.title,
        subtitle: props.subtitle,
        changeParametersList: props.changeParametersList,
        continueButtonText: props.continueButton?.text,
      })
    }
  },
)
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
                <v-alert
                  v-if="!isValid(parameterPair.before)"
                  variant="tonal"
                  density="compact"
                  type="error"
                  class="py-1 my-1"
                >
                  This parameter is mandatory required but not annotated
                </v-alert>
                <v-alert
                  v-else-if="isEmpty(parameterPair.before)"
                  variant="tonal"
                  density="compact"
                  type="info"
                  class="py-1 my-1"
                >
                  This optional parameter is not annotated<br />
                  Do you intentionally keep this parameter blank?
                </v-alert>
              </v-col>
              <v-col :cols="6">
                <div class="text-body-2 font-weight-light">
                  {{ parameterPair.after.description }}
                </div>
                <v-alert
                  v-if="!isValid(parameterPair.after)"
                  variant="tonal"
                  density="compact"
                  type="error"
                  class="py-1 my-1"
                >
                  This parameter is mandatory required but not annotated
                </v-alert>
                <v-alert
                  v-else-if="isEmpty(parameterPair.after)"
                  variant="tonal"
                  density="compact"
                  type="info"
                  class="py-1 my-1"
                >
                  This optional parameter is not annotated<br />
                  Do you intentionally keep this parameter blank?
                </v-alert>
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
                <v-alert
                  v-if="!isValid(parameter)"
                  variant="tonal"
                  density="compact"
                  type="error"
                  class="py-1 my-1"
                >
                  This parameter is mandatory required but not annotated
                </v-alert>
                <v-alert
                  v-else-if="isEmpty(parameter)"
                  variant="tonal"
                  density="compact"
                  type="info"
                  class="py-1 my-1"
                >
                  This optional parameter is not annotated<br />
                  Do you intentionally keep this parameter blank?
                </v-alert>
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
                <v-alert
                  v-if="!isValid(parameter)"
                  variant="tonal"
                  density="compact"
                  type="error"
                  class="py-1 my-1"
                >
                  This parameter is mandatory required but not annotated
                </v-alert>
                <v-alert
                  v-else-if="isEmpty(parameter)"
                  variant="tonal"
                  density="compact"
                  type="info"
                  class="py-1 my-1"
                >
                  This optional parameter is not annotated<br />
                  Do you intentionally keep this parameter blank?
                </v-alert>
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
        <div v-if="continueButton" class="d-flex flex-column align-center">
          <v-btn
            :disabled="continueButton.onlyValidAnnotation && !isValidAnnotation"
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
          <v-alert
            v-if="continueButton.onlyValidAnnotation && !isValidAnnotation"
            variant="tonal"
            density="compact"
            type="error"
            class="py-1 my-1"
          >
            The annotation is not completed
          </v-alert>
        </div>
      </v-sheet>
    </v-card>
  </v-dialog>
</template>
