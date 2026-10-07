import { PrismaService } from '../prisma/prisma.service';
import { AiService } from '../ai/ai.service';
export declare class DocumentsService {
    private prisma;
    private aiService;
    constructor(prisma: PrismaService, aiService: AiService);
    getDocuments(applicantId: string): Promise<{
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
    }[]>;
    getDocument(id: string): Promise<{
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
    }>;
    uploadDocument(applicantId: string, documentType: string, fileData: any): Promise<{
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
    }>;
    processDocument(id: string): Promise<{
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
    }>;
    deleteDocument(id: string): Promise<{
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
    }>;
}
