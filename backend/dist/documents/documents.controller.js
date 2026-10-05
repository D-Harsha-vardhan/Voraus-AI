var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var _a;
import { Controller, Get, Post, Delete, Param, Req, Body } from '@nestjs/common';
import { DocumentsService } from './documents.service';
let DocumentsController = class DocumentsController {
    documentsService;
    constructor(documentsService) {
        this.documentsService = documentsService;
    }
    async getDocuments(req) {
        const applicantId = req.headers['x-applicant-id'];
        return this.documentsService.getDocuments(applicantId);
    }
    async getDocument(id) {
        return this.documentsService.getDocument(id);
    }
    async uploadDocument(req, body) {
        const applicantId = req.headers['x-applicant-id'];
        const { documentType } = body;
        return this.documentsService.uploadDocument(applicantId, documentType, 'mocked-file-metadata');
    }
    async processDocument(id) {
        return this.documentsService.processDocument(id);
    }
    async deleteDocument(id) {
        return this.documentsService.deleteDocument(id);
    }
};
__decorate([
    Get(),
    __param(0, Req()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], DocumentsController.prototype, "getDocuments", null);
__decorate([
    Get(':id'),
    __param(0, Param('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], DocumentsController.prototype, "getDocument", null);
__decorate([
    Post('upload'),
    __param(0, Req()),
    __param(1, Body()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", Promise)
], DocumentsController.prototype, "uploadDocument", null);
__decorate([
    Post(':id/process'),
    __param(0, Param('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], DocumentsController.prototype, "processDocument", null);
__decorate([
    Delete(':id'),
    __param(0, Param('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], DocumentsController.prototype, "deleteDocument", null);
DocumentsController = __decorate([
    Controller('documents'),
    __metadata("design:paramtypes", [typeof (_a = typeof DocumentsService !== "undefined" && DocumentsService) === "function" ? _a : Object])
], DocumentsController);
export { DocumentsController };
//# sourceMappingURL=documents.controller.js.map