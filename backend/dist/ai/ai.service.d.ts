import { PrismaService } from '../prisma/prisma.service';
export declare class AiService {
    private prisma;
    constructor(prisma: PrismaService);
    processDocument(document: any): Promise<{}>;
    updateApplicantProfile(applicantId: string, type: string, data: any): Promise<void>;
    runQualificationCheck(applicantId: string): Promise<void>;
    runJourneyOrchestrator(applicantId: string): Promise<void>;
}
