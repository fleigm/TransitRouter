export default {
    props: {
        feed: {
            type: Object,
            required: true,
        },
    },

    computed: {
        evaluation() {
            return this.feed.extensions['de.fleigm.transitrouter.feeds.evaluation.Evaluation'];
        }
    }
}