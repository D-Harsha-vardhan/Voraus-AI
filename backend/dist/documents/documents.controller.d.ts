import { DocumentsService } from './documents.service';
export declare class DocumentsController {
    private readonly documentsService;
    constructor(documentsService: DocumentsService);
    getDocuments(req: any): Promise<{
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
    uploadDocument(req: any, body: any): Promise<{
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
