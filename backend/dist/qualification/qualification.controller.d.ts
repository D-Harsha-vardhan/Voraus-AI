import { QualificationService } from './qualification.service';
export declare class QualificationController {
    private readonly qualificationService;
    constructor(qualificationService: QualificationService);
    getQualificationStatus(req: any): Promise<any>;
    evaluateQualification(req: any): Promise<any>;
}
