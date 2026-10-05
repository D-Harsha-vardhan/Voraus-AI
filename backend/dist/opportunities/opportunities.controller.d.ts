import { OpportunitiesService } from './opportunities.service';
export declare class OpportunitiesController {
    private readonly opportunitiesService;
    constructor(opportunitiesService: OpportunitiesService);
    getOpportunities(): Promise<any>;
    getOpportunity(id: string): Promise<any>;
    searchOpportunities(req: any): Promise<any>;
}
