import { PrismaService } from '../prisma/prisma.service';
export declare class QualificationService {
    private prisma;
    constructor(prisma: PrismaService);
    getQualificationStatus(applicantId: string): Promise<{
        id: string;
        applicantId: string;
        qualificationStatus: string;
        scoreOrReasoningSummary: string | null;
        evaluatedAt: Date;
    } | {
        status: string;
        message: string;
    }>;
    evaluateQualification(applicantId: string): Promise<{
        id: string;
        applicantId: string;
        qualificationStatus: string;
        scoreOrReasoningSummary: string | null;
        evaluatedAt: Date;
    }>;
}
