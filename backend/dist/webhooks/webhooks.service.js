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
exports.WebhooksService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let WebhooksService = class WebhooksService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async processConsultantAction(payload) {
        const { applicantId, consultantId, action, comment } = payload;
        console.log(`[Webhook] Processing action '${action}' from consultant ${consultantId} for applicant ${applicantId}`);
        await this.prisma.consultantAction.create({
            data: {
                applicantId,
                consultantId,
                action,
                comment
            }
        });
        switch (action) {
            case 'verify_document':
                if (payload.documentId) {
                    await this.prisma.document.update({
                        where: { id: payload.documentId },
                        data: { verificationStatus: 'verified' }
                    });
                }
                break;
            case 'request_information':
                await this.prisma.notification.create({
                    data: {
                        applicantId,
                        title: 'Action Required',
                        message: `Consultant requested: ${comment}`
                    }
                });
                await this.prisma.applicant.update({
                    where: { id: applicantId },
                    data: { journeyStatus: 'info_requested' }
                });
                break;
            default:
                console.warn(`[Webhook] Unhandled action type: ${action}`);
        }
        return { status: 'success' };
    }
};
exports.WebhooksService = WebhooksService;
exports.WebhooksService = WebhooksService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], WebhooksService);
//# sourceMappingURL=webhooks.service.js.map