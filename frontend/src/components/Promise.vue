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
    if (this.$slots.combined) {
      return this.$slots.combined({
        isLoading: !this.resolved,
        response: this.response,
        data: this.response.data,
        error: this.error,
      })
    }


    if (this.error) {
      return this.$slots.rejected(this.error);
    }

    if (this.resolved) {
      return this.$slots.default(this.response);

    }

    return this.$slots.pending();
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