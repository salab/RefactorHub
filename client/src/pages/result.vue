<template>
  <v-container>
    <div class="py-3">
      <h1>Result</h1>
      <v-divider />
    </div>
    <div class="py-2">
      <div v-for="(exp, i) in expsRefs" :key="i">
        <h2>Experiment {{ i + 1 }}</h2>
        <v-simple-table>
          <thead>
            <tr>
              <th>type</th>
              <th class="text-center">A</th>
              <th class="text-center">B</th>
              <th class="text-center">C</th>
              <th class="text-center">D</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, j) in exp" :key="j">
              <td>{{ types[i][j] }}</td>
              <td v-for="(task, k) in user" :key="k" class="text-center">
                <v-btn v-if="task" text @click="fork(task.id)"> open </v-btn>
                <div v-else>null</div>
              </td>
            </tr>
          </tbody>
        </v-simple-table>
      </div>
    </div>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, useAsync } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

const exps = [
  [
    [38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53],
    [74, 75, 76, 77, 66, 67, 68, 69, 78, 79, 80, 81, 70, 71, 72, 73],
    [98, 99, 100, 101, 106, 107, 108, 109, 94, 95, 96, 97, 102, 103, 104, 105],
    [
      134,
      135,
      136,
      137,
      130,
      131,
      132,
      133,
      126,
      127,
      128,
      129,
      122,
      123,
      124,
      125,
    ],
  ],
  [
    [54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65],
    [88, 89, 86, 87, 92, 93, 90, 91, 82, 83, 84, 85],
    [118, 119, 120, 121, 110, 111, 112, 113, 116, 117, 114, 115],
    [140, 141, 138, 139, 144, 145, 142, 143, 148, 149, 146, 147],
  ],
]

const types = [
  [
    'Extract Method',
    'Extract Method',
    'Extract Method',
    'Extract Method',
    'Move Field',
    'Move Field',
    'Move Field',
    'Move Field',
    'Move Class',
    'Move Class',
    'Move Class',
    'Move Class',
    'Rename Variable',
    'Rename Variable',
    'Rename Variable',
    'Rename Variable',
  ],
  [
    'Extract Variable',
    'Extract Variable',
    'Extract Variable',
    'Extract Variable',
    'Move Variable Declaration',
    'Move Variable Declaration',
    'Move Variable Declaration',
    'Move Variable Declaration',
    'Replace Expression With Variable',
    'Replace Expression With Variable',
    'Replace Expression With Variable',
    'Replace Expression With Variable',
  ],
]

const transpose = (a: number[][]) => a[0].map((_, c) => a.map((r) => r[c]))

export default defineComponent({
  setup() {
    const refactorings = ref<Refactoring[]>([])
    const expsRefs = ref<(Refactoring | undefined)[][][]>([])

    useAsync(async () => {
      refactorings.value = (await apis.refactorings.getAllRefactorings()).data
      expsRefs.value = exps.map((exp) =>
        transpose(exp).map((user) =>
          user.map((task) =>
            refactorings.value.find((ref) => ref.parentId === task)
          )
        )
      )
    })

    const fork = async (id: number) => {
      const draft = (await apis.refactorings.forkRefactoring(id)).data
      window.open(`/draft/${draft.id}`, '_blank')
    }

    return { expsRefs, fork, types }
  },
})
</script>
