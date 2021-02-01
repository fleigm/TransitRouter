import {reactive} from '@vue/composition-api';
import {http} from '../../plugins/HttpPlugin';
import {events} from '../../plugins/EventPlugin';
import {Notification} from 'element-ui';
import {objectToFormData} from '../../Helper';

const state = reactive({
    presets: [],
});

async function fetchPresets() {
    const response = await http.get('presets');
    state.presets = response.data;
    return response;
}

async function fetchPreset(id) {
    return await http.get('presets/' + id);
}

async function clearCache() {
    await http.post('commands/presets/clear-cache');
    await fetchPresets();

    Notification.success({
        title: 'Cleared cache',
        message: 'Cleared the cache and reloaded the presets.',
        position: 'bottom-right',
    });
}

async function deletePreset(id) {
    try {
        const response = await http.delete(`presets/${id}`);
        Notification.success(`Deleted preset.`);
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

async function createPreset(preset) {
    console.log(preset);

    const event = reactive({
        data: preset,
        progress: 0
    });

    events.$emit('preset:createdRequest', event);

    try {
        const response = await http.post('presets', objectToFormData(preset), {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: function (progressEvent) {
                event.progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            }
        });

        Notification.success({
            title: 'Upload complete',
            message: 'Your upload was successful and the preset process has started.',
            position: 'bottom-right',
        });

        state.presets.unshift(response.data);

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
    fetchPresets,
    fetchPreset,
    clearCache,
    deletePreset,
    createPreset,
    state,
}
