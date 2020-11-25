import {reactive} from '@vue/composition-api';
import {http} from '../../plugins/HttpPlugin';
import {events} from '../../plugins/EventPlugin';
import {Notification} from 'element-ui';
import {objectToFormData} from '../../Helper';

const state = reactive({
    evaluations: [],
});

async function fetchEvaluations() {
    const response = await http.get('eval');
    state.evaluations = response.data;
    return response;
}

async function clearCache() {
    await http.post('commands/eval/clear-cache');
    await fetchEvaluations();

    Notification.success({
        title: 'Cleared cache',
        message: 'Cleared the cache and reloaded the evaluations.',
        position: 'bottom-right',
    });
}

async function deleteEvaluation(name) {
    try {
        const response = await http.delete(`eval/${name}`);
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
        const response = await http.post('eval', objectToFormData(evaluation), {
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
