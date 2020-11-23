<template>
  <div>
    <div class="card py-4">
      <div class="p-float-label">
        <InputText id="name" type="text" v-model="value2"/>
        <label for="name">Name</label>
      </div>

    </div>
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
        profile: 'bus_shortest',
        sigma: 25.0,
        beta: 2.0,
        candidateSearchRadius: 25.0,
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
