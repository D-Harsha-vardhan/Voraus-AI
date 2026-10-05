import { PrismaService } from '../prisma/prisma.service';
export declare class ApplicantService {
    private prisma;
    constructor(prisma: PrismaService);
    getProfile(applicantId: string): Promise<any>;
    updateProfile(applicantId: string, updateData: any): Promise<any>;
}
