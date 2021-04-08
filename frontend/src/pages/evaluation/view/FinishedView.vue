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
        <div class="flex justify-between gap-4 mb-4">
          <v-metric :value="feed.parameters.profile"
                    class=""
                    size="small"
                    title="Profile"
          ></v-metric>
          <v-metric :value="feed.parameters.useGraphHopperMapMatching ? 'GHMM' : 'TransitRouter'"
                    class=""
                    size="small"
                    title="Router"
          ></v-metric>
        </div>
        <div class="flex justify-between gap-4">
          <v-metric :value="feed.parameters.beta | number('0.00')"
                    class=""
                    title="beta"
          ></v-metric>
          <v-metric :value="feed.parameters.sigma | number('0')"
                    class=""
                    title="sigma"
          ></v-metric>
          <v-metric :value="feed.parameters.candidateSearchRadius | number('0')"
                    class=""
                    title="CSR"
          ></v-metric>
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
import HistogramAverageFrechetDistance from "./HistogramAverageFrechetDistance";
import HistogramMismatchedHopSegments from "./HistogramMismatchedHopSegments";
import HistogramLengthMismatchedHopSegments from "./HistogramLengthMismatchedHopSegments";
import ExecutionTimeChart from "./ExecutionTimeChart";
import EvaluationCard from "./EvaluationCard";
import VAccuracyChart from "../AccuracyChart";
import ErrorCard from "./ErrorCard";
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
    return {
    }
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

  methods: {
  },

  mounted() {
  }
}
</script>