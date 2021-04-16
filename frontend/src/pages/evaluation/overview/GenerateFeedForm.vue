<template>
  <div>
    <el-form ref="evaluation-upload-form" :model="formData" :rules="rules" label-width="120px" size="mini">
      <el-form-item label="name" prop="name">
        <el-input v-model="formData.name"></el-input>
      </el-form-item>
      <el-form-item label="GTFS feed" prop="feed">
        <el-upload
            ref="upload-field"
            :auto-upload="false"
            :limit="1"
            :multiple="false"
            :on-change="setGtfsFeed"
            :on-remove="setGtfsFeed"
            accept=".zip"
            action="">
          <el-button size="small" type="">select file</el-button>
        </el-upload>
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
        <el-button type="primary" @click="submit">Start evaluation</el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </div>

</template>

<script>
import GeneratedFeedService from "../GeneratedFeedService";
import {Notification} from "element-ui";
import {watch} from '@vue/composition-api';
import UploadProgressNotification from "./UploadProgressNotification";
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
  name: "v-generate-feed-form",

  data() {
    return {
      availableProfiles: [
        {
          value: 'bus_shortest',
          label: 'bus - shortest path'
        }, {
          value: 'bus_fastest',
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
            profile: 'bus_fastest',
            sigma: 25.0,
            beta: 2.0,
            candidateSearchRadius: 25.0,
            useGraphHopperMapMatching: false,
            _enabled: true,
          },
        },
        withEvaluation: true,
        feed: null,
      }
    }
  },

  methods: {
    setGtfsFeed(file, fileList) {
      this.formData.feed = fileList.length ? file.raw : null;
    },

    resetForm() {
      this.$refs['evaluation-upload-form'].resetFields();
      this.$refs['upload-field'].clearFiles();
    },

    async submit() {
      this.$events.$once('evaluation:createdRequest', this.displayUploadNotification)

      this.$refs['evaluation-upload-form']
          .validate((valid) => {
            if (valid) {
              let payload = this.preparePayload();
              console.log(payload);
              GeneratedFeedService.createEvaluation(payload).finally(this.resetForm)
            }
            return valid;
          });

    },

    preparePayload() {
        const payload = {
          name: this.formData.name,
          feed: this.formData.feed,
          withEvaluation: this.formData.withEvaluation,
          parameters: "",
        };

        const parameters = {};

      for (const [type, params] of Object.entries(this.formData.parameters)) {
        if (params._enabled) {
          parameters[type] = copyObject(params);
          parameters[type].candidateSearchRadius = parameters[type].sigma;
        }
      }

      payload.parameters = JSON.stringify(parameters);

      return payload;
    },

    displayUploadNotification(event) {
      const uploadNotification = Notification.info({
        title: 'Uploading GTFS feed',
        message: this.$createElement(UploadProgressNotification, {
          props: {event}
        }),
        position: 'bottom-right',
        duration: 0
      });

      watch(event, (e) => {
        if (e.progress === 100) {
          uploadNotification.close();
        }
      });
    },
  }
  ,
}
</script>
