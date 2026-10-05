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
import { Controller, Get, Post, Param, Req } from '@nestjs/common';
import { OpportunitiesService } from './opportunities.service';
let OpportunitiesController = class OpportunitiesController {
    opportunitiesService;
    constructor(opportunitiesService) {
        this.opportunitiesService = opportunitiesService;
    }
    async getOpportunities() {
        return this.opportunitiesService.getAllOpportunities();
    }
    async getOpportunity(id) {
        return this.opportunitiesService.getOpportunity(id);
    }
    async searchOpportunities(req) {
        const applicantId = req.headers['x-applicant-id'];
        return this.opportunitiesService.matchOpportunitiesForApplicant(applicantId);
    }
};
__decorate([
    Get(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", Promise)
], OpportunitiesController.prototype, "getOpportunities", null);
__decorate([
    Get(':id'),
    __param(0, Param('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], OpportunitiesController.prototype, "getOpportunity", null);
__decorate([
    Post('search'),
    __param(0, Req()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], OpportunitiesController.prototype, "searchOpportunities", null);
OpportunitiesController = __decorate([
    Controller('opportunities'),
    __metadata("design:paramtypes", [typeof (_a = typeof OpportunitiesService !== "undefined" && OpportunitiesService) === "function" ? _a : Object])
], OpportunitiesController);
export { OpportunitiesController };
//# sourceMappingURL=opportunities.controller.js.map