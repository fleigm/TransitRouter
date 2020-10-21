import Dashboard from '../pages/dashboard/Index'
import CandidateFinder from "../pages/CandidateFinder";
import Evaluation from '../pages/evaluation/Index';

const routes = [
    {
        path: '/',
        name: 'Dashboard',
        component: Dashboard,
    }, {
        path: '/eval',
        name: 'Evaluation',
        component: Evaluation,
    }, {
        path: '/candidate-finder',
        name: 'Candidate Finder',
        component: CandidateFinder,
    }
]

export default routes