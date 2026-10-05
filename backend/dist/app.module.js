var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
import { Module } from '@nestjs/common';
import { AppController } from './app.controller.js';
import { AppService } from './app.service.js';
import { AuthModule } from './auth/auth.module.js';
import { ApplicantModule } from './applicant/applicant.module.js';
import { PrismaModule } from './prisma/prisma.module.js';
import { DocumentsModule } from './documents/documents.module.js';
import { AiModule } from './ai/ai.module.js';
import { QualificationModule } from './qualification/qualification.module.js';
import { OpportunitiesModule } from './opportunities/opportunities.module.js';
import { WebhooksModule } from './webhooks/webhooks.module.js';
let AppModule = class AppModule {
};
AppModule = __decorate([
    Module({
        imports: [AuthModule, ApplicantModule, PrismaModule, DocumentsModule, AiModule, QualificationModule, OpportunitiesModule, WebhooksModule],
        controllers: [AppController],
        providers: [AppService],
    })
], AppModule);
export { AppModule };
//# sourceMappingURL=app.module.js.map