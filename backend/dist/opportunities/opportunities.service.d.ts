import { PrismaService } from '../prisma/prisma.service';
export declare class OpportunitiesService {
    private prisma;
    constructor(prisma: PrismaService);
    getAllOpportunities(): Promise<{
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
    matchOpportunitiesForApplicant(applicantId: string): Promise<({
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
