import { QualificationService } from './qualification.service';
export declare class QualificationController {
    private readonly qualificationService;
    constructor(qualificationService: QualificationService);
    getQualificationStatus(req: any): Promise<{
        id: string;
        applicantId: string;
        qualificationStatus: string;
        scoreOrReasoningSummary: string | null;
        evaluatedAt: Date;
    } | {
        status: string;
        message: string;
    }>;
    evaluateQualification(req: any): Promise<{
        id: string;
        applicantId: string;
        qualificationStatus: string;
        scoreOrReasoningSummary: string | null;
        evaluatedAt: Date;
    }>;
}
