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
exports.QualificationService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let QualificationService = class QualificationService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getQualificationStatus(applicantId) {
        if (!applicantId)
            throw new common_1.NotFoundException('Applicant ID required');
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
exports.QualificationService = QualificationService;
exports.QualificationService = QualificationService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], QualificationService);
//# sourceMappingURL=qualification.service.js.map