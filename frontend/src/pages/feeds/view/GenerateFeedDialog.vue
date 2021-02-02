<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>Generate Feed</span>

    <portal>
      <el-dialog
          :visible.sync="showDialog"
          title="Generate new feed">

        <el-form ref="generate-feed-form" :model="formData" :rules="rules" label-width="120px" size="mini">
          <el-form-item label="name" prop="name">
            <el-input v-model="formData.name"></el-input>
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
          <el-form-item label="sigma" prop="sigma">
            <el-input-number v-model="formData.sigma" :precision="2"></el-input-number>
          </el-form-item>
          <el-form-item label="beta" prop="beta">
            <el-input-number v-model="formData.beta" :precision="2"></el-input-number>
          </el-form-item>
          <el-form-item label="csr" prop="candidateSearchRadius">
            <el-input-number v-model="formData.candidateSearchRadius" :precision="2"></el-input-number>
          </el-form-item>
          <el-form-item label="use GHMM">
            <el-checkbox v-model="formData.useGraphHopperMapMatching"></el-checkbox>
          </el-form-item>

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
      },
      formData: {
        name: '',
        profile: 'bus_fastest_turn',
        sigma: 25.0,
        beta: 2.0,
        candidateSearchRadius: 25.0,
        useGraphHopperMapMatching: false,
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

    async submit() {
      this.$refs['generate-feed-form'].validate()
          .then(() => {
            this.sendRequest();
            this.close();
          })
          .finally(this.resetForm)

    },

    sendRequest() {
      this.$http.post(`presets/${this.preset.id}/generated-feeds`, this.formData)
          .then(({data}) => {
            this.$events.$emit('presets.generatedFeed', data);
            console.log(data);
          })
    }
  },
}
</script>