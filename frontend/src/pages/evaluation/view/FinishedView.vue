<template>
  <div class="">
    <div class="grid grid-cols-3 gap-4">
      <evaluation-card header="feed info">
        <div class="max-h-64 overflow-y-scroll" v-if="hasFeedDetails">
          <v-metric title="Agency" :value="feedDetails.agencies[0].agency_name" size="mini"></v-metric>
          <el-table :data="routesAndTripsPerType" size="mini" class="p-2">
            <el-table-column prop="type" label="Type" width=""></el-table-column>
            <el-table-column prop="routes" label="Routes" width="80"></el-table-column>
            <el-table-column prop="trips" label="Trips" width="80"></el-table-column>
          </el-table>
        </div>
        <div v-else>
          <div class="w-full text-xl text-secondary font-thin my-8 text-center">Generating feed info...</div>
        </div>
      </evaluation-card>


      <EvaluationCard header="Parameters" class="">
        <div class="grid grid-cols-1 gap-4 max-h-64 overflow-y-scroll">
          <div v-for="(params, type) in feed.parameters" :key="type">
            <p>{{ type | capitalize }}</p>
            <div class="flex">
              <v-metric :value="params.profile"
                        class=""
                        size="mini"
                        title="Profile"
              ></v-metric>
              <v-metric :value="params.useGraphHopperMapMatching ? 'GHMM' : 'TransitRouter'"
                        class=""
                        size="mini"
                        title="Router"
              ></v-metric>
            </div>
            <div class="flex gap-4">
              <v-metric :value="params.beta | number('0.00')"
                        class=""
                        size="mini"
                        title="beta"
              ></v-metric>
              <v-metric :value="params.sigma | number('0')"
                        class=""
                        size="mini"
                        title="sigma"
              ></v-metric>
              <v-metric :value="params.candidateSearchRadius | number('0')"
                        class=""
                        size="mini"
                        title="CSR"
              ></v-metric>
            </div>
          </div>
        </div>
      </EvaluationCard>

      <EvaluationCard header="execution times">
        <ExecutionTimeChart v-if="feed" :feed="feed"></ExecutionTimeChart>
      </EvaluationCard>
      <v-card class="col-span-3">
        <v-report-list :name="$route.params.name"></v-report-list>
      </v-card>
    </div>

    <v-feed-evaluation :feed="feed" v-if="evaluation" class="my-4"></v-feed-evaluation>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import ExecutionTimeChart from "./ExecutionTimeChart";
import EvaluationCard from "./EvaluationCard";
import VFeedEvaluation from "./evaluation/FeedEvaluation";
import {routeTypeToString} from "../../../filters/Filters";

export default {
  name: "v-finished-view",

  components: {
    VFeedEvaluation,
    EvaluationCard,
    ExecutionTimeChart,
    VReportList
  },

  props: {
    feed: {
      type: Object,
      required: true,
    }
  },

  data() {
    return {}
  },

  computed: {
    evaluation() {
      return this.feed.extensions['de.fleigm.transitrouter.feeds.evaluation.Evaluation'];
    },

    hasFeedDetails() {
      return Object.prototype.hasOwnProperty.call(this.feed.extensions, 'de.fleigm.transitrouter.gtfs.FeedDetails');
    },

    feedDetails() {
      return this.feed.extensions['de.fleigm.transitrouter.gtfs.FeedDetails'];
    },

    routesAndTripsPerType() {
      if (!this.hasFeedDetails) {
        return null;
      }

      var map = [];
      map.push({type: 'Total', routes: this.feedDetails.routes, trips: this.feedDetails.trips})

      for (const [key, value] of Object.entries(this.feedDetails.routesPerType)) {
        map.push({
          type: routeTypeToString(key),
          routes: value,
          trips: this.feedDetails.tripsPerType[key]
        });
      }
      return map;
    },

    shapeGenerationErrors() {
      return this.feed.errors.filter(error => error.code === 'shape_generation_failed') ?? [];
    },
    affectedTripCount() {
      return this.shapeGenerationErrors
          .flatMap(error => error.details.trips.length)
          .reduce((sum, count) => sum + count, 0)
    }
  },

  methods: {},

  mounted() {
  }
}
</script>