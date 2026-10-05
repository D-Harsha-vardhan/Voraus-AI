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
import { Controller, Get, Put, Body, Req } from '@nestjs/common';
import { ApplicantService } from './applicant.service';
let ApplicantController = class ApplicantController {
    applicantService;
    constructor(applicantService) {
        this.applicantService = applicantService;
    }
    async getProfile(req) {
        const applicantId = req.headers['x-applicant-id'];
        return this.applicantService.getProfile(applicantId);
    }
    async updateProfile(req, updateData) {
        const applicantId = req.headers['x-applicant-id'];
        return this.applicantService.updateProfile(applicantId, updateData);
    }
};
__decorate([
    Get('me'),
    __param(0, Req()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ApplicantController.prototype, "getProfile", null);
__decorate([
    Put('profile'),
    __param(0, Req()),
    __param(1, Body()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", Promise)
], ApplicantController.prototype, "updateProfile", null);
ApplicantController = __decorate([
    Controller('applicant'),
    __metadata("design:paramtypes", [typeof (_a = typeof ApplicantService !== "undefined" && ApplicantService) === "function" ? _a : Object])
], ApplicantController);
export { ApplicantController };
//# sourceMappingURL=applicant.controller.js.map