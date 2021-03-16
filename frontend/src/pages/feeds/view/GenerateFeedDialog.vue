<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>Generate Feed</span>

    <portal>
      <el-dialog :visible.sync="showDialog" title="Generate new feed" width="800px" top="5vh">
        <el-form ref="generate-feed-form" :model="formData" :rules="rules" label-width="120px" size="mini">
          <el-form-item label="name" prop="name">
            <el-input v-model="formData.name"></el-input>
          </el-form-item>

          <div class="my-12">
            <div class="grid grid-cols-2 gap-8">
              <div v-for="(parameters, type) in formData.parameters" :key="type">
                <el-divider content-position="left" slot="label">{{ type }}</el-divider>
                <el-form-item label="enabled" :prop="'parameters.' + type + '.enabled'">
                  <el-switch v-model="parameters._enabled"></el-switch>
                </el-form-item>
                <el-form-item label="profile" :prop="'parameters.' + type + '.profile'">
                  <el-select v-model="parameters.profile" :disabled="!parameters._enabled">
                    <el-option v-for="option in availableProfiles"
                               :key="option.value"
                               :label="option.label"
                               :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="sigma" :prop="'parameters.' + type + '.sigma'">
                  <el-input-number v-model="parameters.sigma" :precision="2" controls-position="right"
                                   :disabled="!parameters._enabled"></el-input-number>
                </el-form-item>
                <el-form-item label="beta" :prop="'parameters.' + type + '.beta'">
                  <el-input-number v-model="parameters.beta" :precision="2" controls-position="right"
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
              </div>
            </div>
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
import {copyObject} from "../../../Helper";

const parameterRules = {
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
}


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
        feed: [
          {required: true, message: 'Please add a gtfs feed', trigger: 'change'}
        ],
        'parameters.TRAM.profile': parameterRules.profile,
        'parameters.TRAM.sigma': parameterRules.sigma,
        'parameters.TRAM.beta': parameterRules.beta,
        'parameters.SUBWAY.profile': parameterRules.profile,
        'parameters.SUBWAY.sigma': parameterRules.sigma,
        'parameters.SUBWAY.beta': parameterRules.beta,
        'parameters.RAIL.profile':parameterRules.profile,
        'parameters.RAIL.sigma':parameterRules.sigma,
        'parameters.RAIL.beta':parameterRules.beta,
        'parameters.BUS.profile': parameterRules.profile,
        'parameters.BUS.sigma': parameterRules.sigma,
        'parameters.BUS.beta': parameterRules.beta,
      },
      formData: {
        name: '',
        parameters: {
          TRAM: {
            profile: 'rail',
            sigma: 10.0,
            beta: 1.0,
            candidateSearchRadius: 10.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          SUBWAY: {
            profile: 'rail',
            sigma: 10.0,
            beta: 1.0,
            candidateSearchRadius: 10.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          RAIL: {
            profile: 'rail',
            sigma: 10.0,
            beta: 1.0,
            candidateSearchRadius: 10.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
          BUS: {
            profile: 'bus_fastest_turn',
            sigma: 10.0,
            beta: 1.0,
            candidateSearchRadius: 10.0,
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
      const payload = {
        name: this.formData.name,
        feed: this.formData.feed,
        withEvaluation: this.formData.withEvaluation,
        parameters: {},
      };

      for (const [type, params] of Object.entries(this.formData.parameters)) {
        if (params._enabled) {
          payload.parameters[type] = copyObject(params);
          payload.parameters[type].candidateSearchRadius = payload.parameters[type].sigma;
        }
      }

      this.$http.post(`presets/${this.preset.id}/generated-feeds`, payload)
          .then(({data}) => {
            this.$events.$emit('presets.generatedFeed', data);
            console.log(data);
          })
          .finally(this.resetForm)
    }
  },
}
</script>