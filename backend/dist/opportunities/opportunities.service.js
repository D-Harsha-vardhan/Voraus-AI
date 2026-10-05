var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var _a;
import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
let OpportunitiesService = class OpportunitiesService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getAllOpportunities() {
        return this.prisma.opportunity.findMany();
    }
    async getOpportunity(id) {
        const opp = await this.prisma.opportunity.findUnique({ where: { id } });
        if (!opp)
            throw new NotFoundException('Opportunity not found');
        return opp;
    }
    async matchOpportunitiesForApplicant(applicantId) {
        console.log(`[OpportunityAgent] Searching Anakin API and matching for applicant ${applicantId}`);
        const mockedMatches = [
            {
                institution: "Technical University of Munich",
                programName: "M.Sc. Computer Science",
                location: "Munich, Germany",
                degreeType: "Master's",
                matchStatus: "High Match",
                requirements: { "Bachelor": true, "IELTS": 6.5, "German": false }
            },
            {
                institution: "RWTH Aachen University",
                programName: "M.Sc. Artificial Intelligence",
                location: "Aachen, Germany",
                degreeType: "Master's",
                matchStatus: "High Match",
                requirements: { "Bachelor": true, "IELTS": 7.0, "APS": true }
            }
        ];
        return mockedMatches;
    }
};
OpportunitiesService = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [typeof (_a = typeof PrismaService !== "undefined" && PrismaService) === "function" ? _a : Object])
], OpportunitiesService);
export { OpportunitiesService };
//# sourceMappingURL=opportunities.service.js.map