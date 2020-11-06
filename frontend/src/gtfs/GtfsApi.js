import {http} from '../plugins/HttpPlugin';

const fetchRoutes = () => {
    return http.get('gtfs/routes');
}

const fetchTrips = (routeId) => {
    return http.get(`gtfs/routes/${routeId}/trips`)
}

export default {
    fetchRoutes,
    fetchTrips,
}