import { PrismaService } from '../prisma/prisma.service';
export declare class ApplicantService {
    private prisma;
    constructor(prisma: PrismaService);
    getProfile(applicantId: string): Promise<{
        education: {
            id: string;
            applicantId: string;
            institution: string;
            degree: string;
            fieldOfStudy: string;
            graduationDate: Date | null;
            cgpa: string | null;
            country: string | null;
            verificationStatus: string;
        }[];
        employment: {
            id: string;
            applicantId: string;
            verificationStatus: string;
            employer: string;
            role: string;
            responsibilities: string | null;
            startDate: Date | null;
            endDate: Date | null;
        }[];
        skills: {
            skill: string;
            id: string;
            applicantId: string;
            proficiency: string | null;
            source: string | null;
        }[];
        languages: {
            language: string;
            id: string;
            applicantId: string;
            verificationStatus: string;
            proficiency: string;
            certificateId: string | null;
        }[];
        documents: {
            id: string;
            applicantId: string;
            verificationStatus: string;
            documentType: string;
            fileName: string;
            storagePath: string;
            uploadStatus: string;
            processingStatus: string;
            extractedData: string | null;
            uploadedAt: Date;
        }[];
        qualifications: {
            id: string;
            applicantId: string;
            qualificationStatus: string;
            scoreOrReasoningSummary: string | null;
            evaluatedAt: Date;
        }[];
    } & {
        id: string;
        fullName: string;
        phone: string | null;
        location: string | null;
        dateOfBirth: Date | null;
        availability: string | null;
        journeyGoal: string | null;
        profileCompletion: number;
        journeyStatus: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
    }>;
    updateProfile(applicantId: string, updateData: any): Promise<{
        id: string;
        fullName: string;
        phone: string | null;
        location: string | null;
        dateOfBirth: Date | null;
        availability: string | null;
        journeyGoal: string | null;
        profileCompletion: number;
        journeyStatus: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
    }>;
}
