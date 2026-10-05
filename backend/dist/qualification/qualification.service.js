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
let QualificationService = class QualificationService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getQualificationStatus(applicantId) {
        if (!applicantId)
            throw new NotFoundException('Applicant ID required');
        const records = await this.prisma.qualification.findMany({
            where: { applicantId },
            orderBy: { evaluatedAt: 'desc' },
            take: 1
        });
        if (records.length === 0) {
            return { status: 'insufficient_information', message: 'Not evaluated yet' };
        }
        return records[0];
    }
    async evaluateQualification(applicantId) {
        console.log(`[QualificationService] Manual evaluation triggered for applicant ${applicantId}`);
        const result = await this.prisma.qualification.create({
            data: {
                applicantId,
                qualificationStatus: 'partially_qualified',
                scoreOrReasoningSummary: 'Manual evaluation: missing APS.'
            }
        });
        return result;
    }
};
QualificationService = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [typeof (_a = typeof PrismaService !== "undefined" && PrismaService) === "function" ? _a : Object])
], QualificationService);
export { QualificationService };
//# sourceMappingURL=qualification.service.js.map