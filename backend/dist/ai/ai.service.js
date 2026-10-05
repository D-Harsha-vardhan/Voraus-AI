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
import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
let AiService = class AiService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async processDocument(document) {
        console.log(`[DocumentAgent] Processing document ${document.id} of type ${document.documentType}`);
        let extractedData = {};
        switch (document.documentType) {
            case 'degree':
                extractedData = {
                    degree: "B.Tech",
                    field: "Computer Science",
                    institution: "ABC University",
                    graduationYear: "2024",
                    cgpa: "8.5"
                };
                await this.updateApplicantProfile(document.applicantId, 'education', extractedData);
                break;
            case 'ielts':
                extractedData = {
                    overallScore: "7.5",
                    reading: "8.0",
                    listening: "8.0",
                    speaking: "7.0",
                    writing: "7.0",
                    testDate: "2023-10-15"
                };
                await this.updateApplicantProfile(document.applicantId, 'language', {
                    language: 'English',
                    proficiency: 'C1'
                });
                break;
            default:
                extractedData = { summary: "Document processed successfully" };
        }
        return extractedData;
    }
    async updateApplicantProfile(applicantId, type, data) {
        console.log(`[ProfileAgent] Updating profile for applicant ${applicantId} with ${type} data`);
        if (type === 'education') {
            await this.prisma.education.create({
                data: {
                    applicantId,
                    degree: data.degree,
                    fieldOfStudy: data.field,
                    institution: data.institution,
                    cgpa: data.cgpa,
                    verificationStatus: 'verified'
                }
            });
        }
        else if (type === 'language') {
            await this.prisma.language.create({
                data: {
                    applicantId,
                    language: data.language,
                    proficiency: data.proficiency,
                    verificationStatus: 'verified'
                }
            });
        }
        await this.prisma.applicant.update({
            where: { id: applicantId },
            data: { profileCompletion: { increment: 10 } }
        });
        this.runQualificationCheck(applicantId).catch(console.error);
    }
    async runQualificationCheck(applicantId) {
        console.log(`[QualificationAgent] Running qualification check for applicant ${applicantId}`);
        await this.prisma.qualification.create({
            data: {
                applicantId,
                qualificationStatus: 'partially_qualified',
                scoreOrReasoningSummary: 'Has degree and english, missing German and APS.'
            }
        });
        await this.runJourneyOrchestrator(applicantId);
    }
    async runJourneyOrchestrator(applicantId) {
        console.log(`[JourneyAgent] Running orchestrator for applicant ${applicantId}`);
        await this.prisma.agentRun.create({
            data: {
                applicantId,
                agentName: 'JourneyAgent',
                status: 'completed',
                outputSummary: 'Recommended next step: Upload APS certificate'
            }
        });
    }
};
AiService = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [typeof (_a = typeof PrismaService !== "undefined" && PrismaService) === "function" ? _a : Object])
], AiService);
export { AiService };
//# sourceMappingURL=ai.service.js.map