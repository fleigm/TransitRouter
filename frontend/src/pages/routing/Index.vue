<template>
  <div class="flex w-full">
    <div class="p-4 w-64">
      <el-form ref="evaluation-upload-form" :model="formData" :rules="rules" size="mini" label-position="top">
        <el-form-item label="observations" prop="observationInput">
          <div class="relative w-full border rounded px-2">
            <draggable v-model="formData.observations" handle=".sort-handle" gohstClass="bg-blue-200">
              <div v-for="(observation, i) in formData.observations"
                   :key="i"
                   class="flex items-center gap-2 py-1 group">
                <i class="el-icon-menu sort-handle cursor-move text-gray-600 bg-white"></i>
                <div class="flex-grow text-primary text-xs">
                  <span>{{ observation[0] | number('0.00000') }}</span>,
                  <span>{{ observation[1] | number('0.00000') }}</span>
                </div>
                <i class="el-icon-delete text-xs text-secondary hidden group-hover:block cursor-pointer hover:text-red-400"
                   @click="removeObservation(i)"></i>
              </div>
            </draggable>
          </div>
          <el-input placeholder="enter coordiante" v-model="observationInput"
                    @keyup.enter.native="addObservation"></el-input>
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
        <el-form-item>
          <!--<el-button type="primary" @click="submit"></el-button>-->
          <!--<el-button @click="resetForm">Reset</el-button>-->
        </el-form-item>
      </el-form>
    </div>
    <div class="p-4 w-full">
      <v-map :bounds="bounds" v-if="route">
        <l-marker v-for="(observation, i) in route.observations"
                  :key="'o' + i"
                  :lat-lng="observation"
                  :radius="2"
                  color="red"
                  draggable
                  @dragend="(e) => moveObservation(i, e)">
          <l-tooltip>
            <span>Observation #{{ i }}</span>
          </l-tooltip>
        </l-marker>

        <l-circle v-for="(candidate, i) in route.candidates"
                  :key="'c' + i"
                  :radius="1"
                  color="blue"
                  :lat-lng="candidate">
        </l-circle>

        <l-animated-polyline :lat-lngs="route.path"
                             :options="{'delay': 2400, fillOpacity: 0.5, opacity: 0.5}">
          <l-popup>
            <div>Distance: {{ route.distance }}</div>
            <div>Time: {{ route.time }}</div>
          </l-popup>
        </l-animated-polyline>

      </v-map>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import L from "leaflet";
import VMap from "../../components/Map";

export default {
  name: "Index",

  components: {VMap, draggable},

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
        observationInput: [
          {type: 'regexp', pattern: '(\\d*\\.?\\d+), (\\d*\\.?\\d+)', trigger: 'blur'}
        ]
      },
      formData: {
        observations: [
          [48.8095244, 9.1819293],
          [48.8157918, 9.1876709],
        ],
        profile: 'bus_fastest',
        sigma: 25.0,
        beta: 2.0,
        candidateSearchRadius: 25.0,
      },
      observationInput: '',
      route: null,
    }
  },

  computed: {
    bounds() {
      return L.latLngBounds(this.formData.observations);
    },
  },

  methods: {
    addObservation() {
      let values = this.observationInput.match('(\\d*\\.?\\d+), (\\d*\\.?\\d+)');

      if (!values) {
        return;
      }

      this.observationInput = '';

      this.formData.observations.push([Number(values[1]), Number(values[2])]);
    },

    removeObservation(index) {
      this.formData.observations.splice(index, 1);
    },

    moveObservation(index, event) {
      const {lat, lng} = event.target._latlng;
      this.formData.observations.splice(index, 1, [lat, lng])
    },

    fetchRoute() {
      this.$http.post('debug/routing', this.formData)
          .then(({data}) => {
            console.log(data);
            this.route = data;
          })
          .catch((response) => {
            console.log(response);
            this.$notify.error("Ooops, something went wrong");
          })
    }
  },

  watch: {
    formData: {
      handler: 'fetchRoute',
      deep: true
    }
  },

  mounted() {
    this.fetchRoute();
  }
}
</script>