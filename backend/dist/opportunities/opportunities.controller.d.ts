import { OpportunitiesService } from './opportunities.service';
export declare class OpportunitiesController {
    private readonly opportunitiesService;
    constructor(opportunitiesService: OpportunitiesService);
    getOpportunities(): Promise<{
        id: string;
        location: string | null;
        institution: string;
        source: string;
        requirements: string | null;
        programName: string;
        degreeType: string | null;
        languageRequirements: string | null;
        url: string | null;
        lastUpdated: Date;
    }[]>;
    getOpportunity(id: string): Promise<{
        id: string;
        location: string | null;
        institution: string;
        source: string;
        requirements: string | null;
        programName: string;
        degreeType: string | null;
        languageRequirements: string | null;
        url: string | null;
        lastUpdated: Date;
    }>;
    searchOpportunities(req: any): Promise<({
        institution: string;
        programName: string;
        location: string;
        degreeType: string;
        matchStatus: string;
        requirements: {
            Bachelor: boolean;
            IELTS: number;
            German: boolean;
            APS?: undefined;
        };
    } | {
        institution: string;
        programName: string;
        location: string;
        degreeType: string;
        matchStatus: string;
        requirements: {
            Bachelor: boolean;
            IELTS: number;
            APS: boolean;
            German?: undefined;
        };
    })[]>;
}
