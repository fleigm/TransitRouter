import Dashboard from '../pages/dashboard/Index'
import CandidateFinder from "../pages/CandidateFinder";
import EvaluationIndex from '../pages/evaluation/Index';
import EvaluationView from '../pages/evaluation/View';
import EvaluationOverview from '../pages/evaluation/Overview';

const routes = [
    {
        path: '/',
        name: 'Dashboard',
        component: Dashboard,
    }, {
        path: '/eval',
        name: 'evaluation.index',
        component: EvaluationIndex,
        children: [{
            path: '',
            name: 'Overview',
            component: EvaluationOverview,
        }, {
            path: ':name',
            name: 'evaluation.view',
            component: EvaluationView,
        }]
    }, {
        path: '/candidate-finder',
        name: 'Candidate Finder',
        component: CandidateFinder,
    }
]

export default routes