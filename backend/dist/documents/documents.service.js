"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.DocumentsService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const ai_service_1 = require("../ai/ai.service");
let DocumentsService = class DocumentsService {
    prisma;
    aiService;
    constructor(prisma, aiService) {
        this.prisma = prisma;
        this.aiService = aiService;
    }
    async getDocuments(applicantId) {
        return this.prisma.document.findMany({
            where: { applicantId }
        });
    }
    async getDocument(id) {
        const doc = await this.prisma.document.findUnique({ where: { id } });
        if (!doc)
            throw new common_1.NotFoundException('Document not found');
        return doc;
    }
    async uploadDocument(applicantId, documentType, fileData) {
        const storagePath = `documents/${applicantId}/${Date.now()}_${documentType}.pdf`;
        const document = await this.prisma.document.create({
            data: {
                applicantId,
                documentType,
                fileName: `${documentType}.pdf`,
                storagePath,
                uploadStatus: 'uploaded',
                processingStatus: 'pending'
            }
        });
        this.processDocument(document.id).catch(console.error);
        return document;
    }
    async processDocument(id) {
        const document = await this.prisma.document.update({
            where: { id },
            data: { processingStatus: 'processing' }
        });
        const extractionResult = await this.aiService.processDocument(document);
        return this.prisma.document.update({
            where: { id },
            data: {
                processingStatus: 'completed',
                extractedData: extractionResult,
                verificationStatus: 'verified'
            }
        });
    }
    async deleteDocument(id) {
        return this.prisma.document.delete({ where: { id } });
    }
};
exports.DocumentsService = DocumentsService;
exports.DocumentsService = DocumentsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        ai_service_1.AiService])
], DocumentsService);
//# sourceMappingURL=documents.service.js.map