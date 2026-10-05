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
let ApplicantService = class ApplicantService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getProfile(applicantId) {
        if (!applicantId)
            throw new NotFoundException('Applicant ID required');
        const applicant = await this.prisma.applicant.findUnique({
            where: { id: applicantId },
            include: {
                education: true,
                employment: true,
                skills: true,
                languages: true,
                documents: true,
                qualifications: true,
            }
        });
        if (!applicant) {
            throw new NotFoundException('Applicant not found');
        }
        return applicant;
    }
    async updateProfile(applicantId, updateData) {
        if (!applicantId)
            throw new NotFoundException('Applicant ID required');
        return this.prisma.applicant.update({
            where: { id: applicantId },
            data: updateData,
        });
    }
};
ApplicantService = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [typeof (_a = typeof PrismaService !== "undefined" && PrismaService) === "function" ? _a : Object])
], ApplicantService);
export { ApplicantService };
//# sourceMappingURL=applicant.service.js.map