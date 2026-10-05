import { PrismaService } from '../prisma/prisma.service';
import { AiService } from '../ai/ai.service';
export declare class DocumentsService {
    private prisma;
    private aiService;
    constructor(prisma: PrismaService, aiService: AiService);
    getDocuments(applicantId: string): Promise<any>;
    getDocument(id: string): Promise<any>;
    uploadDocument(applicantId: string, documentType: string, fileData: any): Promise<any>;
    processDocument(id: string): Promise<any>;
    deleteDocument(id: string): Promise<any>;
}
