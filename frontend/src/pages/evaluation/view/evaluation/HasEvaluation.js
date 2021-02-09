export default {
    props: {
        feed: {
            type: Object,
            required: true,
        },
    },

    computed: {
        evaluation() {
            return this.feed.extensions['de.fleigm.ptmm.feeds.evaluation.Evaluation'];
        }
    }
}