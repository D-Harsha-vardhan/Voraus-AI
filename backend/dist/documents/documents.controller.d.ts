import { DocumentsService } from './documents.service';
export declare class DocumentsController {
    private readonly documentsService;
    constructor(documentsService: DocumentsService);
    getDocuments(req: any): Promise<any>;
    getDocument(id: string): Promise<any>;
    uploadDocument(req: any, body: any): Promise<any>;
    processDocument(id: string): Promise<any>;
    deleteDocument(id: string): Promise<any>;
}
