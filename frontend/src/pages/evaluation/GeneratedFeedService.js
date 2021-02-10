import {reactive} from '@vue/composition-api';
import {http} from '../../plugins/HttpPlugin';
import {events} from '../../plugins/EventPlugin';
import {Notification} from 'element-ui';
import {objectToFormData} from '../../Helper';

const state = reactive({
    evaluations: [],
    loading: false,
});

async function fetchEvaluations() {
    state.loading = true;
    return http.get('feeds')
        .then((response) => {
            state.evaluations = response.data;
            return response;
        })
        .finally(() => {
            state.loading = false;
        });

}

async function clearCache() {
    await http.post('commands/feeds/clear-cache');
    await fetchEvaluations();

    Notification.success({
        title: 'Cleared cache',
        message: 'Cleared the cache and reloaded the evaluations.',
        position: 'bottom-right',
    });
}

async function deleteEvaluation(id) {
    try {
        const response = await http.delete(`feeds/${id}`);
        let index = state.evaluations.findIndex(feed => feed.id === id);
        if (index >= 0) {
            state.evaluations.splice(index, 1);
        }
        Notification.success(`Deleted evaluation.`);
        return response;
    } catch (error) {
        const message = error.response.status === '409' ? error.response.data.message : 'Unknown error.';
        Notification.error({
            title: 'Oops, something went wrong',
            message,
            position: 'bottom-right',
        });

        throw error;
    }
}

async function createEvaluation(evaluation) {
    console.log(evaluation);

    const event = reactive({
        data: evaluation,
        progress: 0
    });

    events.$emit('evaluation:createdRequest', event);

    try {
        const response = await http.post('feeds', objectToFormData(evaluation), {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: function (progressEvent) {
                event.progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            }
        });

        Notification.success({
            title: 'Upload complete',
            message: 'Your upload was successful and the evaluation process has started.',
            position: 'bottom-right',
        });

        state.evaluations.unshift(response.data);

        return response;
    } catch (error) {
        Notification.error({
            title: 'Upload failed.',
            message: '',
            position: 'bottom-right',
        })
        throw error;
    }
}

export default {
    fetchEvaluations,
    clearCache,
    deleteEvaluation,
    createEvaluation,
    state,
}
