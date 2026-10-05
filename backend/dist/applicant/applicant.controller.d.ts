import { ApplicantService } from './applicant.service';
export declare class ApplicantController {
    private readonly applicantService;
    constructor(applicantService: ApplicantService);
    getProfile(req: any): Promise<any>;
    updateProfile(req: any, updateData: any): Promise<any>;
}
