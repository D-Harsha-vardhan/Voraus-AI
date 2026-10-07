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
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.QualificationController = void 0;
const common_1 = require("@nestjs/common");
const qualification_service_1 = require("./qualification.service");
let QualificationController = class QualificationController {
    qualificationService;
    constructor(qualificationService) {
        this.qualificationService = qualificationService;
    }
    async getQualificationStatus(req) {
        const applicantId = req.headers['x-applicant-id'];
        return this.qualificationService.getQualificationStatus(applicantId);
    }
    async evaluateQualification(req) {
        const applicantId = req.headers['x-applicant-id'];
        return this.qualificationService.evaluateQualification(applicantId);
    }
};
exports.QualificationController = QualificationController;
__decorate([
    (0, common_1.Get)(),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], QualificationController.prototype, "getQualificationStatus", null);
__decorate([
    (0, common_1.Post)('evaluate'),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], QualificationController.prototype, "evaluateQualification", null);
exports.QualificationController = QualificationController = __decorate([
    (0, common_1.Controller)('qualification'),
    __metadata("design:paramtypes", [qualification_service_1.QualificationService])
], QualificationController);
//# sourceMappingURL=qualification.controller.js.map