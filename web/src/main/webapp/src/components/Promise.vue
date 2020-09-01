<script>
    export default {
        name: 'v-promise',

        props: {
            promise: {
                required: true
            },
        },

        data() {
            return {
                resolved: false,
                response: null,
                error: null,
            }
        },

        methods: {},

        render() {
            if (this.$scopedSlots.combined) {
                return this.$scopedSlots.combined({
                    isPending: !this.resolved,
                    response: this.response,
                    error: this.error,
                })
            }


            if (this.error) {
                return this.$scopedSlots.rejected(this.error);
            }

            if (this.resolved) {
                return this.$scopedSlots.default(this.response);

            }

            return this.$scopedSlots.pending();
        },

        watch: {
            promise: {
                immediate: true,

                handler(promise) {
                    if (!promise) return;

                    this.resolved = false;
                    this.error = null;

                    promise
                        .then(response => {
                            this.response = response;
                            this.resolved = true;
                        })
                        .catch(error => {
                            this.error = error;
                            this.resolved = true;
                        });
                }
            }
        }
    }
</script>