import Vue from 'vue';
import {http} from '../../plugins/HttpPlugin';

const parameters = {
    profile: 'profile',
    sigma: 'measurement_error_sigma',
    beta: 'transitions_beta_probability',
    uTurnDistancePenalty: 'u_turn_distance_penalty',
    candidateSearchRadius: 'candidate_search_radius',
}

const defaultOptions = {
    profile: 'bus_shortest',
    sigma: 25,
    candidateSearchRadius: 25,
    beta: 2.0,
    uTurnDistancePenalty: 1500,
}

const options = Vue.observable(JSON.parse(JSON.stringify(defaultOptions)));

const computeRouting = function (routeId) {
    const query = new URLSearchParams();
    for (const [key, value] of Object.entries(options)) {
        if (parameters.hasOwnProperty(key)) {
            query.append(parameters[key], value);
        } else {
            query.append(key, value);
        }
    }

    return http.get(`routing/${routeId}`, {params: query})
}

export default {
    defaultOptions,
    options,
    computeRouting
}