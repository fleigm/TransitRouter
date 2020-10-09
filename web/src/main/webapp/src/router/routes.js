import Dashboard from '../pages/dashboard/Index'
import CandidateFinder from "../pages/CandidateFinder";

const routes = [
    {
        path: '/',
        name: 'Dashboard',
        component: Dashboard,
    }, {
        path: '/candidate-finder',
        name: 'Candidate Finder',
        component: CandidateFinder,
    }
]

export default routes