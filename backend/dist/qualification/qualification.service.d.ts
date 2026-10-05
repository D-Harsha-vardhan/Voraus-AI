import { PrismaService } from '../prisma/prisma.service';
export declare class QualificationService {
    private prisma;
    constructor(prisma: PrismaService);
    getQualificationStatus(applicantId: string): Promise<any>;
    evaluateQualification(applicantId: string): Promise<any>;
}
