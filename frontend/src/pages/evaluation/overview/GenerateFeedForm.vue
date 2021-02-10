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
        <el-divider content-position="left">Tuning parameters</el-divider>
        <el-form-item label="profile" prop="profile">
          <el-select v-model="formData.profile">
            <el-option v-for="option in availableProfiles"
                       :key="option.value"
                       :label="option.label"
                       :value="option.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="sigma" prop="sigma">
          <el-input-number v-model="formData.sigma" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="beta" prop="beta">
          <el-input-number v-model="formData.beta" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="csr" prop="candidateSearchRadius">
          <el-input-number v-model="formData.candidateSearchRadius" :precision="2"></el-input-number>
        </el-form-item>
      </div>


      <div class="my-12">
        <el-divider content-position="left">Options</el-divider>
        <el-form-item label="Router">
          <el-switch
              v-model="formData.useGraphHopperMapMatching"
              active-text="GHMM"
              inactive-text="TransitRouter">
          </el-switch>
        </el-form-item>
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

export default {
  name: "v-generate-feed-form",

  data() {
    return {
      availableProfiles: [
        {
          value: 'bus_shortest',
          label: 'shortest path',
        }, {
          value: 'bus_shortest_turn',
          label: 'shortest path with turn restrictions'
        }, {
          value: 'bus_fastest',
          label: 'fastest path',
        }, {
          value: 'bus_fastest_turn',
          label: 'fastest path with turn restrictions',
        }
      ],
      rules: {
        name: [
          {required: true, message: 'Please enter a name', trigger: 'blur'}
        ],
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
        feed: [
          {required: true, message: 'Please add a gtfs feed', trigger: 'change'}
        ]
      },
      fileList: [],
      formData: {
        name: '',
        profile: 'bus_fastest_turn',
        sigma: 25.0,
        beta: 2.0,
        candidateSearchRadius: 25.0,
        useGraphHopperMapMatching: false,
        withEvaluation: true,
        feed: null
      },
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
              GeneratedFeedService.createEvaluation(this.formData).finally(this.resetForm)
            }
            return valid;
          });

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
