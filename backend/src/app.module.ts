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

@Module({
  imports: [AuthModule, ApplicantModule, PrismaModule, DocumentsModule, AiModule, QualificationModule, OpportunitiesModule, WebhooksModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
