import { PrismaService } from '../prisma/prisma.service';
export declare class OpportunitiesService {
    private prisma;
    constructor(prisma: PrismaService);
    getAllOpportunities(): Promise<any>;
    getOpportunity(id: string): Promise<any>;
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
