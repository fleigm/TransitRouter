<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>Generate Feed</span>

    <portal>
      <el-dialog
          :visible.sync="showDialog"
          title="Generate new feed">

        <el-form ref="generate-feed-form" :model="formData" :rules="rules" label-width="150px" size="mini">
          <el-form-item label="name" prop="name">
            <el-input v-model="formData.name"></el-input>
          </el-form-item>

          <div class="my-12">
            <el-divider content-position="left">Tuning parameters</el-divider>

            <el-tabs tab-position="left" active-name="TRAM" class="mt-4">
              <el-tab-pane v-for="(parameters, type) in formData.parameters"
                           :key="type"
                           :label="type"
                           :name="type">
                <span slot="label">{{ type }}</span>
                <el-form-item label="enabled" prop="enabled">
                  <el-switch v-model="parameters._enabled"></el-switch>
                </el-form-item>
                <el-form-item label="profile" prop="profile">
                  <el-select v-model="parameters.profile" :disabled="!parameters._enabled">
                    <el-option v-for="option in availableProfiles"
                               :key="option.value"
                               :label="option.label"
                               :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="sigma" prop="sigma">
                  <el-input-number v-model="parameters.sigma" :precision="2"
                                   :disabled="!parameters._enabled"></el-input-number>
                </el-form-item>
                <el-form-item label="beta" prop="beta">
                  <el-input-number v-model="parameters.beta" :precision="2"
                                   :disabled="!parameters._enabled"></el-input-number>
                </el-form-item>
                <el-form-item label="csr" prop="candidateSearchRadius">
                  <el-input-number v-model="parameters.candidateSearchRadius" :precision="2"
                                   :disabled="!parameters._enabled"></el-input-number>
                </el-form-item>
                <el-form-item label="Router">
                  <el-switch
                      v-model="parameters.useGraphHopperMapMatching"
                      active-text="GHMM"
                      inactive-text="TransitRouter"
                      :disabled="!parameters._enabled">
                  </el-switch>
                </el-form-item>
              </el-tab-pane>
            </el-tabs>
          </div>


          <div class="my-12">
            <el-divider content-position="left">Options</el-divider>
            <el-form-item label="Evaluate feed">
              <el-switch v-model="formData.withEvaluation"></el-switch>
            </el-form-item>
          </div>

          <el-form-item>
            <el-button type="primary" @click="submit">Generate</el-button>
            <el-button @click="resetForm">Reset</el-button>
          </el-form-item>
        </el-form>

      </el-dialog>
    </portal>
  </el-button>
</template>

<script>

export default {
  name: "v-generate-feed-dialog",

  props: {
    preset: {
      required: true,
      type: Object,
    },
  },

  data() {
    return {
      showDialog: false,
      availableProfiles: [
        {
          value: 'bus_shortest_turn',
          label: 'bus - shortest path'
        }, {
          value: 'bus_fastest_turn',
          label: 'bus - fastest path',
        }, {
          value: 'rail',
          label: 'rail - shortest path',
        }
      ],
      rules: {
        name: [
          {required: true, message: 'Please enter a name', trigger: 'blur'}
        ],
        parameters: {
          '*': {
            profile: [
              {required: true, message: 'Please enter a profile', trigger: 'blur'}
            ],
            sigma: [
              {type: 'number', required: true, min: 0, message: 'sigma value must be positive', trigger: 'blur'}
            ],
            beta: [
              {type: 'number', required: true, min: 0, message: 'Beta value must be positive', trigger: 'blur'}
            ],
            candidateSearchRadius: [
              {type: 'number', required: true, min: 0, message: 'Csr value must be positive', trigger: 'blur'}
            ],
          },
        }

      },
      formData: {
        name: '',
        parameters: {
          TRAM: {
            profile: 'rail',
            sigma: 25.0,
            beta: 2.0,
            candidateSearchRadius: 25.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          SUBWAY: {
            profile: 'rail',
            sigma: 25.0,
            beta: 2.0,
            candidateSearchRadius: 25.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          RAIL: {
            profile: 'rail',
            sigma: 25.0,
            beta: 2.0,
            candidateSearchRadius: 25.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          BUS: {
            profile: 'bus_fastest_turn',
            sigma: 25.0,
            beta: 2.0,
            candidateSearchRadius: 25.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
        },
        withEvaluation: true,
      },
    }
  },

  methods: {
    open() {
      this.showDialog = true;
    },
    close() {
      this.showDialog = false;
    },


    resetForm() {
      this.$refs['generate-feed-form'].resetFields();
    },

    submit() {
      this.$refs['generate-feed-form']
          .validate((valid) => {
            if (valid) {
              this.sendRequest();
              this.close();
            }
            return valid
          })
    },

    sendRequest() {
      this.$http.post(`presets/${this.preset.id}/generated-feeds`, this.formData)
          .then(({data}) => {
            this.$events.$emit('presets.generatedFeed', data);
            console.log(data);
          })
          .finally(this.resetForm)
    }
  },
}
</script>