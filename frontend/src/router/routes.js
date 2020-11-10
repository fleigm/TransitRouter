import Dashboard from '../pages/dashboard/Index'
import CandidateFinder from "../pages/CandidateFinder";
import EvaluationIndex from '../pages/evaluation/Index';
import EvaluationView from '../pages/evaluation/view/Index';
import EvaluationOverview from '../pages/evaluation/overview/Index';

const routes = [
    {
        path: '/',
        name: 'dashboard.index',
        component: Dashboard,
    }, {
        path: '/eval',
        component: EvaluationIndex,
        children: [{
            path: '',
            name: 'evaluation.index',
            component: EvaluationOverview,
        }, {
            path: ':name',
            name: 'evaluation.view',
            component: EvaluationView,
        }]
    }, {
        path: '/candidate-finder',
        name: 'candidateFinder.index',
        component: CandidateFinder,
    }
]

export default routes