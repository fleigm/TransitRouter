<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>Upload GTFS feed <i class="el-icon-upload el-icon-right"></i></span>

    <portal>
      <el-dialog
          :visible.sync="showDialog"
          title="Upload GTFS feed">

        <el-form ref="preset-upload-form" :model="formData" :rules="rules" label-width="120px" size="mini">
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
          <el-form-item>
            <el-button type="primary" @click="submit">Upload</el-button>
            <el-button @click="resetForm">Reset</el-button>
          </el-form-item>
        </el-form>

      </el-dialog>
    </portal>
  </el-button>
</template>

<script>

import {Notification} from "element-ui";
import UploadProgressNotification from "../../evaluation/overview/UploadProgressNotification";
import {watch} from "@vue/composition-api";
import PresetService from "../PresetService";

export default {
  name: "v-preset-upload-dialog",

  data() {
    return {
      showDialog: false,

      rules: {
        name: [
          {required: true, message: 'Please enter a name', trigger: 'blur'}
        ],
        feed: [
          {required: true, message: 'Please add a gtfs feed', trigger: 'change'}
        ]
      },
      fileList: [],
      formData: {
        name: '',
        feed: null,
      }
    }
  },

  methods: {
    open() {
      this.showDialog = true;
    },
    close() {
      this.showDialog = false;
    },

    setGtfsFeed(file, fileList) {
      this.formData.feed = fileList.length ? file.raw : null;
    },

    resetForm() {
      this.$refs['preset-upload-form'].resetFields();
      this.$refs['upload-field'].clearFiles();
    },

    async submit() {
      this.$events.$once('preset:createdRequest', this.displayUploadNotification)

      this.$refs['preset-upload-form'].validate((valid) => {
        if (!valid) {
          return false
        }

        PresetService.createPreset(this.formData);
        this.resetForm();
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
  },

  mounted() {
    this.$events.$on('preset:createdRequest', this.close)
  },

  beforeDestroy() {
    this.$events.$off('preset:createdRequest', this.close)
  }
}
</script>
