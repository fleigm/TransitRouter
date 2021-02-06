import CandidateFinder from "../pages/CandidateFinder";
import EvaluationIndex from '../pages/evaluation/Index';
import EvaluationView from '../pages/evaluation/view/Index';
import EvaluationOverview from '../pages/evaluation/overview/Index';
import RoutingIndex from '../pages/routing/Index';
import FeedIndex from '../pages/feeds/Index';
import FeedOverview from '../pages/feeds/overview/Index';
import FeedView from '../pages/feeds/view/Index';


const routes = [
    {
        path: '/',
        redirect: {
            name: 'presets.index'
        }
    }, {
        path: '/feeds',
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
        path: '/presets',
        component: FeedIndex,
        children: [{
            path: '',
            name: 'presets.index',
            component: FeedOverview,
        }, {
            path: ':id',
            name: 'presets.view',
            component: FeedView,
        }]
    }, {
        path: '/routing',
        name: 'routing.index',
        component: RoutingIndex,
    }, {
        path: '/candidate-finder',
        name: 'candidateFinder.index',
        component: CandidateFinder,
    },
]

export default routes