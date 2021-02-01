import {reactive} from "@vue/composition-api";
import {http} from "../../plugins/HttpPlugin";


const state = reactive({
    loading: false,
    feeds: [],
});

async function fetchFeeds(presetId) {
    state.loading = true;
    return http.get(`presets/${presetId}/generated-feeds`)
        .then(({data}) => data.map(feed => {
            return {
                color: '#',
                report: null,
                feed: feed,
            };
        }))
        .finally(() => state.loading = false)
}


const Filters = {
    isFinished: (feed) => feed.status === 'FINISHED',
    isPending: (feed) => feed.status === 'PENDING',
    hasFailed: (feed) => feed.status === 'FAILED',
    hasEvaluation: (feed) => feed.extensions.hasOwnProperty('de.fleigm.ptmm.eval.EvaluationExtension'),
}

export default {
    state,
    fetchFeeds,
    Filters,
}