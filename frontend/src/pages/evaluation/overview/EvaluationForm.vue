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
      <el-form-item label="profile" prop="profile">
        <el-select v-model="formData.profile">
          <el-option v-for="option in availableProfiles"
                     :key="option.value"
                     :label="option.label"
                     :value="option.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="alpha" prop="alpha">
        <el-input-number v-model="formData.alpha" :precision="2"></el-input-number>
      </el-form-item>
      <el-form-item label="beta" prop="beta">
        <el-input-number v-model="formData.beta" :precision="2"></el-input-number>
      </el-form-item>
      <el-form-item label="csr" prop="candidateSearchRadius">
        <el-input-number v-model="formData.candidateSearchRadius" :precision="2"></el-input-number>
      </el-form-item>
      <el-form-item label="u-turn penalty" prop="uTurnDistancePenalty">
        <el-input-number v-model="formData.uTurnDistancePenalty"></el-input-number>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">Start evaluation</el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </div>

</template>

<script>
import {Helper} from "../../../Helper";
import UploadProgressNotification from "./UploadProgressNotification";
import Vue from 'vue'

export default {
  name: "v-evaluation-form",

  data() {
    return {
      availableProfiles: [
        {
          value: 'bus_custom_shortest',
          label: 'bus custom shortest',
        }, {
          value: 'bus_fastest',
          label: 'bus fastest',
        }
      ],
      rules: {
        name: [
          {required: true, message: 'Please enter a name', trigger: 'blur'}
        ],
        profile: [
          {required: true, message: 'Please enter a profile', trigger: 'blur'}
        ],
        alpha: [
          {type: 'number', required: true, min: 0, message: 'Alpha value must be positive', trigger: 'blur'}
        ],
        beta: [
          {type: 'number', required: true, min: 0, message: 'Beta value must be positive', trigger: 'blur'}
        ],
        candidateSearchRadius: [
          {type: 'number', required: true, min: 0, message: 'Csr value must be positive', trigger: 'blur'}
        ],
        uTurnDistancePenalty: [
          {type: 'number', required: true, min: 0, message: 'uTurn penalty value must be positive', trigger: 'blur'}
        ],
        feed: [
          {required: true, message: 'Please add a gtfs feed', trigger: 'change'}
        ]
      },
      fileList: [],
      formData: {
        name: '',
        profile: 'bus_custom_shortest',
        alpha: 25.0,
        beta: 2.0,
        candidateSearchRadius: 25.0,
        uTurnDistancePenalty: 1500,
        feed: null
      },
    }
  },

  methods: {
    setGtfsFeed(file, fileList) {
      this.formData.feed = fileList.length ? file : null;
    },

    resetForm() {
      this.$refs['evaluation-upload-form'].resetFields();
      this.$refs['upload-field'].clearFiles();
    },

    submit() {
      this.$refs['evaluation-upload-form']
          .validate()
          .then(() => this.sendRequest())
          .catch(() => {
          })
    },

    sendRequest() {
      const createdEvaluationRequest = Vue.observable({
        formData: Helper.copyObject(this.formData),
        progress: 0,
        request: null,
      });

      createdEvaluationRequest.request = this.$http.post('eval', this.buildFormData(), {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: function (progressEvent) {
          createdEvaluationRequest.progress = parseInt(Math.round((progressEvent.loaded * 100) / progressEvent.total));
        }
      }).then(this.sendSuccessNotification);

      this.resetForm();

      const uploadNotification = this.$notify({
        title: 'Uploading GTFS feed',
        message: this.$createElement(UploadProgressNotification, {
          props: {
            event: createdEvaluationRequest
          }
        }),
        type: 'info',
        duration: 0,
        position: 'bottom-right'
      });
      createdEvaluationRequest.request.finally(() => {
        uploadNotification.close();
      });

      this.$events.$emit('evaluation:createdRequest', createdEvaluationRequest)
    },

    buildFormData() {
      const formData = new FormData();
      for (const [key, value] of Object.entries(this.formData)) {
        formData.append(key, value);
      }
      formData.set('feed', this.formData.feed.raw);
      return formData;
    },

    sendSuccessNotification() {
      this.$notify({
        title: 'Upload complete',
        message: 'Your upload was successful and the evaluation process has started.',
        type: 'success',
        position: "bottom-right"
      })
    },
  },
}
</script>
