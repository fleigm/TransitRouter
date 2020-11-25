export enum Events {
    createEvaluation = 'evaluation.createEvaluation'
}

export type Profile = 'bus_fastest' | 'bus_shortest';

export interface CreateEvaluationRequest {
    name: string,
    feed: File,
    profile: Profile,
    sigma: number,
    beta: number,
    candidateSearchRadius: number,
}

export interface CreatedEvaluationRequest {
    data: CreateEvaluationRequest,
    progress: number,
}